package com.example.banking.reporting.config;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
/** Kafka topic declarations. */
@Configuration
public class KafkaTopicConfig {
  /** Audit topic name. */
  public static final String TOPIC = "banking.reporting.generated.v1";
  /** Declares report generated topic. */
  @Bean public NewTopic reportGeneratedTopic(){ return TopicBuilder.name(TOPIC).partitions(3).replicas(1).build(); }
}
