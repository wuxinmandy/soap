package com.example.soap.dto;

import jakarta.validation.constraints.NotBlank;

public class SoapClientRequest {

    @NotBlank
    private String endpointUrl;

    private String soapAction;

    @NotBlank
    private String payloadXml;

    public String getEndpointUrl() {
        return endpointUrl;
    }

    public void setEndpointUrl(String endpointUrl) {
        this.endpointUrl = endpointUrl;
    }

    public String getSoapAction() {
        return soapAction;
    }

    public void setSoapAction(String soapAction) {
        this.soapAction = soapAction;
    }

    public String getPayloadXml() {
        return payloadXml;
    }

    public void setPayloadXml(String payloadXml) {
        this.payloadXml = payloadXml;
    }
}
