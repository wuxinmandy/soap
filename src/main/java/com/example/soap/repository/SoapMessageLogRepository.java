package com.example.soap.repository;

import com.example.soap.entity.SoapMessageLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SoapMessageLogRepository extends JpaRepository<SoapMessageLog, Long> {

    Optional<SoapMessageLog> findByTrackingId(String trackingId);
}
