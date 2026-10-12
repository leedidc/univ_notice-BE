package univ_notice.backend.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequestDto {
  @NotBlank private String id;
  @NotBlank private String password;
}
