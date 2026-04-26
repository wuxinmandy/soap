package com.example.soap.service;

import com.example.soap.model.InboundSoapRequest;
import com.example.soap.storage.SoapMessageLogStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class InboundSoapProcessingService {

    private final SoapMessageLogStore soapMessageLogStore;
    private final SoapClientService soapClientService;

    @Value("${gateway.downstream.endpoint-url}")
    private String downstreamEndpointUrl;

    @Value("${gateway.downstream.soap-action:}")
    private String downstreamSoapAction;

    @Value("${gateway.storage.persist-request-xml:true}")
    private boolean persistRequestXml;

    public InboundSoapProcessingService(SoapMessageLogStore soapMessageLogStore,
                                        SoapClientService soapClientService) {
        this.soapMessageLogStore = soapMessageLogStore;
        this.soapClientService = soapClientService;
    }

    @Async
    public void process(InboundSoapRequest inboundSoapRequest) {
        soapMessageLogStore.saveReceived(
                inboundSoapRequest.trackingId(),
                persistRequestXml ? inboundSoapRequest.requestXml() : null,
                inboundSoapRequest.customerId(),
                inboundSoapRequest.action(),
                LocalDateTime.now()
        );

        try {
            String downstreamResponseXml = soapClientService.send(
                    downstreamEndpointUrl,
                    downstreamSoapAction,
                    inboundSoapRequest.requestXml()
            );
            soapMessageLogStore.markForwarded(
                    inboundSoapRequest.trackingId(),
                    downstreamResponseXml,
                    LocalDateTime.now()
            );
        } catch (Exception ex) {
            soapMessageLogStore.markFailed(
                    inboundSoapRequest.trackingId(),
                    ex.getMessage(),
                    LocalDateTime.now()
            );
        }
    }
}
