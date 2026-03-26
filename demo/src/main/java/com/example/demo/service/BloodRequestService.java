package com.example.demo.service;

import com.example.demo.dto.BloodRequestRequest;
import com.example.demo.dto.BloodRequestResponse;
import com.example.demo.entity.BloodRequest;
import com.example.demo.entity.DonorDetails;
import com.example.demo.entity.User;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.BloodRequestRepository;
import com.example.demo.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class BloodRequestService {
    private static final Logger log = LoggerFactory.getLogger(BloodRequestService.class);
    
    @Autowired
    private BloodRequestRepository bloodRequestRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Transactional
    public BloodRequestResponse createBloodRequest(BloodRequestRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || auth.getName() == null) {
            log.error("Unauthenticated request to create blood request");
            throw new BadRequestException("Unauthenticated: please login to create a blood request");
        }

        String email = auth.getName();
        log.info("Authenticated user email={} is creating blood request", email);

        Optional<User> requesterOpt = userRepository.findByEmailIgnoreCase(email);
        if (requesterOpt.isEmpty()) {
            log.error("Authenticated user not found in DB: {}. Checking for alternate casing...", email);
            Optional<User> fallback = userRepository.findByEmail(email);
            if (fallback.isPresent()) {
                log.warn("Found user by exact email, not case-insensitive: {}", email);
                requesterOpt = fallback;
            }
        }

        if (requesterOpt.isEmpty()) {
            log.error("Authenticated user not found in DB: {}", email);
            throw new ResourceNotFoundException("Authenticated user not found: " + email);
        }

        User requester = requesterOpt.get();
        log.info("Authenticated user id={} role={}", requester.getId(), requester.getRole());

        if (!requester.getRole().equals(User.UserRole.REQUESTER)) {
            log.warn("User role unauthorized: {} for user id={}", requester.getRole(), requester.getId());
            throw new BadRequestException("Unauthorized role: only requesters may create a blood request");
        }

        if (!hasAuthority(auth, "REQUESTER")) {
            log.warn("Security context authority missing REQUESTER, authorities={} for user id={}", auth.getAuthorities(), requester.getId());
            throw new BadRequestException("Unauthorized: invalid authority for making blood request");
        }

        DonorDetails.BloodGroup bloodGroup = parseDonorBloodGroup(request.getBloodGroup());
        
        BloodRequest bloodRequest = BloodRequest.builder()
                .requester(requester)
                .bloodGroup(bloodGroup)
                .hospitalName(request.getHospitalName())
                .location(request.getLocation())
                .urgencyLevel(BloodRequest.UrgencyLevel.from(request.getUrgencyLevel()))
                .unitsRequired(request.getUnitsRequired())
                .notes(request.getNotes())
                .build();
        
        bloodRequest = bloodRequestRepository.save(bloodRequest);
        return mapToBloodRequestResponse(bloodRequest);
    }

    private boolean hasAuthority(Authentication auth, String requiredAuthority) {
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(requiredAuthority::equals);
    }

    public Page<BloodRequestResponse> getActiveRequests(Pageable pageable) {
        return bloodRequestRepository.findActiveRequests(pageable)
                .map(this::mapToBloodRequestResponse);
    }
    
    public Page<BloodRequestResponse> getRequestsByBloodGroup(String bloodGroup, Pageable pageable) {
        return bloodRequestRepository.findActiveRequestsByBloodGroup(bloodGroup, pageable)
                .map(this::mapToBloodRequestResponse);
    }
    
    public Page<BloodRequestResponse> getRequestsByBloodGroupAndLocation(String bloodGroup, String location, Pageable pageable) {
        return bloodRequestRepository.findActiveRequestsByBloodGroupAndLocation(bloodGroup, location, pageable)
                .map(this::mapToBloodRequestResponse);
    }
    
    public BloodRequestResponse getRequestById(Long requestId) {
        BloodRequest bloodRequest = bloodRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Blood request not found"));
        
        return mapToBloodRequestResponse(bloodRequest);
    }
    
    public List<BloodRequestResponse> getRequestsByRequesterId(Long requesterId) {
        List<BloodRequest> requests = bloodRequestRepository.findByRequesterId(requesterId);
        return requests.stream()
                .map(this::mapToBloodRequestResponse)
                .toList();
    }

    public List<BloodRequestResponse> getRequestsForAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            log.error("Unauthenticated call to getRequestsForAuthenticatedUser");
            throw new BadRequestException("Unauthenticated: please login to fetch your requests");
        }

        String email = auth.getName();
        log.info("Authenticated user email={} querying own requests", email);

        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found: " + email));

        if (!user.getRole().equals(User.UserRole.REQUESTER)) {
            log.warn("User role not requester for email={}, role={}", email, user.getRole());
            throw new BadRequestException("Only requesters can fetch their own requests");
        }

        List<BloodRequest> requests = bloodRequestRepository.findByRequesterId(user.getId());
        return requests.stream()
                .map(this::mapToBloodRequestResponse)
                .toList();
    }
    
    @Transactional
    public BloodRequestResponse updateRequestStatus(Long requestId, String status) {
        BloodRequest bloodRequest = bloodRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Blood request not found"));
        
        bloodRequest.setStatus(BloodRequest.RequestStatus.valueOf(status.toUpperCase()));
        bloodRequest = bloodRequestRepository.save(bloodRequest);
        
        return mapToBloodRequestResponse(bloodRequest);
    }
    
    @Transactional
    public void cancelRequest(Long requestId) {
        BloodRequest bloodRequest = bloodRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Blood request not found"));
        
        bloodRequest.setStatus(BloodRequest.RequestStatus.CANCELLED);
        bloodRequestRepository.save(bloodRequest);
    }
    
    private DonorDetails.BloodGroup parseDonorBloodGroup(String bloodGroup) {
        try {
            String normalized = bloodGroup.trim().toUpperCase()
                    .replace("+", "_POSITIVE")
                    .replace("-", "_NEGATIVE");
            return DonorDetails.BloodGroup.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid blood group: " + bloodGroup);
        }
    }
    
    private BloodRequestResponse mapToBloodRequestResponse(BloodRequest bloodRequest) {
        return BloodRequestResponse.builder()
                .id(bloodRequest.getId())
                .requesterName(bloodRequest.getRequester().getName())
                .bloodGroup(bloodRequest.getBloodGroup().getLabel())
                .hospitalName(bloodRequest.getHospitalName())
                .location(bloodRequest.getLocation())
                .urgencyLevel(bloodRequest.getUrgencyLevel().toString())
                .unitsRequired(bloodRequest.getUnitsRequired())
                .status(bloodRequest.getStatus().toString())
                .notes(bloodRequest.getNotes())
                .createdAt(bloodRequest.getCreatedAt())
                .updatedAt(bloodRequest.getUpdatedAt())
                .build();
    }
}
