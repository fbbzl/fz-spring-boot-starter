package com.fz.spring.boot.starter.log.components;


import com.fz.spring.boot.starter.log.components.entity.OperationLog;
import com.fz.springboot.starter.jpa.repository.BaseRepository;
import org.springframework.stereotype.Repository;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2025/9/4 17:09
 */

@Repository
public interface OperationLogRepository extends BaseRepository<OperationLog> {
}
