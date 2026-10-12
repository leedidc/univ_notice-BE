package univ_notice.backend;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import univ_notice.backend.dto.NoticeRequestDto;
import univ_notice.backend.repository.KeywordRepository;
import univ_notice.backend.repository.NoticeRepository;
import univ_notice.backend.repository.UserRepository;
import univ_notice.backend.service.NoticeService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiIntegrationTests {
  private static final String INGESTION_TEST_VALUE = UUID.randomUUID().toString();
  @Autowired MockMvc mvc;
  @Autowired ObjectMapper json;
  @Autowired KeywordRepository keywords;
  @Autowired UserRepository users;
  @Autowired NoticeRepository notices;
  @Autowired NoticeService noticeService;
  @MockitoBean JavaMailSender mailSender;
  private String password;

  @DynamicPropertySource
  static void properties(DynamicPropertyRegistry properties) {
    properties.add("app.ingestion-key", () -> INGESTION_TEST_VALUE);
  }

  @BeforeEach
  void reset() {
    keywords.deleteAll();
    users.deleteAll();
    notices.deleteAll();
    password = UUID.randomUUID().toString();
  }

  String registerAndLogin(String id) throws Exception {
    mvc.perform(
            post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    json.writeValueAsString(
                        Map.of(
                            "id",
                            id,
                            "email",
                            id + "@example.invalid",
                            "password",
                            password,
                            "school",
                            "Demo University",
                            "department",
                            "컴퓨터학부"))))
        .andExpect(status().isOk());
    return login(id, password);
  }

  String login(String id, String suppliedPassword) throws Exception {
    return mvc.perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("id", id, "password", suppliedPassword))))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();
  }

  @Test
  void privateRoutesRequireAuthenticationAndInvalidTokensReturn401() throws Exception {
    for (String path : new String[] {"/api/keywords", "/auth/me", "/auth/test", "/notice/user"}) {
      mvc.perform(get(path)).andExpect(status().isUnauthorized());
      mvc.perform(get(path).header("Authorization", "Bearer invalid-token"))
          .andExpect(status().isUnauthorized());
    }
    mvc.perform(get("/notice/search")).andExpect(status().isOk());
  }

  @Test
  void keywordsAreIsolatedPerUserAndDeletedWithAccount() throws Exception {
    String first = registerAndLogin("demo-first");
    String second = registerAndLogin("demo-second");
    mvc.perform(
            post("/api/keywords")
                .header("Authorization", "Bearer " + first)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"keyword\":\"장학금\"}"))
        .andExpect(status().isOk());
    mvc.perform(get("/api/keywords").header("Authorization", "Bearer " + second))
        .andExpect(status().isOk())
        .andExpect(content().json("[]"));
    mvc.perform(delete("/api/keywords/장학금").header("Authorization", "Bearer " + second))
        .andExpect(status().isNotFound());
    mvc.perform(delete("/auth/delete").header("Authorization", "Bearer " + first))
        .andExpect(status().isOk());
    assertEquals(0, keywords.count());
    mvc.perform(get("/auth/me").header("Authorization", "Bearer " + first))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void passwordChangeChecksCurrentPasswordAndUpdatesTheHash() throws Exception {
    String token = registerAndLogin("demo-change");
    String replacement = UUID.randomUUID().toString();
    mvc.perform(
            put("/auth/password")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    json.writeValueAsString(
                        Map.of(
                            "currentPassword",
                            "incorrect-demo-value",
                            "newPassword",
                            replacement))))
        .andExpect(status().isBadRequest());
    mvc.perform(
            put("/auth/password")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    json.writeValueAsString(
                        Map.of("currentPassword", password, "newPassword", replacement))))
        .andExpect(status().isOk());
    login("demo-change", replacement);
    assertNotEquals(replacement, users.findById("demo-change").orElseThrow().getPassword());
  }

  @Test
  void emptyAndInvalidSignupsAreRejected() throws Exception {
    mvc.perform(post("/auth/signup").contentType(MediaType.APPLICATION_JSON).content("{}"))
        .andExpect(status().isBadRequest());
    mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content("{}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void ingestionRequiresKeyAndPreservesTimestampWithoutSendingMail() throws Exception {
    String payload =
        json.writeValueAsString(
            Map.of(
                "dept",
                " cse ",
                "title",
                "Synthetic notice",
                "link",
                "https://example.invalid/notice/1",
                "postedAt",
                "2025-04-03T12:34:56"));
    mvc.perform(post("/notice").contentType(MediaType.APPLICATION_JSON).content(payload))
        .andExpect(status().isUnauthorized());
    mvc.perform(
            post("/notice")
                .header("X-Ingestion-Key", INGESTION_TEST_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
        .andExpect(status().isCreated());
    assertEquals(12, notices.findAll().get(0).getPostedAt().getHour());
    assertEquals("컴퓨터학부", notices.findAll().get(0).getDepartment());
    verifyNoInteractions(mailSender);
  }

  @Test
  void duplicateTitlesInOtherDepartmentsArePreserved() {
    NoticeRequestDto request = new NoticeRequestDto();
    request.setTitle("Synthetic notice");
    request.setDept("cse");
    request.setPostedAt("2025. 04. 03");
    request.setLink("https://example.invalid/notice/1");
    noticeService.saveNotice(request);
    noticeService.saveNotice(request);
    request.setDept("sw");
    noticeService.saveNotice(request);
    assertEquals(2, notices.count());
    verifyNoInteractions(mailSender);
  }
}
