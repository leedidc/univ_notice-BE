package univ_notice.backend.controller;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import univ_notice.backend.dto.NoticeRequestDto;
import univ_notice.backend.dto.NoticeResponseDto;
import univ_notice.backend.entity.User;
import univ_notice.backend.repository.NoticeRepository;
import univ_notice.backend.service.NoticeService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/notice")
public class NoticeController {

  private final NoticeService noticeService;
  private final NoticeRepository noticeRepository;

  @Value("${app.ingestion-key:}")
  private String ingestionKey;

  //  공지사항 저장
  @PostMapping
  public ResponseEntity<String> saveNotice(
      @RequestBody NoticeRequestDto requestDto,
      @RequestHeader(value = "X-Ingestion-Key", required = false) String suppliedKey) {
    if (ingestionKey.isBlank()
        || suppliedKey == null
        || !MessageDigest.isEqual(
            ingestionKey.getBytes(StandardCharsets.UTF_8),
            suppliedKey.getBytes(StandardCharsets.UTF_8))) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "수집 인증이 필요합니다.");
    }
    noticeService.saveNotice(requestDto);
    return new ResponseEntity<>("Notice saved successfully", HttpStatus.CREATED);
  }

  // 공지사항 목록 조회
  @GetMapping
  public ResponseEntity<Page<NoticeResponseDto>> getNotices(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {

    PageRequest pageRequest =
        PageRequest.of(
            Math.max(page, 0), Math.max(1, Math.min(size, 100)), Sort.by("postedAt").descending());
    Page<NoticeResponseDto> result = noticeService.getNoticesByPage(pageRequest);
    return ResponseEntity.ok(result);
  }

  // 검색용: 키워드 학과 동시 필터링
  @GetMapping("/search")
  public ResponseEntity<Page<NoticeResponseDto>> searchNotices(
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String department,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {

    PageRequest pageRequest =
        PageRequest.of(
            Math.max(page, 0), Math.max(1, Math.min(size, 100)), Sort.by("postedAt").descending());
    Page<NoticeResponseDto> result = noticeService.searchNotices(keyword, department, pageRequest);
    return ResponseEntity.ok(result);
  }

  // 사용자 학과 기준 공지
  @GetMapping("/user")
  public ResponseEntity<Page<NoticeResponseDto>> getNoticesForUser(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {

    User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

    PageRequest pageRequest =
        PageRequest.of(
            Math.max(page, 0), Math.max(1, Math.min(size, 100)), Sort.by("postedAt").descending());
    Page<NoticeResponseDto> result = noticeService.getNoticesByUserDepartment(user, pageRequest);
    return ResponseEntity.ok(result);
  }

  @GetMapping("/departments")
  public ResponseEntity<List<String>> getDepartments() {
    List<String> departments = noticeRepository.findDistinctDepartments();
    return ResponseEntity.ok(departments);
  }
}
