package com.example.demo.service;

import com.example.demo.dto.AdminDashboardResponse;
import com.example.demo.dto.UserResponse;
import com.example.demo.entity.User;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.BloodRequestRepository;
import com.example.demo.repository.DonationHistoryRepository;
import com.example.demo.repository.RequestResponseRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private BloodRequestRepository bloodRequestRepository;
    
    @Autowired
    private RequestResponseRepository requestResponseRepository;
    
    @Autowired
    private DonationHistoryRepository donationHistoryRepository;
    
    public AdminDashboardResponse getDashboardStats() {
        long totalDonors = userRepository.countByRole(User.UserRole.DONOR);
        long totalRequesters = userRepository.countByRole(User.UserRole.REQUESTER);
        long activeDonors = userRepository.countByRoleAndIsBlockedFalse(User.UserRole.DONOR);
        long activeRequests = bloodRequestRepository.countByStatus("PENDING") + 
                             bloodRequestRepository.countByStatus("MATCHED");
        long completedDonations = donationHistoryRepository.count();
        long totalVerifiedUsers = userRepository.countByIsVerifiedTrue();
        long totalBlockedUsers = userRepository.countByIsBlockedTrue();
        
        return AdminDashboardResponse.builder()
                .totalDonors(totalDonors)
                .totalRequesters(totalRequesters)
                .activeDonors(activeDonors)
                .activeRequests(activeRequests)
                .completedDonations(completedDonations)
                .totalVerifiedUsers(totalVerifiedUsers)
                .totalBlockedUsers(totalBlockedUsers)
                .mostRequestedBloodGroup("O+")  // Could be fetched from DB with custom query
                .build();
    }
    
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(this::mapToUserResponse);
    }
    
    @Transactional
    public UserResponse verifyUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        user.setIsVerified(true);
        user = userRepository.save(user);
        
        return mapToUserResponse(user);
    }
    
    @Transactional
    public UserResponse blockUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        user.setIsBlocked(true);
        user = userRepository.save(user);
        
        return mapToUserResponse(user);
    }
    
    @Transactional
    public UserResponse unblockUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        user.setIsBlocked(false);
        user = userRepository.save(user);
        
        return mapToUserResponse(user);
    }
    
    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .city(user.getCity())
                .role(user.getRole().toString())
                .isVerified(user.getIsVerified())
                .isBlocked(user.getIsBlocked())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
