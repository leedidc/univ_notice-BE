package univ_notice.backend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

class EmailServiceTest {
  @Test
  void disabledMailDoesNotInvokeSender() {
    var sender = mock(JavaMailSender.class);
    new EmailService(sender, false, "")
        .sendEmail("recipient@example.invalid", "Demo", "Synthetic data");
    verifyNoInteractions(sender);
  }

  @Test
  void enabledMailRequiresAnExplicitFromAddress() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new EmailService(mock(JavaMailSender.class), true, ""));
  }

  @Test
  void configuredMailUsesTheSenderWithoutNetworkAccess() {
    var sender = mock(JavaMailSender.class);
    new EmailService(sender, true, "sender@example.invalid")
        .sendEmail("recipient@example.invalid", "Demo", "Synthetic data");
    verify(sender)
        .send(
            argThat(
                (SimpleMailMessage message) -> "sender@example.invalid".equals(message.getFrom())));
  }
}
