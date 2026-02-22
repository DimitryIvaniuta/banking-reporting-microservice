package com.example.banking.reporting.domain;
import java.time.LocalDate;
/** Domain request for report generation. */
public record ReportRequest(Long accountId, LocalDate fromDate, LocalDate toDate, ReportType type) {}
