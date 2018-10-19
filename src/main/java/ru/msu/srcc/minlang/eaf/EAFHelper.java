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
import java.util.*;

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
    private final static String ANNOTATIONS_BY_TIER_REF_XPATH = "/ANNOTATION_DOCUMENT/TIER[@TIER_ID=\"%s\"]/ANNOTATION/" +
            "REF_ANNOTATION[@ANNOTATION_REF=\"%s\"]";

    private final static String ANNOTATIONS_BY_REF_XPATH = "./ANNOTATION/" +
            "REF_ANNOTATION[@ANNOTATION_REF=\"%s\"]";

    private final static String ANNOTATIONS_BY_TIER_XPATH = "/ANNOTATION_DOCUMENT/TIER[@TIER_ID=\"%s\"]/ANNOTATION";
    private static final String CHILD_ANNOTATIONS_XPATH = "./ANNOTATION";
    private static final String TIME_VALUE_XPATH = "/ANNOTATION_DOCUMENT/TIME_ORDER/TIME_SLOT/@TIME_VALUE";



    private Document doc;
    private NodeList allTiers;
    private Map<String, Node> allTiersMap = new HashMap<>();


    public Node getTimeSlotByRef(String ref) throws XPathExpressionException {
        String timeSlotRefExpression = String.format("/ANNOTATION_DOCUMENT/TIME_SLOT[@TIME_SLOT_ID=\"%s\"", ref);
        XPath xpath = XPathFactory.newInstance().newXPath();
        return (Node) xpath.evaluate(timeSlotRefExpression, doc, XPathConstants.NODE);

    }

    public List<String> getAnnotationValues(Node node) throws XPathExpressionException {
        String annotationExpression = ".//ANNOTATION_VALUE";
        XPath xpath = XPathFactory.newInstance().newXPath();
        NodeList nodeSet = (NodeList) xpath.evaluate(annotationExpression, node, XPathConstants.NODESET);
        List<String> annotationValues = new ArrayList<>();
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


    public List<String> getFonArray() throws XMLElanException {
        return getAnnotationValuesByTierName(FON_TIER_NAME);
    }


    public List<String> getGlossArray() throws XMLElanException {
        return getAnnotationValuesByTierName(GLOSS_TIER_NAME);
    }

    public List<Long[]> getTimeArray() throws XPathExpressionException {
        List<Long[]> begEnds = new ArrayList<>();
        XPath xpath = XPathFactory.newInstance().newXPath();
        NodeList timeValuesMS = ((NodeList) xpath.evaluate(TIME_VALUE_XPATH, doc, XPathConstants.NODESET));
        for (int i = 0; i < timeValuesMS.getLength() - 1; i += 2) {
            String beginStr = timeValuesMS.item(i).getTextContent();
            String endStr = timeValuesMS.item(i + 1).getTextContent();
            long begin = Long.parseLong(beginStr);
            long end = Long.parseLong(endStr);
            begEnds.add(new Long[]{begin, end});
        }
        return begEnds;
    }

    public List<String> getAnnotationValuesByTierName(String tierName) throws XMLElanException {
        Node tier = allTiersMap.get(tierName);
        if (tier == null) {
            throw new XMLElanException(String.format("No such tier: %s", tierName));
        }
        try {
            return getAnnotationValues(tier);
        } catch (XPathExpressionException e) {
            throw new XMLElanException(String.format("Error occurred whrn getting annotation values: %s",
                    e.getMessage()));
        }
    }

    public List<Node> getAnnotationNodesByTierNameReference(Node tier, String reference) throws XPathExpressionException {
        String annotationExpression = String.format(ANNOTATIONS_BY_REF_XPATH, reference);
        XPath xpath = XPathFactory.newInstance().newXPath();
        NodeList nodeSet = (NodeList) xpath.evaluate(annotationExpression, tier, XPathConstants.NODESET);
        List<Node> annotations = new ArrayList<Node>();
        for (int i = 0; i < nodeSet.getLength(); i++) {
            annotations.add(nodeSet.item(i));
        }
        return annotations;
    }

    public List<Node> getAnnotationNodesByTierName(String tierName) throws XPathExpressionException {
        String annotationExpression = String.format(ANNOTATIONS_BY_TIER_XPATH, tierName);
        XPath xpath = XPathFactory.newInstance().newXPath();
        NodeList nodeSet = (NodeList) xpath.evaluate(annotationExpression, doc, XPathConstants.NODESET);
        List<Node> annotations = new ArrayList<Node>();
        for (int i = 0; i < nodeSet.getLength(); i++) {
            annotations.add(nodeSet.item(i));
        }
        return annotations;
    }


    public List<Node> getAnnotationNodesByTier(Node tier) throws XPathExpressionException {
        XPath xpath = XPathFactory.newInstance().newXPath();
        NodeList nodeSet = (NodeList) xpath.evaluate(CHILD_ANNOTATIONS_XPATH, tier, XPathConstants.NODESET);
        List<Node> annotations = new ArrayList<Node>();
        for (int i = 0; i < nodeSet.getLength(); i++) {
            annotations.add(nodeSet.item(i));
        }
        return annotations;
    }


    public List<Node> getAnnotationNodesByReference(String tierId, String reference) throws XPathExpressionException {
        String annotationExpression = String.format(ANNOTATIONS_BY_TIER_REF_XPATH, tierId, reference);
        XPath xpath = XPathFactory.newInstance().newXPath();
        NodeList nodeSet = (NodeList) xpath.evaluate(annotationExpression, doc, XPathConstants.NODESET);
        List<Node> annotations = new ArrayList<Node>();
        for (int i = 0; i < nodeSet.getLength(); i++) {
            annotations.add(nodeSet.item(i));
        }
        return annotations;
    }

    public Node getOneAnnotationNodeByTierNameReference(String tierId, String reference) throws XPathExpressionException {
        String annotationExpression = String.format(ANNOTATIONS_BY_TIER_REF_XPATH, tierId, reference);
        XPath xpath = XPathFactory.newInstance().newXPath();
        return (Node) xpath.evaluate(annotationExpression, doc, XPathConstants.NODE);
    }

    public Node getOneAnnotationNodeByReferenceFromTier(Node tier, String reference) throws XPathExpressionException {
        String annotationExpression = String.format(ANNOTATIONS_BY_REF_XPATH, reference);
        XPath xpath = XPathFactory.newInstance().newXPath();
        return (Node) xpath.evaluate(annotationExpression, tier, XPathConstants.NODE);
    }



    public List<Node> getFonWordsByReference(String reference) throws XPathExpressionException {
        Node tierFonWords = allTiersMap.get(SIL_FON_WORD_TIER_NAME);
        return getAnnotationNodesByTierNameReference(tierFonWords, reference);
    }

    public List<Node> getTwoSpFonWordsByReference(String reference) throws XPathExpressionException {
        Node tierFirstFonWord = allTiersMap.get(FIRST_FON_WORD_TIER_NAME);
        Node tierSecondFonWord = allTiersMap.get(SECOND_FON_WORD_TIER_NAME);

        List<Node> firstWords = getAnnotationNodesByTierNameReference(tierFirstFonWord, reference);
        List<Node> secondWords = getAnnotationNodesByTierNameReference(tierSecondFonWord, reference);
        firstWords.addAll(secondWords);
        return firstWords;
    }


    public String getTwoSpTranslationByReference(String reference) throws XPathExpressionException {
        Node translation1 = getOneAnnotationNodeByTierNameReference(FIRST_RUS_TIER_NAME, reference);
        if (translation1 == null) {
            return getOneAnnotationNodeByTierNameReference(SECOND_RUS_TIER_NAME, reference).getTextContent();
        }
        return translation1.getTextContent();
    }

    public List<Node> getMorphemesByReference(String reference) throws XPathExpressionException {
        Node tierSILMorphemes = allTiersMap.get(SIL_FON_TIER_NAME);
        return getAnnotationNodesByTierNameReference(tierSILMorphemes, reference);
    }

    public List<Node> getCommentsByReference(String reference) throws XPathExpressionException {
        Node tierComments = allTiersMap.get(COMMENT_TIER_NAME);
        return getAnnotationNodesByTierNameReference(tierComments, reference);
    }

    public List<Node> getOldFonNodes() throws XPathExpressionException {
        return getAnnotationNodesByTierName(FON_TIER_NAME);
    }

    public List<Node> getTwoSpMorphemesByReference(String reference) throws XPathExpressionException {
        Node tierFirstFon = allTiersMap.get(FIRST_FON_TIER_NAME);
        Node tierSecondFon = allTiersMap.get(SECOND_FON_TIER_NAME);

        List<Node> firstMorphemes = getAnnotationNodesByTierNameReference(tierFirstFon, reference);
        List<Node> secondMorphemes = getAnnotationNodesByTierNameReference(tierSecondFon, reference);
        firstMorphemes.addAll(secondMorphemes);
        //TODO: sort
        return firstMorphemes;
    }

    public Node getGlossByReference(String reference) throws XPathExpressionException {
        Node tierSILGloss = allTiersMap.get(SIL_GLOSS_TIER_NAME);
        return getOneAnnotationNodeByReferenceFromTier(tierSILGloss, reference);
    }

    public Node getTwoSpGlossByReference(String reference) throws XPathExpressionException {
        Node nodeFirst = getOneAnnotationNodeByTierNameReference(FIRST_GLOSS_TIER_NAME, reference);
        if (nodeFirst == null) {
            return getOneAnnotationNodeByTierNameReference(SECOND_GLOSS_TIER_NAME, reference);
        }
        return nodeFirst;
    }


    public List<Node> getSILOriginalMessages() throws XPathExpressionException {
        Node tierOriginal = allTiersMap.get(SIL_ORIGINAL_TIER_NAME);
        return getAnnotationNodesByTier(tierOriginal);
    }

    public List<Node> getSILRussianNodes() throws XPathExpressionException {
        Node tierRus = allTiersMap.get(SIL_RUS_TIER_NAME);
        return getAnnotationNodesByTier(tierRus);
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
        allTiersMap.clear();
        for (int i = 0; i < allTiers.getLength(); i++) {
            Node tier = allTiers.item(i);
            NamedNodeMap attributes = tier.getAttributes();
            String tierId = attributes.getNamedItem("TIER_ID").getNodeValue();
            allTiersMap.put(tierId, tier);
        }
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
