package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminDashboardResponse {
    private Long totalDonors;
    private Long totalRequesters;
    private Long activeDonors;
    private Long activeRequests;
    private Long completedDonations;
    private String mostRequestedBloodGroup;
    private Long totalVerifiedUsers;
    private Long totalBlockedUsers;
}
