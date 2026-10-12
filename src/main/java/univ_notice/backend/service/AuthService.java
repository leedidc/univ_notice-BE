package univ_notice.backend.service;

import jakarta.transaction.Transactional;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import univ_notice.backend.dto.ChangePasswordRequest;
import univ_notice.backend.dto.LoginRequestDto;
import univ_notice.backend.dto.SignUpRequestDto;
import univ_notice.backend.entity.User;
import univ_notice.backend.repository.KeywordRepository;
import univ_notice.backend.repository.UserRepository;
import univ_notice.backend.util.JwtUtil;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtUtil jwtUtil;
  private final KeywordRepository keywordRepository;

  public void register(SignUpRequestDto request) {
    validatePassword(request.getPassword());
    if (userRepository.findById(request.getId()).isPresent()) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 등록한 아이디입니다.");
    }
    if (userRepository.findByEmail(request.getEmail()).isPresent()) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 등록한 이메일입니다.");
    }

    User user =
        User.builder()
            .id(request.getId())
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .school(request.getSchool())
            .department(request.getDepartment())
            .build();

    userRepository.save(user);
  }

  public String login(LoginRequestDto request) {
    User user =
        userRepository
            .findById(request.getId())
            .orElseThrow(
                () ->
                    new ResponseStatusException(HttpStatus.UNAUTHORIZED, "아이디 또는 비밀번호를 확인해 주세요."));

    if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "아이디 또는 비밀번호를 확인해 주세요.");
    }

    return jwtUtil.generateToken(user.getId());
  }

  private void validatePassword(String password) {
    if (password == null
        || password.length() < 8
        || password.getBytes(StandardCharsets.UTF_8).length > 72) {
      throw new IllegalArgumentException("비밀번호는 8자 이상, UTF-8 기준 72바이트 이하로 입력해 주세요.");
    }
  }

  @Transactional
  public void changePassword(User user, ChangePasswordRequest request) {
    if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "현재 비밀번호를 확인해 주세요.");
    }
    validatePassword(request.newPassword());
    user.setPassword(passwordEncoder.encode(request.newPassword()));
    userRepository.save(user);
  }

  @Transactional
  public void deleteUser(User user) {
    keywordRepository.deleteByUser(user);
    userRepository.delete(user);
  }
}
