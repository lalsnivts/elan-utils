package ru.msu.srcc.minlang;

import java.util.List;

/**
 * Created by User on 17.02.2015.
 */
public class BookFormat {
    private List<String> originalSentences;
    private List<String> russianSentences;
    private List<BookMorphSentence> morphSentences;

    public List<String> getOriginalSentences() {
        return originalSentences;
    }

    public void setOriginalSentences(List<String> originalSentences) {
        this.originalSentences = originalSentences;
    }

    public List<String> getRussianSentences() {
        return russianSentences;
    }

    public void setRussianSentences(List<String> russianSentences) {
        this.russianSentences = russianSentences;
    }

    public List<BookMorphSentence> getMorphSentences() {
        return morphSentences;
    }

    public void setMorphSentences(List<BookMorphSentence> morphSentences) {
        this.morphSentences = morphSentences;
    }
}
