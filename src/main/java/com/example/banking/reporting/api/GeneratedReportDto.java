package com.example.banking.reporting.api;
import com.example.banking.reporting.domain.*; import java.math.BigDecimal; import java.time.*; import lombok.Builder; import lombok.Value;
/** Response payload representing a generated report. */
@Value @Builder public class GeneratedReportDto { Long id; Long accountId; LocalDate fromDate; LocalDate toDate; ReportType reportType; ReportStatus status; Long transactionCount; BigDecimal totalAmount; Instant createdAt; String failureReason; }
