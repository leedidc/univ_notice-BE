package univ_notice.backend.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KeywordRequestDto {
  @NotBlank
  @Size(max = 100)
  private String keyword;
}
