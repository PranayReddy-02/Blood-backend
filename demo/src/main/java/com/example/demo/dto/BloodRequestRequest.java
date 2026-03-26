package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
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
public class BloodRequestRequest {
    
    @NotBlank(message = "Blood group is required")
    private String bloodGroup;
    
    @NotBlank(message = "Hospital name is required")
    private String hospitalName;
    
    @NotBlank(message = "Location is required")
    private String location;
    
    @NotBlank(message = "Urgency level is required")
    private String urgencyLevel; // LOW, MEDIUM, HIGH, CRITICAL
    
    @NotNull(message = "Units required is mandatory")
    @Positive(message = "Units required must be positive")
    private Integer unitsRequired;
    
    private String notes;
}
