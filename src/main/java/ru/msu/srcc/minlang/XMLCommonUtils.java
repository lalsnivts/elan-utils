package ru.msu.srcc.minlang;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by User on 16.03.2015.
 */
public class XMLCommonUtils {

    protected NodeList allTiers;
    protected Document doc;

    protected List<String> getAnnotationValues(Node node) throws XPathExpressionException {
        String annotationExpression = ".//ANNOTATION_VALUE";
        XPath xpath = XPathFactory.newInstance().newXPath();
        NodeList nodeSet = (NodeList)xpath.evaluate(annotationExpression, node, XPathConstants.NODESET);
        List<String> annotationValues = new ArrayList<String>();
        for (int i = 0; i < nodeSet.getLength(); i++) {
            annotationValues.add(nodeSet.item(i).getTextContent());
        }
        return annotationValues;
    }

    protected List<Node>getAnnotationNodesByTierNameReference(String tierName, String reference) throws XPathExpressionException{
        for (int i = 0; i < this.allTiers.getLength(); i++) {
            Node node = this.allTiers.item(i);
            NamedNodeMap attributes = node.getAttributes();
            Node nameAttrib = attributes.getNamedItem("TIER_ID");
            if (nameAttrib.getNodeValue().equals(tierName)) {
                return getAnnotationNodesByReference(node, reference);
            }
        }
        return null;
    }



    protected String getAnnotationId(Node node) throws XPathExpressionException{
        String annotationIdExpression = ".//@ANNOTATION_ID";
        XPath xpath = XPathFactory.newInstance().newXPath();
        Node annotationIdNode = (Node)xpath.evaluate(annotationIdExpression, node, XPathConstants.NODE);
        if(annotationIdNode == null){
            return null;
        }
        return annotationIdNode.getTextContent();
    }

    protected String getTimeSlot(Node node) throws XPathExpressionException{
        String annotationIdExpression = ".//@TIME_SLOT_REF1";
        XPath xpath = XPathFactory.newInstance().newXPath();
        Node annotationIdNode = (Node)xpath.evaluate(annotationIdExpression, node, XPathConstants.NODE);
        if(annotationIdNode == null){
            return null;
        }
        return annotationIdNode.getTextContent();
    }

    protected List<Node>getAnnotationNodesByReference(Node node, String reference) throws XPathExpressionException{
        String annotationExpression = ".//ANNOTATION/REF_ANNOTATION[@ANNOTATION_REF=\""+reference+"\"]";
        XPath xpath = XPathFactory.newInstance().newXPath();
        NodeList nodeSet = (NodeList)xpath.evaluate(annotationExpression, node, XPathConstants.NODESET);
        List<Node> annotations = new ArrayList<Node>();
        for (int i = 0; i < nodeSet.getLength(); i++) {
            annotations.add(nodeSet.item(i));
        }
        return annotations;
    }



    protected List<Node> getAnnotations(Node node) throws XPathExpressionException{
        String annotationExpression = ".//ANNOTATION";
        XPath xpath = XPathFactory.newInstance().newXPath();
        NodeList nodeSet = (NodeList)xpath.evaluate(annotationExpression, node, XPathConstants.NODESET);
        List<Node> annotations = new ArrayList<Node>();
        for (int i = 0; i < nodeSet.getLength(); i++) {
            annotations.add(nodeSet.item(i));
        }
        return annotations;
    }


    protected Document createDoc(File file) throws SAXException, ParserConfigurationException, IOException {
        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        f.setValidating(false);
        DocumentBuilder builder = f.newDocumentBuilder();
        return builder.parse(file);
    }



    protected List<String>getAnnotationValuesByTierName(String tierName) throws XPathExpressionException{
        for (int i = 0; i < this.allTiers.getLength(); i++) {
            Node node = this.allTiers.item(i);
            NamedNodeMap attributes = node.getAttributes();
            Node nameAttrib = attributes.getNamedItem("TIER_ID");
            if (nameAttrib.getNodeValue().equals(tierName)) {
                return getAnnotationValues(node);
            }
        }
        return null;
    }

    protected List<Node>getAnnotationNodesByTierName(String tierName) throws XPathExpressionException{
        for (int i = 0; i < allTiers.getLength(); i++) {
            Node node = allTiers.item(i);
            NamedNodeMap attributes = node.getAttributes();
            Node nameAttrib = attributes.getNamedItem("TIER_ID");
            if (nameAttrib.getNodeValue().equals(tierName)) {
                return getAnnotations(node);
            }
        }
        return null;
    }


    protected Node getOneAnnotationNodeByTierNameReference(String tierName, String reference) throws XPathExpressionException{
        for (int i = 0; i < this.allTiers.getLength(); i++) {
            Node node = this.allTiers.item(i);
            NamedNodeMap attributes = node.getAttributes();
            Node nameAttrib = attributes.getNamedItem("TIER_ID");
            if (nameAttrib.getNodeValue().equals(tierName)) {
                return getOneAnnotationNode(node, reference);
            }
        }
        return null;
    }

    protected Node getOneAnnotationNode(Node node, String reference) throws XPathExpressionException{
        String annotationExpression = ".//ANNOTATION/REF_ANNOTATION[@ANNOTATION_REF=\""+reference+"\"]";
        XPath xpath = XPathFactory.newInstance().newXPath();
        return  (Node)xpath.evaluate(annotationExpression, node, XPathConstants.NODE);

    }
}
