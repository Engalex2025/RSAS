package com.retail.smart.repository;

import com.retail.smart.model.RestockLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RestockLogRepository extends JpaRepository<RestockLog, Long> {
}
