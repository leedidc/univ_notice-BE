package univ_notice.backend.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

public final class NoticeDateParser {
  private NoticeDateParser() {}

  public static LocalDateTime parse(String value) {
    if (value == null || value.isBlank()) return null;
    String normalized = value.trim().replaceAll("\\.\\s*", "-").replaceAll("-$", "");
    try {
      return normalized.contains("T")
          ? LocalDateTime.parse(normalized)
          : LocalDate.parse(normalized).atStartOfDay();
    } catch (DateTimeParseException exception) {
      throw new IllegalArgumentException("공지 날짜는 yyyy-MM-dd 또는 yyyy-MM-ddTHH:mm:ss 형식이어야 합니다.");
    }
  }
}
