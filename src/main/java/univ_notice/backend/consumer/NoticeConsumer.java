package univ_notice.backend.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import univ_notice.backend.dto.NoticeRequestDto;
import univ_notice.backend.service.NoticeService;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoticeConsumer {

  private final NoticeService noticeService;

  @KafkaListener(
      topics = "${app.notice-topic}",
      groupId = "${spring.kafka.consumer.group-id}",
      containerFactory = "kafkaListenerContainerFactory")
  public void consume(NoticeRequestDto dto) {
    noticeService.saveNotice(dto);
  }
}
