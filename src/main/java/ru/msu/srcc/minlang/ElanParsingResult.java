package ru.msu.srcc.minlang;

import org.w3c.dom.Node;

import java.util.List;

/**
 * Created by User on 17.02.2015.
 */
public class ElanParsingResult {
    private List<Node> originalSentences;
    private List<String> russianSentences;
    private List<String> words;
    private List<String> glosses;

    public ElanParsingResult(List<Node> originalSentences, List<String> russianSentences) {
        this.originalSentences = originalSentences;
        this.russianSentences = russianSentences;

    }

    public List<Node> getOriginalSentences() {
        return originalSentences;
    }

    public void setOriginalSentences(List<Node> originalSentences) {
        this.originalSentences = originalSentences;
    }

    public List<String> getRussianSentences() {
        return russianSentences;
    }

    public void setRussianSentences(List<String> russianSentences) {
        this.russianSentences = russianSentences;
    }

    public List<String> getGlosses() {
        return glosses;
    }

    public void setGlosses(List<String> glosses) {
        this.glosses = glosses;
    }

    public List<String> getWords() {
        return words;
    }

    public void setWords(List<String> words) {
        this.words = words;
    }
}
