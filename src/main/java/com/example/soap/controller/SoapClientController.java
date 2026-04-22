package com.example.soap.controller;

import com.example.soap.dto.SoapClientRequest;
import com.example.soap.service.SoapClientService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/soap-client")
public class SoapClientController {

    private final SoapClientService soapClientService;

    public SoapClientController(SoapClientService soapClientService) {
        this.soapClientService = soapClientService;
    }

    @PostMapping(value = "/send", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> send(@Valid @RequestBody SoapClientRequest request) {
        String responseXml = soapClientService.send(
                request.getEndpointUrl(),
                request.getSoapAction(),
                request.getPayloadXml()
        );
        return ResponseEntity.ok(responseXml);
    }
}
