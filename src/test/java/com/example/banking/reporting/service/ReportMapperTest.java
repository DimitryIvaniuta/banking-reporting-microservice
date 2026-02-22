package com.example.banking.reporting.service;
import com.example.banking.reporting.api.GenerateReportRequestDto; import com.example.banking.reporting.domain.ReportType; import java.time.LocalDate; import org.junit.jupiter.api.Test; import static org.junit.jupiter.api.Assertions.*;
/** Unit tests for ReportMapper. */
class ReportMapperTest {
 /** Verifies request mapping. */
 @Test void mapsDtoToDomain(){ var dto=new GenerateReportRequestDto(); dto.setAccountId(1L); dto.setFromDate(LocalDate.of(2026,1,1)); dto.setToDate(LocalDate.of(2026,1,2)); dto.setType(ReportType.DAILY_BALANCE); var d=new ReportMapper().toDomain(dto); assertEquals(1L,d.accountId()); }
}
