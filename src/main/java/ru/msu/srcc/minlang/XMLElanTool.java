package ru.msu.srcc.minlang;

import ru.msu.srcc.minlang.formatting.XMLFormatter;
import ru.msu.srcc.minlang.transliteration.TransliterationHelper;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;

import static javax.swing.SpringLayout.NORTH;
import static javax.swing.SpringLayout.SOUTH;

public class XMLElanTool {
    private static final String DEFAULT_TIER_NAME = "ev";
    private static final int MAX_TIER_NAME_LENGTH = 20;
    private JFileChooser fc;
    private JPanel textPanel;
    private JFrame myFrame;
    private JList allTiersList;
    private Set<String> allTiersListValue;
    private JTextField newTierName;
    private TransliterationHelper transliterationHelper = new TransliterationHelper();
    private XMLFormatter xmlFormatter = new XMLFormatter();
    private JButton addTransliterationButton;
    private JButton checkGlossesButton;
    private JButton saveToHTMLFormat;
    private JCheckBox isSilCheckBox;
    private JCheckBox isTwoSpeakersCheckBox;
    private JCheckBox isArchive;
    private Cursor waitCursor = new Cursor(Cursor.WAIT_CURSOR);
    private Cursor defaultCursor = new Cursor(Cursor.DEFAULT_CURSOR);

    public XMLElanTool() {
        initializeMyFrame();
        initalizeControls();
        fc = new JFileChooser("resources/");
        FileNameExtensionFilter filter = new FileNameExtensionFilter("ELAN files", "eaf");
        fc.setFileFilter(filter);
        myFrame.setVisible(true);
    }

    public static void main(String[] args) {
        new XMLElanTool();
    }

    private void initializeMyFrame() {
        myFrame = new JFrame();
        myFrame.setDefaultCloseOperation(2);
        myFrame.setSize(700, 300);
        textPanel = new JPanel();
        textPanel.setLayout(new BorderLayout());
        myFrame.getContentPane().add(textPanel);
        allTiersList = new JList();
        newTierName = new JTextField(MAX_TIER_NAME_LENGTH);
        newTierName.setText(DEFAULT_TIER_NAME);
        newTierName.setMaximumSize(newTierName.getPreferredSize());
    }

    private void initalizeControls() {
        JButton openFileButton = new JButton("Open file");
        addTransliterationButton = new JButton("Add transliteration");
        addTransliterationButton.setEnabled(false);
        openFileButton.addActionListener(new OpenFileListener());
        addTransliterationButton.addActionListener(new AddTransliterationListener());
        saveToHTMLFormat = new JButton("Save to HTML");
        saveToHTMLFormat.setEnabled(false);
        saveToHTMLFormat.addActionListener(new SaveToHTMLListener());
        isSilCheckBox = new JCheckBox("is sil", true);
        isTwoSpeakersCheckBox = new JCheckBox("two speakers");
        isArchive = new JCheckBox("is archive");

        checkGlossesButton = new JButton("Check glosses");
        checkGlossesButton.setEnabled(false);
        checkGlossesButton.addActionListener(new CheckGlossesListener());

        JPanel buttonsAndTextField = new JPanel();
        buttonsAndTextField.setLayout(new BoxLayout(buttonsAndTextField, BoxLayout.X_AXIS));
        buttonsAndTextField.add(openFileButton);
        buttonsAndTextField.add(addTransliterationButton);
        buttonsAndTextField.add(saveToHTMLFormat);
        buttonsAndTextField.add(checkGlossesButton);
        buttonsAndTextField.add(newTierName);
        buttonsAndTextField.add(isSilCheckBox);
        buttonsAndTextField.add(isTwoSpeakersCheckBox);
        buttonsAndTextField.add(isArchive);
        textPanel.add(NORTH, allTiersList);
        textPanel.add(SOUTH, buttonsAndTextField);
    }

    private void reportException(String exceptionMessage) {
        JOptionPane.showMessageDialog(this.textPanel, exceptionMessage);
    }

    private class AddTransliterationListener
            implements ActionListener {
        private AddTransliterationListener() {
        }

        public void actionPerformed(ActionEvent e) {
            try {
                String newTierNameValue = newTierName.getText();
                if (newTierNameValue.trim().isEmpty()) {
                    throw new XMLElanException("No new tier name");
                }
                Object tierToTransliterateName = allTiersList.getSelectedValue();
                if (tierToTransliterateName == null) {
                    throw new XMLElanException("No tier to transliterate selected");
                }
                if (allTiersListValue.contains(newTierNameValue)) {
                    throw new XMLElanException(String.format("Tier already exists: %s", newTierNameValue));
                }
                textPanel.setCursor(waitCursor);
                File newFile = transliterationHelper.addTransliteration((String) tierToTransliterateName,
                        newTierNameValue, isSilCheckBox.isSelected());
                if (newFile == null) {
                    throw new XMLElanException("There occurred errors during processing");
                }
                JOptionPane.showMessageDialog(textPanel, String.format("New file created: %s",
                        newFile.getAbsolutePath()));
            } catch (Exception ex) {
                reportException(ex.getMessage());
            } finally {
                textPanel.setCursor(defaultCursor);
            }
        }
    }

    private class SaveToHTMLListener
            implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            try {
                LocalDateTime start = LocalDateTime.now();
                textPanel.setCursor(waitCursor);
                List<String> filePaths = xmlFormatter.saveFiles(isSilCheckBox.isSelected(),
                        isTwoSpeakersCheckBox.isSelected(),
                        isArchive.isSelected());
                LocalDateTime end = LocalDateTime.now();
                long diff = ChronoUnit.MILLIS.between(start, end);
                JOptionPane.showMessageDialog(textPanel, String.format("Files created: %s in %s milliseconds",
                        String.join("\n", filePaths), diff));

            } catch (Exception ex) {
                reportException(ex.getMessage());
            } finally {
                textPanel.setCursor(defaultCursor);
            }
        }
    }

    private class CheckGlossesListener
            implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            FileWriter fileWriter = null;
            PrintWriter printWriter = null;
            try {
                LocalDateTime start = LocalDateTime.now();
                textPanel.setCursor(waitCursor);
                List<String> errors = xmlFormatter.checkGlosses(isSilCheckBox.isSelected(),
                        isTwoSpeakersCheckBox.isSelected(),
                        isArchive.isSelected());
                LocalDateTime end = LocalDateTime.now();
                long diff = ChronoUnit.MILLIS.between(start, end);
                JOptionPane.showMessageDialog(textPanel, String.format("Errors found: %s in %s milliseconds",
                        String.join("\n", errors), diff));
                fileWriter = new FileWriter(xmlFormatter.getOldFileName() + "_errors.txt");
                printWriter = new PrintWriter(fileWriter);
                for (String error : errors) {
                    printWriter.println(error);
                }


            } catch (Exception ex) {
                reportException(ex.getMessage());
            } finally {
                textPanel.setCursor(defaultCursor);
                if (printWriter != null) {
                    printWriter.close();
                }
            }
        }
    }

    private class OpenFileListener
            implements ActionListener {
        private OpenFileListener() {
        }

        public void actionPerformed(ActionEvent e) {
            fc.showOpenDialog(myFrame);
            File selected = fc.getSelectedFile();
            if (selected != null)
                try {
                    allTiersListValue = null;
                    textPanel.setCursor(waitCursor);
                    transliterationHelper.readFile(selected);
                    allTiersListValue = transliterationHelper.getTierNames();
                    textPanel.setCursor(defaultCursor);
                    if ((allTiersListValue == null) || (allTiersListValue.size() == 0)) {
                        throw new XMLElanException(String.format("No tiers found in: %s",
                                selected.getAbsolutePath()));
                    }
                    allTiersList.setListData(allTiersListValue.toArray());
                    addTransliterationButton.setEnabled(true);
                    saveToHTMLFormat.setEnabled(true);
                    checkGlossesButton.setEnabled(true);
                    xmlFormatter.setAllTiers(transliterationHelper.getAllTiers());
                    xmlFormatter.setDoc(transliterationHelper.getDoc());
                    xmlFormatter.setOldFileName(selected.getAbsolutePath());
                } catch (Exception ex) {
                    reportException(ex.getMessage());
                } finally {
                    textPanel.setCursor(defaultCursor);
                }
        }
    }
}
