package com.example.demo.controller;

import com.example.demo.dto.DonorDetailsResponse;
import com.example.demo.dto.RequestResponseDTO;
import com.example.demo.exception.BadRequestException;
import com.example.demo.service.DonorService;
import com.example.demo.service.RequestResponseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/donors")
@CrossOrigin(origins = "*", maxAge = 3600)
public class DonorController {
    
    @Autowired
    private DonorService donorService;
    
    @Autowired
    private RequestResponseService requestResponseService;
    
    @GetMapping("/search")
    public ResponseEntity<Page<DonorDetailsResponse>> searchDonors(
            @RequestParam(required = false) String bloodGroup,
            @RequestParam(required = false) String city,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        if (bloodGroup == null || bloodGroup.trim().isEmpty()) {
            throw new BadRequestException("Blood group is required for search");
        }
        
        Pageable pageable = PageRequest.of(page, size);
        
        Page<DonorDetailsResponse> donors;
        if (city != null && !city.isEmpty()) {
            donors = donorService.searchDonorsByBloodGroupAndCity(bloodGroup, city, pageable);
        } else {
            donors = donorService.searchDonorsByBloodGroup(bloodGroup, pageable);
        }
        
        return ResponseEntity.ok(donors);
    }
    
    @GetMapping("/{donorId}")
    public ResponseEntity<DonorDetailsResponse> getDonorDetails(@PathVariable Long donorId) {
        DonorDetailsResponse donor = donorService.getDonorById(donorId);
        return ResponseEntity.ok(donor);
    }
    
    @PreAuthorize("@authorizationService.isDonor(#donorId)")
    @PutMapping("/{donorId}/availability")
    public ResponseEntity<Void> updateAvailability(
            @PathVariable Long donorId,
            @RequestParam Boolean isAvailable) {
        donorService.updateDonorAvailability(donorId, isAvailable);
        return ResponseEntity.noContent().build();
    }
    
    @PreAuthorize("@authorizationService.isDonor(#donorId)")
    @GetMapping("/{donorId}/responses")
    public ResponseEntity<Page<RequestResponseDTO>> getMyResponses(
            @PathVariable Long donorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<RequestResponseDTO> responses = requestResponseService.getDonorResponses(donorId, pageable);
        
        return ResponseEntity.ok(responses);
    }
    
    @PostMapping("/respond")
    public ResponseEntity<RequestResponseDTO> respondToRequest(
            @RequestParam Long requestId,
            @RequestParam Long donorId,
            @RequestParam String action) {
        
        RequestResponseDTO response = requestResponseService.respondToRequest(requestId, donorId, action);
        return ResponseEntity.ok(response);
    }
}
