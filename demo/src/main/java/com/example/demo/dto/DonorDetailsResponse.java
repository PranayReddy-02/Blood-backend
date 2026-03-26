package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DonorDetailsResponse {
    private Long id;
    private String name;
    private String email;
    private String phoneNumber;
    private String city;
    private String bloodGroup;
    private LocalDate lastDonationDate;
    private Boolean isAvailable;
    private Integer totalDonations;
    private Boolean isEligible;
}
