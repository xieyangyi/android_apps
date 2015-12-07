package com.fsl.fslclubs.me;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.InputStream;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Created by B47714 on 12/4/2015.
 */
public class ParseXmlService {
    public HashMap<String, String> parseXml(InputStream inputStream) throws Exception {
        HashMap<String, String> hashMap = new HashMap<>();
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = builderFactory.newDocumentBuilder();
        Document document = builder.parse(inputStream);
        Element root = document.getDocumentElement();
        NodeList childNodes = root.getChildNodes();

        for (int j = 0; j < childNodes.getLength(); j++) {
            Node childNode = (Node) childNodes.item(j);
            if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                Element childElement = (Element) childNode;
                if (childElement.getNodeName().equals("version")) {
                    hashMap.put("version", childElement.getFirstChild().getNodeValue());
                } else if (childElement.getNodeName().equals("name")) {
                    hashMap.put("name", childElement.getFirstChild().getNodeValue());
                } else if (childElement.getNodeName().equals("url")) {
                    hashMap.put("url", childElement.getFirstChild().getNodeValue());
                }
            }
        }

        return hashMap;
    }

}
