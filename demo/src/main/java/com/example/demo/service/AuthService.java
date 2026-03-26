package com.example.demo.service;

import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.LoginResponse;
import com.example.demo.dto.UserRegistrationRequest;
import com.example.demo.dto.UserResponse;
import com.example.demo.entity.DonorDetails;
import com.example.demo.entity.User;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.UnauthorizedException;
import com.example.demo.repository.DonorDetailsRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private DonorDetailsRepository donorDetailsRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    
    @Transactional
    public UserResponse register(UserRegistrationRequest request) {
        log.info("Registering new user with email={} role={}", request.getEmail(), request.getRole());

        // Validate if user already exists
        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new BadRequestException("Email already registered");
        }
        
        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new BadRequestException("Phone number already registered");
        }
        
        // Create user with normalized role and strict validation
        String roleValue = request.getRole().trim().toUpperCase();
        User.UserRole role;
        try {
            role = User.UserRole.valueOf(roleValue);
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Invalid role: " + request.getRole() + ". Allowed roles: DONOR, REQUESTER, ADMIN");
        }

        User user = User.builder()
                .email(request.getEmail().trim())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName().trim())
                .phoneNumber(request.getPhoneNumber())
                .city(request.getCity().trim())
                .role(role)
                .isVerified(false)
                .isBlocked(false)
                .build();
        
        user = userRepository.save(user);
        
        // If donor, create donor details
        if (request.getRole().equalsIgnoreCase("DONOR")) {
            if (request.getBloodGroup() == null) {
                throw new BadRequestException("Blood group is required for donors");
            }
            
            DonorDetails.BloodGroup bloodGroup = parseBloodGroup(request.getBloodGroup());
            
            DonorDetails donorDetails = DonorDetails.builder()
                    .user(user)
                    .bloodGroup(bloodGroup)
                    .isAvailable(true)
                    .totalDonations(0)
                    .build();
            
            donorDetailsRepository.save(donorDetails);
        }
        
        return mapToUserResponse(user);
    }
    
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));
        
        if (user.getIsBlocked()) {
            throw new UnauthorizedException("User account is blocked");
        }
        
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Invalid email or password");
        }
        
        String token = jwtTokenProvider.generateToken(request.getEmail(), user.getRole().toString());
        
        return LoginResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole().toString())
                .token(token)
                .expiresIn(jwtTokenProvider.getExpirationTime())
                .build();
    }
    
    private DonorDetails.BloodGroup parseBloodGroup(String bloodGroup) {
        try {
            String normalized = bloodGroup.trim().toUpperCase()
                    .replace("+", "_POSITIVE")
                    .replace("-", "_NEGATIVE");
            return DonorDetails.BloodGroup.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid blood group: " + bloodGroup);
        }
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
