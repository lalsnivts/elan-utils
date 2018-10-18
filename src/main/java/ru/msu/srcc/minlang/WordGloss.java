package ru.msu.srcc.minlang;

public class WordGloss {
    private String word;
    private String gloss;

    public WordGloss(String word, String gloss) {
        this.word = word;
        this.gloss = gloss;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getGloss() {
        return gloss;
    }

    public void setGloss(String gloss) {
        this.gloss = gloss;
    }
}
