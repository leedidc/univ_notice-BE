package univ_notice.backend.controller;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class ApiExceptionHandler {
  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<Map<String, String>> status(ResponseStatusException exception) {
    return ResponseEntity.status(exception.getStatusCode())
        .body(
            Map.of(
                "message",
                exception.getReason() == null ? "요청을 처리하지 못했습니다." : exception.getReason()));
  }

  @ExceptionHandler({IllegalArgumentException.class, MethodArgumentNotValidException.class})
  public ResponseEntity<Map<String, String>> invalid(Exception exception) {
    String message =
        exception instanceof IllegalArgumentException ? exception.getMessage() : "입력값을 확인해 주세요.";
    return ResponseEntity.badRequest()
        .body(Map.of("message", message == null ? "입력값을 확인해 주세요." : message));
  }
}
