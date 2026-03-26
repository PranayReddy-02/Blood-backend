package com.example.demo.service;

import com.example.demo.dto.RequestResponseDTO;
import com.example.demo.entity.BloodRequest;
import com.example.demo.entity.RequestResponse;
import com.example.demo.entity.User;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.BloodRequestRepository;
import com.example.demo.repository.RequestResponseRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
public class RequestResponseService {
    
    @Autowired
    private RequestResponseRepository requestResponseRepository;
    
    @Autowired
    private BloodRequestRepository bloodRequestRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    private final Random random = new Random();
    
    @Transactional
    public RequestResponseDTO respondToRequest(Long requestId, Long donorId, String action) {
        BloodRequest bloodRequest = bloodRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Blood request not found"));
        
        User donor = userRepository.findById(donorId)
                .orElseThrow(() -> new ResourceNotFoundException("Donor not found"));
        
        RequestResponse existing = requestResponseRepository.findByBloodRequestIdAndDonorId(requestId, donorId)
                .orElse(null);
        
        if (existing != null && !existing.getStatus().equals(RequestResponse.ResponseStatus.REJECTED)) {
            throw new BadRequestException("Donor has already responded to this request");
        }
        
        RequestResponse response = RequestResponse.builder()
                .bloodRequest(bloodRequest)
                .donor(donor)
                .status(action.equalsIgnoreCase("ACCEPT") ? 
                        RequestResponse.ResponseStatus.ACCEPTED : RequestResponse.ResponseStatus.REJECTED)
                .build();
        
        if (action.equalsIgnoreCase("ACCEPT")) {
            response.setOtp(generateOTP());
            response.setOtpGeneratedAt(LocalDateTime.now());
        }
        
        response = requestResponseRepository.save(response);
        
        if (action.equalsIgnoreCase("ACCEPT")) {
            bloodRequest.setStatus(BloodRequest.RequestStatus.MATCHED);
            bloodRequestRepository.save(bloodRequest);
        }
        
        return mapToRequestResponseDTO(response);
    }
    
    public Page<RequestResponseDTO> getDonorResponses(Long donorId, Pageable pageable) {
        return requestResponseRepository.findByDonorId(donorId, pageable)
                .map(this::mapToRequestResponseDTO);
    }
    
    public Page<RequestResponseDTO> getRequestResponses(Long requestId, Pageable pageable) {
        return requestResponseRepository.findByBloodRequestId(requestId, pageable)
                .map(this::mapToRequestResponseDTO);
    }
    
    @Transactional
    public RequestResponseDTO verifyOTP(Long requestResponseId, String otp) {
        RequestResponse response = requestResponseRepository.findById(requestResponseId)
                .orElseThrow(() -> new ResourceNotFoundException("Response not found"));
        
        if (!response.getOtp().equals(otp)) {
            throw new BadRequestException("Invalid OTP");
        }
        
        response.setOtpVerified(true);
        response.setStatus(RequestResponse.ResponseStatus.COMPLETED);
        response = requestResponseRepository.save(response);
        
        return mapToRequestResponseDTO(response);
    }
    
    @Transactional
    public RequestResponseDTO updateUnitsProvided(Long requestResponseId, Integer units) {
        RequestResponse response = requestResponseRepository.findById(requestResponseId)
                .orElseThrow(() -> new ResourceNotFoundException("Response not found"));
        
        response.setUnitsProvided(units);
        response = requestResponseRepository.save(response);
        
        return mapToRequestResponseDTO(response);
    }
    
    private String generateOTP() {
        return String.format("%06d", random.nextInt(999999));
    }
    
    private RequestResponseDTO mapToRequestResponseDTO(RequestResponse response) {
        return RequestResponseDTO.builder()
                .id(response.getId())
                .requestId(response.getBloodRequest().getId())
                .donorId(response.getDonor().getId())
                .donorName(response.getDonor().getName())
                .status(response.getStatus().toString())
                .otp(response.getOtp())
                .otpVerified(response.getOtpVerified())
                .unitsProvided(response.getUnitsProvided())
                .responseDate(response.getResponseDate())
                .build();
    }
}
