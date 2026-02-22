package com.example.banking.reporting.persistence;
import com.example.banking.reporting.domain.ReportStatus;
import com.example.banking.reporting.domain.ReportType;
import jakarta.persistence.*; import java.math.BigDecimal; import java.time.*; import lombok.Getter; import lombok.Setter;
/** Stores generated report metadata and computed totals. */
@Entity @Table(name="generated_report") @Getter @Setter
public class GeneratedReportEntity {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false) private Long accountId;
 @Column(nullable=false) private LocalDate fromDate;
 @Column(nullable=false) private LocalDate toDate;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=64) private ReportType reportType;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=32) private ReportStatus status;
 @Column(nullable=false) private Long transactionCount;
 @Column(nullable=false,precision=19,scale=2) private BigDecimal totalAmount;
 @Column(nullable=false) private Instant createdAt;
 @Column(length=512) private String failureReason;
}
