package univ_notice.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "keywords")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Keyword {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "keyword_id")
  private Long id;

  @Column(nullable = false)
  private String name; // ex: "장학금", "기숙사", "수강신청"

  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  private User user; // 키워드 등록한 사용자
}
