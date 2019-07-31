package com.ndumiso.SpringBatchDemo.repository;

import com.ndumiso.SpringBatchDemo.domain.XPBStatement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface XPBStatementRepository extends JpaRepository<XPBStatement, Long> {
}
