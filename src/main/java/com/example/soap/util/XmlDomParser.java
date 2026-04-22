package com.example.soap.util;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamResult;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Optional;

public final class XmlDomParser {

    private XmlDomParser() {
    }

    public static Document toDocument(Source source) throws Exception {
        DOMResult domResult = new DOMResult();
        TransformerFactory.newInstance()
                .newTransformer()
                .transform(source, domResult);

        Node node = domResult.getNode();
        if (node instanceof Document document) {
            return document;
        }
        if (node != null && node.getOwnerDocument() != null) {
            return node.getOwnerDocument();
        }
        throw new IllegalArgumentException("Cannot convert Source to DOM Document.");
    }

    public static Optional<String> getTagValue(Document document, String localName) {
        if (document == null || document.getDocumentElement() == null) {
            return Optional.empty();
        }
        Node firstMatch = document.getDocumentElement()
                .getElementsByTagNameNS("*", localName)
                .item(0);
        if (firstMatch == null) {
            return Optional.empty();
        }
        String value = firstMatch.getTextContent();
        return value == null || value.isBlank() ? Optional.empty() : Optional.of(value.trim());
    }

    public static String sourceToString(Source source) throws Exception {
        StringWriter writer = new StringWriter();
        TransformerFactory.newInstance()
                .newTransformer()
                .transform(source, new StreamResult(writer));
        return writer.toString();
    }

    public static Document stringToDocument(String xml) throws Exception {
        javax.xml.parsers.DocumentBuilderFactory factory = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        javax.xml.parsers.DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new InputSource(new StringReader(xml)));
    }
}
