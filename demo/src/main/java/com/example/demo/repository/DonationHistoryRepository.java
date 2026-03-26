package com.example.demo.repository;

import com.example.demo.entity.DonationHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DonationHistoryRepository extends JpaRepository<DonationHistory, Long> {
    List<DonationHistory> findByDonorId(Long donorId);
    long count();
    
    @Query("SELECT dh.bloodGroup, COUNT(dh) as count FROM DonationHistory dh GROUP BY dh.bloodGroup ORDER BY count DESC LIMIT 1")
    String findMostRequestedBloodGroup();
}
