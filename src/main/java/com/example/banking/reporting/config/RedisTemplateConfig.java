package com.example.banking.reporting.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
/** Redis template configuration. */
@Configuration
public class RedisTemplateConfig {
  /** Generic Redis template used by cache service. */
  @Bean public RedisTemplate<String,Object> redisTemplate(RedisConnectionFactory cf){
    RedisTemplate<String,Object> t=new RedisTemplate<>(); t.setConnectionFactory(cf); return t;
  }
}
