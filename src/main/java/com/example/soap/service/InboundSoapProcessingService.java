package com.example.soap.service;

import com.example.soap.entity.SoapMessageLog;
import com.example.soap.model.InboundSoapRequest;
import com.example.soap.repository.SoapMessageLogRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class InboundSoapProcessingService {

    private final SoapMessageLogRepository soapMessageLogRepository;
    private final SoapClientService soapClientService;

    @Value("${gateway.downstream.endpoint-url}")
    private String downstreamEndpointUrl;

    @Value("${gateway.downstream.soap-action:}")
    private String downstreamSoapAction;

    @Value("${gateway.storage.persist-request-xml:true}")
    private boolean persistRequestXml;

    public InboundSoapProcessingService(SoapMessageLogRepository soapMessageLogRepository,
                                        SoapClientService soapClientService) {
        this.soapMessageLogRepository = soapMessageLogRepository;
        this.soapClientService = soapClientService;
    }

    @Async
    public void process(InboundSoapRequest inboundSoapRequest) {
        SoapMessageLog log = new SoapMessageLog();
        log.setTrackingId(inboundSoapRequest.trackingId());
        log.setRequestXml(persistRequestXml ? inboundSoapRequest.requestXml() : null);
        log.setCustomerId(inboundSoapRequest.customerId());
        log.setAction(inboundSoapRequest.action());
        log.setStatus("RECEIVED");
        log.setCreatedAt(LocalDateTime.now());
        soapMessageLogRepository.save(log);

        try {
            String downstreamResponseXml = soapClientService.send(
                    downstreamEndpointUrl,
                    downstreamSoapAction,
                    inboundSoapRequest.requestXml()
            );
            log.setDownstreamResponseXml(downstreamResponseXml);
            log.setStatus("FORWARDED");
            log.setProcessedAt(LocalDateTime.now());
            soapMessageLogRepository.save(log);
        } catch (Exception ex) {
            log.setStatus("FAILED");
            log.setErrorMessage(ex.getMessage());
            log.setProcessedAt(LocalDateTime.now());
            soapMessageLogRepository.save(log);
        }
    }
}
