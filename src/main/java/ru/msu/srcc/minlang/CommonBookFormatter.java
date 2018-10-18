package ru.msu.srcc.minlang;

import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.math.BigInteger;

/**
 * Created by User on 28.02.2015.
 */
public class CommonBookFormatter {
    protected static final String SENTENCE_STYLE = "ML-GlossSentence";
    protected static final String INTERLINEAR_STYLE = "ML-GlossInterlinear";
    protected static final String GLOSS_SPACER_STYLE = "ML-GlossSpacer";
    protected static final String TEMPLATE_FILENAME = "C:\\Users\\User\\workspace\\ElanUtils\\src\\main\\resources\\IR_book2011_big_word2007.docx";

    protected static final int MAX_CHAR_PER_ROW = 70;
    protected static final int MAX_CHAR_PER_PAGE =  2100;
    protected static final int ROW_NUM = 3;



    protected org.docx4j.wml.ObjectFactory factory;
    protected PPrBase.Spacing spacing;

    public CommonBookFormatter(){
        prepareStyles();
    }

    protected void prepareStyles(){
        factory = Context.getWmlObjectFactory();
        spacing = factory.createPPrBaseSpacing();
        spacing.setAfter(BigInteger.ZERO);
        spacing.setBefore(BigInteger.ZERO);
    }

    protected PPr createParagraphProperties(String style){
        PPr paragraphProperties = factory.createPPr();
        paragraphProperties.setSpacing(spacing);
        PPrBase.PStyle parStyle = factory.createPPrBasePStyle();
        parStyle.setVal(style);
        paragraphProperties.setPStyle(parStyle);
        return paragraphProperties;
    }

    protected  WordprocessingMLPackage getTemplate()
            throws Docx4JException, FileNotFoundException {
        WordprocessingMLPackage template = WordprocessingMLPackage
                .load(new FileInputStream(new File(TEMPLATE_FILENAME)));
        return template;
    }


    protected class TblCreationResult{
        private Tbl tb;
        private int maxCharactersLeft;
        private int lastSentenceNum;

        public Tbl getTb() {
            return tb;
        }

        public void setTb(Tbl tb) {
            this.tb = tb;
        }

        public int getMaxCharactersLeft() {
            return maxCharactersLeft;
        }

        public void setMaxCharactersLeft(int maxCharactersLeft) {
            this.maxCharactersLeft = maxCharactersLeft;
        }

        public int getLastSentenceNum() {
            return lastSentenceNum;
        }

        public void setLastSentenceNum(int lastSentenceNum) {
            this.lastSentenceNum = lastSentenceNum;
        }
    }

    protected Tbl createNewTable(){
        Tbl tbl = factory.createTbl();
        TblWidth tblW = Context.getWmlObjectFactory().createTblWidth();
        tblW.setW(BigInteger.ZERO);
        tblW.setType(TblWidth.TYPE_AUTO);
        TblPr tblPr = Context.getWmlObjectFactory().createTblPr();
        tbl.setTblPr(tblPr);
        tblPr.setTblW(tblW);
        return tbl;
    }

    protected Tr[] createRows(int rowNum){
        ObjectFactory factory = Context.getWmlObjectFactory();
        Tr rows[] = new Tr[rowNum];
        for(int k = 0; k<rowNum; ++k){
            rows[k] = factory.createTr();
        }
        return rows;
    }

    protected void updateTableRows(Tbl tbl, Tr[]rows, boolean isNewTable, MainDocumentPart mainDocumentPart){


        if(isNewTable) {
            for(Tr row : rows){
                tbl.getContent().add(row);
            }
            mainDocumentPart.addObject(tbl);
        }
        addGlossSpacer(mainDocumentPart);
    }



    protected P createTextParagraphWithStyle(String text, String style, MainDocumentPart mainDocumentPart){
        P par = mainDocumentPart.createParagraphOfText(text.trim());
        par.setPPr(createParagraphProperties(style));
        return par;
    }

    protected void addGlossSpacer(MainDocumentPart mainDocumentPart) {
        P spacerPar = createTextParagraphWithStyle("", GLOSS_SPACER_STYLE, mainDocumentPart);
        mainDocumentPart.addObject(spacerPar);
    }

    protected P createSentenceParagraph(String text, MainDocumentPart mainDocumentPart){
        return createTextParagraphWithStyle(text, SENTENCE_STYLE, mainDocumentPart);
    }

    protected Tc createCellWithText(String text, String style, MainDocumentPart mainDocumentPart){
        ObjectFactory factory = Context.getWmlObjectFactory();
        Tc cell = factory.createTc();
        cell.getContent().add(createTextParagraphWithStyle(text, style, mainDocumentPart));
        return cell;
    }
}
