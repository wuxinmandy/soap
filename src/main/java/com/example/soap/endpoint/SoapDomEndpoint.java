package com.example.soap.endpoint;

import com.example.soap.constant.SoapNamespaces;
import com.example.soap.model.InboundSoapRequest;
import com.example.soap.service.InboundSoapProcessingService;
import com.example.soap.util.XmlDomParser;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;
import org.springframework.xml.transform.StringSource;
import org.w3c.dom.Document;

import javax.xml.transform.Source;
import java.util.UUID;

@Endpoint
public class SoapDomEndpoint {

    private final InboundSoapProcessingService inboundSoapProcessingService;

    public SoapDomEndpoint(InboundSoapProcessingService inboundSoapProcessingService) {
        this.inboundSoapProcessingService = inboundSoapProcessingService;
    }

    @PayloadRoot(namespace = SoapNamespaces.NAMESPACE_URI, localPart = SoapNamespaces.REQUEST_LOCAL_PART)
    @ResponsePayload
    public Source processRequest(@RequestPayload Source request) throws Exception {
        String requestXml = XmlDomParser.sourceToString(request);
        Document document = XmlDomParser.stringToDocument(requestXml);
        String customerId = XmlDomParser.getTagValue(document, "customerId").orElse("");
        String action = XmlDomParser.getTagValue(document, "action").orElse("");

        if (customerId.isBlank() || action.isBlank()) {
            String rejectedResponseXml = """
                    <ns:processResponse xmlns:ns="http://example.com/soap">
                        <ns:status>REJECTED</ns:status>
                        <ns:message>Validation failed: customerId and action are required.</ns:message>
                    </ns:processResponse>
                    """;
            return new StringSource(rejectedResponseXml);
        }

        String trackingId = UUID.randomUUID().toString();

        inboundSoapProcessingService.process(
                new InboundSoapRequest(trackingId, requestXml, customerId, action)
        );

        String responseXml = """
                <ns:processResponse xmlns:ns="http://example.com/soap">
                    <ns:status>ACCEPTED</ns:status>
                    <ns:message>Request received and forwarded asynchronously.</ns:message>
                    <ns:trackingId>%s</ns:trackingId>
                    <ns:customerId>%s</ns:customerId>
                    <ns:action>%s</ns:action>
                </ns:processResponse>
                """.formatted(escapeXml(trackingId), escapeXml(customerId), escapeXml(action));

        return new StringSource(responseXml);
    }

    private String escapeXml(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}
