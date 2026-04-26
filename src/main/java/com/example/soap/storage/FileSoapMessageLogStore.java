package com.example.soap.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
@ConditionalOnProperty(name = "gateway.storage.mode", havingValue = "file")
public class FileSoapMessageLogStore implements SoapMessageLogStore {

    private final ObjectMapper objectMapper;
    private final Path logFilePath;

    public FileSoapMessageLogStore(ObjectMapper objectMapper,
                                   @Value("${gateway.storage.file.path:./data/soap-message-log.jsonl}") String logFilePath) {
        this.objectMapper = objectMapper;
        this.logFilePath = Path.of(logFilePath);
    }

    @Override
    public void saveReceived(String trackingId,
                             String requestXml,
                             String customerId,
                             String action,
                             LocalDateTime createdAt) {
        Map<String, Object> event = baseEvent("RECEIVED", trackingId, createdAt);
        event.put("customerId", customerId);
        event.put("action", action);
        event.put("requestXml", requestXml);
        append(event);
    }

    @Override
    public void markForwarded(String trackingId, String downstreamResponseXml, LocalDateTime processedAt) {
        Map<String, Object> event = baseEvent("FORWARDED", trackingId, processedAt);
        event.put("downstreamResponseXml", downstreamResponseXml);
        append(event);
    }

    @Override
    public void markFailed(String trackingId, String errorMessage, LocalDateTime processedAt) {
        Map<String, Object> event = baseEvent("FAILED", trackingId, processedAt);
        event.put("errorMessage", errorMessage);
        append(event);
    }

    private Map<String, Object> baseEvent(String status, String trackingId, LocalDateTime timestamp) {
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("status", status);
        event.put("trackingId", trackingId);
        event.put("timestamp", timestamp);
        return event;
    }

    private synchronized void append(Map<String, Object> event) {
        try {
            Path parent = logFilePath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            String jsonLine = objectMapper.writeValueAsString(event) + System.lineSeparator();
            Files.writeString(
                    logFilePath,
                    jsonLine,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to append SOAP message log to file.", ex);
        }
    }
}
