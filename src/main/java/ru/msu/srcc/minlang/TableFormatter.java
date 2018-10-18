package ru.msu.srcc.minlang;


import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.FootnotesPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.wml.*;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Created by User on 28.02.2015.
 */
public class TableFormatter extends CommonBookFormatter {

    private static final String TEMPLATE_FILENAME = "D://ForElan//Book//IR_book2011_big_word2007.dotx";

    private org.docx4j.wml.ObjectFactory factory;
    private PPrBase.Spacing spacing;

    public void formatTablesFromFile(String filename) throws Docx4JException, JAXBException, FileNotFoundException, UnsupportedEncodingException {
        String outputFilename = filename + "_aligned.doc";
        copyTables(filename, outputFilename);

    }

    private void copyTables(String inputFilename, String outputFilename) throws Docx4JException, JAXBException, FileNotFoundException, UnsupportedEncodingException {
        WordprocessingMLPackage inWordMLPackage = WordprocessingMLPackage.load(new File(inputFilename));
        MainDocumentPart inDocumentPart = inWordMLPackage.getMainDocumentPart();

        PrintWriter writer = new PrintWriter("D:\\out.xml", "UTF-8");

        writer.println(XmlUtils.marshaltoString(inDocumentPart.getJaxbElement(), true, true));
        writer.close();



        WordprocessingMLPackage outWordMLPackage = getTemplate();
        MainDocumentPart outMainDocumentPart = outWordMLPackage.getMainDocumentPart();

        Tbl tableToUse = null;

        int lastSentenceNum = 0;
        int charactersLeft = MAX_CHAR_PER_ROW;

        //int fullWidth = outWordMLPackage.getDocumentModel().getSections().get(0).getPageDimensions().getWritableWidthTwips();


        List<Object> listOfTables = inDocumentPart.getJAXBNodesViaXPath("//*[local-name()='tbl']", false);
        for(Object obj : listOfTables){
            Tbl tbl = (Tbl)((JAXBElement)obj).getValue();



            List<Object>rows = tbl.getContent();
            if(rows!=null && !rows.isEmpty()){
                TblCreationResult result = copyRows(rows, outMainDocumentPart, tableToUse, lastSentenceNum, charactersLeft);
                tableToUse = result.getTb();
                charactersLeft = result.getMaxCharactersLeft();
                if(result.getLastSentenceNum() != 0){
                    lastSentenceNum = result.getLastSentenceNum();
                }
            }

        }

        RelationshipsPart rp = inDocumentPart.getRelationshipsPart();

        RelationshipsPart outRp = outMainDocumentPart.getRelationshipsPart();

        String headerType = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/header";
        String footerType = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/footer";
        String stylesType =  "http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles";
        String themeType =  "http://schemas.openxmlformats.org/officeDocument/2006/relationships/theme";




        FootnotesPart footnotesPart = inDocumentPart.getFootnotesPart();
        FootnotesPart outFootnotesPart = outMainDocumentPart.getFootnotesPart();



        CTFootnotes footnotes = footnotesPart.getContents();
        outFootnotesPart.setContents(footnotes);

        //TODO: normal style for footnotes


        Set<String> allTypes  = new HashSet<String>();

       /* for(int i = 0; i < rp.getRelationships().getRelationship().size(); ++i ){
            System.out.println(i);
            System.out.println(rp.getRelationships().getRelationship().get(i).getType() + ":" + rp.getRelationships().getRelationship().get(i).getTargetMode());
            System.out.println(outRp.getRelationships().getRelationship().get(i).getType() + ":" + outRp.getRelationships().getRelationship().get(i).getTargetMode());
        }   */



        /*for(String type:allTypes){
            outRp.removeRelationshipsByType(type);
        }


        for (Relationship r : rp.getRelationships().getRelationship()) {
            System.out.println(r.getType());
            outRp.addRelationship(r);

        }    */



        outWordMLPackage.save(new java.io.File(outputFilename) );
    }




    private TblCreationResult copyRows(List<Object>rows, MainDocumentPart outMainDocumentPart, Tbl tbl, int lastSentenceNum, int charactersLeft){
        Tr row1 = (Tr)rows.get(0);
        Tr row2 = (Tr)rows.get(1);
        Tr row3 = (Tr)rows.get(2);


        Tr newRows[]= new Tr[3];
        TblCreationResult result = new TblCreationResult();



        boolean isNewTable = false;
        if(tbl == null){
            tbl = createNewTable();
            isNewTable = true;
            newRows = createRows(ROW_NUM);
            charactersLeft = MAX_CHAR_PER_ROW;
        }
        else{
            List<Object>rowContent = tbl.getContent();
            for(int i = 0; i<ROW_NUM; ++i){
                newRows[i] = (Tr)rowContent.get(i);
            }
        }



        boolean isPrevNumber = false;

        Tc savedCell1 = null;
        Tc savedCell2 = null;
        Tc savedCell3 = null;

        int numOfColumns = row1.getContent().size();
        for(int i = 0; i < numOfColumns; ++i){
            //TODO: refactor?

            Tc cell1 = (Tc)((JAXBElement)(row1.getContent().get(i))).getValue();
            Tc cell2 = (Tc)((JAXBElement)(row2.getContent().get(i))).getValue();
            Tc cell3 = (Tc)((JAXBElement)(row3.getContent().get(i))).getValue();

            Object content1 =  cell1.getContent();
            Object content2 =  cell2.getContent();
            Object content3 =  cell3.getContent();

            String cell1Text = content1.toString();
            String cell2Text = content2.toString();
            String cell3Text = content3.toString();

            if(cell1Text.contains("owilʼdʼen")){
                System.out.println("aaaa");
            }

            int maxLength = Math.max(Math.max(cell1Text.length(), cell2Text.length()), cell3Text.length());




            if(maxLength > charactersLeft){
                updateTableRows(tbl, newRows, isNewTable, outMainDocumentPart);
                tbl = createNewTable();
                newRows = createRows(ROW_NUM);
                isNewTable = true;
                charactersLeft = MAX_CHAR_PER_ROW;
            }
            charactersLeft -= maxLength;

            Pattern pattern = Pattern.compile("\\d\\.");

            if(isPrevNumber){
                newRows[0].getContent().add(savedCell1);
                newRows[1].getContent().add(savedCell2);
                newRows[2].getContent().add(savedCell3);
                isPrevNumber = false;
            }

            savedCell1 = createCellWithContent(content1);
            savedCell2 = createCellWithContent(content2);
            savedCell3 = createCellWithContent(content3);



            if(pattern.matcher(cell1Text).find()){
                //setSentenceNum(savedCell1, lastSentenceNum);
                lastSentenceNum += 1;
                result.setLastSentenceNum(lastSentenceNum);

                charactersLeft -= 4;
                isPrevNumber = true;
            }
            else{
                newRows[0].getContent().add(savedCell1);
                newRows[1].getContent().add(savedCell2);
                newRows[2].getContent().add(savedCell3);
            }







        }

        result.setTb(tbl);
        result.setMaxCharactersLeft(charactersLeft);
        updateTableRows(tbl, newRows, isNewTable, outMainDocumentPart);
        return result;
    }


    private Tc createCellWithContent(Object content){
        ObjectFactory factory = Context.getWmlObjectFactory();
        Tc cell = factory.createTc();

        ArrayList contentAsList = (ArrayList) content;
        for(Object obj : contentAsList){

            cell.getContent().add(obj);

            if(obj instanceof P){
                P par = (P) obj;

            }
        }


        return cell;
    }

    private void setSentenceNum(Tc cell, int lastSentenceNum){
        boolean hasChanged = false;
        for(Object obj: cell.getContent()){
            if(obj instanceof P){
                P objAsPar = (P) obj;
                for(Object subObj:objAsPar.getContent()){

                    if(subObj instanceof R){

                        R subObjAsR = (R) subObj;

                        for(Object subSubObj:subObjAsR.getContent()){

                            if(lastSentenceNum == 7){
                                int aaa = 9;
                            }


                            if(subSubObj instanceof JAXBElement){
                                if(((JAXBElement) subSubObj).getValue() instanceof Text) {
                                    Text value = (Text)((JAXBElement) subSubObj).getValue();
                                    if(!hasChanged){
                                        value.setValue((lastSentenceNum + 1) + ".");
                                        hasChanged = true;
                                    }
                                    else{
                                        value.setValue("");
                                    }




                                }
                            }
                        }
                    }
                }
            }
        }
    }








    public static void main(String args[]) {
        try {
            new TableFormatter().formatTablesFromFile("D://ForElan//Book//KetTexts//KetTexts_2015//Котусова_ML.docx");
        } catch (Docx4JException e) {
            e.printStackTrace();
        } catch (JAXBException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}
