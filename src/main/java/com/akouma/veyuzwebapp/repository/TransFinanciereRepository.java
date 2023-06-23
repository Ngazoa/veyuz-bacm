package com.akouma.veyuzwebapp.repository;

import com.akouma.veyuzwebapp.model.TransFinanciere;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface TransFinanciereRepository extends JpaRepository<TransFinanciere, Long> {
    Page<TransFinanciere> findByStatusAndIsRejected(int Status, boolean isRejected, Pageable pageable);
}