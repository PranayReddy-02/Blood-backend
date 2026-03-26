package com.example.demo.controller;

import com.example.demo.dto.BloodRequestRequest;
import com.example.demo.dto.BloodRequestResponse;
import com.example.demo.dto.DonationCompletionRequest;
import com.example.demo.dto.RequestResponseDTO;
import com.example.demo.service.BloodRequestService;
import com.example.demo.service.DonationService;
import com.example.demo.service.RequestResponseService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*", maxAge = 3600)
public class RequesterController {
    
    @Autowired
    private BloodRequestService bloodRequestService;
    
    @Autowired
    private RequestResponseService requestResponseService;
    
    @Autowired
    private DonationService donationService;
    
    @PreAuthorize("hasAuthority('REQUESTER')")
    @PostMapping("/requests")
    public ResponseEntity<BloodRequestResponse> createBloodRequest(
            @Valid @RequestBody BloodRequestRequest request) {
        
        BloodRequestResponse response = bloodRequestService.createBloodRequest(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    
    @PreAuthorize("@authorizationService.isRequester(#requesterId)")
    @GetMapping("/requesters/{requesterId}/requests")
    public ResponseEntity<List<BloodRequestResponse>> getRequestsByRequesterId(@PathVariable Long requesterId) {
        List<BloodRequestResponse> requests = bloodRequestService.getRequestsByRequesterId(requesterId);
        return ResponseEntity.ok(requests);
    }
    
    @PreAuthorize("hasAuthority('REQUESTER')")
    @GetMapping("/requests")
    public ResponseEntity<List<BloodRequestResponse>> getMyRequests() {
        List<BloodRequestResponse> requests = bloodRequestService.getRequestsForAuthenticatedUser();
        return ResponseEntity.ok(requests);
    }
    
    @GetMapping("/requests/active")
    public ResponseEntity<Page<BloodRequestResponse>> getActiveRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<BloodRequestResponse> requests = bloodRequestService.getActiveRequests(pageable);
        
        return ResponseEntity.ok(requests);
    }
    
    @GetMapping("/requests/search")
    public ResponseEntity<Page<BloodRequestResponse>> searchRequests(
            @RequestParam String bloodGroup,
            @RequestParam(required = false) String location,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<BloodRequestResponse> requests;
        
        if (location != null && !location.isEmpty()) {
            requests = bloodRequestService.getRequestsByBloodGroupAndLocation(bloodGroup, location, pageable);
        } else {
            requests = bloodRequestService.getRequestsByBloodGroup(bloodGroup, pageable);
        }
        
        return ResponseEntity.ok(requests);
    }
    
    @GetMapping("/requests/{requestId}")
    public ResponseEntity<BloodRequestResponse> getRequestDetails(@PathVariable Long requestId) {
        BloodRequestResponse request = bloodRequestService.getRequestById(requestId);
        return ResponseEntity.ok(request);
    }
    
    @GetMapping("/requests/{requestId}/responses")
    public ResponseEntity<Page<RequestResponseDTO>> getRequestResponses(
            @PathVariable Long requestId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<RequestResponseDTO> responses = requestResponseService.getRequestResponses(requestId, pageable);
        
        return ResponseEntity.ok(responses);
    }
    
    @PreAuthorize("@authorizationService.isRequester(#requesterId)")
    @PostMapping("/{requesterId}/donations/verify")
    public ResponseEntity<Void> verifyDonation(
            @PathVariable Long requesterId,
            @Valid @RequestBody DonationCompletionRequest request) {
        
        requestResponseService.verifyOTP(request.getRequestResponseId(), request.getOtp());
        donationService.recordDonation(
                request.getRequestResponseId(),
                request.getUnitsProvided(),
                ""
        );
        
        return ResponseEntity.noContent().build();
    }
    
    @PreAuthorize("@authorizationService.isRequester(#requesterId)")
    @DeleteMapping("/{requesterId}/requests/{requestId}")
    public ResponseEntity<Void> cancelRequest(
            @PathVariable Long requesterId,
            @PathVariable Long requestId) {
        
        bloodRequestService.cancelRequest(requestId);
        return ResponseEntity.noContent().build();
    }
}
