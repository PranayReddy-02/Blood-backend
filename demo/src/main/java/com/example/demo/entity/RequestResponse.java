package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "request_responses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestResponse {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blood_request_id", nullable = false)
    private BloodRequest bloodRequest;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "donor_id", nullable = false)
    private User donor;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ResponseStatus status;
    
    @Column(length = 6)
    private String otp;
    
    @Column(name = "otp_generated_at")
    private LocalDateTime otpGeneratedAt;
    
    @Column(name = "otp_verified")
    private Boolean otpVerified = false;
    
    @Column(name = "units_provided")
    private Integer unitsProvided;
    
    @Column(name = "response_date", nullable = false, updatable = false)
    private LocalDateTime responseDate;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        responseDate = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = ResponseStatus.PENDING;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public enum ResponseStatus {
        PENDING, ACCEPTED, REJECTED, COMPLETED, CANCELLED
    }
}
