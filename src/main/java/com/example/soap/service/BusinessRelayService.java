package com.example.soap.service;

import com.example.soap.entity.SoapMessageLog;
import com.example.soap.repository.SoapMessageLogRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class BusinessRelayService {

    private final SoapClientService soapClientService;
    private final SoapMessageLogRepository soapMessageLogRepository;

    @Value("${gateway.external.endpoint-url}")
    private String externalEndpointUrl;

    @Value("${gateway.external.soap-action:}")
    private String externalSoapAction;

    @Value("${gateway.storage.persist-request-xml:true}")
    private boolean persistRequestXml;

    public BusinessRelayService(SoapClientService soapClientService,
                                SoapMessageLogRepository soapMessageLogRepository) {
        this.soapClientService = soapClientService;
        this.soapMessageLogRepository = soapMessageLogRepository;
    }

    public String forwardToExternal(String requestXml, String customerId, String action) {
        SoapMessageLog log = new SoapMessageLog();
        log.setTrackingId(UUID.randomUUID().toString());
        log.setRequestXml(persistRequestXml ? requestXml : null);
        log.setCustomerId(customerId);
        log.setAction(action == null || action.isBlank() ? "BUSINESS_RELAY" : action);
        log.setStatus("RECEIVED");
        log.setCreatedAt(LocalDateTime.now());
        soapMessageLogRepository.save(log);

        try {
            String responseXml = soapClientService.send(externalEndpointUrl, externalSoapAction, requestXml);
            log.setDownstreamResponseXml(responseXml);
            log.setStatus("FORWARDED");
            log.setProcessedAt(LocalDateTime.now());
            soapMessageLogRepository.save(log);
            return responseXml;
        } catch (Exception ex) {
            log.setStatus("FAILED");
            log.setErrorMessage(ex.getMessage());
            log.setProcessedAt(LocalDateTime.now());
            soapMessageLogRepository.save(log);
            throw new IllegalStateException("Failed to forward business service request to external server.", ex);
        }
    }
}
