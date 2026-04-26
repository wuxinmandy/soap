package com.example.soap.storage;

import com.example.soap.entity.SoapMessageLog;
import com.example.soap.repository.SoapMessageLogRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@ConditionalOnProperty(name = "gateway.storage.mode", havingValue = "db", matchIfMissing = true)
public class DbSoapMessageLogStore implements SoapMessageLogStore {

    private final SoapMessageLogRepository soapMessageLogRepository;

    public DbSoapMessageLogStore(SoapMessageLogRepository soapMessageLogRepository) {
        this.soapMessageLogRepository = soapMessageLogRepository;
    }

    @Override
    public void saveReceived(String trackingId,
                             String requestXml,
                             String customerId,
                             String action,
                             LocalDateTime createdAt) {
        SoapMessageLog log = new SoapMessageLog();
        log.setTrackingId(trackingId);
        log.setRequestXml(requestXml);
        log.setCustomerId(customerId);
        log.setAction(action);
        log.setStatus("RECEIVED");
        log.setCreatedAt(createdAt);
        soapMessageLogRepository.save(log);
    }

    @Override
    public void markForwarded(String trackingId, String downstreamResponseXml, LocalDateTime processedAt) {
        soapMessageLogRepository.findByTrackingId(trackingId).ifPresent(log -> {
            log.setDownstreamResponseXml(downstreamResponseXml);
            log.setStatus("FORWARDED");
            log.setProcessedAt(processedAt);
            soapMessageLogRepository.save(log);
        });
    }

    @Override
    public void markFailed(String trackingId, String errorMessage, LocalDateTime processedAt) {
        soapMessageLogRepository.findByTrackingId(trackingId).ifPresent(log -> {
            log.setStatus("FAILED");
            log.setErrorMessage(errorMessage);
            log.setProcessedAt(processedAt);
            soapMessageLogRepository.save(log);
        });
    }
}
