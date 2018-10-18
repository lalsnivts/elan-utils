package ru.msu.srcc.minlang;

import org.w3c.dom.*;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: User
 * Date: 04.03.14
 * Time: 22:17
 * To change this template use File | Settings | File Templates.
 */
public class XMLFormatter {

    private String HTML_HEADER = "\n" +
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

    private String HTML_END = "</body>\n" +
            "</html>\n";


    private String ORIGINAL_TIER_NAME = "ev";
    private String SIL_ORIGINAL_TIER_NAME = "ev";
    private String SIL_FON_TIER_NAME = "fon";
    private String FON_TIER_NAME = "fon";
    private String SIL_FON_WORD_TIER_NAME = "fonWord";
    private String FON_WORD_TIER_NAME = "fonWord";
    private String SIL_RUS_TIER_NAME = "rus";
    private String RUS_TIER_NAME = "rus";
    private String SIL_GLOSS_TIER_NAME = "gl";
    private String GLOSS_TIER_NAME = "gl";
    private String COMMENT_TIER_NAME = "comment";


    private String FIRST_ORIGINAL_TIER_NAME = "ev1";
    private String FIRST_FON_TIER_NAME = "fon1";
    private String FIRST_FON_WORD_TIER_NAME = "fonWord1";
    private String FIRST_GLOSS_TIER_NAME = "gl1";
    private String FIRST_RUS_TIER_NAME = "rus1";


    private String SECOND_ORIGINAL_TIER_NAME = "ev2";
    private String SECOND_FON_TIER_NAME = "fon2";
    private String SECOND_FON_WORD_TIER_NAME = "fonWord2";
    private String SECOND_GLOSS_TIER_NAME = "gl2";
    private String SECOND_RUS_TIER_NAME = "rus2";




    private final String TIER = "TIER";
    /*  40 */   private final String TIER_ID = "TIER_ID";
    /*  41 */   private final String PARENT_REF = "PARENT_REF";
    /*  42 */   private final String ref1 = "TIME_SLOT_REF1";
    /*  43 */   private final String ref2 = "TIME_SLOT_REF2";
    /*  44 */   private final String anIdSIL = "ANNOTATION_ID";
    /*  45 */   private final String anRefSIL = "ANNOTATION_REF";
    /*  46 */   private final String ANNOTATION_ID = "ANNOTATION_ID";
    /*     */   private List<String> tierNames;
    /*     */   private Document doc;
    /*     */   private NodeList allTiers;
    /*     */   private String oldFileName;

    public String getOldFileName() {
        return oldFileName;
    }

    public void setOldFileName(String oldFileName) {
        this.oldFileName = oldFileName;
    }

    public List<String> saveFiles(boolean isSil, boolean isTwoSpeakers, boolean isArchive) throws XMLElanException {
        String xmlFilePath = saveDocumentToFile(isSil, isTwoSpeakers, isArchive);
        //TODO
        if(isSil && !isTwoSpeakers){
            String rusFilePath = saveRusDocumentToFile();
            String originalFilePath = saveOriginalDocumentToFile();
            return Arrays.asList(new String[]{xmlFilePath, rusFilePath, originalFilePath});
        }
        return Arrays.asList(new String[]{xmlFilePath});
    }

    public void setAllTiers(NodeList allTiers){
        this.allTiers = allTiers;
    }

    public void setDoc(Document doc){
        this.doc = doc;
    }

    private String saveDocumentToFile(boolean isSil, boolean isTwoSpeakers, boolean isArchive) throws XMLElanException{
        String html = createDocumentHTML(isSil, isTwoSpeakers, isArchive);
        List<String> lines = Arrays.asList(html);
        String newName =  oldFileName + "_glosses_.html";
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
            Files.write(file, Arrays.asList(text), Charset.forName("UTF-8"));
        } catch (IOException e) {
            throw new XMLElanException(e.getMessage());
        }
        return newName;
    }

    private String saveOriginalDocumentToFile() throws XMLElanException {
        String newName =  oldFileName + "_" + SIL_ORIGINAL_TIER_NAME + ".txt";
        String text = generateLines(SIL_ORIGINAL_TIER_NAME);
        Path file = Paths.get(newName);
        try {
            Files.write(file, Arrays.asList(text), Charset.forName("UTF-8"));
        } catch (IOException e) {
            throw new XMLElanException(e.getMessage());
        }
        return newName;
    }

    private String generateLines(String tierName) throws XMLElanException {
        int counter = 1;
        List<String>lines = getAnnotationValuesByTierName(tierName);
        StringBuffer stringBuffer = new StringBuffer();
        for(String line : lines){
            if(counter > 1){
                stringBuffer.append(" ");
            }
            stringBuffer.append("(");
            stringBuffer.append(counter);
            stringBuffer.append(") ");
            stringBuffer.append(line);
            counter += 1;
        }
        return stringBuffer.toString();
    }

    private String createDocumentHTML(boolean isSil, boolean isTwoSpeakers, boolean isArchive) throws XMLElanException{
        String textCreated =  createText(isSil, isTwoSpeakers, isArchive);
        return HTML_HEADER + textCreated + HTML_END;
    }

    private String createText(boolean isSil, boolean isTwoSpeakers, boolean isArchive) throws XMLElanException{
        if(isSil){
            if(!isTwoSpeakers){
                return createTextSIL(isArchive);
            }
            else return createTextSILTwoSpeakers();
        }
        else{
            if(!isTwoSpeakers){
                return createTextSimple();
            }
            else return createTextSimpleTwoSpeakers();
        }
    }


       private String createTextSIL(boolean isArchive) throws XMLElanException{

           StringBuffer result = new StringBuffer("<div class=\"gl-block\">\n");
           List<Node>originalMessages = null;
           try {
               originalMessages = getSILOriginalMessages();
           } catch (XPathExpressionException e) {
               throw new XMLElanException("Error getting original array:" + e.getMessage());
           }
           List<String>translations = null;
           try {
               translations = getSILRussianArray();
           } catch (XMLElanException e) {
               throw new XMLElanException("Error getting Russian array:" + e.getMessage());
           }
           List<Long[]>timeMsArray = null;
        if(!isArchive){
            try {
                timeMsArray = getTimeArray();
            } catch (XPathExpressionException e) {
                throw new XMLElanException("Error getting time array:" + e.getMessage());
            }
        }

        int sentNum = translations.size();
        if(sentNum!=translations.size()){
            throw new XMLElanException("Bad file: sentNum!=translations.size()");
        }
        for(int i=0; i<sentNum ;++i){


            String beg = "";
            String end = "";
            String time = "";
            String begMinSec = "";
            String endMinSec = "";

            String timeSec = "";

            if(!isArchive) {


                Long[] begEnd = timeMsArray.get(i);

                beg =   String.valueOf(begEnd[0]);
                end =   String.valueOf(begEnd[1]);
                begMinSec = getMinSec(begEnd[0]);
                endMinSec = getMinSec(begEnd[1]);

                time =   beg + "-" + end;
                timeSec =  begMinSec +" &mdash; " + endMinSec;

            }

            List<Node>allWords = null;
            try {
                allWords = getFonWordsByReference(getAnnotationId(originalMessages.get(i)));
            } catch (XPathExpressionException e) {
                throw new XMLElanException("Error getting annotation id:" + e.getMessage());
            }

            result.append("<div class=\"gl-el\">\r\n");
            result.append("    \t<div class=\"gl-nm\">");
            result.append(String.valueOf(i+1));
            result.append("</div>\r\n");
            result.append("        <div class=\"gl-rate\" title=\"");
            result.append(time);
            result.append("\">");
            result.append(timeSec);
            result.append("</div>\r\n");
            result.append("<div class=\"data-container\">\r\n");
            result.append("            <table class=\"gl-data\">\r\n");
            int wordNum = allWords.size();



            String wordString = "<tr class=\"gl-data-tx-1\">\r\n";
            String morphString = "<tr class=\"gl-data-tx-2\">\r\n";
            String morphGlossString = "<tr class=\"gl-data-tx-3\">\r\n";



            boolean isTableNecessary = true;
            boolean prevTableStarted = true;
            for(int j=0;j<wordNum;++j){



                Node curWord = allWords.get(j);


                if(curWord.getTextContent().trim().equals(".")){
                    continue;
                }

                List<Node>morphemes = null;
                try {
                    morphemes = getMorphemesByReference(getAnnotationId(curWord));
                } catch (XPathExpressionException e) {
                    throw new XMLElanException("Error getting annotation id:" + e.getMessage());
                }



                String curWordMorphemes = "";
                String curWordGlosses = "";
                prevTableStarted = false;
                int curIndex = 0;



                for(Node morpheme:morphemes){
                    curIndex += 1;
                    Node gloss = null;
                    try {
                        gloss = getGlossByReference(getAnnotationId(morpheme));
                    } catch (XPathExpressionException e) {
                        throw new XMLElanException("Error getting annotation id:" + e.getMessage());
                    }
                    if(gloss == null){
                        throw new XMLElanException("Bad file: no gloss for morpheme " + morpheme.getTextContent() + "(the sentence is" +translations.get(i) + ")");
                    }


                    isTableNecessary = true;

                    String curDelimiter = " ";
                    if(morphemes.size()==1){
                        curDelimiter = "";
                        isTableNecessary = false;
                    }
                    if(curWordMorphemes.contains("-") || morpheme.getTextContent().contains("-")) {
                        curDelimiter = "-";
                        isTableNecessary = false;
                    }
                    else if(curWordMorphemes.contains("=") || morpheme.getTextContent().contains("=")) {
                        curDelimiter = "=";
                        isTableNecessary = false;
                    }

                    if(isTableNecessary){
                        String wordTagToAdd = "";
                        String morphTagToAdd = "";
                        String tagToAddClose = "";
                        if(!prevTableStarted){
                            wordTagToAdd = "<table><td class=\"gl-data-tx-1\">";
                            morphTagToAdd = "<table><td class=\"gl-data-tx-2\">";
                        }
                        else{
                            wordTagToAdd = "</td><td>";
                            morphTagToAdd = "</td><td>";
                        }
                        curWordGlosses += wordTagToAdd +  gloss.getTextContent().trim() + tagToAddClose;

                        curWordMorphemes += morphTagToAdd + morpheme.getTextContent().trim().replaceAll("-", "") + tagToAddClose;


                        if(prevTableStarted && curIndex == morphemes.size() && curIndex > 1){
                            curWordGlosses += "</td></table>";
                            curWordMorphemes += "</td></table>";
                        }
                    }
                    else{

                        curWordGlosses +=  curDelimiter + gloss.getTextContent().trim();

                        curWordMorphemes +=  curDelimiter + morpheme.getTextContent().trim().replaceAll("-", "");
                    }

                    prevTableStarted =  isTableNecessary;

                }


                if(curWordGlosses.startsWith("-") || curWordGlosses.startsWith(" ") || curWordGlosses.startsWith("=")){
                    curWordGlosses = curWordGlosses.substring(1);
                }

                if(curWordMorphemes.startsWith("-") || curWordMorphemes.startsWith(" ") || curWordMorphemes.startsWith("=")){
                    curWordMorphemes = curWordMorphemes.substring(1);
                }



                if(curWordGlosses.startsWith("<table><td class=\"gl-data-tx-1\">") && !prevTableStarted){
                    int len = "<table><td class=\"gl-data-tx-2\">".length();
                    curWordGlosses = curWordGlosses.substring(len);
                }

                if(curWordMorphemes.startsWith("<table><td class=\"gl-data-tx-2\">") && !prevTableStarted){
                    int len = "<table><td class=\"gl-data-tx-2\">".length();
                    curWordMorphemes = curWordMorphemes.substring(len);
                }




                curWordMorphemes = curWordMorphemes.replaceAll("--", "-");

                curWordGlosses = curWordGlosses.replaceAll("--", "-");
                morphString+="<td>" + curWordMorphemes.replaceAll("[-=]=", "=") + "</td>";

                String beforeWord = "";
                String afterWord = "";
                if(curWordMorphemes.contains("<table")){
                    beforeWord = "<table><td class=\"gl-data-tx-1\">";
                    afterWord = "</td></table>";
                }

                wordString+="<td>" + beforeWord + curWord.getTextContent().trim()  + afterWord+ "</td>";


                morphGlossString+="<td>" + curWordGlosses.replaceAll("[-=]=", "=") + "</td>";



            }
            wordString+="</tr>";
            morphString+="</tr>\r\n";
            morphGlossString+="</tr>\r\n";

            result.append(wordString);
            result.append(morphString);
            result.append(morphGlossString);


            result.append("</table>\r\n");
            result.append("</div>\r\n");
            result.append("<div class=\"translate\">");
            result.append(translations.get(i));
            result.append("</div>\r\n");



            List<Node>comments = null;
            try {
                comments = getCommentsByReference(getAnnotationId(originalMessages.get(i)));
            } catch (XPathExpressionException e) {
                throw new XMLElanException("Error getting comments:" + e.getMessage());
            }
            if(comments != null && !comments.isEmpty())  {
                result.append("<div class=\"comment\">");
                result.append(comments.get(0).getTextContent());
                result.append("</div>\r\n");
            }

            result.append("</div>\r\n");

        }

        result.append("</div>\r\n");
        return result.toString();

    }



    private String createTextSILTwoSpeakers() throws XMLElanException{

        String result="<div class=\"gl-block\">\n";
        List<Node>originalMessages = null;
        try {
            originalMessages = getTwoSpeakersOriginalMessages();
        } catch (XPathExpressionException e) {
            throw new XMLElanException("Error getting original array:" + e.getMessage());
        }


        List<Long[]>timeMsArray = null;
        try {
            timeMsArray = getTimeArray();
        } catch (XPathExpressionException e) {
            throw new XMLElanException("Error getting time array:" + e.getMessage());
        }
        int sentNum = originalMessages.size();


        for(int i=0; i<sentNum ;++i){

            Long[] begEnd = timeMsArray.get(i);

            long beg =   begEnd[0];
            long end =   begEnd[1];


            String annotationId = null;
            try {
                annotationId = getAnnotationId(originalMessages.get(i));
            } catch (XPathExpressionException e) {
                throw new XMLElanException("Error getting annotation id:" + e.getMessage());
            }

            List<Node>allWords = null;
            try {
                allWords = getTwoSpFonWordsByReference(annotationId);
            } catch (XPathExpressionException e) {
                throw new XMLElanException("Error getting fonWords:" + e.getMessage());
            }

            String translation = null;
            try {
                translation = getTwoSpTranslationByReference(annotationId);
            } catch (XPathExpressionException e) {
                throw new XMLElanException("Error getting original array:" + e.getMessage());
            }
            if(translation==null){
                throw new XMLElanException("Bad file: null translation for sentence " + originalMessages.get(i).getTextContent());
            }



            result += "<div class=\"gl-el\">\r\n" +
                    "    \t<div class=\"gl-nm\">" + (i+1) + "</div>\r\n" +
                    "        <div class=\"gl-rate\" title=\""+ beg + "-" + end + "\">"+getMinSec(beg)+" &mdash; " + getMinSec(end) +"</div>\r\n" +
                    "<div class=\"data-container\">\r\n" +
                    "            <table class=\"gl-data\">\r\n";
            int wordNum = allWords.size();



            String wordString = "<tr class=\"gl-data-tx-1\">\r\n";
            String morphString = "<tr class=\"gl-data-tx-2\">\r\n";
            String morphGlossString = "<tr class=\"gl-data-tx-3\">\r\n";






            for(int j=0;j<wordNum;++j){
                Node curWord = allWords.get(j);

                List<Node>morphemes = null;
                try {
                    morphemes = getTwoSpMorphemesByReference(getAnnotationId(curWord));
                } catch (XPathExpressionException e) {
                    throw new XMLElanException("Error getting annotation id:" + e.getMessage());
                }


                String curWordMorphemes = "";
                String curWordGlosses = "";
                for(Node morpheme:morphemes){
                    Node gloss = null;
                    try {
                        gloss = getTwoSpGlossByReference(getAnnotationId(morpheme));
                    } catch (XPathExpressionException e) {
                        throw new XMLElanException("Error getting annotation id:" + e.getMessage());
                    }

                    if(gloss == null){
                        throw new IllegalArgumentException("Bad file: no gloss for morpheme " + morpheme.getTextContent() + "(the sentence is" + translation + ")");
                    }
                    curWordMorphemes += morpheme.getTextContent().trim();
                    String curDelimiter = "";
                    if(curWordMorphemes.contains("-")) {
                        curDelimiter = "-";
                    }
                    else if(curWordMorphemes.contains("=")) {
                        curDelimiter = "=";
                    }
                    curWordGlosses += curDelimiter + gloss.getTextContent().trim();
                }


                wordString+="<td>" + curWord.getTextContent().trim() + "</td>";
                morphString+="<td>" + curWordMorphemes.replaceAll("[-=]=", "=") + "</td>";
                morphGlossString+="<td>" + curWordGlosses.replaceAll("[-=]=", "=") + "</td>";
            }
            wordString+="</tr>";
            morphString+="</tr>\r\n";
            morphGlossString+="</tr>\r\n";

            result+= wordString;
            result+= morphString;
            result+= morphGlossString;

            result+="</table>\r\n";
            result+="</div>\r\n";
            result+="<div class=\"translate\">" + translation+ "</div>\r\n";
            result+="</div>\r\n";


        }

        result+="</div>\r\n";
        return result;

    }


    private String createTextSimple() throws XMLElanException{

        String result="<div class=\"gl-block\">\n";
        List<String>translations = null;
        translations = getRussianArray();

        List<String>glSentences = null;
        glSentences = getGlossArray();

        List<String>fonSentences = null;
        fonSentences = getFonArray();

        List<Long[]>timeMsArray = null;
        try {
            timeMsArray = getTimeArray();
        } catch (XPathExpressionException e) {
            throw new XMLElanException("Error getting time array:" + e.getMessage());
        }
        int fonNum = fonSentences.size();


        if(fonNum!=translations.size() || fonNum!=glSentences.size()){
            throw new IllegalArgumentException("Bad file: fonNum!=translations.size() || fonNum!=glosses.size()");
        }
        for(int i=0; i<fonNum ;++i){

            Long[] begEnd = timeMsArray.get(i);

            long beg =   begEnd[0];
            long end =   begEnd[1];

            String allWords[] = tokenizeSentence(fonSentences.get(i));
            String allGlosses[] = tokenizeSentence(glSentences.get(i));
            result += "<div class=\"gl-el\">\r\n" +
                    "    \t<div class=\"gl-nm\">" + (i+1) + "</div>\r\n" +
                    "        <div class=\"gl-rate\" title=\""+ beg + "-" + end + "\">"+getMinSec(beg)+" &mdash; " + getMinSec(end) +"</div>\r\n" +
                    "<div class=\"data-container\">\r\n" +
                    "            <table class=\"gl-data\">\r\n";
            int wordNum = allWords.length;
            if(wordNum!=allGlosses.length){
                System.out.println(wordNum);
                System.out.println(allGlosses.length);
                throw new IllegalArgumentException("Bad file: wordNum!=allGlosses.length "+fonSentences.get(i));
            }



            String wordString = "<tr class=\"gl-data-tx-1\">\r\n";
            String morphString = "<tr class=\"gl-data-tx-2\">\r\n";
            String morphGlossString = "<tr class=\"gl-data-tx-3\">\r\n";
            for(int j=0;j<wordNum;++j){
                String curWord = allWords[j];
                String[]morphemes = curWord.split("[-=]");
                String[]morphGlosses = allGlosses[j].split("[-=]");
                if(morphemes.length != morphGlosses.length){
                    throw new IllegalArgumentException("Bad file: morphemes.length != morphGlosses.length for " +curWord +" in "+fonSentences.get(i));
                }

                wordString+="<td>" + curWord.replaceAll("[-=]","") + "</td>";
                morphString+="<td>" + curWord.replaceAll("[-=]=", "=") + "</td>";
                morphGlossString+="<td>" + allGlosses[j].replaceAll("[-=]=", "=") + "</td>";
            }
            wordString+="</tr>";
            morphString+="</tr>\r\n";
            morphGlossString+="</tr>\r\n";

            result+= wordString;
            result+= morphString;
            result+= morphGlossString;

            result+="</table>\r\n";
            result+="</div>\r\n";
            result+="<div class=\"translate\">" + translations.get(i)+ "</div>\r\n";
            result+="</div>\r\n";


        }

        result+="</div>\r\n";
        return result;

    }

    private String createTextSimpleTwoSpeakers() throws XMLElanException{

        String result="<div class=\"gl-block\">\n";
        List<Node>fonMessages = null;
        try {
            fonMessages = getTwoSpeakersFon();
        } catch (XPathExpressionException e) {
            throw new XMLElanException("Error getting fon array:" + e.getMessage());
        }
        try {
            List<Node>originalMessages = getTwoSpeakersOriginalMessages();
        } catch (XPathExpressionException e) {
            throw new XMLElanException("Error getting original array:" + e.getMessage());
        }
        List<Node>glosses = null;
        try {
            glosses = getTwoSpeakersGl();
        } catch (XPathExpressionException e) {
            throw new XMLElanException("Error getting fon array:" + e.getMessage());
        }
        List<Node>translations = null;
        try {
            translations = getTwoSpeakersRus();
        } catch (XPathExpressionException e) {
            throw new XMLElanException("Error getting Russian array:" + e.getMessage());
        }

        List<Long[]>timeMsArray = null;
        try {
            timeMsArray = getTimeArray();
        } catch (XPathExpressionException e) {
            throw new XMLElanException("Error getting time array:" + e.getMessage());
        }

        int sentNum = fonMessages.size();


        for(int i=0; i<sentNum ;++i){

            Long[] begEnd = timeMsArray.get(i);

            long beg =   begEnd[0];
            long end =   begEnd[1];






            Node gloss = glosses.get(i);
            String translation = translations.get(i).getTextContent().trim();

            if(translation==null){
                throw new IllegalArgumentException("Bad file: null translation for sentence " + fonMessages.get(i).getTextContent());
            }



            String allWords[] = tokenizeSentence(fonMessages.get(i).getTextContent().trim());
            String allGlosses[] = tokenizeSentence(gloss.getTextContent().trim());


            result += "<div class=\"gl-el\">\r\n" +
                    "    \t<div class=\"gl-nm\">" + (i+1) + "</div>\r\n" +
                    "        <div class=\"gl-rate\" title=\""+ beg + "-" + end + "\">"+getMinSec(beg)+" &mdash; " + getMinSec(end) +"</div>\r\n" +
                    "<div class=\"data-container\">\r\n" +
                    "            <table class=\"gl-data\">\r\n";
            int wordNum = allWords.length;
            if(wordNum!=allGlosses.length){
                System.out.println(wordNum);
                System.out.println(allGlosses.length);
                throw new IllegalArgumentException("Bad file: wordNum!=allGlosses.length "+fonMessages.get(i).getTextContent());
            }



            String wordString = "<tr class=\"gl-data-tx-1\">\r\n";
            String morphString = "<tr class=\"gl-data-tx-2\">\r\n";
            String morphGlossString = "<tr class=\"gl-data-tx-3\">\r\n";
            for(int j=0;j<wordNum;++j){
                String curWord = allWords[j];
                String[]morphemes = curWord.split("[-=]");
                String[]morphGlosses = allGlosses[j].split("[-=]");
                if(morphemes.length != morphGlosses.length){
                    throw new IllegalArgumentException("Bad file: morphemes.length != morphGlosses.length for " +curWord +" in "+fonMessages.get(i));
                }

                wordString+="<td>" + curWord.replaceAll("[-=]","") + "</td>";
                morphString+="<td>" + curWord + "</td>";
                morphGlossString+="<td>" + allGlosses[j] + "</td>";
            }
            wordString+="</tr>";
            morphString+="</tr>\r\n";
            morphGlossString+="</tr>\r\n";

            result+= wordString;
            result+= morphString;
            result+= morphGlossString;

            result+="</table>\r\n";
            result+="</div>\r\n";
            result+="<div class=\"translate\">" + translation+ "</div>\r\n";
            result+="</div>\r\n";

        }

        result+="</div>\r\n";
        return result;

    }


    public List<Node>getFonWordsByReference(String reference)  throws XPathExpressionException{
        return getAnnotationNodesByTierNameReference(SIL_FON_WORD_TIER_NAME, reference);
    }

    public List<Node>getTwoSpFonWordsByReference(String reference)  throws XPathExpressionException{
        List<Node>firstWords = getAnnotationNodesByTierNameReference(FIRST_FON_WORD_TIER_NAME, reference);
        List<Node>secondWords = getAnnotationNodesByTierNameReference(SECOND_FON_WORD_TIER_NAME, reference);
        firstWords.addAll(secondWords);
        return firstWords;
    }


    private String getTwoSpTranslationByReference(String reference)  throws XPathExpressionException{
        System.out.println(reference);
        Node translation1 = getOneAnnotationNodeByTierNameReference(FIRST_RUS_TIER_NAME, reference);
        if(translation1 == null){
            return  getOneAnnotationNodeByTierNameReference(SECOND_RUS_TIER_NAME, reference).getTextContent();
        }
        return translation1.getTextContent();
    }

    private List<Node>getMorphemesByReference(String reference)  throws XPathExpressionException{
        return getAnnotationNodesByTierNameReference(SIL_FON_TIER_NAME, reference);
    }

    private List<Node>getCommentsByReference(String reference)  throws XPathExpressionException{
        return getAnnotationNodesByTierNameReference(COMMENT_TIER_NAME, reference);
    }

    public List<Node>getOldFonNodes()  throws XPathExpressionException{
        return getAnnotationNodesByTierName(FON_TIER_NAME);
    }

    private List<Node>getTwoSpMorphemesByReference(String reference)  throws XPathExpressionException{
        List<Node>firstMorphemes = getAnnotationNodesByTierNameReference(FIRST_FON_TIER_NAME, reference);
        List<Node>secondMorphemes = getAnnotationNodesByTierNameReference(SECOND_FON_TIER_NAME, reference);
        firstMorphemes.addAll(secondMorphemes);
        //TODO: sort
        return firstMorphemes;
    }

    private Node getGlossByReference(String reference)  throws XPathExpressionException{
        return getOneAnnotationNodeByTierNameReference(SIL_GLOSS_TIER_NAME, reference);
    }

    private Node getTwoSpGlossByReference(String reference)  throws XPathExpressionException{
        Node nodeFirst = getOneAnnotationNodeByTierNameReference(FIRST_GLOSS_TIER_NAME, reference);
        if(nodeFirst == null){
            return  getOneAnnotationNodeByTierNameReference(SECOND_GLOSS_TIER_NAME, reference);
        }
        return nodeFirst;
    }



    private List<Node>getSILOriginalMessages() throws XPathExpressionException{
        return getAnnotationNodesByTierName(SIL_ORIGINAL_TIER_NAME);
    }

    public List<Node>getSILRussianNodes() throws XPathExpressionException{
        return getAnnotationNodesByTierName(SIL_RUS_TIER_NAME);
    }

    private List<Node>getTwoSpeakersOriginalMessages() throws XPathExpressionException{
        List<Node>firstAnnotations = getAnnotationNodesByTierName(FIRST_ORIGINAL_TIER_NAME);
        List<Node>secondAnnotations = getAnnotationNodesByTierName(SECOND_ORIGINAL_TIER_NAME);
        firstAnnotations.addAll(secondAnnotations);



        AnnotationSorter annotationSorter = new AnnotationSorter();
        Collections.sort(firstAnnotations, annotationSorter);
        return firstAnnotations;

    }

    private List<Node>getTwoSpeakersFon() throws XPathExpressionException{
        List<Node>firstAnnotations = getAnnotationNodesByTierName(FIRST_FON_TIER_NAME);
        List<Node>secondAnnotations = getAnnotationNodesByTierName(SECOND_FON_TIER_NAME);
        firstAnnotations.addAll(secondAnnotations);



        AnnotationSorter annotationSorter = new AnnotationSorter();
        Collections.sort(firstAnnotations, annotationSorter);
        return firstAnnotations;

    }


    private List<Node>getTwoSpeakersGl() throws XPathExpressionException{
        List<Node>firstAnnotations = getAnnotationNodesByTierName(FIRST_GLOSS_TIER_NAME);
        List<Node>secondAnnotations = getAnnotationNodesByTierName(SECOND_GLOSS_TIER_NAME);
        firstAnnotations.addAll(secondAnnotations);



        AnnotationSorter annotationSorter = new AnnotationSorter();
        Collections.sort(firstAnnotations, annotationSorter);
        return firstAnnotations;

    }


    private List<Node>getTwoSpeakersRus() throws XPathExpressionException{
        List<Node>firstAnnotations = getAnnotationNodesByTierName(FIRST_RUS_TIER_NAME);
        List<Node>secondAnnotations = getAnnotationNodesByTierName(SECOND_RUS_TIER_NAME);
        firstAnnotations.addAll(secondAnnotations);



        AnnotationSorter annotationSorter = new AnnotationSorter();
        Collections.sort(firstAnnotations, annotationSorter);
        return firstAnnotations;

    }

    private List<String>getRussianArray() throws XMLElanException{
        return getAnnotationValuesByTierName(RUS_TIER_NAME);
    }



    private List<String>getSILRussianArray() throws XMLElanException{

        return getAnnotationValuesByTierName(SIL_RUS_TIER_NAME);
    }

    private List<String>getSILOriginalArray() throws XMLElanException{
        return getAnnotationValuesByTierName(SIL_ORIGINAL_TIER_NAME);
    }


    private List<String>getTwoSpeakersRussian() throws XMLElanException{
        List<String>firstAnnotations = getAnnotationValuesByTierName(FIRST_RUS_TIER_NAME);
        List<String>secondAnnotations = getAnnotationValuesByTierName(SECOND_RUS_TIER_NAME);
        firstAnnotations.addAll(secondAnnotations);
        //TODO: sort!
        return firstAnnotations;
    }


    private List<String>getFonArray() throws XMLElanException{
        return getAnnotationValuesByTierName(FON_TIER_NAME);
    }

    private List<String>getSILFonWordArray() throws XMLElanException{
        return getAnnotationValuesByTierName(FON_WORD_TIER_NAME);
    }

    private List<String>getGlossArray() throws XMLElanException{
        return getAnnotationValuesByTierName(GLOSS_TIER_NAME);
    }

    private List<Long[]>getTimeArray() throws XPathExpressionException{
        List<Long[]>begEnds = new ArrayList<Long[]>();
        XPath xpath = XPathFactory.newInstance().newXPath();
        NodeList timeValuesMS = ((NodeList)xpath.evaluate("//TIME_SLOT/@TIME_VALUE", this.doc, XPathConstants.NODESET));
        for(int i=0;i<timeValuesMS.getLength()-1;i+=2){
            String beginStr = timeValuesMS.item(i).getTextContent();
            String endStr = timeValuesMS.item(i+1).getTextContent();
            long begin = Long.parseLong(beginStr);
            long end = Long.parseLong(endStr);
            begEnds.add(new Long[]{begin, end});
        }
        return begEnds;
    }

    private List<Node>getTimeArrayNodes() throws XPathExpressionException{
        List<Node>timeArrayNodes = new ArrayList<Node>();
        XPath xpath = XPathFactory.newInstance().newXPath();
        NodeList timeValuesMS = ((NodeList)xpath.evaluate("//TIME_SLOT", this.doc, XPathConstants.NODESET));
        for(int i=0;i<timeValuesMS.getLength();++i){
            Node timeValue = timeValuesMS.item(i);
            timeArrayNodes.add(timeValue);
        }
        return timeArrayNodes;
    }

    private List<String>getAnnotationValuesByTierName(String tierName) throws XMLElanException{
        for (int i = 0; i < this.allTiers.getLength(); i++) {
             Node node = this.allTiers.item(i);
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

    private List<Node>getAnnotationNodesByTierNameReference(String tierName, String reference) throws XPathExpressionException{
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

    private Node getOneAnnotationNodeByTierNameReference(String tierName, String reference) throws XPathExpressionException{
        for (int i = 0; i < this.allTiers.getLength(); i++) {
            Node node = this.allTiers.item(i);
            NamedNodeMap attributes = node.getAttributes();
            Node nameAttrib = attributes.getNamedItem("TIER_ID");
            if (nameAttrib.getNodeValue().equals(tierName)) {
                return getOneAnnotationNodeByTierNameReference(node, reference);
            }
        }
        return null;
    }

    private List<Node>getAnnotationNodesByTierName(String tierName) throws XPathExpressionException{
        for (int i = 0; i < this.allTiers.getLength(); i++) {
            Node node = this.allTiers.item(i);
            NamedNodeMap attributes = node.getAttributes();
            Node nameAttrib = attributes.getNamedItem("TIER_ID");
            if (nameAttrib.getNodeValue().equals(tierName)) {
                return getAnnotations(node);
            }
        }
        return null;
    }

    private List<Node>getAnnotationNodesByReference(Node node, String reference) throws XPathExpressionException{
        String annotationExpression = ".//ANNOTATION/REF_ANNOTATION[@ANNOTATION_REF=\""+reference+"\"]";
        XPath xpath = XPathFactory.newInstance().newXPath();
        NodeList nodeSet = (NodeList)xpath.evaluate(annotationExpression, node, XPathConstants.NODESET);
        List<Node> annotations = new ArrayList<Node>();
        for (int i = 0; i < nodeSet.getLength(); i++) {
            annotations.add(nodeSet.item(i));
        }
        return annotations;
    }

    private Node getOneAnnotationNodeByTierNameReference(Node node, String reference) throws XPathExpressionException{
        String annotationExpression = ".//ANNOTATION/REF_ANNOTATION[@ANNOTATION_REF=\""+reference+"\"]";
        XPath xpath = XPathFactory.newInstance().newXPath();
        return  (Node)xpath.evaluate(annotationExpression, node, XPathConstants.NODE);

}



    private List<String> getAnnotationValues(Node node) throws XPathExpressionException{
        String annotationExpression = ".//ANNOTATION_VALUE";
        XPath xpath = XPathFactory.newInstance().newXPath();
        NodeList nodeSet = (NodeList)xpath.evaluate(annotationExpression, node, XPathConstants.NODESET);
        List<String> annotationValues = new ArrayList<String>();
        for (int i = 0; i < nodeSet.getLength(); i++) {
            annotationValues.add(nodeSet.item(i).getTextContent());
        }
        return annotationValues;
    }

    public String getAnnotationId(Node node) throws XPathExpressionException{
        String annotationIdExpression = ".//@ANNOTATION_ID";
        XPath xpath = XPathFactory.newInstance().newXPath();
        Node annotationIdNode = (Node)xpath.evaluate(annotationIdExpression, node, XPathConstants.NODE);
        if(annotationIdNode == null){
            return null;
        }
        return annotationIdNode.getTextContent();
    }


    private String getTimeSlotRef(Node node) throws XPathExpressionException{
        String timeSlotRefExpression = ".//@"+ref1;
        XPath xpath = XPathFactory.newInstance().newXPath();
        Node timeSlotNode = (Node)xpath.evaluate(timeSlotRefExpression, node, XPathConstants.NODE);
        if(timeSlotNode == null){
            return null;
        }
        return timeSlotNode.getTextContent();
    }


    private Node getTimeSlotByRef(String ref) throws XPathExpressionException{
        String timeSlotRefExpression = ".//TIME_SLOT[@TIME_SLOT_ID=\""+ref+"\"]";
        XPath xpath = XPathFactory.newInstance().newXPath();
        return (Node)xpath.evaluate(timeSlotRefExpression, doc, XPathConstants.NODE);

    }


    private List<Node> getAnnotations(Node node) throws XPathExpressionException{
        String annotationExpression = ".//ANNOTATION";
        XPath xpath = XPathFactory.newInstance().newXPath();
        NodeList nodeSet = (NodeList)xpath.evaluate(annotationExpression, node, XPathConstants.NODESET);
        List<Node> annotations = new ArrayList<Node>();
        for (int i = 0; i < nodeSet.getLength(); i++) {
            annotations.add(nodeSet.item(i));
        }
        return annotations;
    }

    private String getMinSec(long ms){
        long minSecMs =  ms%3600000;
        long min = minSecMs/60000;
        long sec =  (minSecMs%60000)/1000;
        return getTimeValueFromLong(min)+":"+getTimeValueFromLong(sec);
    }

    private String getTimeValueFromLong(long timeValue){
        String timeStr = String.valueOf(timeValue);
        if(timeStr.length()==1){
            timeStr = "0" + timeStr;
        }
        return timeStr;
    }

    private String[] tokenizeSentence(String sentence){
        return sentence.split("\\s");
    }



    private class AnnotationSorter implements Comparator<Node>{
        @Override

        public int compare(Node o1, Node o2) {
            try {
                Node node1TimeSlotRef = getTimeSlotByRef(getTimeSlotRef(o1));
                Node node2TimeSlotRef = getTimeSlotByRef(getTimeSlotRef(o2));
                XPath xpath = XPathFactory.newInstance().newXPath();

                Long timeValue1 =    Long.parseLong(((Node)xpath.evaluate("@TIME_VALUE", node1TimeSlotRef, XPathConstants.NODE)).getTextContent());
                Long timeValue2 =    Long.parseLong(((Node)xpath.evaluate("@TIME_VALUE", node2TimeSlotRef, XPathConstants.NODE)).getTextContent());

                return   (int)(timeValue1 - timeValue2);
        } catch (XPathExpressionException e) {
                e.printStackTrace();
                return 0;
            }
        }
    }


}
