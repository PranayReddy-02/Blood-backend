package com.example.demo.controller;

import com.example.demo.dto.AdminDashboardResponse;
import com.example.demo.dto.UserResponse;
import com.example.demo.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*", maxAge = 3600)
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminController {
    
    @Autowired
    private AdminService adminService;
    
    @GetMapping("/dashboard")
    public ResponseEntity<AdminDashboardResponse> getDashboardStats() {
        AdminDashboardResponse stats = adminService.getDashboardStats();
        return ResponseEntity.ok(stats);
    }
    
    @GetMapping("/users")
    public ResponseEntity<Page<UserResponse>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<UserResponse> users = adminService.getAllUsers(pageable);
        
        return ResponseEntity.ok(users);
    }
    
    @PutMapping("/users/{userId}/verify")
    public ResponseEntity<UserResponse> verifyUser(@PathVariable Long userId) {
        UserResponse user = adminService.verifyUser(userId);
        return ResponseEntity.ok(user);
    }
    
    @PutMapping("/users/{userId}/block")
    public ResponseEntity<UserResponse> blockUser(@PathVariable Long userId) {
        UserResponse user = adminService.blockUser(userId);
        return ResponseEntity.ok(user);
    }
    
    @PutMapping("/users/{userId}/unblock")
    public ResponseEntity<UserResponse> unblockUser(@PathVariable Long userId) {
        UserResponse user = adminService.unblockUser(userId);
        return ResponseEntity.ok(user);
    }
}
