package ru.msu.srcc.minlang;

import java.util.List;

/**
 * Created by User on 17.02.2015.
 */
public class BookMorphSentence {
    private List<BookMorphWord> words;
    private Long begTime;
    private Long endTime;

    public List<BookMorphWord> getWords() {
        return words;
    }

    public void setWords(List<BookMorphWord> words) {
        this.words = words;
    }


    public Long getBegTime() {
        return begTime;
    }

    public void setBegTime(Long begTime) {
        this.begTime = begTime;
    }

    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }
}
