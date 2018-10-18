 package ru.msu.srcc.minlang;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerConfigurationException;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathExpressionException;
 import javax.xml.xpath.XPathFactory;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 public class AddTransliteration
 {
   private final String TIER = "TIER";
   private final String TIER_ID = "TIER_ID";
   private final String PARENT_REF = "PARENT_REF";
   private final String ref1 = "TIME_SLOT_REF1";
   private final String ref2 = "TIME_SLOT_REF2";
   private final String anIdSIL = "ANNOTATION_ID";
   private final String anRefSIL = "ANNOTATION_REF";
   private final String ANNOTATION_ID = "ANNOTATION_ID";
   private List<String> tierNames;
   private Document doc;
   private NodeList allTiers;
   private String oldFileName;


  private XMLFormatter xmlFormatter = new XMLFormatter();
  private EvenkiConverter evenkiConverter = new EvenkiConverter();

 
   public File addTransliteration(String tierToTransliterate, String newTierName, boolean isSil)
   {
     File newFile = null;

     xmlFormatter.setAllTiers(allTiers);
 
     for (int i = 0; i < this.allTiers.getLength(); i++) {
       Node node = this.allTiers.item(i);
       NamedNodeMap attributes = node.getAttributes();
       Node nameAttrib = attributes.getNamedItem("TIER_ID");
       if (nameAttrib.getNodeValue().equals(tierToTransliterate)) {
           String currentTierType = attributes.getNamedItem("LINGUISTIC_TYPE_REF").getNodeValue();
           Element newTier = createNewTier(doc, newTierName, currentTierType);
           addNewTier(doc, newTier);
           try {
           convert(doc, newTier, isSil);

           newFile = saveFile(this.doc, this.oldFileName);
         }
         catch (Exception e) {
             System.out.println(e.getMessage());
         }
       }
     }
     return newFile;
   }

            public NodeList getAllTiers(){
                return allTiers;
            }

    public Document getDoc() {
        return doc;
    }

       public boolean readFile(File file) {
       this.oldFileName = file.getAbsolutePath();

     createDoc(file);
     if (this.doc == null) return false;
     readTierNames();

     return this.allTiers != null;
   }
   public List<String> getTierNames() {
     if (this.allTiers == null) return null;
     List tierNames = new ArrayList();
     for (int i = 0; i < this.allTiers.getLength(); i++) {
       Node node = this.allTiers.item(i);
       NamedNodeMap attributes = node.getAttributes();
       Node nameAttrib = attributes.getNamedItem("TIER_ID");
       tierNames.add(nameAttrib.getTextContent());
     }
     return tierNames;
   }
   private void readTierNames() {
     XPath xpath = XPathFactory.newInstance().newXPath();
     this.allTiers = null;
     try {
       this.allTiers = ((NodeList)xpath.evaluate("//TIER", this.doc, XPathConstants.NODESET));
     }
     catch (XPathExpressionException xpe) {
       System.out.println(xpe);
     }
   }
 
   private void createDoc(File file) { DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
     f.setValidating(false);
     this.doc = null;
     this.oldFileName = file.getAbsolutePath();
     try {
       DocumentBuilder builder = f.newDocumentBuilder();
       this.doc = builder.parse(file);
     }
     catch (SAXException sae) {
       System.out.println("SAXException:" + sae.getMessage());
     }
     catch (ParserConfigurationException pce) {
       System.out.println("ParserConfigurationException:" + pce.getMessage());
     }
     catch (IOException ioe) {
       System.out.println("IOException:" + ioe.getMessage());
     } }
 
   private void addNewTier(Document doc, Element newTier) {
     doc.getFirstChild().appendChild(newTier);
   }

   private Element createNewTier(Document doc, String newTierName, String newTierType){
       Element newTier = doc.createElement("TIER");
       newTier.setAttribute("TIER_ID", newTierName);
       newTier.setAttribute("DEFAULT_LOCALE", "ru");
       newTier.setAttribute("LINGUISTIC_TYPE_REF", newTierType);
       return newTier;
   }
 
   private File saveFile(Document doc, String oldFileName) {
     TransformerFactory transformerFactory = TransformerFactory.newInstance();
     File newFile = null;
     try {
       Transformer transformer = transformerFactory.newTransformer();
       DOMSource source = new DOMSource(doc);
       newFile = new File(oldFileName.split("\\.eaf")[0] + "_transliterated.eaf");
       StreamResult result = new StreamResult(newFile);
       transformer.transform(source, result);
 
       return newFile;
     }
     catch (TransformerConfigurationException tce)
     {
       tce = 
         tce;
 
       System.out.println(tce.getMessage());
 
       return newFile;
     }
     catch (TransformerException te)
     {
       te = 
         te;
 
       System.out.println(te.getMessage());
 
       return newFile; }
   }




     private void convert(Document doc, Element newTier, boolean isSil) throws XPathExpressionException {

         int tsId = 0;
         if (isSil) {

             List<Node>russianNodes = xmlFormatter.getSILRussianNodes();

             int sentNum = russianNodes.size();

             for (int i = 0; i < sentNum; ++i) {
                 List<Node> allWords = xmlFormatter.getFonWordsByReference(xmlFormatter.getAnnotationId(russianNodes.get(i)));
                 String sentenceToConvert = "";
                 for (Node word : allWords) {
                     sentenceToConvert += " " + word.getTextContent().trim();
                 }
                 String sentenceConverted = capitalize(evenkiConverter.convert(sentenceToConvert.trim())) + ".";
                 System.out.println(sentenceConverted) ;

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
         }
             else{
                List<Node>fonNodes = xmlFormatter.getOldFonNodes();
                int sentNum = fonNodes.size();

             for (int i = 0; i < sentNum; ++i) {

                 String fonValue =  fonNodes.get(i).getTextContent();



                 String sentenceConverted = capitalize(evenkiConverter.convert(fonValue.trim())) + ".";


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

             }
     }

     //TODO: remove this old trash!
   private void oldConvert(Document doc, Node node, boolean isSil) {
     XPath xpath = XPathFactory.newInstance().newXPath();
     String annotationExpression = "./ANNOTATION";
     String expression = ".//ANNOTATION_VALUE";
     try {
       NodeList nodeSet = (NodeList)xpath.evaluate(annotationExpression, node, XPathConstants.NODESET);
       Node newNode = doc.getFirstChild().getLastChild();
 

       String currentParent = null;
       String currentOriginal = "";
       String prevId = null;
       Node annotation = null;
       String newId = null;
       String curChildName = null;
       int tsId = 0;
       for (int i = 0; i < nodeSet.getLength(); i++) {





         annotation = nodeSet.item(i);
         Node annotationValue = (Node)xpath.evaluate(expression, annotation, XPathConstants.NODE);
         String original = annotationValue.getTextContent();
 
         curChildName = "ALIGNABLE_ANNOTATION";
				  Node oldChild = (Node)xpath.evaluate("./" + curChildName, annotation, XPathConstants.NODE);
         if (isSil || oldChild == null) {
           curChildName = "REF_ANNOTATION";
         }
 
         oldChild = (Node)xpath.evaluate("./" + curChildName, annotation, XPathConstants.NODE);
 
         NamedNodeMap attributes = oldChild.getAttributes();
         newId = attributes.getNamedItem("ANNOTATION_ID").getNodeValue().replace("a", "b");

           Node newAnnotation = annotation.cloneNode(false);
           Element newAnnotationValue = doc.createElement("ANNOTATION_VALUE");
           Element newAlignableAnnotation = doc.createElement(curChildName);
 
           newAnnotationValue.setTextContent(capitalize(evenkiConverter.convert(original))+".");

					if(attributes.getNamedItem("TIME_SLOT_REF1")!=null){
						newAlignableAnnotation.setAttribute("TIME_SLOT_REF1", attributes.getNamedItem("TIME_SLOT_REF1").getNodeValue());
					}
					if(attributes.getNamedItem("TIME_SLOT_REF2")!=null){
						newAlignableAnnotation.setAttribute("TIME_SLOT_REF2", attributes.getNamedItem("TIME_SLOT_REF2").getNodeValue());
					}
           
 
          
 
           newAlignableAnnotation.setAttribute("ANNOTATION_ID", newId);
           newAlignableAnnotation.appendChild(newAnnotationValue);
           newAnnotation.appendChild(newAlignableAnnotation);
           newNode.appendChild(newAnnotation);
         }
        /* else {
           String newParent = attributes.getNamedItem("ANNOTATION_REF").getNodeValue();
           if ((currentParent != null) && (!newParent.equals(currentParent))) {
             Node newAnnotation = annotation.cloneNode(false);
             Element newAnnotationValue = doc.createElement("ANNOTATION_VALUE");
             Element newAlignableAnnotation = doc.createElement(curChildName);
                      currentOriginal=currentOriginal.trim().replaceAll(" *- *","");
             newAnnotationValue.setTextContent(capitalize(evenkiConverter.convert(currentOriginal))+".");
 
             newAlignableAnnotation.setAttribute("ANNOTATION_ID", newId);
             newAlignableAnnotation.appendChild(newAnnotationValue);
             newAnnotation.appendChild(newAlignableAnnotation);

               tsId += 1;
             newAlignableAnnotation.setAttribute("TIME_SLOT_REF1", "ts" + tsId);
               tsId += 1;
             newAlignableAnnotation.setAttribute("TIME_SLOT_REF2", "ts" + tsId);
 
             //newAlignableAnnotation.setAttribute("ANNOTATION_REF", currentParent);
 
             newNode.appendChild(newAnnotation);
             currentOriginal = original;
           }
           else
           {
             currentOriginal = currentOriginal + " " + original;
           }
           currentParent = newParent;
         }
 
       }       */
 
       if ((isSil) && 
         (currentParent != null)) {
         Node newAnnotation = annotation.cloneNode(false);
         Element newAnnotationValue = doc.createElement("ANNOTATION_VALUE");
         Element newAlignableAnnotation = doc.createElement(curChildName);
         newAnnotationValue.setTextContent(evenkiConverter.convert(currentOriginal));
 
         newAlignableAnnotation.setAttribute("ANNOTATION_ID", newId);
         newAlignableAnnotation.appendChild(newAnnotationValue);
         newAnnotation.appendChild(newAlignableAnnotation);
 
         //newAlignableAnnotation.setAttribute("ANNOTATION_REF", currentParent);
 
         newNode.appendChild(newAnnotation);
       }
     }
     catch (XPathExpressionException xpe)
     {
       System.out.println(xpe.getMessage());
     }
   }
 
   private String capitalize(String sentence) {  
                if(sentence.length()<1){
                    return sentence;
                }
    return sentence.substring(0, 1).toUpperCase() + sentence.substring(1); }
 
 
   public static void main(String[] args)
   {
   }
 }

/* Location:           E:\ELAN2011\ElanUtils\ElanUtils\out\production\ElanUtils\
 * Qualified Name:     AddTransliteration
 * JD-Core Version:    0.6.2
 */