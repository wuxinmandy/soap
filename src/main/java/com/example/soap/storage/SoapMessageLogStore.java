package com.example.soap.storage;

import java.time.LocalDateTime;

public interface SoapMessageLogStore {

    void saveReceived(String trackingId,
                      String requestXml,
                      String customerId,
                      String action,
                      LocalDateTime createdAt);

    void markForwarded(String trackingId, String downstreamResponseXml, LocalDateTime processedAt);

    void markFailed(String trackingId, String errorMessage, LocalDateTime processedAt);
}
