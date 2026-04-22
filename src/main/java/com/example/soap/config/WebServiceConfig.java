package com.example.soap.config;

import jakarta.xml.soap.MessageFactory;
import jakarta.xml.soap.SOAPException;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ws.config.annotation.EnableWs;
import org.springframework.ws.transport.http.MessageDispatcherServlet;
import org.springframework.ws.transport.http.WsdlDefinitionHandlerAdapter;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;
import org.springframework.ws.client.core.WebServiceTemplate;

@EnableWs
@Configuration
public class WebServiceConfig {

    @Bean
    public ServletRegistrationBean<MessageDispatcherServlet> messageDispatcherServlet(ApplicationContext applicationContext) {
        MessageDispatcherServlet servlet = new MessageDispatcherServlet();
        servlet.setApplicationContext(applicationContext);
        servlet.setTransformWsdlLocations(true);
        return new ServletRegistrationBean<>(servlet, "/ws/*");
    }

    @Bean
    public SaajSoapMessageFactory messageFactory() throws SOAPException {
        SaajSoapMessageFactory messageFactory = new SaajSoapMessageFactory(MessageFactory.newInstance());
        messageFactory.afterPropertiesSet();
        return messageFactory;
    }

    @Bean
    public WebServiceTemplate webServiceTemplate(SaajSoapMessageFactory messageFactory) {
        return new WebServiceTemplate(messageFactory);
    }

    @Bean
    public WsdlDefinitionHandlerAdapter wsdlDefinitionHandlerAdapter() {
        return new WsdlDefinitionHandlerAdapter();
    }
}
