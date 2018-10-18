package ru.msu.srcc.minlang;

import org.docx4j.Docx4J;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.*;

import java.io.FileNotFoundException;
import java.util.List;

/**
 * Created by User on 17.02.2015.
 */
public class BookFormatter extends CommonBookFormatter{


    private WordprocessingMLPackage wordMLPackage;
    private MainDocumentPart mainDocumentPart;





    public void formatForBook(BookFormat textContent, String outputFilename, boolean isToAddFirstLine) throws Docx4JException, FileNotFoundException {
        wordMLPackage = getTemplate();
        mainDocumentPart = wordMLPackage.getMainDocumentPart();
        addTextContentToDocument(textContent, isToAddFirstLine);
        // Save it
        wordMLPackage.save(new java.io.File(outputFilename), Docx4J.FLAG_SAVE_ZIP_FILE);
    }

    private  void addTextContentToDocument(BookFormat textContent, boolean isToAddFirstLine){
        int numOfSentences = textContent.getMorphSentences().size();
        //int numOfPages = (int)Math.ceil(numOfSentences / (double) NUM_OF_SENTENCES_PER_PAGE);
        int curStart = 0;
        int curEnd = 0;
        int totalSentenceNum = textContent.getRussianSentences().size();
        while(curEnd < totalSentenceNum){
            int numberOfSentencesForPage = countNumberOfSentences(textContent, curStart);
            System.out.println(numberOfSentencesForPage);

            curEnd = curStart + numberOfSentencesForPage;


            addOriginalText(textContent, curStart, curEnd);
            addEmptyPar();
            addRussianText(textContent, curStart, curEnd);
            addBreak();
            addEmptyPar();
            addMorphText(textContent, curStart, curEnd, isToAddFirstLine);
            addBreak();
            curStart = curEnd;
        }
    }

    private int countNumberOfSentences(BookFormat textContent, int curStart){
       List<BookMorphSentence> subList = textContent.getMorphSentences().subList(curStart, textContent.getMorphSentences().size());
       int numOfCharacters = 0;
       int numOfSentences = 0;
       for(BookMorphSentence morphSentence : subList){
           if(numOfCharacters >= MAX_CHAR_PER_PAGE){
               return numOfSentences;
           }
           for(BookMorphWord word : morphSentence.getWords()) {
               numOfCharacters += word.getFon().length();
               numOfCharacters += word.getGl().length();
               numOfCharacters += word.getWord().length();
           }
           numOfSentences += 1;
       }

       return numOfSentences;
    }

    private  void addRussianText(BookFormat textContent, int from, int to){
        addSentences(textContent.getRussianSentences(), from, to);
    }

    private  void addOriginalText(BookFormat textContent, int from, int to){
        addSentences(textContent.getOriginalSentences(), from, to);
    }   
    
    private  void addMorphText(BookFormat textContent, int from, int to, boolean isToAddFirstLine) {
        int minNum = Math.min(to, textContent.getMorphSentences().size());
        Tbl tableToUse = null;
        int charactersLeft = 0;
        for(int i = from; i < minNum; ++i){
            BookMorphSentence sentence = textContent.getMorphSentences().get(i);
            TblCreationResult result = createSentenceTable(i, sentence, tableToUse, charactersLeft, isToAddFirstLine);
            tableToUse = result.getTb();
            charactersLeft = result.getMaxCharactersLeft();
        }
    }

    private TblCreationResult createSentenceTable(int index, BookMorphSentence sentence, Tbl tbl, int charactersLeft, boolean isToAddFirstLine){
        int wordNum = sentence.getWords().size();
        int rowNum = calculateRowNum(isToAddFirstLine);
        Tr rows[]= new Tr[rowNum];
        TblCreationResult result = new TblCreationResult();

        boolean isNewTable = false;
        if(tbl == null){
            tbl = createNewTable();
            isNewTable = true;
            rows = createRows(rowNum);
            charactersLeft = MAX_CHAR_PER_ROW;
        }
        else{
            List<Object>rowContent = tbl.getContent();
            for(int i = 0; i<rowNum; ++i){
                rows[i] = (Tr)rowContent.get(i);
            }
        }

        boolean isPrevNumber = false;
        for(int i = 0; i< wordNum; ++i){
            if(i == 0){
                isPrevNumber = true;
            }
            BookMorphWord curWord = sentence.getWords().get(i);
            int maxLength = Math.max(Math.max(curWord.getFon().length(), curWord.getWord().length()),curWord.getGl().length());
            if(maxLength > charactersLeft){
              updateTableRows(tbl, rows, isNewTable, mainDocumentPart);
              tbl = createNewTable();
              rows = createRows(rowNum);
              isNewTable = true;
              charactersLeft = MAX_CHAR_PER_ROW;
            }
            charactersLeft -= maxLength;
            if(isPrevNumber){
                if(isToAddFirstLine){
                    addNumberTimeToTable(index, sentence, rows);
                }
                else{
                    addNumberTimeToTableTwoLines(index, sentence, rows);
                }

               charactersLeft-=5;
            }
            if(isToAddFirstLine){
                rows[0].getContent().add(createCellWithText(curWord.getWord(), SENTENCE_STYLE, mainDocumentPart));
                rows[1].getContent().add(createCellWithText(curWord.getFon(), SENTENCE_STYLE, mainDocumentPart));
                rows[2].getContent().add(createCellWithText(curWord.getGl(), INTERLINEAR_STYLE, mainDocumentPart));
            }
            else{
                rows[0].getContent().add(createCellWithText(curWord.getFon(), SENTENCE_STYLE, mainDocumentPart));
                rows[1].getContent().add(createCellWithText(curWord.getGl(), INTERLINEAR_STYLE, mainDocumentPart));
            }

            isPrevNumber = false;
        }
        result.setTb(tbl);
        result.setMaxCharactersLeft(charactersLeft);
        updateTableRows(tbl, rows, isNewTable, mainDocumentPart);
        return result;

    }





    private void addNumberTimeToTable(int index, BookMorphSentence sentence, Tr rows[]){
        //TODO: refactor
        ObjectFactory factory = Context.getWmlObjectFactory();
        Tc cell1 = factory.createTc();
        Tc cell2 = factory.createTc();
        Tc cell3 = factory.createTc();
        cell1.getContent().add(createSentenceParagraph( (index + 1) + ".",  mainDocumentPart));
        cell2.getContent().add(createSentenceParagraph("", mainDocumentPart));
        cell3.getContent().add(createGlossParagraph(getTimeValue(sentence)));
        rows[0].getContent().add(cell1);
        rows[1].getContent().add(cell2);
        rows[2].getContent().add(cell3);
    }

    private void addNumberTimeToTableTwoLines(int index, BookMorphSentence sentence, Tr rows[]){
        //TODO: refactor
        ObjectFactory factory = Context.getWmlObjectFactory();
        Tc cell1 = factory.createTc();

        Tc cell2 = factory.createTc();
        cell1.getContent().add(createSentenceParagraph( (index + 1) + ".",  mainDocumentPart));
        cell2.getContent().add(createGlossParagraph(getTimeValue(sentence)));
        rows[0].getContent().add(cell1);
        rows[1].getContent().add(cell2);
    }



    private  void addEmptyPar(){
        mainDocumentPart.addStyledParagraphOfText(SENTENCE_STYLE, "");
    }





    private P createGlossParagraph(String text){
        return createTextParagraphWithStyle(text, INTERLINEAR_STYLE);
    }

    private P createTextParagraphWithStyle(String text, String style){
        P par = mainDocumentPart.createParagraphOfText(text.trim());
        par.setPPr(createParagraphProperties(style));
        return par;
    }

    private  void addBreak(){
        Br objBr = new Br();
        objBr.setType(STBrType.PAGE);
        org.docx4j.wml.ObjectFactory factory = Context.getWmlObjectFactory();

        P para = factory.createP();
        para.getContent().add(objBr);

        mainDocumentPart.addObject(para);
    }

    private  void addSentences(List<String> sentences, int from, int to){
        StringBuilder russianText = new StringBuilder();
        int minNum = Math.min(to, sentences.size());
        for(int index = from; index < minNum; ++index){
            russianText.append(formatIndex(index) + " " + sentences.get(index).trim() + " ");
        }
        mainDocumentPart.addStyledParagraphOfText(SENTENCE_STYLE, russianText.toString());
    }

    private  String formatIndex(int index){
        return "(" +(index + 1) + ")";
    }


    private String getTimeValue(BookMorphSentence sentence){
        return getMinSec(sentence.getBegTime());
    }

    private  String getMinSec(long ms){
        long minSecMs =  ms%3600000;
        long min = minSecMs/60000;
        long sec =  (minSecMs%60000)/1000;
        return getTimeValueFromLong(min) + ":" + getTimeValueFromLong(sec);
    }

    private  String getTimeValueFromLong(long timeValue){
        String timeStr = String.valueOf(timeValue);
        if(timeStr.length()==1){
            timeStr = "0" + timeStr;
        }
        return timeStr;
    }

    private int calculateRowNum(boolean isToAddFirstLine){
        if(isToAddFirstLine){
            return 3;
        }
        return 2;
    }


}
