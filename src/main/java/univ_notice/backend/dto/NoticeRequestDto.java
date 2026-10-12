package univ_notice.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class NoticeRequestDto {
  private String school;
  private String dept;
  private String title;
  private String link;

  @JsonProperty("postedAt")
  private String postedAt;
}
