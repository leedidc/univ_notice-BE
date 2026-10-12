package univ_notice.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import univ_notice.backend.entity.User;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserResponseDto {
  private String id;
  private String email;
  private String school;
  private String department;

  public UserResponseDto(User user) {
    this.id = user.getId();
    this.email = user.getEmail();
    this.school = user.getSchool();
    this.department = user.getDepartment();
  }
}
