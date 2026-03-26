package com.example.demo.repository;

import com.example.demo.entity.RequestResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RequestResponseRepository extends JpaRepository<RequestResponse, Long> {
    List<RequestResponse> findByDonorId(Long donorId);
    List<RequestResponse> findByBloodRequestId(Long bloodRequestId);
    Optional<RequestResponse> findByBloodRequestIdAndDonorId(Long bloodRequestId, Long donorId);
    Page<RequestResponse> findByDonorId(Long donorId, Pageable pageable);
    Page<RequestResponse> findByBloodRequestId(Long bloodRequestId, Pageable pageable);
    long countByStatus(String status);
}
