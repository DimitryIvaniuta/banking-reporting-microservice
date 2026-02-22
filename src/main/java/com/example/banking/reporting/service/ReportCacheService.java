package com.example.banking.reporting.service;
import com.example.banking.reporting.api.GeneratedReportDto; import java.time.Duration; import java.util.Optional; import lombok.RequiredArgsConstructor; import org.springframework.data.redis.core.RedisTemplate; import org.springframework.stereotype.Service;
/** Redis cache for recent report reads. */
@Service @RequiredArgsConstructor public class ReportCacheService {
 private final RedisTemplate<String,Object> redis; private static final Duration TTL=Duration.ofMinutes(10);
 /** Stores report in cache. */ public void put(GeneratedReportDto dto){ redis.opsForValue().set("report:"+dto.getId(), dto, TTL); }
 /** Reads report from cache. */ public Optional<GeneratedReportDto> get(long id){ Object v=redis.opsForValue().get("report:"+id); return v instanceof GeneratedReportDto d? Optional.of(d):Optional.empty(); }
}
