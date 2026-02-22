package com.example.banking.reporting.service;
import com.example.banking.reporting.config.KafkaTopicConfig; import java.util.Map; import lombok.RequiredArgsConstructor; import org.springframework.kafka.core.KafkaTemplate; import org.springframework.stereotype.Service;
/** Publishes report generation events to Kafka. */
@Service @RequiredArgsConstructor public class ReportEventPublisher {
 private final KafkaTemplate<String,Object> kafka;
 /** Publishes report generated event. */
 public void publish(Long id, Long accountId, String type, long txCount, String total){ kafka.send(KafkaTopicConfig.TOPIC, String.valueOf(id), Map.of("reportId",id,"accountId",accountId,"type",type,"transactionCount",txCount,"totalAmount",total));}
}
