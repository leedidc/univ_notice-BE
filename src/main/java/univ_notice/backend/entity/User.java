package univ_notice.backend.entity;

import jakarta.persistence.*;
import java.util.Collection;
import java.util.Collections;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Entity
@Table(name = "user")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails {

  @Id
  @Column(name = "id", nullable = false, unique = true)
  private String id; // 로그인 아이디

  @Column(nullable = false, unique = true)
  private String email;

  @Column(nullable = false)
  private String password;

  private String school;
  private String department;

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return Collections.emptyList(); // 기본 권한 없음 (필요하면 ROLE 추가 가능)
  }

  @Override
  public String getUsername() {
    return id; // 로그인 식별자로 쓸 값
  }

  @Override
  public boolean isAccountNonExpired() {
    return true; // 계정 만료 여부 → true = 만료 안 됨
  }

  @Override
  public boolean isAccountNonLocked() {
    return true; // 계정 잠김 여부 → true = 잠기지 않음
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true; // 비밀번호 만료 여부
  }

  @Override
  public boolean isEnabled() {
    return true; // 활성화 여부
  }
}
