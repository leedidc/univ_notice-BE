package univ_notice.backend.service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import univ_notice.backend.dto.NoticeRequestDto;
import univ_notice.backend.dto.NoticeResponseDto;
import univ_notice.backend.entity.Keyword;
import univ_notice.backend.entity.Notice;
import univ_notice.backend.entity.User;
import univ_notice.backend.repository.KeywordRepository;
import univ_notice.backend.repository.NoticeRepository;
import univ_notice.backend.util.NoticeDateParser;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoticeService {

  private final NoticeRepository noticeRepository;
  private final KeywordRepository keywordRepository;
  private final EmailService emailService;

  private String convertDeptToKorean(String dept) {
    if (dept == null || dept.isBlank()) throw new IllegalArgumentException("학과를 입력해 주세요.");
    return switch (dept.trim().toLowerCase(Locale.ROOT)) {
      case "ai" -> "AI융합학부";
      case "sw" -> "소프트웨어학부";
      case "cse" -> "컴퓨터학부";
      case "info" -> "전자정보공학부";
      case "sec" -> "정보보호학과";
      case "media" -> "글로벌미디어학부";
      default -> dept.trim();
    };
  }

  @Transactional
  public void saveNotice(NoticeRequestDto requestDto) {
    String department = convertDeptToKorean(requestDto.getDept());
    if (requestDto.getTitle() == null || requestDto.getTitle().isBlank())
      throw new IllegalArgumentException("제목을 입력해 주세요.");
    if (noticeRepository.existsByTitleAndDepartment(requestDto.getTitle(), department)) {
      return;
    }

    Notice notice = new Notice();
    notice.setPostedAt(NoticeDateParser.parse(requestDto.getPostedAt()));
    notice.setDepartment(department);
    notice.setTitle(requestDto.getTitle());
    notice.setLink(requestDto.getLink());
    notice.setCreatedAt(LocalDateTime.now());

    noticeRepository.save(notice);

    // 이메일
    String fullText = notice.getTitle() + " " + notice.getDepartment() + " " + notice.getLink();
    List<Keyword> keywords = keywordRepository.findAll();

    var notifiedUsers = new HashSet<String>();
    for (Keyword keyword : keywords) {
      User user = keyword.getUser();

      // 공백 및 따옴표 제거 후 비교
      String keywordName = keyword.getName().trim().replace("\"", "");
      String cleanedFullText = fullText.trim();
      String userDept = user.getDepartment() == null ? "" : user.getDepartment().trim();
      String noticeDept = notice.getDepartment().trim();

      if (!keywordName.isBlank()
          && cleanedFullText.contains(keywordName)
          && userDept.equalsIgnoreCase(noticeDept)
          && notifiedUsers.add(user.getId())) {
        String email = user.getEmail();
        String subject = "[공지 알림] '" + keywordName + "' 관련 공지사항이 등록되었습니다";
        String body = "제목: " + notice.getTitle() + "\n링크: " + notice.getLink();

        try {
          emailService.sendEmail(email, subject, body);

        } catch (Exception e) {
          log.warn(
              "Notice notification failed for notice {} ({})",
              notice.getNoticeId(),
              e.getClass().getSimpleName());
        }
      }
    }
  }

  // 페이지네이션 조회
  @Transactional(readOnly = true)
  public Page<NoticeResponseDto> getNoticesByPage(Pageable pageable) {
    return noticeRepository.findAll(pageable).map(NoticeResponseDto::new);
  }

  @Transactional(readOnly = true)
  public Page<NoticeResponseDto> searchNoticesByKeyword(String keyword, Pageable pageable) {
    return noticeRepository
        .findByTitleContainingIgnoreCase(keyword, pageable)
        .map(NoticeResponseDto::new);
  }

  // 사용자 학과 공지
  @Transactional(readOnly = true)
  public Page<NoticeResponseDto> getNoticesByUserDepartment(User user, Pageable pageable) {
    return noticeRepository
        .findByDepartmentOrderByPostedAtDesc(user.getDepartment(), pageable)
        .map(NoticeResponseDto::new);
  }

  // 키워드 및 학과 기준 조회
  @Transactional(readOnly = true)
  public Page<NoticeResponseDto> searchNotices(
      String keyword, String department, Pageable pageable) {
    Page<Notice> notices;

    if (keyword != null && !keyword.isBlank() && department != null && !department.isBlank()) {
      notices =
          noticeRepository.findByTitleContainingIgnoreCaseAndDepartment(
              keyword, department, pageable);
    } else if (keyword != null && !keyword.isBlank()) {
      notices = noticeRepository.findByTitleContainingIgnoreCase(keyword, pageable);
    } else if (department != null && !department.isBlank()) {
      notices = noticeRepository.findByDepartmentOrderByPostedAtDesc(department, pageable);
    } else {
      notices = noticeRepository.findAll(pageable);
    }

    return notices.map(NoticeResponseDto::new); // 엔티티 → DTO 변환
  }
}
