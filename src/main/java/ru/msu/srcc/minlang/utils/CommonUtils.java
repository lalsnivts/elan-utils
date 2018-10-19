package ru.msu.srcc.minlang.utils;

import org.apache.commons.lang3.StringUtils;

public class CommonUtils {
    private static final String DEFAULT_PUNCTUATION_MARK = ".";
    private static final String[] PUNCTUATION_MARKS = new String[]{"?", "!", ":", ",", "...", "\"", "»"};
    private static final String TECHNICAL_SYMBOLS = "=- ";

    public static String capitalize(String sentence) {
        if (sentence.length() < 1) {
            return sentence;
        }
        return sentence.substring(0, 1).toUpperCase() + sentence.substring(1);
    }

    public static String formatSentence(String sentenceConverted, String lastPunctuationMark) {
        return capitalize(sentenceConverted.trim()) + lastPunctuationMark;
    }


    public static String getPunctuationMark(String sentence) {
        if (sentence == null || sentence.isEmpty()) {
            return DEFAULT_PUNCTUATION_MARK;
        }
        for (String punctuationMark : PUNCTUATION_MARKS) {
            if (sentence.endsWith(punctuationMark)) {
                return punctuationMark;
            }
        }
        return DEFAULT_PUNCTUATION_MARK;
    }


    public static String[] tokenizeSentence(String sentence) {
        return sentence.split("\\s");
    }

    public static String formatMinSec(long ms) {
        long minSecMs = ms % 3600000;
        long min = minSecMs / 60000;
        long sec = (minSecMs % 60000) / 1000;
        return getTimeValueFromLong(min) + ":" + getTimeValueFromLong(sec);
    }

    public static String getTimeValueFromLong(long timeValue) {
        String timeStr = String.valueOf(timeValue);
        if (timeStr.length() == 1) {
            timeStr = "0" + timeStr;
        }
        return timeStr;
    }

    public static String stripTechnicalSymbols(String stringToStrip) {
        return StringUtils.strip(stringToStrip, TECHNICAL_SYMBOLS);
    }

    public static String makeReplacementsForGlosses(String stringToProcess) {
        return stringToProcess.replaceAll("--", "-")
                .replaceAll("[-=]=", "=");
    }
}
