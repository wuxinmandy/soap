package com.example.soap.service;

import org.springframework.stereotype.Service;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.client.core.WebServiceMessageCallback;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.xml.transform.StringSource;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;

@Service
public class SoapClientService {

    private final WebServiceTemplate webServiceTemplate;

    public SoapClientService(WebServiceTemplate webServiceTemplate) {
        this.webServiceTemplate = webServiceTemplate;
    }

    public String send(String endpointUrl, String soapAction, String payloadXml) {
        StringSource requestPayload = new StringSource(payloadXml);

        Source responseSource = webServiceTemplate.sendSourceAndReceive(
                endpointUrl,
                requestPayload,
                soapActionCallback(soapAction),
                response -> response
        );

        return sourceToString(responseSource);
    }

    private WebServiceMessageCallback soapActionCallback(String soapAction) {
        return message -> {
            if (soapAction == null || soapAction.isBlank()) {
                return;
            }
            if (message instanceof SoapMessage soapMessage) {
                soapMessage.setSoapAction(soapAction);
            }
        };
    }

    private String sourceToString(Source source) {
        if (source == null) {
            return "";
        }
        try {
            StringWriter writer = new StringWriter();
            TransformerFactory.newInstance()
                    .newTransformer()
                    .transform(source, new StreamResult(writer));
            return writer.toString();
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to transform SOAP response Source to String.", ex);
        }
    }
}
