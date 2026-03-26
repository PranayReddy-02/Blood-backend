package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BloodRequestResponse {
    private Long id;
    private String requesterName;
    private String bloodGroup;
    private String hospitalName;
    private String location;
    private String urgencyLevel;
    private Integer unitsRequired;
    private String status;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
