package com.fleetguard360.monitoring_service.controller;

import com.fleetguard360.monitoring_service.model.User;
import com.fleetguard360.monitoring_service.model.Role;
import com.fleetguard360.monitoring_service.repository.UserRepository;
import com.fleetguard360.monitoring_service.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RoleRepository roleRepository;

    @GetMapping("/users")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @GetMapping("/roles")
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }
}