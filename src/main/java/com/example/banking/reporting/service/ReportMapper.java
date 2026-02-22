package com.example.banking.reporting.service;
import com.example.banking.reporting.api.*; import com.example.banking.reporting.domain.ReportRequest; import com.example.banking.reporting.persistence.GeneratedReportEntity; import org.springframework.stereotype.Component;
/** Converts API/domain/persistence types. */
@Component public class ReportMapper {
 /** Maps API request to domain request. */
 public ReportRequest toDomain(GenerateReportRequestDto d){ return new ReportRequest(d.getAccountId(), d.getFromDate(), d.getToDate(), d.getType()); }
 /** Maps entity to API DTO. */
 public GeneratedReportDto toDto(GeneratedReportEntity e){ return GeneratedReportDto.builder().id(e.getId()).accountId(e.getAccountId()).fromDate(e.getFromDate()).toDate(e.getToDate()).reportType(e.getReportType()).status(e.getStatus()).transactionCount(e.getTransactionCount()).totalAmount(e.getTotalAmount()).createdAt(e.getCreatedAt()).failureReason(e.getFailureReason()).build(); }
}
