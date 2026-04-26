package com.example.soap.service;

import com.example.soap.storage.SoapMessageLogStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class BusinessRelayService {

    private final SoapClientService soapClientService;
    private final SoapMessageLogStore soapMessageLogStore;

    @Value("${gateway.external.endpoint-url}")
    private String externalEndpointUrl;

    @Value("${gateway.external.soap-action:}")
    private String externalSoapAction;

    @Value("${gateway.storage.persist-request-xml:true}")
    private boolean persistRequestXml;

    public BusinessRelayService(SoapClientService soapClientService,
                                SoapMessageLogStore soapMessageLogStore) {
        this.soapClientService = soapClientService;
        this.soapMessageLogStore = soapMessageLogStore;
    }

    public String forwardToExternal(String requestXml, String customerId, String action) {
        String trackingId = UUID.randomUUID().toString();
        soapMessageLogStore.saveReceived(
                trackingId,
                persistRequestXml ? requestXml : null,
                customerId,
                action == null || action.isBlank() ? "BUSINESS_RELAY" : action,
                LocalDateTime.now()
        );

        try {
            String responseXml = soapClientService.send(externalEndpointUrl, externalSoapAction, requestXml);
            soapMessageLogStore.markForwarded(trackingId, responseXml, LocalDateTime.now());
            return responseXml;
        } catch (Exception ex) {
            soapMessageLogStore.markFailed(trackingId, ex.getMessage(), LocalDateTime.now());
            throw new IllegalStateException("Failed to forward business service request to external server.", ex);
        }
    }
}
