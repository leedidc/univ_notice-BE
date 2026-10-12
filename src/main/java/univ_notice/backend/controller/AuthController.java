package univ_notice.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import univ_notice.backend.dto.ChangePasswordRequest;
import univ_notice.backend.dto.LoginRequestDto;
import univ_notice.backend.dto.SignUpRequestDto;
import univ_notice.backend.dto.UserResponseDto;
import univ_notice.backend.entity.User;
import univ_notice.backend.service.AuthService;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;

  @PostMapping("/signup")
  public ResponseEntity<String> register(@Valid @RequestBody SignUpRequestDto request) {
    authService.register(request);
    return ResponseEntity.ok("회원가입 성공");
  }

  @PostMapping("/login")
  public ResponseEntity<String> login(@Valid @RequestBody LoginRequestDto request) {
    String token = authService.login(request);
    return ResponseEntity.ok(token);
  }

  @GetMapping("/test")
  public ResponseEntity<String> testTokenAuth() {
    return ResponseEntity.ok("인증된 사람만 볼 수 있음");
  }

  @GetMapping("/me")
  public ResponseEntity<UserResponseDto> getMyInfo(@AuthenticationPrincipal User user) {
    return ResponseEntity.ok(new UserResponseDto(user));
  }

  @PutMapping("/password")
  public ResponseEntity<String> changePassword(
      @AuthenticationPrincipal User user, @Valid @RequestBody ChangePasswordRequest request) {
    authService.changePassword(user, request);
    return ResponseEntity.ok("비밀번호를 변경했습니다.");
  }

  @DeleteMapping("/delete")
  public ResponseEntity<String> deleteUser(@AuthenticationPrincipal User user) {
    authService.deleteUser(user);
    return ResponseEntity.ok("회원 탈퇴 완료");
  }
}
