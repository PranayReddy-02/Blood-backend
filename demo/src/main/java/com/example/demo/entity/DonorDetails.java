package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(name = "donor_details")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DonorDetails {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BloodGroup bloodGroup;
    
    @Column(name = "last_donation_date")
    private LocalDate lastDonationDate;
    
    @Column(nullable = false)
    private Boolean isAvailable = true;
    
    @Column(name = "total_donations", nullable = false)
    private Integer totalDonations = 0;
    
    public enum BloodGroup {
        O_POSITIVE("O+"),
        O_NEGATIVE("O-"),
        A_POSITIVE("A+"),
        A_NEGATIVE("A-"),
        B_POSITIVE("B+"),
        B_NEGATIVE("B-"),
        AB_POSITIVE("AB+"),
        AB_NEGATIVE("AB-");
        
        private final String label;
        
        BloodGroup(String label) {
            this.label = label;
        }
        
        public String getLabel() {
            return label;
        }
    }
    
    // Check if donor is eligible to donate (90 days since last donation)
    public boolean isEligibleToDonate() {
        if (lastDonationDate == null) {
            return true;
        }
        LocalDate ninetyDaysAgo = LocalDate.now().minusDays(90);
        return lastDonationDate.isBefore(ninetyDaysAgo) || lastDonationDate.isEqual(ninetyDaysAgo);
    }
}
