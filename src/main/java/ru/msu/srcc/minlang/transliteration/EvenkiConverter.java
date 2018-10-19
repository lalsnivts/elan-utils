package ru.msu.srcc.minlang.transliteration;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class EvenkiConverter {
    Map<String, String> notIotisedMap = new HashMap();
    Map<String, String> iotisedMap = new HashMap();
    Map<String, String> beforeIotisedCharMap = new HashMap();
    Map<String, String> charMap = new HashMap();
    private String[] evenkiPalatalisedConsonants = {"j", "ď", "ń", "ś", "ť", "d'", "n'", "s'", "t'"};
    private String[] evenkiNotPalatalisedI = {"d", "s", "t"};

    public EvenkiConverter() {
        notIotisedMap.put("i", "ы");
        notIotisedMap.put("ī", "ӣ");
        iotisedMap.put("a", "я");
        iotisedMap.put("a", "я");
        iotisedMap.put("ā", "я");
        iotisedMap.put("e", "е");
        iotisedMap.put("ə", "е");
        iotisedMap.put("ə", "е");
        iotisedMap.put("o", "ё");
        iotisedMap.put("ō", "ё");
        iotisedMap.put("i", "и");
        iotisedMap.put("ī", "ӣ");
        iotisedMap.put("u", "ю");
        iotisedMap.put("ū", "ю");
        beforeIotisedCharMap.put("j", "");
        beforeIotisedCharMap.put("ď", "д");
        beforeIotisedCharMap.put("d'", "д");
        beforeIotisedCharMap.put("ń", "н");
        beforeIotisedCharMap.put("n'", "н");
        beforeIotisedCharMap.put("ś", "с");
        beforeIotisedCharMap.put("s'", "с");
        beforeIotisedCharMap.put("s’", "с");
        beforeIotisedCharMap.put("ť", "т");
        beforeIotisedCharMap.put("t'", "т");
        charMap.put("-", "");
        charMap.put("ts", "ц");
        charMap.put("ø", "");
        charMap.put("a", "а");
        charMap.put("ā", "а̄");
        charMap.put("ɨ", "ы");
        charMap.put("b", "б");
        charMap.put("č", "ч");
        charMap.put("d", "д");
        charMap.put("ď", "дь");
        charMap.put("d'", "дь");
        charMap.put("e", "э");
        charMap.put("ə", "э");
        charMap.put("f", "ф");
        charMap.put("g", "г");
        charMap.put("γ", "г");
        charMap.put("h", "h");
        charMap.put("i", "и");
        charMap.put("ī", "ӣ");
        charMap.put("j", "й");
        charMap.put("k", "к");
        charMap.put("l", "л");
        charMap.put("m", "м");
        charMap.put("n", "н");
        charMap.put("ń", "нь");
        charMap.put("n'", "нь");
        charMap.put("ŋ", "ӈ");
        charMap.put("o", "о");
        charMap.put("ɒ", "о");
        charMap.put("ō", "о̄");
        charMap.put("p", "п");
        charMap.put("r", "р");
        charMap.put("s", "с");
        charMap.put("ś", "сь");
        charMap.put("s'", "сь");
        charMap.put("š", "ш");
        charMap.put("t", "т");
        charMap.put("ť", "ть");
        charMap.put("t'", "ть");
        charMap.put("u", "у");
        charMap.put("ū", "ӯ");
        charMap.put("v", "в");
        charMap.put("w", "в");
        charMap.put("y", "ы");
        charMap.put("z", "з");
        charMap.put("ž", "ж");
    }

    public String convert(String evenkiFon) {
        String res = evenkiFon.toLowerCase().replaceAll("’", "'");
        Set<String> iotisedVowels = iotisedMap.keySet();
        for (String vowel : iotisedVowels) {
            for (String palatalisedConsonant : evenkiPalatalisedConsonants) {
                res = res.replaceAll(palatalisedConsonant + vowel, beforeIotisedCharMap.get(palatalisedConsonant) + (String) iotisedMap.get(vowel));
            }
        }
        Set<String> notPalatalised = notIotisedMap.keySet();
        for (String vowel : notPalatalised) {
            for (String notPalatalisedConsonant : evenkiNotPalatalisedI) {
                res = res.replaceAll(notPalatalisedConsonant + vowel, charMap.get(notPalatalisedConsonant) + (String) notIotisedMap.get(vowel));
            }
        }
        Set<String> allKeys = charMap.keySet();
        for (String vowel : allKeys) {
            res = res.replaceAll(vowel, charMap.get(vowel));
        }
        res = res.replaceAll("'", "");
        return res;
    }
}