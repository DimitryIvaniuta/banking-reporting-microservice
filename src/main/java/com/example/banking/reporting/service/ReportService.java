package com.example.banking.reporting.service;
import com.example.banking.reporting.api.GeneratedReportDto; import com.example.banking.reporting.domain.*; import com.example.banking.reporting.persistence.*;
import jakarta.ws.rs.*; import java.math.BigDecimal; import java.time.*; import java.util.List; import lombok.RequiredArgsConstructor; import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional;
/** Core service implementing banking report generation and retrieval. */
@Service @RequiredArgsConstructor public class ReportService {
 private final GeneratedReportRepository reports; private final TransactionLedgerRepository ledger; private final ReportMapper mapper; private final ReportEventPublisher publisher; private final ReportCacheService cache;
 /** Generates a report using transaction ledger aggregates. */
 @Transactional public GeneratedReportDto generate(ReportRequest r){
   if(r.accountId()==null || r.accountId()<=0) throw new BadRequestException("accountId must be positive");
   if(r.fromDate()==null || r.toDate()==null || r.type()==null) throw new BadRequestException("Missing required fields");
   if(r.fromDate().isAfter(r.toDate())) throw new BadRequestException("fromDate must be <= toDate");
   if(r.toDate().isAfter(r.fromDate().plusYears(1))) throw new BadRequestException("Range too large (max 1 year)");
   Instant from=r.fromDate().atStartOfDay().toInstant(ZoneOffset.UTC);
   Instant to=r.toDate().plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);
   long count=ledger.countInRange(r.accountId(), from, to);
   BigDecimal total=ledger.sumInRange(r.accountId(), from, to); if(total==null) total=BigDecimal.ZERO;
   GeneratedReportEntity e=new GeneratedReportEntity(); e.setAccountId(r.accountId()); e.setFromDate(r.fromDate()); e.setToDate(r.toDate()); e.setReportType(r.type()); e.setStatus(ReportStatus.COMPLETED); e.setTransactionCount(count); e.setTotalAmount(total); e.setCreatedAt(Instant.now());
   e=reports.save(e); var dto=mapper.toDto(e); cache.put(dto); publisher.publish(e.getId(), e.getAccountId(), e.getReportType().name(), e.getTransactionCount(), e.getTotalAmount().toPlainString()); return dto;
 }
 /** Gets report by id using cache first. */
 @Transactional(readOnly=true) public GeneratedReportDto getById(long id){ return cache.get(id).orElseGet(() -> reports.findById(id).map(mapper::toDto).orElseThrow(() -> new NotFoundException("Report not found: "+id))); }
 /** Lists reports for an account. */
 @Transactional(readOnly=true) public List<GeneratedReportDto> listByAccount(long accountId){ if(accountId<=0) throw new BadRequestException("accountId must be positive"); return reports.findByAccountIdOrderByCreatedAtDesc(accountId).stream().map(mapper::toDto).toList(); }
}
