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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by User on 17.02.2015.
 */
public class XMLElanUtils {


    private static final String SIL_RUS_TIER_NAME = "rus";
    private static final String SIL_FON_WORD_TIER_NAME = "fonWord";
    private static final String SIL_FON_TIER_NAME = "fon";
    private static final String SIL_GLOSS_TIER_NAME = "gl";
    private NodeList allTiers;
    private Document doc;

    public ElanParsingResult parseElanFile(String filename, String originalName) throws ElanException {
        File file = new File(filename);
        try {
            allTiers = parseXMLFile(file);
            List<Node> originalSentences = getSILOriginalMessages(originalName);
            List<String> russianSentences = getSILRussianArray();
            return new ElanParsingResult(originalSentences, russianSentences);

        } catch (IOException e) {
            throw new ElanException(String.format("Error occurred when reading the file: %s", e.getMessage()));
        } catch (SAXException e) {
            throw new ElanException(String.format("Error: The file is not a valid XML file: %s", e.getMessage()));
        } catch (ParserConfigurationException e) {
            throw new ElanException(String.format("Error: The parser is not configured properly: %s", e.getMessage()));
        } catch (XPathExpressionException e) {
            throw new ElanException(String.format("Error: There are no tiers in the document: %s", e.getMessage()));
        }
    }


    public ElanParsingResult parseElanFileTwoSpeakers(String filename, String originalName) throws ElanException {
        File file = new File(filename);
        try {
            allTiers = parseXMLFile(file);
            List<Node> originalSentences1 = getSILOriginalMessages(originalName + "1");
            List<Node> originalSentences2 = getSILOriginalMessages(originalName + "2");

            originalSentences1.addAll(originalSentences2);
            Collections.sort(originalSentences1, new SentenceComparator());
            List<String> russianSentences = new ArrayList<String>();
            for (Node node : originalSentences1) {


                String reference = getAnnotationId(node);


                List<Node> rusNode = getAnnotationNodesByTierNameReference(SIL_RUS_TIER_NAME + "1", reference);
                if (rusNode == null || rusNode.isEmpty()) {

                    rusNode = getAnnotationNodesByTierNameReference(SIL_RUS_TIER_NAME + "2", reference);
                }
                russianSentences.add(rusNode.get(0).getTextContent());
            }


            return new ElanParsingResult(originalSentences1, russianSentences);

        } catch (IOException e) {
            throw new ElanException(String.format("Error occurred when reading the file: %s", e.getMessage()));
        } catch (SAXException e) {
            throw new ElanException(String.format("Error: The file is not a valid XML file: %s", e.getMessage()));
        } catch (ParserConfigurationException e) {
            throw new ElanException(String.format("Error: The parser is not configured properly: %s", e.getMessage()));
        } catch (XPathExpressionException e) {
            throw new ElanException(String.format("Error: There are no tiers in the document: %s", e.getMessage()));
        }
    }

    public List<Long[]> getTimeArray() throws ElanException {
        List<Long[]> begEnds = new ArrayList<Long[]>();
        XPath xpath = XPathFactory.newInstance().newXPath();
        NodeList timeValuesMS = null;
        try {
            timeValuesMS = ((NodeList) xpath.evaluate("//TIME_SLOT/@TIME_VALUE", doc, XPathConstants.NODESET));
        } catch (XPathExpressionException e) {
            throw new ElanException(String.format("Error: no time values found: %s", e.getMessage()));
        }
        for (int i = 0; i < timeValuesMS.getLength() - 1; i += 2) {
            String beginStr = timeValuesMS.item(i).getTextContent();
            String endStr = timeValuesMS.item(i + 1).getTextContent();
            long begin = Long.parseLong(beginStr);
            long end = Long.parseLong(endStr);
            begEnds.add(new Long[]{begin, end});
        }
        return begEnds;
    }

    public List<Node> getFonWordsByReference(Node node) throws ElanException {
        try {
            String reference = getAnnotationId(node);
            return getAnnotationNodesByTierNameReference(SIL_FON_WORD_TIER_NAME, reference);
        } catch (XPathExpressionException e) {
            throw new ElanException(String.format("Error: no %s tier found: %s", SIL_FON_WORD_TIER_NAME, e.getMessage()));
        }
    }


    public List<Node> getFonWordsByReferenceTwoSpeakers(Node node) throws ElanException {
        try {
            String reference = getAnnotationId(node);
            List<Node> fonWords = getAnnotationNodesByTierNameReference(SIL_FON_WORD_TIER_NAME + "1", reference);
            if (fonWords == null || fonWords.isEmpty()) {
                fonWords = getAnnotationNodesByTierNameReference(SIL_FON_WORD_TIER_NAME + "2", reference);
            }
            return fonWords;
        } catch (XPathExpressionException e) {
            throw new ElanException(String.format("Error: no %s tier found: %s", SIL_FON_WORD_TIER_NAME, e.getMessage()));
        }
    }


    public List<Node> getMorphemesByReference(Node node) throws ElanException {
        try {
            String reference = getAnnotationId(node);
            return getAnnotationNodesByTierNameReference(SIL_FON_TIER_NAME, reference);
        } catch (XPathExpressionException e) {
            throw new ElanException(String.format("Error: no %s morpheme found: %s", SIL_FON_TIER_NAME, e.getMessage()));
        }
    }

    public List<Node> getMorphemesByReferenceTwoSpeakers(Node node) throws ElanException {
        try {
            String reference = getAnnotationId(node);
            List<Node> morphNodes = getAnnotationNodesByTierNameReference(SIL_FON_TIER_NAME + "1", reference);
            if (morphNodes == null || morphNodes.isEmpty()) {
                morphNodes = getAnnotationNodesByTierNameReference(SIL_FON_TIER_NAME + "2", reference);
            }
            return morphNodes;
        } catch (XPathExpressionException e) {
            throw new ElanException(String.format("Error: no %s morpheme found: %s", SIL_FON_TIER_NAME, e.getMessage()));
        }
    }

    public Node getGlossByReference(Node node) throws ElanException {
        try {
            String reference = getAnnotationId(node);
            return getOneAnnotationNodeByTierNameReference(SIL_GLOSS_TIER_NAME, reference);
        } catch (XPathExpressionException e) {
            throw new ElanException(String.format("Error: no %s gloss found: %s", SIL_GLOSS_TIER_NAME, e.getMessage()));
        }

    }

    public Node getGlossByReferenceTwoSpeakers(Node node) throws ElanException {
        try {
            String reference = getAnnotationId(node);

            List<Node> glossNodes = getAnnotationNodesByTierNameReference(SIL_GLOSS_TIER_NAME + "1", reference);
            if (glossNodes == null || glossNodes.isEmpty()) {
                glossNodes = getAnnotationNodesByTierNameReference(SIL_GLOSS_TIER_NAME + "2", reference);
            }
            return glossNodes.get(0);
        } catch (XPathExpressionException e) {
            throw new ElanException(String.format("Error: no %s gloss found: %s", SIL_GLOSS_TIER_NAME, e.getMessage()));
        }

    }

    private NodeList parseXMLFile(File file) throws IOException, SAXException, ParserConfigurationException, XPathExpressionException {
        doc = createDoc(file);
        if (doc == null) {
            throw new IllegalArgumentException(String.format("The file is incorrect: %s", file.getAbsolutePath()));
        }
        XPath xpath = XPathFactory.newInstance().newXPath();
        return ((NodeList) xpath.evaluate("//TIER", doc, XPathConstants.NODESET));
    }

    private List<Node> getSILOriginalMessages(String originalTierName) throws XPathExpressionException {
        return getAnnotationNodesByTierName(originalTierName);
    }

    private List<String> getSILRussianArray() throws XPathExpressionException {
        return getAnnotationValuesByTierName(SIL_RUS_TIER_NAME);
    }


    private List<Node> getAnnotationNodesByTierName(String tierName) throws XPathExpressionException {
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


    private Node getOneAnnotationNodeByTierNameReference(String tierName, String reference) throws XPathExpressionException {
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

    private Node getOneAnnotationNode(Node node, String reference) throws XPathExpressionException {
        String annotationExpression = ".//ANNOTATION/REF_ANNOTATION[@ANNOTATION_REF=\"" + reference + "\"]";
        XPath xpath = XPathFactory.newInstance().newXPath();
        return (Node) xpath.evaluate(annotationExpression, node, XPathConstants.NODE);

    }


    private List<Node> getAnnotationNodesByReference(Node node, String reference) throws XPathExpressionException {
        String annotationExpression = ".//ANNOTATION/REF_ANNOTATION[@ANNOTATION_REF=\"" + reference + "\"]";
        XPath xpath = XPathFactory.newInstance().newXPath();
        NodeList nodeSet = (NodeList) xpath.evaluate(annotationExpression, node, XPathConstants.NODESET);
        List<Node> annotations = new ArrayList<Node>();
        for (int i = 0; i < nodeSet.getLength(); i++) {
            annotations.add(nodeSet.item(i));
        }
        return annotations;
    }

    private List<Node> getAnnotations(Node node) throws XPathExpressionException {
        String annotationExpression = ".//ANNOTATION";
        XPath xpath = XPathFactory.newInstance().newXPath();
        NodeList nodeSet = (NodeList) xpath.evaluate(annotationExpression, node, XPathConstants.NODESET);
        List<Node> annotations = new ArrayList<Node>();
        for (int i = 0; i < nodeSet.getLength(); i++) {
            annotations.add(nodeSet.item(i));
        }
        return annotations;
    }


    private Document createDoc(File file) throws SAXException, ParserConfigurationException, IOException {
        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        f.setValidating(false);
        DocumentBuilder builder = f.newDocumentBuilder();
        return builder.parse(file);
    }


    private List<String> getAnnotationValuesByTierName(String tierName) throws XPathExpressionException {
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

    private List<String> getAnnotationValues(Node node) throws XPathExpressionException {
        String annotationExpression = ".//ANNOTATION_VALUE";
        XPath xpath = XPathFactory.newInstance().newXPath();
        NodeList nodeSet = (NodeList) xpath.evaluate(annotationExpression, node, XPathConstants.NODESET);
        List<String> annotationValues = new ArrayList<String>();
        for (int i = 0; i < nodeSet.getLength(); i++) {
            annotationValues.add(nodeSet.item(i).getTextContent());
        }
        return annotationValues;
    }

    private List<Node> getAnnotationNodesByTierNameReference(String tierName, String reference) throws XPathExpressionException {
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

    private String getAnnotationId(Node node) throws XPathExpressionException {
        String annotationIdExpression = ".//@ANNOTATION_ID";
        XPath xpath = XPathFactory.newInstance().newXPath();
        Node annotationIdNode = (Node) xpath.evaluate(annotationIdExpression, node, XPathConstants.NODE);
        if (annotationIdNode == null) {
            return null;
        }
        return annotationIdNode.getTextContent();
    }


    private class SentenceComparator implements Comparator<Node> {

        public int compare(Node o1, Node o2) {
            Integer timeIndex1 = Integer.parseInt(o1.getFirstChild().getNextSibling().getAttributes().getNamedItem("TIME_SLOT_REF1").getNodeValue().split("ts")[1]);
            Integer timeIndex2 = Integer.parseInt(o2.getFirstChild().getNextSibling().getAttributes().getNamedItem("TIME_SLOT_REF1").getNodeValue().split("ts")[1]);


            return timeIndex1 - timeIndex2;
        }
    }
}
