package com.example.demo.repository;

import com.example.demo.entity.BloodRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BloodRequestRepository extends JpaRepository<BloodRequest, Long> {
    
    @Query("SELECT br FROM BloodRequest br WHERE br.status = 'PENDING' OR br.status = 'MATCHED'")
    Page<BloodRequest> findActiveRequests(Pageable pageable);
    
    @Query("SELECT br FROM BloodRequest br WHERE br.bloodGroup = :bloodGroup AND (br.status = 'PENDING' OR br.status = 'MATCHED')")
    Page<BloodRequest> findActiveRequestsByBloodGroup(
        @Param("bloodGroup") String bloodGroup,
        Pageable pageable
    );
    
    @Query("SELECT br FROM BloodRequest br WHERE br.bloodGroup = :bloodGroup AND br.location = :location AND (br.status = 'PENDING' OR br.status = 'MATCHED')")
    Page<BloodRequest> findActiveRequestsByBloodGroupAndLocation(
        @Param("bloodGroup") String bloodGroup,
        @Param("location") String location,
        Pageable pageable
    );
    
    List<BloodRequest> findByRequesterId(Long requesterId);
    
    long countByStatus(String status);
}
