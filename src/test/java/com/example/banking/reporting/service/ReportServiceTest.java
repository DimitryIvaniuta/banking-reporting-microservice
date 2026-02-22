package com.example.banking.reporting.service;
import com.example.banking.reporting.domain.*; import com.example.banking.reporting.persistence.*; import java.math.BigDecimal; import java.time.LocalDate;
import org.junit.jupiter.api.Test; import static org.junit.jupiter.api.Assertions.*; import static org.mockito.Mockito.*;
/** Unit tests for ReportService. */
class ReportServiceTest {
 /** Verifies successful generation path. */
 @Test void generatesReport(){
   var reports=mock(GeneratedReportRepository.class); var ledger=mock(TransactionLedgerRepository.class); var mapper=new ReportMapper(); var publisher=mock(ReportEventPublisher.class); var cache=mock(ReportCacheService.class);
   when(ledger.countInRange(anyLong(),any(),any())).thenReturn(3L); when(ledger.sumInRange(anyLong(),any(),any())).thenReturn(new BigDecimal("735.50"));
   when(reports.save(any())).thenAnswer(inv -> { var e=(GeneratedReportEntity) inv.getArgument(0); e.setId(42L); return e;});
   var service=new ReportService(reports, ledger, mapper, publisher, cache);
   var dto=service.generate(new ReportRequest(1001L, LocalDate.now().minusDays(3), LocalDate.now(), ReportType.TRANSACTION_SUMMARY));
   assertEquals(42L, dto.getId()); assertEquals(3L, dto.getTransactionCount()); verify(publisher).publish(any(), any(), any(), anyLong(), any());
 }
 /** Verifies validation of invalid ranges. */
 @Test void rejectsInvalidRange(){
   var service=new ReportService(mock(GeneratedReportRepository.class), mock(TransactionLedgerRepository.class), new ReportMapper(), mock(ReportEventPublisher.class), mock(ReportCacheService.class));
   assertThrows(RuntimeException.class, () -> service.generate(new ReportRequest(1L, LocalDate.of(2026,2,2), LocalDate.of(2026,2,1), ReportType.DAILY_BALANCE)));
 }
}
