package com.example.soap.endpoint;

import com.example.soap.constant.SoapNamespaces;
import com.example.soap.service.BusinessRelayService;
import com.example.soap.util.XmlDomParser;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;
import org.springframework.xml.transform.StringSource;
import org.w3c.dom.Document;

import javax.xml.transform.Source;

@Endpoint
public class BusinessRelayEndpoint {

    private final BusinessRelayService businessRelayService;

    public BusinessRelayEndpoint(BusinessRelayService businessRelayService) {
        this.businessRelayService = businessRelayService;
    }

    @PayloadRoot(namespace = SoapNamespaces.NAMESPACE_URI, localPart = SoapNamespaces.BUSINESS_RELAY_LOCAL_PART)
    @ResponsePayload
    public Source relayBusinessRequest(@RequestPayload Source request) throws Exception {
        String requestXml = XmlDomParser.sourceToString(request);
        Document document = XmlDomParser.stringToDocument(requestXml);

        String customerId = XmlDomParser.getTagValue(document, "customerId").orElse("UNKNOWN");
        String action = XmlDomParser.getTagValue(document, "action").orElse("BUSINESS_RELAY");

        String externalResponseXml = businessRelayService.forwardToExternal(requestXml, customerId, action);
        return new StringSource(externalResponseXml);
    }
}
