package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "blood_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BloodRequest {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DonorDetails.BloodGroup bloodGroup;
    
    @Column(nullable = false)
    private String hospitalName;
    
    @Column(nullable = false)
    private String location;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UrgencyLevel urgencyLevel;
    
    @Column(nullable = false)
    private Integer unitsRequired;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestStatus status;
    
    @Column(length = 500)
    private String notes;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = RequestStatus.PENDING;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public enum UrgencyLevel {
        LOW, MEDIUM, HIGH, URGENT, CRITICAL;

        public static UrgencyLevel from(String value) {
            if (value == null || value.trim().isEmpty()) {
                throw new IllegalArgumentException("Urgency level is required");
            }
            String normalized = value.trim().toUpperCase();
            switch (normalized) {
                case "URGENT":
                    return URGENT;
                case "HIGH":
                    return HIGH;
                case "CRITICAL":
                    return CRITICAL;
                case "MEDIUM":
                    return MEDIUM;
                case "LOW":
                    return LOW;
                default:
                    throw new IllegalArgumentException("Invalid urgency level: " + value);
            }
        }
    }
    
    public enum RequestStatus {
        PENDING, MATCHED, IN_PROGRESS, COMPLETED, CANCELLED
    }

    public class BloodGroup {

        public static BloodGroup valueOf(String normalized) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'valueOf'");
        }
    }
}
