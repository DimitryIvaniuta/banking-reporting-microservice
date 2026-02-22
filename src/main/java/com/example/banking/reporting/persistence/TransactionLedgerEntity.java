package com.example.banking.reporting.persistence;
import jakarta.persistence.*; import java.math.BigDecimal; import java.time.Instant; import lombok.Getter; import lombok.Setter;
/** Source ledger transactions used by reporting computations. */
@Entity @Table(name="transaction_ledger") @Getter @Setter
public class TransactionLedgerEntity {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false) private Long accountId;
 @Column(nullable=false,precision=19,scale=2) private BigDecimal amount;
 @Column(nullable=false,length=3) private String currency;
 @Column(nullable=false) private Instant bookedAt;
 @Column(nullable=false,length=32) private String channel;
}
