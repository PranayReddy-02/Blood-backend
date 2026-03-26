package com.example.demo.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DonationCompletionRequest {
    
    @NotNull(message = "Request response ID is required")
    private Long requestResponseId;
    
    @NotNull(message = "OTP is required")
    private String otp;
    
    @NotNull(message = "Units provided is required")
    @Positive(message = "Units provided must be positive")
    private Integer unitsProvided;
    
    private String notes;
}
