package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestResponseDTO {
    private Long id;
    private Long requestId;
    private Long donorId;
    private String donorName;
    private String status;
    private String otp;
    private Boolean otpVerified;
    private Integer unitsProvided;
    private LocalDateTime responseDate;
}
