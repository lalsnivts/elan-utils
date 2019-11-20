package ru.msu.srcc.minlang;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.w3c.dom.Node;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by User on 17.02.2015.
 */
public class ElanBookExporter {

    private final static String DOC_EXTENSION = ".docx";
    private XMLElanUtils xmlElanUtils = new XMLElanUtils();
    private XMLElanOldUtils xmlOldElanUtils = new XMLElanOldUtils();
    private BookFormatter bookFormatter = new BookFormatter();

    public static void main(String args[]) {


        /*


        Scanner in = new Scanner(System.in);
        System.out.println("Enter path to the eaf file:");


        String filepath = in.nextLine();

        System.out.println("Enter the name of the original tier (ket/selk/ev):");
        String tierName = in.nextLine();

        System.out.println("Is the file from an archive(Y/N)?");
        String isFromArchiveYN = in.nextLine();
        boolean isFromArchive = false;
        if(isFromArchiveYN.equals("Y")){
            isFromArchive = true;
        }


        System.out.println("Are there two speakers (Y/N)?");
        String hasTwoSpeakersYN = in.next();
        boolean hasTwoSpeakers = false;
        if(hasTwoSpeakersYN.equals("Y")){
            hasTwoSpeakers = true;
        }
        ElanBookExporter elanBookExporter = new ElanBookExporter();
       // elanBookExporter.exportOldEafForBook(filepath, tierName);

       if(hasTwoSpeakers){
            elanBookExporter.exportEafForBookTwoSpeakers(filepath,
                    tierName, isFromArchive);
        }
        else{
            elanBookExporter.exportEafForBook(filepath,
                    tierName, isFromArchive);
        }                   */

        ElanBookExporter elanBookExporter = new ElanBookExporter();
       /* elanBookExporter.exportOldEafForBook("D:\\ForElan\\OldMethod\\1998_Sovrechka_Saygotina_Vera_LR\\" +
                        "1998_Sovrechka_Saygotina_Vera_LR_transliterated_new2.eaf",
                "ev");                             */
        elanBookExporter.exportEafForBook("D:\\ForElan\\OldMethod\\2007_Strelka_Andreeva_LR8\\2007_Strelka_Andreeva_LR8.eaf_new.eaf",
                "ev", false);


    }

    public void exportEafForBook(String elanFilename, String originalLevelName, boolean isArchive) {
        try {
            BookFormat bookFormat = getBookFormatFromFile(elanFilename, originalLevelName, isArchive);
            String outputFilename = elanFilename + DOC_EXTENSION;
            boolean isToAddFirstLine = getFirstLineInfo(originalLevelName);
            bookFormatter.formatForBook(bookFormat, outputFilename, isToAddFirstLine);
            System.out.println("exported to " + outputFilename);
        } catch (ElanException e) {
            e.printStackTrace();
        } catch (Docx4JException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void exportEafForBookTwoSpeakers(String elanFilename, String originalLevelName, boolean isArchive) {
        try {
            BookFormat bookFormat = getBookFormatFromFileTwoSpeakers(elanFilename, originalLevelName, isArchive);
            String outputFilename = elanFilename + DOC_EXTENSION;
            boolean isToAddFirstLine = getFirstLineInfo(originalLevelName);
            bookFormatter.formatForBook(bookFormat, outputFilename, isToAddFirstLine);
            System.out.println("exported to " + outputFilename);
        } catch (ElanException e) {
            e.printStackTrace();
        } catch (Docx4JException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void exportOldEafForBook(String elanFilename, String originalLevelName) {
        try {
            BookFormat bookFormat = getBookFormatFromOldFile(elanFilename, originalLevelName);
            String outputFilename = elanFilename + DOC_EXTENSION;
            boolean isToAddFirstLine = getFirstLineInfo(originalLevelName);
            bookFormatter.formatForBook(bookFormat, outputFilename, isToAddFirstLine);
            System.out.println("exported to " + outputFilename);
        } catch (ElanException e) {
            e.printStackTrace();
        } catch (Docx4JException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public BookFormat getBookFormatFromFile(String elanFilename, String originalLevelName, boolean isArchive) throws ElanException {

        ElanParsingResult elanParsingResult = xmlElanUtils.parseElanFile(elanFilename, originalLevelName);
        List<Node> originalMessages = elanParsingResult.getOriginalSentences();
        if (originalMessages == null || originalMessages.size() == 0) {
            throw new ElanException("Error: No tier for " + originalLevelName + " has been found");
        }
        List<String> translations = elanParsingResult.getRussianSentences();
        List<Long[]> timeMsArray = null;
        if (!isArchive) {
            timeMsArray = xmlElanUtils.getTimeArray();
        }

        BookFormat bookFormat = new BookFormat();
        List<String> originalSentences = new ArrayList<String>();


        int sentNum = translations.size();
        if (sentNum != translations.size()) {
            throw new ElanException(String.format("Bad file: number of sentences (%s)!=number of translations (%s)", sentNum, translations.size()));
        }
        List<BookMorphSentence> morphSentences = new ArrayList<BookMorphSentence>();
        for (int i = 0; i < sentNum; ++i) {
            BookMorphSentence bookMorphSentence = parseMorphSentence(isArchive, i, originalMessages, translations, timeMsArray);
            morphSentences.add(bookMorphSentence);
            originalSentences.add(originalMessages.get(i).getTextContent().trim());


        }
        bookFormat.setOriginalSentences(originalSentences);
        bookFormat.setRussianSentences(translations);
        bookFormat.setMorphSentences(morphSentences);
        return bookFormat;
    }

    public BookFormat getBookFormatFromFileTwoSpeakers(String elanFilename, String originalLevelName, boolean isArchive) throws ElanException {

        ElanParsingResult elanParsingResult = xmlElanUtils.parseElanFileTwoSpeakers(elanFilename, originalLevelName);
        List<Node> originalMessages = elanParsingResult.getOriginalSentences();
        if (originalMessages == null || originalMessages.size() == 0) {
            throw new ElanException("Error: No tier for " + originalLevelName + " has been found");
        }
        List<String> translations = elanParsingResult.getRussianSentences();
        List<Long[]> timeMsArray = null;
        if (!isArchive) {
            timeMsArray = xmlElanUtils.getTimeArray();
        }

        BookFormat bookFormat = new BookFormat();
        List<String> originalSentences = new ArrayList<String>();


        int sentNum = translations.size();
        if (sentNum != translations.size()) {
            throw new ElanException(String.format("Bad file: number of sentences (%s)!=number of translations (%s)", sentNum, translations.size()));
        }
        List<BookMorphSentence> morphSentences = new ArrayList<BookMorphSentence>();
        for (int i = 0; i < sentNum; ++i) {
            BookMorphSentence bookMorphSentence = parseMorphSentenceTwoSpeakers(isArchive, i, originalMessages, translations, timeMsArray);
            morphSentences.add(bookMorphSentence);
            originalSentences.add(originalMessages.get(i).getTextContent().trim());


        }
        bookFormat.setOriginalSentences(originalSentences);
        bookFormat.setRussianSentences(translations);
        bookFormat.setMorphSentences(morphSentences);
        return bookFormat;
    }

    private BookFormat getBookFormatFromOldFile(String elanFilename, String originalLevelName) throws ElanException {

        ElanParsingResult elanParsingResult = xmlOldElanUtils.parseElanFile(elanFilename, originalLevelName);
        List<Node> originalMessages = elanParsingResult.getOriginalSentences();


        if (originalMessages == null || originalMessages.size() == 0) {
            throw new ElanException("Error: No tier for " + originalLevelName + " has been found");
        }
        List<String> translations = elanParsingResult.getRussianSentences();


        List<String> words = elanParsingResult.getWords();
        List<String> glosses = elanParsingResult.getGlosses();


        List<Long> timeMsArray = null;


        BookFormat bookFormat = new BookFormat();
        List<String> originalSentences = new ArrayList<String>();

        timeMsArray = xmlOldElanUtils.getTimeArray();


        int sentNum = translations.size();
        if (sentNum != translations.size()) {
            throw new ElanException(String.format("Bad file: number of sentences (%s)!=number of translations (%s)", sentNum, translations.size()));
        }
        List<BookMorphSentence> morphSentences = new ArrayList<BookMorphSentence>();
        for (int i = 0; i < sentNum; ++i) {
            BookMorphSentence bookMorphSentence = parseOldMorphSentence(i, originalMessages, translations, timeMsArray, words, glosses);
            morphSentences.add(bookMorphSentence);
            originalSentences.add(originalMessages.get(i).getTextContent().trim());


        }
        bookFormat.setOriginalSentences(originalSentences);
        bookFormat.setRussianSentences(translations);
        bookFormat.setMorphSentences(morphSentences);
        return bookFormat;


    }

    private BookMorphSentence parseMorphSentence(boolean isArchive, int index, List<Node> originalMessages, List<String> translations, List<Long[]> timeMsArray) throws ElanException {
        BookMorphSentence bookMorphSentence = new BookMorphSentence();
        if (!isArchive) {


            if (timeMsArray.size() < 1) {
                int a = 0;
            }
            Long[] begEnd = timeMsArray.get(index);

            bookMorphSentence.setBegTime(begEnd[0]);
            bookMorphSentence.setEndTime(begEnd[1]);
        }
        List<BookMorphWord> bookMorphWords = getBookMorphWords(originalMessages.get(index), translations.get(index));
        bookMorphSentence.setWords(bookMorphWords);
        return bookMorphSentence;
    }

    private BookMorphSentence parseMorphSentenceTwoSpeakers(boolean isArchive, int index, List<Node> originalMessages, List<String> translations, List<Long[]> timeMsArray) throws ElanException {
        BookMorphSentence bookMorphSentence = new BookMorphSentence();
        if (!isArchive) {


            Long[] begEnd = timeMsArray.get(index);

            bookMorphSentence.setBegTime(begEnd[0]);
            bookMorphSentence.setEndTime(begEnd[1]);
        }
        List<BookMorphWord> bookMorphWords = getBookMorphWordsTwoSpeakers(originalMessages.get(index), translations.get(index));
        bookMorphSentence.setWords(bookMorphWords);
        return bookMorphSentence;
    }

    private BookMorphSentence parseOldMorphSentence(int index, List<Node> originalMessages, List<String> translations, List<Long> timeMsArray, List<String> words, List<String> glosses) throws ElanException {
        BookMorphSentence bookMorphSentence = new BookMorphSentence();

        String timeIndex = originalMessages.get(index).getFirstChild().getNextSibling().getAttributes().getNamedItem("TIME_SLOT_REF1").getNodeValue().split("ts")[1];

        Long beg = timeMsArray.get(Integer.parseInt((timeIndex)) - 1);

        bookMorphSentence.setBegTime(beg);
        bookMorphSentence.setEndTime(null);

        List<BookMorphWord> bookMorphWords = getOldBookMorphWords(index, words, glosses, translations.get(index));
        bookMorphSentence.setWords(bookMorphWords);
        return bookMorphSentence;
    }

    private List<BookMorphWord> getBookMorphWords(Node originalMessage, String translation) throws ElanException {
        try {
            List<Node> allWords = xmlElanUtils.getFonWordsByReference(originalMessage);

            List<BookMorphWord> bookMorphWords = new ArrayList<BookMorphWord>();
            for (Node word : allWords) {
                BookMorphWord bookMorphWord = parseSingleWord(word, translation, false);
                if (bookMorphWord != null) {
                    bookMorphWords.add(bookMorphWord);
                }
            }
            return bookMorphWords;


        } catch (ElanException e) {
            throw new ElanException(String.format("Error: Bad file: cannot parse morphology: %s", e.getMessage()));
        }
    }

    private List<BookMorphWord> getBookMorphWordsTwoSpeakers(Node originalMessage, String translation) throws ElanException {
        try {
            List<Node> allWords = xmlElanUtils.getFonWordsByReferenceTwoSpeakers(originalMessage);

            List<BookMorphWord> bookMorphWords = new ArrayList<BookMorphWord>();
            for (Node word : allWords) {
                BookMorphWord bookMorphWord = parseSingleWord(word, translation, true);
                if (bookMorphWord != null) {
                    bookMorphWords.add(bookMorphWord);
                }
            }
            return bookMorphWords;


        } catch (ElanException e) {
            throw new ElanException(String.format("Error: Bad file: cannot parse morphology: %s", e.getMessage()));
        }
    }

    private List<BookMorphWord> getOldBookMorphWords(int currentNum, List<String> allWords, List<String> allGlosses, String translation) throws ElanException {
        try {
            System.out.println(translation);
            List<BookMorphWord> bookMorphWords = new ArrayList<BookMorphWord>();
            String words[] = allWords.get(currentNum).split(" ");
            String glosses[] = allGlosses.get(currentNum).split(" ");


            if (words.length != glosses.length) {
                throw new ElanException(String.format("Bad file: different number of words and glosses:%s %s %s", allWords.get(currentNum), Arrays.toString(words), Arrays.toString(glosses)));
            }


            for (int j = 0; j < words.length; ++j) {

                BookMorphWord bookMorphWord = new BookMorphWord();


                bookMorphWord.setWord(words[j].trim());
                bookMorphWord.setFon(words[j].trim());
                bookMorphWord.setGl(glosses[j].trim());

                bookMorphWords.add(bookMorphWord);

            }
            return bookMorphWords;


        } catch (ElanException e) {
            throw new ElanException(String.format("Error: Bad file: cannot parse morphology: %s", e.getMessage()));
        }
    }

    private BookMorphWord parseSingleWord(Node word, String translation, boolean hasTwoSpeakers) throws ElanException {
        BookMorphWord bookMorphWord = new BookMorphWord();
        System.out.println(translation);
        if (word.getTextContent().trim().equals(".")) {
            return null;
        }

        List<Node> morphemes;
        if (hasTwoSpeakers) {
            morphemes = xmlElanUtils.getMorphemesByReferenceTwoSpeakers(word);
        } else {
            morphemes = xmlElanUtils.getMorphemesByReference(word);
        }

        StringBuffer curWordMorphemes = new StringBuffer();
        StringBuffer curWordGlosses = new StringBuffer();
        for (int i = 0; i < morphemes.size(); ++i) {

            Node morpheme = morphemes.get(i);

            Node gloss;


            if (hasTwoSpeakers) {
                gloss = xmlElanUtils.getGlossByReferenceTwoSpeakers(morpheme);
            } else {
                gloss = xmlElanUtils.getGlossByReference(morpheme);
            }

            if (gloss == null) {
                throw new ElanException(String.format("Bad file: no gloss for morpheme %s (the sentence is %s)", morpheme.getTextContent(), translation));
            }

            String curDelimiter = " ";
            if (morphemes.size() == 1) {
                curDelimiter = "";
            }
            if ((i > 0) && (curWordMorphemes.indexOf("-") >= 0 || morpheme.getTextContent().contains("-"))) {
                curDelimiter = "-";
            } else if ((i > 0) && (curWordMorphemes.indexOf("-") >= 0 || morpheme.getTextContent().contains("="))) {
                curDelimiter = "=";
            }

            curWordGlosses.append(curDelimiter).append(gloss.getTextContent().trim());

            curWordMorphemes.append(curDelimiter).append(morpheme.getTextContent().trim().replaceAll("-", ""));

        }
        bookMorphWord.setWord(word.getTextContent().trim());
        bookMorphWord.setFon(curWordMorphemes.toString().replaceAll("[-=]=", "="));
        bookMorphWord.setGl(curWordGlosses.toString().replaceAll("[-=]=", "="));
        return bookMorphWord;
    }

    boolean getFirstLineInfo(String originalName) {
        return originalName.equals("ket");
    }
}
