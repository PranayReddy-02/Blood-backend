package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "donation_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DonationHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "donor_id", nullable = false)
    private User donor;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_response_id")
    private RequestResponse requestResponse;
    
    @Column(nullable = false)
    private Integer unitsDonated;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DonorDetails.BloodGroup bloodGroup;
    
    @Column(nullable = false)
    private String hospitalName;
    
    @Column(nullable = false)
    private LocalDateTime donationDate;
    
    @Column(length = 500)
    private String notes;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (donationDate == null) {
            donationDate = LocalDateTime.now();
        }
    }
}
