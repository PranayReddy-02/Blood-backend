package com.example.demo.service;

import com.example.demo.dto.DonorDetailsResponse;
import com.example.demo.entity.DonorDetails;
import com.example.demo.entity.User;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.DonorDetailsRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class DonorService {
    
    @Autowired
    private DonorDetailsRepository donorDetailsRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    public Page<DonorDetailsResponse> searchDonorsByBloodGroup(String bloodGroup, Pageable pageable) {
        DonorDetails.BloodGroup bg = parseBloodGroup(bloodGroup);
        
        return donorDetailsRepository.findAvailableDonorsByBloodGroup(bg, pageable)
                .map(this::mapToDonorDetailsResponse);
    }
    
    public Page<DonorDetailsResponse> searchDonorsByBloodGroupAndCity(String bloodGroup, String city, Pageable pageable) {
        DonorDetails.BloodGroup bg = parseBloodGroup(bloodGroup);
        
        return donorDetailsRepository.findAvailableDonorsByBloodGroupAndCity(bg, city, pageable)
                .map(this::mapToDonorDetailsResponse);
    }
    
    public DonorDetailsResponse getDonorById(Long donorId) {
        User user = userRepository.findById(donorId)
                .orElseThrow(() -> new ResourceNotFoundException("Donor not found"));
        
        DonorDetails donorDetails = donorDetailsRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Donor details not found"));
        
        return mapToDonorDetailsResponse(donorDetails);
    }
    
    public void updateDonorAvailability(Long userId, Boolean isAvailable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        DonorDetails donorDetails = donorDetailsRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Donor details not found"));
        
        donorDetails.setIsAvailable(isAvailable);
        donorDetailsRepository.save(donorDetails);
    }
    
    public void updateLastDonationDate(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        DonorDetails donorDetails = donorDetailsRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Donor details not found"));
        
        donorDetails.setLastDonationDate(java.time.LocalDate.now());
        donorDetails.setTotalDonations(donorDetails.getTotalDonations() + 1);
        donorDetailsRepository.save(donorDetails);
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
    
    public DonorDetailsResponse mapToDonorDetailsResponse(DonorDetails donorDetails) {
        return DonorDetailsResponse.builder()
                .id(donorDetails.getId())
                .name(donorDetails.getUser().getName())
                .email(donorDetails.getUser().getEmail())
                .phoneNumber(donorDetails.getUser().getPhoneNumber())
                .city(donorDetails.getUser().getCity())
                .bloodGroup(donorDetails.getBloodGroup().getLabel())
                .lastDonationDate(donorDetails.getLastDonationDate())
                .isAvailable(donorDetails.getIsAvailable())
                .totalDonations(donorDetails.getTotalDonations())
                .isEligible(donorDetails.isEligibleToDonate())
                .build();
    }
}
