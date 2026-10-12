package univ_notice.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "notice")
@Getter
@Setter
public class Notice {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long noticeId;

  // private String school;
  private String department;
  private String title;

  @Column(columnDefinition = "TEXT")
  private String link;

  private LocalDateTime postedAt;
  @CreationTimestamp private LocalDateTime createdAt;
}
