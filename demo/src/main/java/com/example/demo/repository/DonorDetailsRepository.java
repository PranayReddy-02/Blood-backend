package com.example.demo.repository;

import com.example.demo.entity.DonorDetails;
import com.example.demo.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DonorDetailsRepository extends JpaRepository<DonorDetails, Long> {
    Optional<DonorDetails> findByUser(User user);
    
    @Query("SELECT d FROM DonorDetails d WHERE d.bloodGroup = :bloodGroup AND d.isAvailable = true AND d.user.isBlocked = false")
    Page<DonorDetails> findAvailableDonorsByBloodGroup(
        @Param("bloodGroup") DonorDetails.BloodGroup bloodGroup,
        Pageable pageable
    );
    
    @Query("SELECT d FROM DonorDetails d WHERE d.bloodGroup = :bloodGroup AND d.isAvailable = true AND d.user.isBlocked = false AND d.user.city = :city")
    Page<DonorDetails> findAvailableDonorsByBloodGroupAndCity(
        @Param("bloodGroup") DonorDetails.BloodGroup bloodGroup,
        @Param("city") String city,
        Pageable pageable
    );
    
    long countByBloodGroup(DonorDetails.BloodGroup bloodGroup);
    long countByIsAvailableTrue();
}
