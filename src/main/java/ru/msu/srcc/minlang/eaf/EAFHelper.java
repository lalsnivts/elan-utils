package ru.msu.srcc.minlang.eaf;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import ru.msu.srcc.minlang.XMLElanException;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class EAFHelper {
    private static final String ORIGINAL_TIER_NAME = "ev";
    private static final String SIL_ORIGINAL_TIER_NAME = "ev";
    private static final String SIL_FON_TIER_NAME = "fon";
    private static final String FON_TIER_NAME = "fon";
    private static final String SIL_FON_WORD_TIER_NAME = "fonWord";
    private static final String FON_WORD_TIER_NAME = "fonWord";
    private static final String SIL_RUS_TIER_NAME = "rus";
    private static final String RUS_TIER_NAME = "rus";
    private static final String SIL_GLOSS_TIER_NAME = "gl";
    private static final String GLOSS_TIER_NAME = "gl";
    private static final String COMMENT_TIER_NAME = "comment";
    private static final String FIRST_ORIGINAL_TIER_NAME = "ev1";
    private static final String FIRST_FON_TIER_NAME = "fon1";
    private static final String FIRST_FON_WORD_TIER_NAME = "fonWord1";
    private static final String FIRST_GLOSS_TIER_NAME = "gl1";
    private static final String FIRST_RUS_TIER_NAME = "rus1";
    private static final String SECOND_ORIGINAL_TIER_NAME = "ev2";
    private static final String SECOND_FON_TIER_NAME = "fon2";
    private static final String SECOND_FON_WORD_TIER_NAME = "fonWord2";
    private static final String SECOND_GLOSS_TIER_NAME = "gl2";
    private static final String SECOND_RUS_TIER_NAME = "rus2";
    private final static String TIER = "TIER";
    private final static String TIER_ID = "TIER_ID";
    private final static String PARENT_REF = "PARENT_REF";
    private final static String ref1 = "TIME_SLOT_REF1";
    private final static String ref2 = "TIME_SLOT_REF2";
    private final static String anIdSIL = "ANNOTATION_ID";
    private final static String anRefSIL = "ANNOTATION_REF";
    private final static String ANNOTATION_ID = "ANNOTATION_ID";
    private Document doc;
    private NodeList allTiers;

    public Node getTimeSlotByRef(String ref) throws XPathExpressionException {
        String timeSlotRefExpression = String.format(".//TIME_SLOT[@TIME_SLOT_ID=\"%s\"", ref);
        XPath xpath = XPathFactory.newInstance().newXPath();
        return (Node) xpath.evaluate(timeSlotRefExpression, doc, XPathConstants.NODE);

    }

    public List<String> getAnnotationValues(Node node) throws XPathExpressionException {
        String annotationExpression = ".//ANNOTATION_VALUE";
        XPath xpath = XPathFactory.newInstance().newXPath();
        NodeList nodeSet = (NodeList) xpath.evaluate(annotationExpression, node, XPathConstants.NODESET);
        List<String> annotationValues = new ArrayList<String>();
        for (int i = 0; i < nodeSet.getLength(); i++) {
            annotationValues.add(nodeSet.item(i).getTextContent());
        }
        return annotationValues;
    }

    public String getAnnotationId(Node node) throws XPathExpressionException {
        String annotationIdExpression = ".//@ANNOTATION_ID";
        XPath xpath = XPathFactory.newInstance().newXPath();
        Node annotationIdNode = (Node) xpath.evaluate(annotationIdExpression, node, XPathConstants.NODE);
        if (annotationIdNode == null) {
            return null;
        }
        return annotationIdNode.getTextContent();
    }


    public String getTimeSlotRef(Node node) throws XPathExpressionException {
        String timeSlotRefExpression = ".//@" + ref1;
        XPath xpath = XPathFactory.newInstance().newXPath();
        Node timeSlotNode = (Node) xpath.evaluate(timeSlotRefExpression, node, XPathConstants.NODE);
        if (timeSlotNode == null) {
            return null;
        }
        return timeSlotNode.getTextContent();
    }


    public List<String> getRussianArray() throws XMLElanException {
        return getAnnotationValuesByTierName(RUS_TIER_NAME);
    }


    public List<String> getSILRussianArray() throws XMLElanException {

        return getAnnotationValuesByTierName(SIL_RUS_TIER_NAME);
    }

    public List<String> getSILOriginalArray() throws XMLElanException {
        return getAnnotationValuesByTierName(SIL_ORIGINAL_TIER_NAME);
    }


    public List<String> getTwoSpeakersRussian() throws XMLElanException {
        List<String> firstAnnotations = getAnnotationValuesByTierName(FIRST_RUS_TIER_NAME);
        List<String> secondAnnotations = getAnnotationValuesByTierName(SECOND_RUS_TIER_NAME);
        firstAnnotations.addAll(secondAnnotations);
        //TODO: sort!
        return firstAnnotations;
    }


    public List<String> getFonArray() throws XMLElanException {
        return getAnnotationValuesByTierName(FON_TIER_NAME);
    }

    public List<String> getSILFonWordArray() throws XMLElanException {
        return getAnnotationValuesByTierName(FON_WORD_TIER_NAME);
    }

    public List<String> getGlossArray() throws XMLElanException {
        return getAnnotationValuesByTierName(GLOSS_TIER_NAME);
    }

    public List<Long[]> getTimeArray() throws XPathExpressionException {
        List<Long[]> begEnds = new ArrayList<Long[]>();
        XPath xpath = XPathFactory.newInstance().newXPath();
        NodeList timeValuesMS = ((NodeList) xpath.evaluate("//TIME_SLOT/@TIME_VALUE", this.doc, XPathConstants.NODESET));
        for (int i = 0; i < timeValuesMS.getLength() - 1; i += 2) {
            String beginStr = timeValuesMS.item(i).getTextContent();
            String endStr = timeValuesMS.item(i + 1).getTextContent();
            long begin = Long.parseLong(beginStr);
            long end = Long.parseLong(endStr);
            begEnds.add(new Long[]{begin, end});
        }
        return begEnds;
    }

    public List<Node> getTimeArrayNodes() throws XPathExpressionException {
        List<Node> timeArrayNodes = new ArrayList<Node>();
        XPath xpath = XPathFactory.newInstance().newXPath();
        NodeList timeValuesMS = ((NodeList) xpath.evaluate("//TIME_SLOT", this.doc, XPathConstants.NODESET));
        for (int i = 0; i < timeValuesMS.getLength(); ++i) {
            Node timeValue = timeValuesMS.item(i);
            timeArrayNodes.add(timeValue);
        }
        return timeArrayNodes;
    }

    public List<String> getAnnotationValuesByTierName(String tierName) throws XMLElanException {
        for (int i = 0; i < allTiers.getLength(); i++) {
            Node node = allTiers.item(i);
            NamedNodeMap attributes = node.getAttributes();
            Node nameAttrib = attributes.getNamedItem("TIER_ID");
            if (nameAttrib.getNodeValue().equals(tierName)) {
                try {
                    return getAnnotationValues(node);
                } catch (XPathExpressionException e) {
                    new XMLElanException("getAnnotationValuesByTierName:" + e.getMessage());
                }
            }
        }
        return null;
    }

    public List<Node> getAnnotationNodesByTierNameReference(String tierName, String reference) throws XPathExpressionException {
        for (int i = 0; i < allTiers.getLength(); i++) {
            Node node = allTiers.item(i);
            NamedNodeMap attributes = node.getAttributes();
            Node nameAttrib = attributes.getNamedItem("TIER_ID");
            if (nameAttrib.getNodeValue().equals(tierName)) {
                return getAnnotationNodesByReference(node, reference);
            }
        }
        return null;
    }

    public Node getOneAnnotationNodeByTierNameReference(String tierName, String reference) throws XPathExpressionException {
        for (int i = 0; i < allTiers.getLength(); i++) {
            Node node = allTiers.item(i);
            NamedNodeMap attributes = node.getAttributes();
            Node nameAttrib = attributes.getNamedItem("TIER_ID");
            if (nameAttrib.getNodeValue().equals(tierName)) {
                return getOneAnnotationNodeByTierNameReference(node, reference);
            }
        }
        return null;
    }

    public List<Node> getAnnotationNodesByTierName(String tierName) throws XPathExpressionException {
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

    public List<Node> getAnnotationNodesByReference(Node node, String reference) throws XPathExpressionException {
        String annotationExpression = ".//ANNOTATION/REF_ANNOTATION[@ANNOTATION_REF=\"" + reference + "\"]";
        XPath xpath = XPathFactory.newInstance().newXPath();
        NodeList nodeSet = (NodeList) xpath.evaluate(annotationExpression, node, XPathConstants.NODESET);
        List<Node> annotations = new ArrayList<Node>();
        for (int i = 0; i < nodeSet.getLength(); i++) {
            annotations.add(nodeSet.item(i));
        }
        return annotations;
    }

    public Node getOneAnnotationNodeByTierNameReference(Node node, String reference) throws XPathExpressionException {
        String annotationExpression = ".//ANNOTATION/REF_ANNOTATION[@ANNOTATION_REF=\"" + reference + "\"]";
        XPath xpath = XPathFactory.newInstance().newXPath();
        return (Node) xpath.evaluate(annotationExpression, node, XPathConstants.NODE);

    }


    public List<Node> getAnnotations(Node node) throws XPathExpressionException {
        String annotationExpression = ".//ANNOTATION";
        XPath xpath = XPathFactory.newInstance().newXPath();
        NodeList nodeSet = (NodeList) xpath.evaluate(annotationExpression, node, XPathConstants.NODESET);
        List<Node> annotations = new ArrayList<Node>();
        for (int i = 0; i < nodeSet.getLength(); i++) {
            annotations.add(nodeSet.item(i));
        }
        return annotations;
    }


    public List<Node> getFonWordsByReference(String reference) throws XPathExpressionException {
        return getAnnotationNodesByTierNameReference(SIL_FON_WORD_TIER_NAME, reference);
    }

    public List<Node> getTwoSpFonWordsByReference(String reference) throws XPathExpressionException {
        List<Node> firstWords = getAnnotationNodesByTierNameReference(FIRST_FON_WORD_TIER_NAME, reference);
        List<Node> secondWords = getAnnotationNodesByTierNameReference(SECOND_FON_WORD_TIER_NAME, reference);
        firstWords.addAll(secondWords);
        return firstWords;
    }


    public String getTwoSpTranslationByReference(String reference) throws XPathExpressionException {
        System.out.println(reference);
        Node translation1 = getOneAnnotationNodeByTierNameReference(FIRST_RUS_TIER_NAME, reference);
        if (translation1 == null) {
            return getOneAnnotationNodeByTierNameReference(SECOND_RUS_TIER_NAME, reference).getTextContent();
        }
        return translation1.getTextContent();
    }

    public List<Node> getMorphemesByReference(String reference) throws XPathExpressionException {
        return getAnnotationNodesByTierNameReference(SIL_FON_TIER_NAME, reference);
    }

    public List<Node> getCommentsByReference(String reference) throws XPathExpressionException {
        return getAnnotationNodesByTierNameReference(COMMENT_TIER_NAME, reference);
    }

    public List<Node> getOldFonNodes() throws XPathExpressionException {
        return getAnnotationNodesByTierName(FON_TIER_NAME);
    }

    public List<Node> getTwoSpMorphemesByReference(String reference) throws XPathExpressionException {
        List<Node> firstMorphemes = getAnnotationNodesByTierNameReference(FIRST_FON_TIER_NAME, reference);
        List<Node> secondMorphemes = getAnnotationNodesByTierNameReference(SECOND_FON_TIER_NAME, reference);
        firstMorphemes.addAll(secondMorphemes);
        //TODO: sort
        return firstMorphemes;
    }

    public Node getGlossByReference(String reference) throws XPathExpressionException {
        return getOneAnnotationNodeByTierNameReference(SIL_GLOSS_TIER_NAME, reference);
    }

    public Node getTwoSpGlossByReference(String reference) throws XPathExpressionException {
        Node nodeFirst = getOneAnnotationNodeByTierNameReference(FIRST_GLOSS_TIER_NAME, reference);
        if (nodeFirst == null) {
            return getOneAnnotationNodeByTierNameReference(SECOND_GLOSS_TIER_NAME, reference);
        }
        return nodeFirst;
    }


    public List<Node> getSILOriginalMessages() throws XPathExpressionException {
        return getAnnotationNodesByTierName(SIL_ORIGINAL_TIER_NAME);
    }

    public List<Node> getSILRussianNodes() throws XPathExpressionException {
        return getAnnotationNodesByTierName(SIL_RUS_TIER_NAME);
    }

    public List<Node> getTwoSpeakersOriginalMessages() throws XPathExpressionException {
        List<Node> firstAnnotations = getAnnotationNodesByTierName(FIRST_ORIGINAL_TIER_NAME);
        List<Node> secondAnnotations = getAnnotationNodesByTierName(SECOND_ORIGINAL_TIER_NAME);
        firstAnnotations.addAll(secondAnnotations);


        AnnotationSorter annotationSorter = new AnnotationSorter();
        firstAnnotations.sort(annotationSorter);
        return firstAnnotations;

    }

    public List<Node> getTwoSpeakersFon() throws XPathExpressionException {
        List<Node> firstAnnotations = getAnnotationNodesByTierName(FIRST_FON_TIER_NAME);
        List<Node> secondAnnotations = getAnnotationNodesByTierName(SECOND_FON_TIER_NAME);
        firstAnnotations.addAll(secondAnnotations);


        AnnotationSorter annotationSorter = new AnnotationSorter();
        firstAnnotations.sort(annotationSorter);
        return firstAnnotations;

    }


    public List<Node> getTwoSpeakersGl() throws XPathExpressionException {
        List<Node> firstAnnotations = getAnnotationNodesByTierName(FIRST_GLOSS_TIER_NAME);
        List<Node> secondAnnotations = getAnnotationNodesByTierName(SECOND_GLOSS_TIER_NAME);
        firstAnnotations.addAll(secondAnnotations);


        AnnotationSorter annotationSorter = new AnnotationSorter();
        firstAnnotations.sort(annotationSorter);
        return firstAnnotations;

    }


    public List<Node> getTwoSpeakersRus() throws XMLElanException {
        List<Node> firstAnnotations;
        try {
            firstAnnotations = getAnnotationNodesByTierName(FIRST_RUS_TIER_NAME);
        } catch (XPathExpressionException e) {
            throw new XMLElanException(String.format("Error occurred when getting annotiations: %s",
                    e.getMessage()));
        }
        if (firstAnnotations == null) {
            throw new XMLElanException("Empty list of annotations received");
        }
        List<Node> secondAnnotations;
        try {
            secondAnnotations = getAnnotationNodesByTierName(SECOND_RUS_TIER_NAME);
        } catch (XPathExpressionException e) {
            throw new XMLElanException(String.format("Error occurred when getting annotiations: %s",
                    e.getMessage()));
        }
        firstAnnotations.addAll(secondAnnotations);


        AnnotationSorter annotationSorter = new AnnotationSorter();
        firstAnnotations.sort(annotationSorter);
        return firstAnnotations;

    }


    public Document getDoc() {
        return doc;
    }

    public void setDoc(Document doc) {
        this.doc = doc;
    }

    public void setAllTiers(NodeList allTiers) {
        this.allTiers = allTiers;
    }

    public class AnnotationSorter implements Comparator<Node> {
        @Override

        public int compare(Node o1, Node o2) {
            try {
                Node node1TimeSlotRef = getTimeSlotByRef(getTimeSlotRef(o1));
                Node node2TimeSlotRef = getTimeSlotByRef(getTimeSlotRef(o2));
                XPath xpath = XPathFactory.newInstance().newXPath();

                Long timeValue1 = Long.parseLong(((Node) xpath.evaluate("@TIME_VALUE", node1TimeSlotRef, XPathConstants.NODE)).getTextContent());
                Long timeValue2 = Long.parseLong(((Node) xpath.evaluate("@TIME_VALUE", node2TimeSlotRef, XPathConstants.NODE)).getTextContent());

                return (int) (timeValue1 - timeValue2);
            } catch (XPathExpressionException e) {
                e.printStackTrace();
                return 0;
            }
        }
    }
}
