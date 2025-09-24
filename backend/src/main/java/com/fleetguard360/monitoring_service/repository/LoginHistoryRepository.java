package com.fleetguard360.monitoring_service.repository;

import com.fleetguard360.monitoring_service.model.LoginHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoginHistoryRepository extends JpaRepository<LoginHistory, Long> {
}
