/*     */
package ru.msu.srcc.minlang;
/*     */
/*     */

import com.sun.org.apache.xerces.internal.util.XMLChar;

import javax.swing.*;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */

/*     */
/*     */ public class XMLValidator
        /*     */ {
    /*     */   private List<String> lines;
    /*     */   private JFileChooser fc;
    /*     */   private JPanel textPanel;
    /*     */   private JFrame myFrame;

    /*     */
    /*     */
    public XMLValidator()
    /*     */ {
        /*  26 */
        initializeMyFrame();
        /*  27 */
        this.fc = new JFileChooser();
        /*  28 */
        JButton chooseFile = new JButton("Choose file");
        /*  29 */
        chooseFile.addActionListener(new ChooseFileListener());
        /*  30 */
        this.textPanel.add("North", chooseFile);
        /*  31 */
        this.myFrame.setVisible(true);
        /*     */
    }

    /*     */
    /*     */
    public static void main(String[] args)
    /*     */ {
        /* 121 */
        XMLValidator validator = new XMLValidator();
        /*     */
    }

    /*     */
    public void validateXML(String fileName) {
        /*  34 */
        validateXML(new File(fileName));
        /*     */
    }

    /*     */
    public void validateXML(File file) {
        /*  37 */
        readFile(file);
        /*  38 */
        showContents();
        /*     */
    }

    /*     */
    private void readFile(File fileName) {
        /*  41 */
        BufferedReader reader = null;
        /*  42 */
        this.lines = new ArrayList();
        /*     */
        try {
            /*  44 */
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "UTF-8"));
            /*     */
            String line;
            /*  46 */
            while ((line = reader.readLine()) != null)
                /*  47 */ this.lines.add(line);
            /*     */
        }
        /*     */ catch (FileNotFoundException fnf)
            /*     */ {
            /*  51 */
            reportException("No such file: " + fileName);
            /*     */
        }
        /*     */ catch (IOException ioe) {
            /*  54 */
            reportException("Error reading file: " + fileName);
            /*     */
        }
        /*     */ finally {
            /*     */
            try {
                /*  58 */
                if (reader != null) reader.close();
                /*     */
            }
            /*     */ catch (IOException ioe)
                /*     */ {
                /*  61 */
                reportException("Error closing file: " + fileName);
                /*     */
            }
            /*     */
        }
        /*     */
    }

    /*     */
    /*  66 */
    private void showContents() {
        checkAndAdd();
        /*  67 */
        this.myFrame.setVisible(true);
    }

    /*     */
    /*     */
    private void initializeMyFrame() {
        /*  70 */
        this.myFrame = new JFrame();
        /*  71 */
        this.myFrame.setSize(500, 500);
        /*  72 */
        this.textPanel = new JPanel(new BorderLayout());
        /*  73 */
        this.myFrame.getContentPane().add(this.textPanel);
        /*     */
    }

    /*     */
    private void checkAndAdd() {
        /*  76 */
        JEditorPane textArea = new JEditorPane();
        /*  77 */
        textArea.setEditorKit(new HTMLEditorKit());
        /*  78 */
        String linesWithHTML = "";
        /*  79 */
        JEditorPane lineNumbers = new JEditorPane();
        /*     */
        /*  81 */
        lineNumbers.setEditorKit(new HTMLEditorKit());
        /*  82 */
        lineNumbers.setAlignmentX(1.0F);
        /*  83 */
        String lineNumbersText = "";
        /*  84 */
        int i = 1;
        /*  85 */
        System.out.println(this.lines.size());
        /*  86 */
        for (String line : this.lines) {
            /*  87 */
            linesWithHTML = linesWithHTML + processLine(line) + "<br/>";
            /*  88 */
            lineNumbersText = lineNumbersText + i + "<br/>";
            /*  89 */
            i++;
            /*     */
        }
        /*  91 */
        lineNumbers.setText(lineNumbersText);
        /*  92 */
        this.textPanel.add(lineNumbers, "West");
        /*  93 */
        textArea.setText(linesWithHTML);
        /*  94 */
        this.textPanel.add(textArea, "Center");
        /*     */
    }

    /*     */
    private String processLine(String line) {
        /*  97 */
        String res = "";
        /*  98 */
        for (int i = 0; i < line.length(); i++) {
            /*  99 */
            char current = line.charAt(i);
            /* 100 */
            if (XMLChar.isValid(current)) {
                /* 101 */
                res = res + current;
                /*     */
            }
            /*     */
            else {
                /* 104 */
                res = res + "<b>" + current + "<\\/b>";
                /*     */
            }
            /*     */
        }
        /*     */
        /* 108 */
        return res;
        /*     */
    }

    /*     */
    private void reportException(String exceptionMessage) {
        /* 111 */
        JOptionPane.showMessageDialog(this.textPanel, exceptionMessage);
        /*     */
    }

    /*     */
    /*     */   private class ChooseFileListener
            /*     */ implements ActionListener
            /*     */ {
        /*     */
        private ChooseFileListener()
        /*     */ {
            /*     */
        }

        /*     */
        /*     */
        public void actionPerformed(ActionEvent e)
        /*     */ {
            /* 115 */
            XMLValidator.this.fc.showOpenDialog(XMLValidator.this.myFrame);
            /* 116 */
            File file = XMLValidator.this.fc.getSelectedFile();
            /* 117 */
            if (file != null) XMLValidator.this.validateXML(XMLValidator.this.fc.getSelectedFile());
            /*     */
        }
        /*     */
    }
    /*     */
}

/* Location:           E:\ELAN2011\ElanUtils\ElanUtils\out\production\ElanUtils\
 * Qualified Name:     XMLValidator
 * JD-Core Version:    0.6.2
 */