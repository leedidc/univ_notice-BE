package univ_notice.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
  private final JavaMailSender mailSender;
  private final boolean enabled;
  private final String from;

  public EmailService(
      JavaMailSender mailSender,
      @Value("${app.mail-enabled:false}") boolean enabled,
      @Value("${app.mail-from:}") String from) {
    this.mailSender = mailSender;
    this.enabled = enabled;
    this.from = from;
    if (enabled && from.isBlank())
      throw new IllegalArgumentException("MAIL_FROM is required when mail is enabled.");
  }

  public void sendEmail(String to, String subject, String body) {
    if (!enabled) return;
    SimpleMailMessage message = new SimpleMailMessage();
    message.setFrom(from);
    message.setTo(to);
    message.setSubject(subject);
    message.setText(body);
    mailSender.send(message);
  }
}
