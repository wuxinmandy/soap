package com.example.soap.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "soap_message_log")
public class SoapMessageLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String trackingId;

    @Column(length = 128)
    private String customerId;

    @Column(length = 128)
    private String action;

    @Lob
    @Column
    private String requestXml;

    @Lob
    private String downstreamResponseXml;

    @Column(nullable = false, length = 32)
    private String status;

    @Column(length = 1000)
    private String errorMessage;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime processedAt;

    public Long getId() {
        return id;
    }

    public String getTrackingId() {
        return trackingId;
    }

    public void setTrackingId(String trackingId) {
        this.trackingId = trackingId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getRequestXml() {
        return requestXml;
    }

    public void setRequestXml(String requestXml) {
        this.requestXml = requestXml;
    }

    public String getDownstreamResponseXml() {
        return downstreamResponseXml;
    }

    public void setDownstreamResponseXml(String downstreamResponseXml) {
        this.downstreamResponseXml = downstreamResponseXml;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }
}
