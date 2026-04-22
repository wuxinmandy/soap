package com.example.soap.model;

public record InboundSoapRequest(
        String trackingId,
        String requestXml,
        String customerId,
        String action
) {
}
