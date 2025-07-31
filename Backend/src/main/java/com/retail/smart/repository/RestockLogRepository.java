package com.retail.smart.repository;

import com.retail.smart.entity.RestockLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RestockLogRepository extends JpaRepository<RestockLog, Long> {

    List<RestockLog> findByProduct_ProductId(String productId);
}
