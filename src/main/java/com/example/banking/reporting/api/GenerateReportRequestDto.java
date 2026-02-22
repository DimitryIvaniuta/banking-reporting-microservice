package com.example.banking.reporting.api;
import com.example.banking.reporting.domain.ReportType; import jakarta.validation.constraints.NotNull; import java.time.LocalDate; import lombok.Data;
/** Request payload for report generation. */
@Data public class GenerateReportRequestDto { @NotNull private Long accountId; @NotNull private LocalDate fromDate; @NotNull private LocalDate toDate; @NotNull private ReportType type; }
