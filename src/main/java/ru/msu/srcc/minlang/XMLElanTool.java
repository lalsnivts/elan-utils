/*     */ package ru.msu.srcc.minlang;
/*     */ 
/*     */ import java.awt.BorderLayout;
/*     */
/*     */ import java.awt.Cursor;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.awt.event.MouseAdapter;
/*     */ import java.awt.event.MouseEvent;
/*     */ import java.io.BufferedReader;
/*     */ import java.io.File;
/*     */ import java.io.FileInputStream;
/*     */ import java.io.FileNotFoundException;
/*     */ import java.io.IOException;
/*     */ import java.io.InputStreamReader;
/*     */ import java.util.List;
/*     */ import javax.swing.BoxLayout;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JCheckBox;
/*     */ import javax.swing.JEditorPane;
/*     */ import javax.swing.JFileChooser;
/*     */ import javax.swing.JFrame;
/*     */ import javax.swing.JList;
/*     */ import javax.swing.JOptionPane;
/*     */ import javax.swing.JPanel;
/*     */ import javax.swing.JScrollPane;
/*     */ import javax.swing.JTextField;
/*     */ import javax.swing.text.html.HTMLEditorKit;
/*     */ 
/*     */ public class XMLElanTool
/*     */ {
/*     */   private JFileChooser fc;
/*  22 */   private JEditorPane[] textAreas = new JEditorPane[2];
/*     */   private JPanel textPanel;
/*     */   private JFrame myFrame;
/*     */   private JList allTiersList;
/*     */   private List<String> allTiersListValue;
/*     */   private JTextField newTierName;
/*  29 */   private AddTransliteration addTransliteration = new AddTransliteration();
            private XMLFormatter xmlFormatter = new XMLFormatter();
/*     */   private JButton addTransliterationButton;
            private JButton saveToHTMLFormat;
/*     */   private JCheckBox isSilCheckBox;
            private JCheckBox isTwoSpeakersCheckBox;
            private JCheckBox isArchive;
/*  32 */   private Cursor waitCursor = new Cursor(3);
/*  33 */   private Cursor defaultCursor = new Cursor(0);
/*     */   private static final String newTierNameDefault = "new tier name";
/*     */ 
/*     */   public XMLElanTool()
/*     */   {
/*  36 */     initializeMyFrame();
/*  37 */     initalizeControls();
/*  38 */     initalizeTextPane();
/*  39 */     this.fc = new JFileChooser("resources/");
/*     */ 
/*  43 */     this.myFrame.setVisible(true);
/*     */   }
/*     */   private void initializeMyFrame() {
/*  46 */     this.myFrame = new JFrame();
/*  47 */     this.myFrame.setDefaultCloseOperation(2);
/*  48 */     this.myFrame.setSize(500, 500);
/*  49 */     this.textPanel = new JPanel(new BorderLayout());
/*  50 */     this.myFrame.getContentPane().add(this.textPanel);
/*  51 */     this.allTiersList = new JList();
/*  52 */     this.newTierName = new JTextField();
/*  53 */     this.newTierName.setText("new tier name");
/*  54 */     this.newTierName.addMouseListener(new NewTierNameListener());
/*     */   }
/*     */   private void initalizeControls() {
/*  57 */     JButton openFileButton = new JButton("Open file");
/*  58 */     this.addTransliterationButton = new JButton("Add transliteration");
/*  59 */     this.addTransliterationButton.setEnabled(false);
/*  60 */     openFileButton.addActionListener(new OpenFileListener());
/*  61 */     this.addTransliterationButton.addActionListener(new AddTransliterationListener());

              this.saveToHTMLFormat = new JButton("Save to XML");
              this.saveToHTMLFormat.setEnabled(false);
              this.saveToHTMLFormat.addActionListener(new SaveToXMLListener());



/*  62 */     JPanel controls = new JPanel(new BorderLayout());
/*  63 */     controls.add(this.allTiersList, "West");
/*  64 */     JPanel buttonsAndTextField = new JPanel();
/*  65 */     buttonsAndTextField.setLayout(new BoxLayout(buttonsAndTextField, 0));
/*  66 */     buttonsAndTextField.add(openFileButton);
/*  67 */     buttonsAndTextField.add(this.addTransliterationButton);
              buttonsAndTextField.add(this.saveToHTMLFormat);
/*  68 */     buttonsAndTextField.add(this.newTierName);
/*  69 */     this.textPanel.add("North", controls);
/*  70 */     this.textPanel.add("South", buttonsAndTextField);
/*     */ 
/*  72 */     this.isSilCheckBox = new JCheckBox("is sil");
/*  73 */     buttonsAndTextField.add(this.isSilCheckBox);

              this.isTwoSpeakersCheckBox = new JCheckBox("two speakers");
/*  73 */     buttonsAndTextField.add(this.isTwoSpeakersCheckBox);


              isArchive = new JCheckBox("is archive");
/*  73 */     buttonsAndTextField.add(isArchive);
/*     */   }
/*     */ 
/*     */   private void initalizeTextPane() {
/*  77 */     for (int i = 0; i < 2; i++) {
/*  78 */       this.textAreas[i] = new JEditorPane();
/*  79 */       this.textAreas[i].setEditorKit(new HTMLEditorKit());
/*  80 */       this.textAreas[i].setEditable(false);
/*     */     }
/*     */ 
/*  84 */     JPanel textAreaPanel = new JPanel();
/*  85 */     textAreaPanel.setLayout(new BoxLayout(textAreaPanel, 0));
/*  86 */     textAreaPanel.add(new JScrollPane(this.textAreas[0]));
/*  87 */     textAreaPanel.add(new JScrollPane(this.textAreas[1]));
/*     */ 
/*  89 */     this.textPanel.add(textAreaPanel, "Center");
/*     */   }
/*     */ 
/*     */   private void showContents(int textPaneNum, File file)
/*     */   {
/* 171 */     readFile(textPaneNum, file);
/*     */   }
/*     */   private void showContents(int textPaneNum, String text) {
/* 174 */     this.textAreas[textPaneNum].setText(text);
/*     */   }
/*     */   private void readFile(int textPaneNum, File fileName) {
/* 177 */     BufferedReader reader = null;
/* 178 */     String res = "";
/*     */     try {
/* 180 */       reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "UTF-8"));
/*     */       String line;
/* 182 */       while ((line = reader.readLine()) != null) {
/* 183 */         res = res + "<br/>" + line;
/*     */       }
/* 185 */       this.textAreas[textPaneNum].setText(res);
/*     */     }
/*     */     catch (FileNotFoundException fnf) {
/* 188 */       reportException("No such file: " + fileName);
/*     */     }
/*     */     catch (IOException ioe) {
/* 191 */       reportException("Error reading file: " + fileName);
/*     */     }
/*     */     finally {
/*     */       try {
/* 195 */         if (reader != null) reader.close(); 
/*     */       }
/*     */       catch (IOException ioe)
/*     */       {
/* 198 */         reportException("Error closing file: " + fileName);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/* 203 */   private void reportException(String exceptionMessage) { JOptionPane.showMessageDialog(this.textPanel, exceptionMessage); }
/*     */ 
/*     */   public static void main(String[] args)
/*     */   {
/* 207 */     new XMLElanTool();
/*     */   }
/*     */ 
/*     */   private class NewTierNameListener extends MouseAdapter
/*     */   {
/*     */     private NewTierNameListener()
/*     */     {
/*     */     }
/*     */ 
/*     */     public void mouseClicked(MouseEvent e)
/*     */     {
/* 160 */       if (XMLElanTool.this.newTierName.getText().equals("new tier name"))
/* 161 */         XMLElanTool.this.newTierName.setText("");
/*     */     }
/*     */ 
/*     */     public void mouseExited(MouseEvent e) {
/* 165 */       if (XMLElanTool.this.newTierName.getText().equals(""))
/* 166 */         XMLElanTool.this.newTierName.setText("new tier name");
/*     */     }
/*     */   }
/*     */ 
/*     */   private class AddTransliterationListener
/*     */     implements ActionListener
/*     */   {
/*     */     private AddTransliterationListener()
/*     */     {
/*     */     }
/*     */ 
/*     */     public void actionPerformed(ActionEvent e)
/*     */     {
/*     */       try
/*     */       {
/* 132 */         String newTierNameValue = XMLElanTool.this.newTierName.getText();
/* 133 */         if (newTierNameValue.equals("")) {
/* 134 */           throw new XMLElanException("No new tier name!");
/*     */         }
/* 136 */         if (XMLElanTool.this.allTiersListValue.contains(newTierNameValue)) {
/* 137 */           throw new XMLElanException("The name of the new tier is the same as the name of an old tier" + newTierNameValue);
/*     */         }
/*     */ 
/* 141 */         XMLElanTool.this.textPanel.setCursor(XMLElanTool.this.waitCursor);
/* 142 */         File newFile = XMLElanTool.this.addTransliteration.addTransliteration((String)XMLElanTool.this.allTiersList.getSelectedValue(), XMLElanTool.this.newTierName.getText(), XMLElanTool.this.isSilCheckBox.isSelected());
/*     */ 
/* 144 */         if (newFile == null) {
/* 145 */           throw new XMLElanException("There occurred errors during processing");
/*     */         }
/* 147 */         JOptionPane.showMessageDialog(XMLElanTool.this.textPanel, "New file created: " + newFile.getAbsolutePath());
/*     */ 
/* 149 */         //XMLElanTool.this.showContents(1, newFile);
/* 150 */         XMLElanTool.this.textPanel.setCursor(XMLElanTool.this.defaultCursor);
/*     */       }
/*     */       catch (XMLElanException xee) {
/* 153 */         XMLElanTool.this.reportException(xee.getMessage());
/* 154 */         XMLElanTool.this.textPanel.setCursor(XMLElanTool.this.defaultCursor);
/*     */       }
/*     */     }
/*     */   }



            private class SaveToXMLListener
/*     */     implements ActionListener
/*     */   {

        /*     */
/*     */     public void actionPerformed(ActionEvent e)
/*     */     {
/*     */       try
/*     */       {
/* 132 */
/* 141 */         XMLElanTool.this.textPanel.setCursor(XMLElanTool.this.waitCursor);
/* 142 */         List<String> filePaths = XMLElanTool.this.xmlFormatter.saveFiles(XMLElanTool.this.isSilCheckBox.isSelected(),
                                                        XMLElanTool.this.isTwoSpeakersCheckBox.isSelected(),
                                                            isArchive.isSelected());
/*     */
/* 144 */
/* 147 */         JOptionPane.showMessageDialog(XMLElanTool.this.textPanel, "Files created: " + filePaths.toString());


/*     */
/* 149 */         //XMLElanTool.this.showContents(1, newFile);
/* 150 */         XMLElanTool.this.textPanel.setCursor(XMLElanTool.this.defaultCursor);
/*     */       }
/*     */       catch (XMLElanException xee) {
/* 153 */         XMLElanTool.this.reportException(xee.getMessage());
/* 154 */         XMLElanTool.this.textPanel.setCursor(XMLElanTool.this.defaultCursor);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   private class OpenFileListener
/*     */     implements ActionListener
/*     */   {
/*     */     private OpenFileListener()
/*     */     {
/*     */     }
/*     */ 
/*     */     public void actionPerformed(ActionEvent e)
/*     */     {
/* 102 */       XMLElanTool.this.fc.showOpenDialog(XMLElanTool.this.myFrame);
/* 103 */       File selected = XMLElanTool.this.fc.getSelectedFile();
/* 104 */       if (selected != null)
/*     */         try {
/* 106 */
/* 107 */           XMLElanTool.this.allTiersListValue = null;
/* 108 */           XMLElanTool.this.textPanel.setCursor(XMLElanTool.this.waitCursor);
/* 109 */           //XMLElanTool.this.showContents(0, selected);
/* 110 */           boolean success = XMLElanTool.this.addTransliteration.readFile(selected);
/* 111 */           if (!success) {
/* 112 */             throw new XMLElanException("The file cannot be parsed as XML: " + selected.getAbsolutePath());
/*     */           }
/* 114 */           XMLElanTool.this.allTiersListValue = XMLElanTool.this.addTransliteration.getTierNames();
/* 115 */           XMLElanTool.this.textPanel.setCursor(XMLElanTool.this.defaultCursor);
/* 116 */           if ((XMLElanTool.this.allTiersListValue == null) || (XMLElanTool.this.allTiersListValue.size() == 0)) {
/* 117 */             throw new XMLElanException("No tiers found in: " + selected.getAbsolutePath());
/*     */           }
/* 119 */           XMLElanTool.this.allTiersList.setListData(XMLElanTool.this.allTiersListValue.toArray());
/* 120 */           XMLElanTool.this.addTransliterationButton.setEnabled(true);
                    XMLElanTool.this.saveToHTMLFormat.setEnabled(true);
                    XMLElanTool.this.xmlFormatter.setAllTiers(XMLElanTool.this.addTransliteration.getAllTiers());
                    XMLElanTool.this.xmlFormatter.setDoc(XMLElanTool.this.addTransliteration.getDoc());
                    XMLElanTool.this.xmlFormatter.setOldFileName(selected.getAbsolutePath());
/*     */         }
/*     */         catch (XMLElanException xee) {
/* 123 */           XMLElanTool.this.reportException(xee.getMessage());
/* 124 */           XMLElanTool.this.myFrame.setCursor(XMLElanTool.this.defaultCursor);
/*     */         }
/*     */     }
/*     */   }
/*     */ }

/* Location:           E:\ELAN2011\ElanUtils\ElanUtils\out\production\ElanUtils\
 * Qualified Name:     XMLElanTool
 * JD-Core Version:    0.6.2
 */