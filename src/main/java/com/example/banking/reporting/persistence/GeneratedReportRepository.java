package com.example.banking.reporting.persistence;
import java.util.List; import org.springframework.data.jpa.repository.JpaRepository;
/** Repository for generated reports. */
public interface GeneratedReportRepository extends JpaRepository<GeneratedReportEntity,Long>{
  /** Lists reports by account ordered newest first. */
  List<GeneratedReportEntity> findByAccountIdOrderByCreatedAtDesc(Long accountId);
}
