package ru.msu.srcc.minlang.formatting;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import ru.msu.srcc.minlang.XMLElanException;
import ru.msu.srcc.minlang.eaf.EAFHelper;
import ru.msu.srcc.minlang.utils.CommonUtils;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class XMLFormatter {
    private static final String HTML_HEADER = "\n" +
            "<!doctype html>\n" +
            "<html>\n" +
            "<head>\n" +
            "<meta charset=\"utf-8\">\n" +
            "<title>Untitled Document</title>\n" +
            "\n" +
            "<style>\n" +
            "table { width:100%; table-layout: auto;  border: solid 0 #FFF; border-collapse:collapse;}\n" +
            "td { padding:5px 10px; table-layout:fixed; white-space: nowrap;\n" +
            "word-wrap: normal; border: solid 0 #FFF; border-collapse:collapse; }\n" +
            ".gl-el .data-container { overflow-y: auto; clear:both; padding-top:7px;}\n" +
            ".gl-rate  {}\n" +
            "\n" +
            "/* style 1 */\n" +
            "\n" +
            "body { background: #dfdfdf;  font-family:'PT Sans', sans-serif; }\n" +
            "h1 { text-align:center; font-size:100px;}\n" +
            ".gl-block { width:720px; margin:0 auto; margin-top:30px; padding:20px; border-radius:10px; background:#FFF; }\n" +
            ".gl-el { margin-bottom:30px; }\n" +
            ".gl-nm { float:left; color:#F00;}\n" +
            ".gl-rate { float:right;}\n" +
            ".gl-data { clear:both; padding-top:5px;}\n" +
            ".gl-data-tx-1 { background:#099; color:#fff; padding:5px; }\n" +
            ".gl-data-tx-2 { color:#099; background:#fff; padding:5px; }\n" +
            ".gl-data-tx-3 { background: #FF9; padding:5px; }\n" +
            ".translate { color:#000; font-style:italic; background:#fff; padding:5px 10px; }\n" +
            ".comment { color:#DCDCDC; font-style:italic; background:#fff; padding:5px 10px; }\n" +
            "\n" +
            "/* style 2 \n" +
            "\n" +
            "body { background: #fff;  font-family:'PT Sans', sans-serif; font-size:18px; }\n" +
            "h1 { text-align:center; font-size:100px;}\n" +
            ".gl-block { width:720px; margin:0 auto; margin-top:30px; padding:20px; border-radius:10px; background:#eeeeee; }\n" +
            ".gl-el { margin-bottom:10px;}\n" +
            ".gl-nm { float:left; color:#676767; font-weight:bold;}\n" +
            ".gl-rate { float:right; color:#676767; }\n" +
            ".gl-data { clear:both; padding-top:5px; padding-bottom:20px;}\n" +
            ".gl-data-tx-1 { background:#fa8281; color:#fff; padding:5px; }\n" +
            ".gl-data-tx-2 { color:#fff; background:#676767; padding:5px; }\n" +
            ".gl-data-tx-3 { color:#676767; background: #cccccc; padding:5px; }\n" +
            "\n" +
            "*/\n" +
            "\n" +
            "\n" +
            "\n" +
            "</style>\n" +
            "\n" +
            "\n" +
            "</head>\n" +
            "\n" +
            "<body>\n" +
            "\n" +
            "<h1>ГЛОССЫ 2.0</h1>\n";
    private static final String HTML_END = "</body>\n" +
            "</html>\n";
    private static final String SIL_ORIGINAL_TIER_NAME = "ev";
    private static final String SIL_RUS_TIER_NAME = "rus";
    private String oldFileName;
    private EAFHelper eafHelper = new EAFHelper();

    public void setOldFileName(String oldFileName) {
        this.oldFileName = oldFileName;
    }

    public List<String> saveFiles(boolean isSil, boolean isTwoSpeakers, boolean isArchive) throws XMLElanException {
        String xmlFilePath = saveDocumentToFile(isSil, isTwoSpeakers, isArchive);
        if (isSil && !isTwoSpeakers) {
            String rusFilePath = saveRusDocumentToFile();
            String originalFilePath = saveOriginalDocumentToFile();
            return Arrays.asList(xmlFilePath, rusFilePath, originalFilePath);
        }
        return Collections.singletonList(xmlFilePath);
    }


    public void setAllTiers(NodeList allTiers) {
        this.eafHelper.setAllTiers(allTiers);
    }

    public void setDoc(Document doc) {
        eafHelper.setDoc(doc);
    }

    public List<String> checkGlosses(boolean isSil, boolean isTwoSpeakers, boolean isArchive) throws XMLElanException {
        if (isSil) {
            throw new NotImplementedException();
        }
        return checkGlossesSimple();
    }


    private List<String> checkGlossesSimple() throws XMLElanException {
        List<String> errors = new ArrayList<>();
        List<String> glSentences = eafHelper.getGlossArray();
        List<String> fonSentences = eafHelper.getFonArray();


        int fonNum = fonSentences.size();


        if (fonNum != glSentences.size()) {
            throw new IllegalArgumentException(
                    String.format("Bad file: number of morpheme annotations (%s) " +
                                    "!= number of gloss annotations (%s) ",
                            fonNum, glSentences.size()));
        }
        for (int i = 0; i < fonNum; ++i) {
            String allWords[] = CommonUtils.tokenizeSentence(fonSentences.get(i));
            String allGlosses[] = CommonUtils.tokenizeSentence(glSentences.get(i));

            int wordNum = allWords.length;
            if (wordNum != allGlosses.length) {
                errors.add(String.format("number of words (%s) != number of glosses (%s) " +
                                "for sentence # %s (%s : %s)",
                        wordNum, allGlosses.length, i, fonSentences.get(i), glSentences.get(i)));
                continue;
            }


            for (int j = 0; j < wordNum; ++j) {
                String curWord = allWords[j];
                String[] morphemes = curWord.split("[-=]");
                String[] morphGlosses = allGlosses[j].split("[-=]");
                if (morphemes.length != morphGlosses.length) {
                    errors.add(String.format("number of morphemes (%s) != number of glosses (%s) " +
                                    "for sentence # %s (%s) for word %s : %s)",
                            morphemes.length, morphGlosses.length, i, fonSentences.get(i), curWord, allGlosses[j]));
                }

            }

        }
        return errors;
    }


    private String saveDocumentToFile(boolean isSil, boolean isTwoSpeakers, boolean isArchive) throws XMLElanException {
        String html = createDocumentHTML(isSil, isTwoSpeakers, isArchive);
        List<String> lines = Collections.singletonList(html);
        String newName = oldFileName + "_glosses.html";
        Path file = Paths.get(newName);
        try {
            Files.write(file, lines, Charset.forName("UTF-8"));
        } catch (IOException e) {
            throw new XMLElanException(e.getMessage());
        }
        return newName;
    }

    private String saveRusDocumentToFile() throws XMLElanException {
        String newName = oldFileName + "_" + SIL_RUS_TIER_NAME + ".txt";
        String text = generateLines(SIL_RUS_TIER_NAME);
        Path file = Paths.get(newName);
        try {
            Files.write(file, Collections.singletonList(text), Charset.forName("UTF-8"));
        } catch (IOException e) {
            throw new XMLElanException(e.getMessage());
        }
        return newName;
    }

    private String saveOriginalDocumentToFile() throws XMLElanException {
        String newName = oldFileName + "_" + SIL_ORIGINAL_TIER_NAME + ".txt";
        String text = generateLines(SIL_ORIGINAL_TIER_NAME);
        Path file = Paths.get(newName);
        try {
            Files.write(file, Collections.singletonList(text), Charset.forName("UTF-8"));
        } catch (IOException e) {
            throw new XMLElanException(e.getMessage());
        }
        return newName;
    }

    private String generateLines(String tierName) throws XMLElanException {
        int counter = 1;
        List<String> lines = eafHelper.getAnnotationValuesByTierName(tierName);
        StringBuilder linesConcatenated = new StringBuilder();
        for (String line : lines) {
            if (counter > 1) {
                linesConcatenated.append(" ");
            }
            linesConcatenated.append("(");
            linesConcatenated.append(counter);
            linesConcatenated.append(") ");
            linesConcatenated.append(line);
            counter += 1;
        }
        return linesConcatenated.toString();
    }

    private String createDocumentHTML(boolean isSil, boolean isTwoSpeakers, boolean isArchive)
            throws XMLElanException {
        String textCreated = createText(isSil, isTwoSpeakers, isArchive);
        return HTML_HEADER + textCreated + HTML_END;
    }

    private String createText(boolean isSil, boolean isTwoSpeakers, boolean isArchive) throws XMLElanException {
        if (isSil) {
            if (!isTwoSpeakers) {
                return createTextSIL(isArchive);
            }
            return createTextSILTwoSpeakers();
        }
        if (!isTwoSpeakers) {
            return createTextSimple();
        }
        return createTextSimpleTwoSpeakers();
    }


    private String createTextSIL(boolean isArchive) throws XMLElanException {
        List<Node> originalMessages;
        try {
            originalMessages = eafHelper.getSILOriginalMessages();
        } catch (XPathExpressionException e) {
            throw new XMLElanException("Error getting original array:" + e.getMessage());
        }
        List<String> translations;
        try {
            translations = eafHelper.getSILRussianArray();
        } catch (XMLElanException e) {
            throw new XMLElanException("Error getting Russian array:" + e.getMessage());
        }
        List<Long[]> timeMsArray = null;
        if (!isArchive) {
            try {
                timeMsArray = eafHelper.getTimeArray();
            } catch (XPathExpressionException e) {
                throw new XMLElanException("Error getting time array:" + e.getMessage());
            }
        }

        int sentNum = translations.size();
        if (sentNum != originalMessages.size()) {
            throw new XMLElanException(String.format("Bad file: different number of sentences" +
                            " in the original tier (%s) and the Russian tier (%s)",
                    originalMessages.size(), sentNum));
        }

        return concatenateTextSIL(sentNum, isArchive, timeMsArray,
                originalMessages, translations);
    }

    private String concatenateTextSIL(int sentNum, boolean isArchive, List<Long[]> timeMsArray,
                                      List<Node> originalMessages,
                                      List<String> translations) throws XMLElanException {
        LocalDateTime start = LocalDateTime.now();

        StringBuilder result = new StringBuilder("<div class=\"gl-block\">\n");
        for (int i = 0; i < sentNum; ++i) {
            appendTimeReferences(result, timeMsArray, isArchive, i);
            appendSentenceTable(result, originalMessages, translations, i);



            result.append("<div class=\"translate\">");
            result.append(translations.get(i));
            result.append("</div>\r\n");


            List<Node> comments;
            try {
                comments = eafHelper.getCommentsByReference(eafHelper.getAnnotationId(originalMessages.get(i)));
            } catch (XPathExpressionException e) {
                throw new XMLElanException("Error getting comments: " + e.getMessage());
            }
            if (comments != null && !comments.isEmpty()) {
                result.append("<div class=\"comment\">");
                result.append(comments.get(0).getTextContent());
                result.append("</div>\r\n");
            }

            result.append("</div>\r\n");

        }

        result.append("</div>\r\n");

        LocalDateTime end = LocalDateTime.now();
        long diff = ChronoUnit.MILLIS.between(start, end);
        System.out.println(String.format("concatenateTextSIL: %s milliseconds", diff));
        return result.toString();
    }


    private void appendTimeReferences(StringBuilder result, List<Long[]> timeMsArray, boolean isArchive,
                                      int currentSentenceIndex){
        String beg;
        String end;
        String time = "";
        String begMinSec;
        String endMinSec;
        String timeSec = "";
        if (!isArchive) {
            Long[] begEnd = timeMsArray.get(currentSentenceIndex);

            beg = String.valueOf(begEnd[0]);
            end = String.valueOf(begEnd[1]);
            begMinSec = CommonUtils.formatMinSec(begEnd[0]);
            endMinSec = CommonUtils.formatMinSec(begEnd[1]);

            time = beg + "-" + end;
            timeSec = begMinSec + " &mdash; " + endMinSec;
        }
        result.append("<div class=\"gl-el\">\r\n");
        result.append("    \t<div class=\"gl-nm\">");
        result.append(String.valueOf(currentSentenceIndex + 1));
        result.append("</div>\r\n");
        result.append("        <div class=\"gl-rate\" title=\"");
        result.append(time);
        result.append("\">");
        result.append(timeSec);
        result.append("</div>\r\n");
    }

    private void appendSentenceTable(StringBuilder result,
                                 List<Node> originalSentences,
                                 List<String> translations,
                                 int sentenceIndex) throws XMLElanException {
        result.append("<div class=\"data-container\">\r\n");
        result.append("            <table class=\"gl-data\">\r\n");
        List<Node> allWords;
        Node currentSentence = originalSentences.get(sentenceIndex);
        try {
            allWords = eafHelper.getFonWordsByReference(eafHelper.getAnnotationId(currentSentence));
        } catch (XPathExpressionException e) {
            throw new XMLElanException("Error getting annotation id:" + e.getMessage());
        }

        StringBuilder wordString = new StringBuilder("<tr class=\"gl-data-tx-1\">\r\n");
        StringBuilder morphString = new StringBuilder("<tr class=\"gl-data-tx-2\">\r\n");
        StringBuilder morphGlossString = new StringBuilder("<tr class=\"gl-data-tx-3\">\r\n");

        boolean prevTableStarted = false;
        for (Node curWord : allWords) {
            prevTableStarted = appendWordTable(prevTableStarted, curWord, translations, sentenceIndex,
                    wordString, morphString, morphGlossString);
        }
        wordString.append("</tr>");
        morphString.append("</tr>\r\n");
        morphGlossString.append("</tr>\r\n");

        result.append(wordString);
        result.append(morphString);
        result.append(morphGlossString);


        result.append("</table>\r\n");
        result.append("</div>\r\n");
    }

    private boolean appendWordTable(boolean prevTableStarted,
                                 Node curWord,
                                 List<String> translations, int sentenceIndex,
                                 StringBuilder wordString,
                                 StringBuilder morphString,
                                 StringBuilder morphGlossString) throws XMLElanException {
        if (curWord.getTextContent().trim().equals(".")) {
            return true;
        }

        List<Node> morphemes;
        try {
            morphemes = eafHelper.getMorphemesByReference(eafHelper.getAnnotationId(curWord));
        } catch (XPathExpressionException e) {
            throw new XMLElanException("Error getting annotation id:" + e.getMessage());
        }


        StringBuilder curWordMorphemesConcatenated = new StringBuilder();
        StringBuilder curWordGlossesConcatenated = new StringBuilder();
        int curIndex = 0;

        for (Node morpheme : morphemes) {
            curIndex += 1;
            boolean isFirstMorpheme = (curIndex == 1);
            boolean isLastMorpheme =  (curIndex == morphemes.size() && curIndex > 1);
            prevTableStarted = appendMorphemeTable(morpheme, translations, sentenceIndex,
                    curWordMorphemesConcatenated,
                    curWordGlossesConcatenated,
                    prevTableStarted,
                    isLastMorpheme,
                    isFirstMorpheme
                    );
        }

        String curWordGlosses = CommonUtils.stripTechnicalSymbols(curWordGlossesConcatenated.toString());
        String curWordMorphemes = CommonUtils.stripTechnicalSymbols(curWordMorphemesConcatenated.toString());

        curWordMorphemes = CommonUtils.makeReplacementsForGlosses(curWordMorphemes);
        curWordGlosses = CommonUtils.makeReplacementsForGlosses(curWordGlosses);

        morphString.append("<td>");
        morphString.append(curWordMorphemes);
        morphString.append("</td>");

        wordString.append("<td>");
        wordString.append(curWord.getTextContent().trim());
        wordString.append("</td>");


        morphGlossString.append("<td>");
        morphGlossString.append(curWordGlosses);
        morphGlossString.append("</td>");


        return prevTableStarted;
    }

    private boolean appendMorphemeTable(Node morpheme,
                                        List<String> translations,
                                        int sentenceIndex,
                                        StringBuilder curWordMorphemesConcatenated,
                                        StringBuilder curWordGlossesConcatenated,
                                        boolean prevTableStarted,
                                        boolean isLastMorpheme,
                                        boolean isFirstMorpheme) throws XMLElanException {
        Node gloss;
        boolean isTableNecessary = true;
        try {
            gloss = eafHelper.getGlossByReference(eafHelper.getAnnotationId(morpheme));
        } catch (XPathExpressionException e) {
            throw new XMLElanException("Error getting annotation id:" + e.getMessage());
        }
        if (gloss == null) {
            throw new XMLElanException(String.format("Bad file: no gloss for morpheme %s " +
                            "in sentence %s (# %s)",
                    morpheme.getTextContent(),
                    translations.get(sentenceIndex),
                    sentenceIndex));
        }

        String curDelimiter = " ";
        if (!isFirstMorpheme || morpheme.getTextContent().contains("-")) {
            curDelimiter = "-";
            isTableNecessary = false;
        } else if (morpheme.getTextContent().contains("=")) {
            curDelimiter = "=";
            isTableNecessary = false;
        }


        if (isTableNecessary) {
            curWordGlossesConcatenated.append(gloss.getTextContent().trim());
            curWordMorphemesConcatenated.append(morpheme.getTextContent().trim());

            if (prevTableStarted && isLastMorpheme) {
                curWordGlossesConcatenated.append("</td></table>");
                curWordMorphemesConcatenated.append("</td></table>");
            }
        } else {
            curWordGlossesConcatenated.append(curDelimiter);
            curWordGlossesConcatenated.append(gloss.getTextContent().trim());

            curWordMorphemesConcatenated.append(curDelimiter);
            curWordMorphemesConcatenated.append(morpheme.getTextContent().trim());
        }
        return isTableNecessary;
    }

    private String createTextSILTwoSpeakers() throws XMLElanException {

        String result = "<div class=\"gl-block\">\n";
        List<Node> originalMessages;
        try {
            originalMessages = eafHelper.getTwoSpeakersOriginalMessages();
        } catch (XPathExpressionException e) {
            throw new XMLElanException("Error getting original array:" + e.getMessage());
        }


        List<Long[]> timeMsArray;
        try {
            timeMsArray = eafHelper.getTimeArray();
        } catch (XPathExpressionException e) {
            throw new XMLElanException("Error getting time array:" + e.getMessage());
        }
        int sentNum = originalMessages.size();


        for (int i = 0; i < sentNum; ++i) {

            Long[] begEnd = timeMsArray.get(i);

            long beg = begEnd[0];
            long end = begEnd[1];


            String annotationId;
            try {
                annotationId = eafHelper.getAnnotationId(originalMessages.get(i));
            } catch (XPathExpressionException e) {
                throw new XMLElanException("Error getting annotation id:" + e.getMessage());
            }

            List<Node> allWords;
            try {
                allWords = eafHelper.getTwoSpFonWordsByReference(annotationId);
            } catch (XPathExpressionException e) {
                throw new XMLElanException("Error getting fonWords:" + e.getMessage());
            }

            String translation = null;
            try {
                translation = eafHelper.getTwoSpTranslationByReference(annotationId);
            } catch (XPathExpressionException e) {
                throw new XMLElanException("Error getting original array:" + e.getMessage());
            }
            if (translation == null) {
                throw new XMLElanException("Bad file: null translation for sentence " + originalMessages.get(i).getTextContent());
            }


            result += "<div class=\"gl-el\">\r\n" +
                    "    \t<div class=\"gl-nm\">" + (i + 1) + "</div>\r\n" +
                    "        <div class=\"gl-rate\" title=\"" + beg + "-" + end + "\">" +
                    CommonUtils.formatMinSec(beg) + " &mdash; " + CommonUtils.formatMinSec(end) + "</div>\r\n" +
                    "<div class=\"data-container\">\r\n" +
                    "            <table class=\"gl-data\">\r\n";


            String wordString = "<tr class=\"gl-data-tx-1\">\r\n";
            String morphString = "<tr class=\"gl-data-tx-2\">\r\n";
            String morphGlossString = "<tr class=\"gl-data-tx-3\">\r\n";


            for (Node curWord : allWords) {
                List<Node> morphemes;
                try {
                    morphemes = eafHelper.getTwoSpMorphemesByReference(eafHelper.getAnnotationId(curWord));
                } catch (XPathExpressionException e) {
                    throw new XMLElanException("Error getting annotation id:" + e.getMessage());
                }


                String curWordMorphemes = "";
                String curWordGlosses = "";
                for (Node morpheme : morphemes) {
                    Node gloss;
                    try {
                        gloss = eafHelper.getTwoSpGlossByReference(eafHelper.getAnnotationId(morpheme));
                    } catch (XPathExpressionException e) {
                        throw new XMLElanException("Error getting annotation id:" + e.getMessage());
                    }

                    if (gloss == null) {
                        throw new IllegalArgumentException("Bad file: no gloss for morpheme " + morpheme.getTextContent() + "(the sentence is" + translation + ")");
                    }
                    curWordMorphemes += morpheme.getTextContent().trim();
                    String curDelimiter = "";
                    if (curWordMorphemes.contains("-")) {
                        curDelimiter = "-";
                    } else if (curWordMorphemes.contains("=")) {
                        curDelimiter = "=";
                    }
                    curWordGlosses += curDelimiter + gloss.getTextContent().trim();
                }


                wordString += "<td>" + curWord.getTextContent().trim() + "</td>";
                morphString += "<td>" + curWordMorphemes.replaceAll("[-=]=", "=") + "</td>";
                morphGlossString += "<td>" + curWordGlosses.replaceAll("[-=]=", "=") + "</td>";
            }
            wordString += "</tr>";
            morphString += "</tr>\r\n";
            morphGlossString += "</tr>\r\n";

            result += wordString;
            result += morphString;
            result += morphGlossString;

            result += "</table>\r\n";
            result += "</div>\r\n";
            result += "<div class=\"translate\">" + translation + "</div>\r\n";
            result += "</div>\r\n";


        }

        result += "</div>\r\n";
        return result;

    }


    private String createTextSimple() throws XMLElanException {

        String result = "<div class=\"gl-block\">\n";
        List<String> translations = null;
        translations = eafHelper.getRussianArray();

        List<String> glSentences = null;
        glSentences = eafHelper.getGlossArray();

        List<String> fonSentences = null;
        fonSentences = eafHelper.getFonArray();

        List<Long[]> timeMsArray = null;
        try {
            timeMsArray = eafHelper.getTimeArray();
        } catch (XPathExpressionException e) {
            throw new XMLElanException("Error getting time array:" + e.getMessage());
        }
        int fonNum = fonSentences.size();


        if (fonNum != translations.size() || fonNum != glSentences.size()) {
            throw new IllegalArgumentException("Bad file: fonNum!=translations.size() || fonNum!=glosses.size()");
        }
        for (int i = 0; i < fonNum; ++i) {

            Long[] begEnd = timeMsArray.get(i);

            long beg = begEnd[0];
            long end = begEnd[1];

            String allWords[] = CommonUtils.tokenizeSentence(fonSentences.get(i));
            String allGlosses[] = CommonUtils.tokenizeSentence(glSentences.get(i));
            result += "<div class=\"gl-el\">\r\n" +
                    "    \t<div class=\"gl-nm\">" + (i + 1) + "</div>\r\n" +
                    "        <div class=\"gl-rate\" title=\"" + beg + "-" + end + "\">" +
                    CommonUtils.formatMinSec(beg) + " &mdash; " +
                    CommonUtils.formatMinSec(end) + "</div>\r\n" +
                    "<div class=\"data-container\">\r\n" +
                    "            <table class=\"gl-data\">\r\n";
            int wordNum = allWords.length;
            if (wordNum != allGlosses.length) {
                System.out.println(wordNum);
                System.out.println(allGlosses.length);
                throw new IllegalArgumentException("Bad file: wordNum!=allGlosses.length " + fonSentences.get(i));
            }


            String wordString = "<tr class=\"gl-data-tx-1\">\r\n";
            String morphString = "<tr class=\"gl-data-tx-2\">\r\n";
            String morphGlossString = "<tr class=\"gl-data-tx-3\">\r\n";
            for (int j = 0; j < wordNum; ++j) {
                String curWord = allWords[j];
                String[] morphemes = curWord.split("[-=]");
                String[] morphGlosses = allGlosses[j].split("[-=]");
                if (morphemes.length != morphGlosses.length) {
                    throw new IllegalArgumentException("Bad file: morphemes.length != morphGlosses.length for " + curWord + " in " + fonSentences.get(i));
                }

                wordString += "<td>" + curWord.replaceAll("[-=]", "") + "</td>";
                morphString += "<td>" + curWord.replaceAll("[-=]=", "=") + "</td>";
                morphGlossString += "<td>" + allGlosses[j].replaceAll("[-=]=", "=") + "</td>";
            }
            wordString += "</tr>";
            morphString += "</tr>\r\n";
            morphGlossString += "</tr>\r\n";

            result += wordString;
            result += morphString;
            result += morphGlossString;

            result += "</table>\r\n";
            result += "</div>\r\n";
            result += "<div class=\"translate\">" + translations.get(i) + "</div>\r\n";
            result += "</div>\r\n";


        }

        result += "</div>\r\n";
        return result;

    }

    private String createTextSimpleTwoSpeakers() throws XMLElanException {

        String result = "<div class=\"gl-block\">\n";
        List<Node> fonMessages = null;
        try {
            fonMessages = eafHelper.getTwoSpeakersFon();
        } catch (XPathExpressionException e) {
            throw new XMLElanException("Error getting fon array:" + e.getMessage());
        }
        try {
            List<Node> originalMessages = eafHelper.getTwoSpeakersOriginalMessages();
        } catch (XPathExpressionException e) {
            throw new XMLElanException("Error getting original array:" + e.getMessage());
        }
        List<Node> glosses;
        try {
            glosses = eafHelper.getTwoSpeakersGl();
        } catch (XPathExpressionException e) {
            throw new XMLElanException("Error getting fon array:" + e.getMessage());
        }
        List<Node> translations = eafHelper.getTwoSpeakersRus();


        List<Long[]> timeMsArray = null;
        try {
            timeMsArray = eafHelper.getTimeArray();
        } catch (XPathExpressionException e) {
            throw new XMLElanException("Error getting time array:" + e.getMessage());
        }

        int sentNum = fonMessages.size();


        for (int i = 0; i < sentNum; ++i) {

            Long[] begEnd = timeMsArray.get(i);

            long beg = begEnd[0];
            long end = begEnd[1];


            Node gloss = glosses.get(i);
            String translation = translations.get(i).getTextContent().trim();

            if (translation == null) {
                throw new IllegalArgumentException("Bad file: null translation for sentence " + fonMessages.get(i).getTextContent());
            }


            String allWords[] = CommonUtils.tokenizeSentence(fonMessages.get(i).getTextContent().trim());
            String allGlosses[] = CommonUtils.tokenizeSentence(gloss.getTextContent().trim());


            result += "<div class=\"gl-el\">\r\n" +
                    "    \t<div class=\"gl-nm\">" + (i + 1) + "</div>\r\n" +
                    "        <div class=\"gl-rate\" title=\"" + beg + "-" + end + "\">" +
                    CommonUtils.formatMinSec(beg) + " &mdash; " +
                    CommonUtils.formatMinSec(end) + "</div>\r\n" +
                    "<div class=\"data-container\">\r\n" +
                    "            <table class=\"gl-data\">\r\n";
            int wordNum = allWords.length;
            if (wordNum != allGlosses.length) {
                System.out.println(wordNum);
                System.out.println(allGlosses.length);
                throw new IllegalArgumentException("Bad file: wordNum!=allGlosses.length " + fonMessages.get(i).getTextContent());
            }


            String wordString = "<tr class=\"gl-data-tx-1\">\r\n";
            String morphString = "<tr class=\"gl-data-tx-2\">\r\n";
            String morphGlossString = "<tr class=\"gl-data-tx-3\">\r\n";
            for (int j = 0; j < wordNum; ++j) {
                String curWord = allWords[j];
                String[] morphemes = curWord.split("[-=]");
                String[] morphGlosses = allGlosses[j].split("[-=]");
                if (morphemes.length != morphGlosses.length) {
                    throw new IllegalArgumentException("Bad file: morphemes.length != morphGlosses.length for " + curWord + " in " + fonMessages.get(i));
                }

                wordString += "<td>" + curWord.replaceAll("[-=]", "") + "</td>";
                morphString += "<td>" + curWord + "</td>";
                morphGlossString += "<td>" + allGlosses[j] + "</td>";
            }
            wordString += "</tr>";
            morphString += "</tr>\r\n";
            morphGlossString += "</tr>\r\n";

            result += wordString;
            result += morphString;
            result += morphGlossString;

            result += "</table>\r\n";
            result += "</div>\r\n";
            result += "<div class=\"translate\">" + translation + "</div>\r\n";
            result += "</div>\r\n";

        }

        result += "</div>\r\n";
        return result;

    }


}
