package ru.msu.srcc.minlang.transliteration;

import org.w3c.dom.*;
import org.xml.sax.SAXException;
import ru.msu.srcc.minlang.EvenkiConverter;
import ru.msu.srcc.minlang.XMLElanException;
import ru.msu.srcc.minlang.XMLFormatter;
import ru.msu.srcc.minlang.utils.CommonUtils;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TransliterationHelper {
    private Set<String> tierNames;
    private Document doc;
    private NodeList allTiers;
    private String oldFileName;


    private XMLFormatter xmlFormatter = new XMLFormatter();
    private EvenkiConverter evenkiConverter = new EvenkiConverter();

    /**
     * adds a new tier with a transliteration of an old tier
     * and returns a new file with it
     *
     * @param tierToTransliterate the name of the tier to transliterate
     * @param newTierName         the name of the resulting tier
     * @param isSil               true if the format is new (SIL), false otherwise
     * @return a new EAF file
     * @throws XMLElanException
     */
    public File addTransliteration(String tierToTransliterate,
                                   String newTierName, boolean isSil) throws XMLElanException {
        xmlFormatter.setAllTiers(allTiers);
        for (int i = 0; i < allTiers.getLength(); ++i) {
            Node node = allTiers.item(i);
            NamedNodeMap attributes = node.getAttributes();
            Node nameAttrib = attributes.getNamedItem("TIER_ID");
            if (nameAttrib.getNodeValue().equals(tierToTransliterate)) {
                String currentTierType = attributes.getNamedItem("LINGUISTIC_TYPE_REF").getNodeValue();
                Element newTier = createNewTier(doc, newTierName, currentTierType);
                addNewTier(doc, newTier);
                try {
                    convert(doc, newTier, isSil);
                    return saveFile(doc);
                } catch (Exception e) {
                    throw new XMLElanException(String.format("Error occurred during transliteration: %s",
                            e.getMessage()));
                }
            }
        }
        throw new XMLElanException(String.format("No tier to transliterate found: %s", tierToTransliterate));
    }

    /**
     * creates new annotations consisting of words which are concatenated into a single sentence
     * and then transliterated
     *
     * @param doc
     * @param newTier
     * @param isSil
     * @throws XMLElanException
     */
    private void convert(Document doc, Element newTier, boolean isSil) throws XMLElanException {
        int tsId = 0;
        if (isSil) {
            convertSIL(doc, newTier);
        } else {
            convertOldFormat(doc, newTier);
        }
    }

    /**
     * creates new annotations for a SIL format document (with the Russian tier as the main tier)
     * //TODO: describe the format
     *
     * @param doc
     * @param newTier
     * @throws XMLElanException
     */
    private void convertSIL(Document doc, Element newTier) throws XMLElanException {
        try {
            int timeslotId = 0;
            List<Node> russianNodes = xmlFormatter.getSILRussianNodes();

            int sentNum = russianNodes.size();

            for (int i = 0; i < sentNum; ++i) {
                Node russianSentenceNode = russianNodes.get(i);
                Node annotation = convertSILSentence(russianSentenceNode, i, timeslotId);
                timeslotId += 2;
                newTier.appendChild(annotation);
            }
        } catch (XPathExpressionException e) {
            throw new XMLElanException(String.format("Error occurred when converting: %s", e.getMessage()));
        }
    }

    /**
     * creates an transliterating annotation for a sentence
     *
     * @param russianSentenceNode
     * @param annotationIndex
     * @param timeslotId
     * @return
     * @throws XMLElanException
     */
    private Node convertSILSentence(Node russianSentenceNode, int annotationIndex, int timeslotId)
            throws XMLElanException {
        try {
            String annotationId = xmlFormatter.getAnnotationId(russianSentenceNode);
            String punctuationMark = getPunctuationMark(russianSentenceNode);
            String sentenceConverted = convertAnnotation(annotationId, punctuationMark);
            return createTransliterationAnnotation(sentenceConverted, annotationIndex, timeslotId);
        } catch (XPathExpressionException e) {
            throw new XMLElanException(String.format("Error occurred when converting: %s", e.getMessage()));
        }
    }

    private String getPunctuationMark(Node russianSentenceNode) {
        String russianSentence = russianSentenceNode.getTextContent().trim();
        return CommonUtils.getPunctuationMark(russianSentence);
    }

    private Node createTransliterationAnnotation(String sentenceConverted, int annotationIndex, int timeslotId) {
        Element annotation = doc.createElement("ANNOTATION");
        Element alignableAnnotation = createAlignableAnnotation(sentenceConverted, annotationIndex, timeslotId);
        annotation.appendChild(alignableAnnotation);
        return annotation;
    }

    private Element createAlignableAnnotation(String sentenceConverted, int annotationIndex, int timeslotId) {
        Element alignableAnnotation = doc.createElement("ALIGNABLE_ANNOTATION");
        alignableAnnotation.setAttribute("ANNOTATION_ID", "aTranslit" + annotationIndex);
        timeslotId += 1;
        alignableAnnotation.setAttribute("TIME_SLOT_REF1", "ts" + timeslotId);
        timeslotId += 1;
        alignableAnnotation.setAttribute("TIME_SLOT_REF2", "ts" + timeslotId);
        Element annotationValue = createAnnotationValue(sentenceConverted);
        alignableAnnotation.appendChild(annotationValue);
        return alignableAnnotation;
    }

    /**
     * @param annotationId
     * @return a concatenation of all words referring to the same annotation with @annotationId
     * @throws XMLElanException
     */
    private String concatenateOriginalWordsByAnnotationId(String annotationId) throws XMLElanException {
        List<Node> allWords = null;
        try {
            allWords = xmlFormatter.getFonWordsByReference(annotationId);
        } catch (XPathExpressionException e) {
            throw new XMLElanException(String.format("Error occurred when concatenating words: %s", e.getMessage()));
        }
        StringBuffer sentenceToConvert = new StringBuffer();
        for (Node word : allWords) {
            sentenceToConvert.append(" ");
            sentenceToConvert.append(word.getTextContent().trim());
        }
        return sentenceToConvert.toString().trim();
    }

    private Element createAnnotationValue(String sentenceConverted) {
        Element annotationValue = doc.createElement("ANNOTATION_VALUE");
        annotationValue.setTextContent(sentenceConverted);
        return annotationValue;
    }

    private String convertAnnotation(String annotationId, String punctuationMark) throws XMLElanException {
        String sentenceToConvert = concatenateOriginalWordsByAnnotationId(annotationId);
        return CommonUtils.formatSentence(evenkiConverter.convert(sentenceToConvert), punctuationMark);
    }


    private void convertOldFormat(Document doc, Element newTier) throws XMLElanException {
        try {
            int tsId = 0;
            List<Node> fonNodes = xmlFormatter.getOldFonNodes();
            int sentNum = fonNodes.size();

            for (int i = 0; i < sentNum; ++i) {

                String fonValue = fonNodes.get(i).getTextContent();


                String sentenceConverted = CommonUtils.capitalize(evenkiConverter.convert(fonValue.trim())) + ".";


                Element annotation = doc.createElement("ANNOTATION");
                Element alignableAnnotation = doc.createElement("ALIGNABLE_ANNOTATION");
                alignableAnnotation.setAttribute("ANNOTATION_ID", "aTranslit" + i);

                tsId += 1;
                alignableAnnotation.setAttribute("TIME_SLOT_REF1", "ts" + tsId);
                tsId += 1;
                alignableAnnotation.setAttribute("TIME_SLOT_REF2", "ts" + tsId);

                annotation.appendChild(alignableAnnotation);
                Element annotationValue = doc.createElement("ANNOTATION_VALUE");
                annotationValue.setTextContent(sentenceConverted);
                alignableAnnotation.appendChild(annotationValue);
                newTier.appendChild(annotation);


            }
        } catch (XPathExpressionException e) {
            throw new XMLElanException(String.format("Error occurred when converting: %s", e.getMessage()));
        }
    }


    public void readFile(File file) throws XMLElanException {
        oldFileName = file.getAbsolutePath();
        createDoc(file);
        if (doc == null) {
            throw new XMLElanException(String.format("Empty document received: %s", oldFileName));
        }
        readTierNames();
    }


    private void readTierNames() throws XMLElanException {
        XPath xpath = XPathFactory.newInstance().newXPath();
        allTiers = null;
        try {
            allTiers = ((NodeList) xpath.evaluate("//TIER", this.doc, XPathConstants.NODESET));
            populateTierNames();
        } catch (XPathExpressionException xpe) {
            throw new XMLElanException(String.format("Error occurred when reading tier names: %s", xpe.getMessage()));
        }
    }

    private void createDoc(File file) throws XMLElanException {
        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        f.setValidating(false);
        doc = null;
        oldFileName = file.getAbsolutePath();
        try {
            DocumentBuilder builder = f.newDocumentBuilder();
            doc = builder.parse(file);
        } catch (SAXException | ParserConfigurationException | IOException e) {
            throw new XMLElanException(String.format("Error occurred when parsing %s: %s",
                    file.getAbsolutePath(),
                    e.getMessage()));
        }
    }

    //TODO: recombine the other tiers
    private void addNewTier(Document doc, Element newTier) {
        doc.getFirstChild().appendChild(newTier);
    }

    private Element createNewTier(Document doc, String newTierName, String newTierType) {
        Element newTier = doc.createElement("TIER");
        newTier.setAttribute("TIER_ID", newTierName);
        newTier.setAttribute("DEFAULT_LOCALE", "ru");
        newTier.setAttribute("LINGUISTIC_TYPE_REF", newTierType);
        return newTier;
    }

    private File saveFile(Document doc) throws XMLElanException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        File newFile;
        try {
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            newFile = new File(createFileNameForTransliteration());
            StreamResult result = new StreamResult(newFile);
            transformer.transform(source, result);
            return newFile;
        } catch (TransformerException e) {
            throw new XMLElanException(String.format("Error occurred when saving a new file: %s", e.getMessageAndLocation()));
        }
    }

    private String createFileNameForTransliteration() {
        return oldFileName.split("\\.eaf")[0] + "_transliterated.eaf";
    }

    private void populateTierNames() {
        if (allTiers == null) {
            return;
        }
        tierNames = new HashSet<>();
        for (int i = 0; i < this.allTiers.getLength(); i++) {
            Node node = this.allTiers.item(i);
            NamedNodeMap attributes = node.getAttributes();
            Node nameAttrib = attributes.getNamedItem("TIER_ID");
            tierNames.add(nameAttrib.getTextContent());
        }
    }


    public NodeList getAllTiers() {
        return allTiers;
    }

    public Document getDoc() {
        return doc;
    }

    public Set<String> getTierNames() {
        return tierNames;
    }
}

