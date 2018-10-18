package ru.msu.srcc.minlang;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

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
 * Created by User on 17.02.2015.
 */
public class XMLElanOldUtils extends XMLCommonUtils {


    private static final String RUS_TIER_NAME = "rus";
    private static final String FON_TIER_NAME = "fon";
    private static final String GLOSS_TIER_NAME = "gl";

    public ElanParsingResult parseElanFile(String filename, String originalName) throws ElanException {
        File file = new File(filename);
        try {
            allTiers = parseXMLFile(file);
            List<Node> originalSentences = getOriginalMessages(originalName);
            List<String> russianSentences = getRussianArray();
            List<String> fons = getFonWords();
            List<String> fonGlosses = getGlosses();
            List<WordGloss> wordGlosses = combineFonsGlosses(fons, fonGlosses);

            ElanParsingResult res = new ElanParsingResult(originalSentences, russianSentences);
            res.setWords(getWordsFromWordGlosses(wordGlosses));
            res.setGlosses(getGlossesFromWordGlosses(wordGlosses));
            return res;

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

    private List<String> getWordsFromWordGlosses(List<WordGloss> wordGlosses) {
        List<String> words = new ArrayList<>();
        for (WordGloss wordGloss : wordGlosses) {
            words.add(wordGloss.getWord());
        }
        return words;
    }

    private List<String> getGlossesFromWordGlosses(List<WordGloss> wordGlosses) {
        List<String> glosses = new ArrayList<>();
        for (WordGloss wordGloss : wordGlosses) {
            glosses.add(wordGloss.getGloss());
        }
        return glosses;
    }

    private List<WordGloss> combineFonsGlosses(List<String> fons, List<String> fonGlosses) {
        List<WordGloss> wordsGlosses = new ArrayList<>();
        StringBuffer wordBuffer = new StringBuffer();
        StringBuffer glossBuffer = new StringBuffer();
        for (int i = 0; i < fons.size(); ++i) {
            String curFon = fons.get(i);
            String curGloss = fonGlosses.get(i);
            if (isWordBorder(curFon) && i > 0) {
                String curWord = wordBuffer.toString();
                String curWordGloss = glossBuffer.toString();
                wordBuffer.delete(0, wordBuffer.length());
                glossBuffer.delete(0, glossBuffer.length());
                wordsGlosses.add(new WordGloss(curWord, curWordGloss));
            } else if (i > 0) {
                String borderElement = String.valueOf(curFon.charAt(0));
                glossBuffer.append(borderElement);
            }
            wordBuffer.append(curFon);
            glossBuffer.append(curGloss);

        }
        String curWord = wordBuffer.toString();
        String curWordGloss = glossBuffer.toString();
        wordsGlosses.add(new WordGloss(curWord, curWordGloss));
        return wordsGlosses;
    }

    private boolean isWordBorder(String curFon) {
        return !curFon.startsWith("-") && !curFon.startsWith("=");
    }

    public List<Long> getTimeArray() throws ElanException {
        List<Long> begEnds = new ArrayList<Long>();
        XPath xpath = XPathFactory.newInstance().newXPath();
        NodeList timeValuesMS = null;
        try {
            timeValuesMS = ((NodeList) xpath.evaluate("//TIME_SLOT/@TIME_VALUE", doc, XPathConstants.NODESET));
        } catch (XPathExpressionException e) {
            throw new ElanException(String.format("Error: no time values found: %s", e.getMessage()));
        }
        for (int i = 0; i < timeValuesMS.getLength() - 1; ++i) {
            begEnds.add(Long.parseLong(timeValuesMS.item(i).getTextContent()));
        }
        return begEnds;
    }


    private NodeList parseXMLFile(File file) throws IOException, SAXException, ParserConfigurationException, XPathExpressionException {
        doc = createDoc(file);
        if (doc == null) {
            throw new IllegalArgumentException(String.format("The file is incorrect: %s", file.getAbsolutePath()));
        }
        XPath xpath = XPathFactory.newInstance().newXPath();
        return ((NodeList) xpath.evaluate("//TIER", doc, XPathConstants.NODESET));
    }

    private List<String> getGlossArray() throws XPathExpressionException {
        return getAnnotationValuesByTierName(GLOSS_TIER_NAME);
    }

    private List<String> getFonArray() throws XPathExpressionException {
        return getAnnotationValuesByTierName(FON_TIER_NAME);
    }

    private List<Node> getOriginalMessages(String originalTierName) throws XPathExpressionException {
        return getAnnotationNodesByTierName(originalTierName);
    }

    private List<String> getRussianArray() throws XPathExpressionException {
        return getAnnotationValuesByTierName(RUS_TIER_NAME);
    }

    public List<String> getFonWords() throws XPathExpressionException {
        return getAnnotationValuesByTierName(FON_TIER_NAME);
    }

    public List<String> getGlosses() throws XPathExpressionException {
        return getAnnotationValuesByTierName(GLOSS_TIER_NAME);
    }


}
