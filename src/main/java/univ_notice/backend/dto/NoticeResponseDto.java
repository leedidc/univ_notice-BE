package univ_notice.backend.dto;

import java.time.LocalDateTime;
import lombok.Getter;
import univ_notice.backend.entity.Notice;

@Getter
public class NoticeResponseDto {

  private final Long noticeId;
  // private final String school;
  private final String department;
  private final String title;
  private final String link;
  private final LocalDateTime postedAt;
  private final LocalDateTime createdAt;

  public NoticeResponseDto(Notice notice) {
    this.noticeId = notice.getNoticeId();
    // this.school = notice.getSchool();
    this.department = notice.getDepartment();
    this.title = notice.getTitle();
    this.link = notice.getLink();
    this.postedAt = notice.getPostedAt();
    this.createdAt = notice.getCreatedAt();
  }
}
