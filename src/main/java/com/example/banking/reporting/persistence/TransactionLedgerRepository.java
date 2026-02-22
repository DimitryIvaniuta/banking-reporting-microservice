package com.example.banking.reporting.persistence;
import java.math.BigDecimal; import java.time.Instant;
import org.springframework.data.jpa.repository.*; import org.springframework.data.repository.query.Param;
/** Repository for source transaction rows. */
public interface TransactionLedgerRepository extends JpaRepository<TransactionLedgerEntity,Long>{
 @Query("select count(t) from TransactionLedgerEntity t where t.accountId=:accountId and t.bookedAt>=:from and t.bookedAt<:to")
 long countInRange(@Param("accountId") Long accountId,@Param("from") Instant from,@Param("to") Instant to);
 @Query("select sum(t.amount) from TransactionLedgerEntity t where t.accountId=:accountId and t.bookedAt>=:from and t.bookedAt<:to")
 BigDecimal sumInRange(@Param("accountId") Long accountId,@Param("from") Instant from,@Param("to") Instant to);
}
