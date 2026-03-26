package com.example.demo.service;

import com.example.demo.entity.DonationHistory;
import com.example.demo.entity.DonorDetails;
import com.example.demo.entity.RequestResponse;
import com.example.demo.entity.User;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.DonationHistoryRepository;
import com.example.demo.repository.DonorDetailsRepository;
import com.example.demo.repository.RequestResponseRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class DonationService {
    
    @Autowired
    private DonationHistoryRepository donationHistoryRepository;
    
    @Autowired
    private RequestResponseRepository requestResponseRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private DonorDetailsRepository donorDetailsRepository;
    
    @Transactional
    public void recordDonation(Long requestResponseId, Integer unitsDonated, String hospitalName) {
        RequestResponse requestResponse = requestResponseRepository.findById(requestResponseId)
                .orElseThrow(() -> new ResourceNotFoundException("Request Response not found"));
        
        User donor = requestResponse.getDonor();
        
        DonationHistory donation = DonationHistory.builder()
                .donor(donor)
                .requestResponse(requestResponse)
                .unitsDonated(unitsDonated)
                .bloodGroup(requestResponse.getBloodRequest().getBloodGroup())
                .hospitalName(hospitalName)
                .donationDate(LocalDateTime.now())
                .build();
        
        donationHistoryRepository.save(donation);
        
        // Update donor's last donation date
        DonorDetails donorDetails = donorDetailsRepository.findByUser(donor)
                .orElseThrow(() -> new ResourceNotFoundException("Donor details not found"));
        
        donorDetails.setLastDonationDate(LocalDate.now());
        donorDetails.setTotalDonations(donorDetails.getTotalDonations() + 1);
        donorDetailsRepository.save(donorDetails);
    }
    
    public List<DonationHistory> getDonorDonationHistory(Long donorId) {
        return donationHistoryRepository.findByDonorId(donorId);
    }
}
