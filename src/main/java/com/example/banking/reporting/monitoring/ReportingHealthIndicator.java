package com.example.banking.reporting.monitoring;
import org.springframework.boot.actuate.health.*; import org.springframework.stereotype.Component;
/** Custom health indicator for reporting subsystem. */
@Component("reportingSubsystem") public class ReportingHealthIndicator implements HealthIndicator{
 /** Returns reporting subsystem health. */
 public Health health(){ return Health.up().withDetail("generator","sync").withDetail("cache","redis").withDetail("events","kafka").build(); }
}
