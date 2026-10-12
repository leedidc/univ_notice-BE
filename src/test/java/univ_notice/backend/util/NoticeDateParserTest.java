package univ_notice.backend.util;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class NoticeDateParserTest {
  @ParameterizedTest
  @ValueSource(strings = {"2025-04-03", "2025.04.03", "2025. 04. 03", "2025. 04. 03."})
  void parsesDateWithoutAddingTimeTwice(String date) {
    assertEquals(LocalDateTime.of(2025, 4, 3, 0, 0), NoticeDateParser.parse(date));
  }

  @ParameterizedTest
  @ValueSource(strings = {"2025-04-03T12:34:56", "2025.04.03T12:34:56", "2025. 04. 03T12:34:56"})
  void preservesProvidedTime(String date) {
    assertEquals(LocalDateTime.of(2025, 4, 3, 12, 34, 56), NoticeDateParser.parse(date));
  }

  @Test
  void rejectsImpossibleDatesAndAllowsMissingDate() {
    assertThrows(IllegalArgumentException.class, () -> NoticeDateParser.parse("2025-02-30"));
    assertNull(NoticeDateParser.parse(" "));
  }
}
