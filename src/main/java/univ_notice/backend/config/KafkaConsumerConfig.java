package univ_notice.backend.config;

import java.util.HashMap;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import univ_notice.backend.dto.NoticeRequestDto;

@EnableKafka
@Configuration
public class KafkaConsumerConfig {
  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, NoticeRequestDto>
      kafkaListenerContainerFactory(
          @Value("${spring.kafka.bootstrap-servers}") String servers,
          @Value("${spring.kafka.consumer.group-id}") String group,
          @Value("${spring.kafka.listener.auto-startup:false}") boolean enabled) {
    var properties = new HashMap<String, Object>();
    properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, servers);
    properties.put(ConsumerConfig.GROUP_ID_CONFIG, group);
    properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
    properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    // Ignore producer type headers; deserialize only the expected notice DTO.
    var deserializer = new JsonDeserializer<>(NoticeRequestDto.class, false);
    var factory = new ConcurrentKafkaListenerContainerFactory<String, NoticeRequestDto>();
    factory.setConsumerFactory(
        new DefaultKafkaConsumerFactory<>(properties, new StringDeserializer(), deserializer));
    factory.setAutoStartup(enabled);
    factory.setBatchListener(false);
    factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);
    return factory;
  }
}
