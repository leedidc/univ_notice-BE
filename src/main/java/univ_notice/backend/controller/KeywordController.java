package univ_notice.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import univ_notice.backend.dto.KeywordRequestDto;
import univ_notice.backend.entity.Keyword;
import univ_notice.backend.entity.User;
import univ_notice.backend.repository.KeywordRepository;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/keywords")
public class KeywordController {

  private final KeywordRepository keywordRepository;

  @GetMapping
  public ResponseEntity<?> getKeywords(@AuthenticationPrincipal User user) {
    return ResponseEntity.ok(
        keywordRepository.findByUser(user).stream().map(Keyword::getName).toList());
  }

  @PostMapping
  public ResponseEntity<String> saveKeyword(
      @Valid @RequestBody KeywordRequestDto request, @AuthenticationPrincipal User user) {
    String keywordName = request.getKeyword().trim();

    if (keywordName.contains("\"") || keywordName.contains("'")) {
      return ResponseEntity.badRequest().body("키워드에 따옴표(\", ')를 포함할 수 없습니다.");
    }

    boolean exists = keywordRepository.existsByNameAndUser(keywordName, user);
    if (exists) {
      return ResponseEntity.status(HttpStatus.CONFLICT).body("이미 등록된 키워드입니다.");
    }

    Keyword keyword = new Keyword();
    keyword.setName(keywordName);
    keyword.setUser(user);
    keywordRepository.save(keyword);

    return ResponseEntity.ok("키워드 저장 완료");
  }

  @DeleteMapping("/{keywordName}")
  public ResponseEntity<String> deleteKeyword(
      @PathVariable String keywordName, @AuthenticationPrincipal User user) {
    Keyword keyword = keywordRepository.findByNameAndUser(keywordName, user).orElse(null);

    if (keyword == null) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body("해당 키워드가 존재하지 않습니다.");
    }

    keywordRepository.delete(keyword);
    return ResponseEntity.ok("키워드 삭제 완료");
  }
}
