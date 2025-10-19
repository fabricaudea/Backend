package com.fleetguard360.monitoring_service.repository;

import com.fleetguard360.monitoring_service.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);
}
