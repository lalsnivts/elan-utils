/*     */ package ru.msu.srcc.minlang;
/*     */ 
/*     */ import java.util.HashMap;
/*     */ import java.util.Map;
/*     */ import java.util.Set;
/*     */ 
/*     */ public class EvenkiConverter
/*     */ {
/*  16 */   private String[] evenkiPalatalisedConsonants = { "j", "ď", "ń", "ś", "ť", "d'", "n'", "s'", "t'" };
/*  17 */   private String[] evenkiNotPalatalisedI = { "d", "s", "t" };
/*  18 */   Map<String, String> notIotisedMap = new HashMap();
/*  19 */   Map<String, String> iotisedMap = new HashMap();
/*  20 */   Map<String, String> beforeIotisedCharMap = new HashMap();
/*  21 */   Map<String, String> charMap = new HashMap();
/*     */ 
/*     */   public EvenkiConverter()
/*     */   {
/*  25 */     this.notIotisedMap.put("i", "ы");
/*  26 */     this.notIotisedMap.put("ī", "ӣ");

/*     */ 
/*  29 */     this.iotisedMap.put("a", "я");
/*  30 */     this.iotisedMap.put("a", "я");
/*  31 */     this.iotisedMap.put("ā", "я");
/*  32 */     this.iotisedMap.put("e", "е");
/*  33 */     this.iotisedMap.put("ə", "е");
/*  34 */     this.iotisedMap.put("ə", "е");
/*  35 */     this.iotisedMap.put("o", "ё");
/*  36 */     this.iotisedMap.put("ō", "ё");
/*  37 */     this.iotisedMap.put("i", "и");
/*  38 */     this.iotisedMap.put("ī", "ӣ");
/*  39 */     this.iotisedMap.put("u", "ю");
/*  40 */     this.iotisedMap.put("ū", "ю");
/*     */ 
/*  42 */     this.beforeIotisedCharMap.put("j", "");
/*  43 */     this.beforeIotisedCharMap.put("ď", "д");
/*  44 */     this.beforeIotisedCharMap.put("d'", "д");
/*  45 */     this.beforeIotisedCharMap.put("ń", "н");
/*  46 */     this.beforeIotisedCharMap.put("n'", "н");
/*  47 */     this.beforeIotisedCharMap.put("ś", "с");
/*  48 */     this.beforeIotisedCharMap.put("s'", "с");
/*  49 */     this.beforeIotisedCharMap.put("s’", "с");
/*     */ 
/*  51 */     this.beforeIotisedCharMap.put("ť", "т");
/*  52 */     this.beforeIotisedCharMap.put("t'", "т");
/*     */ 
/*  56 */     this.charMap.put("-", "");
/*  57 */     this.charMap.put("ts", "ц");
/*     */ 
/*  59 */     this.charMap.put("ø", "");
/*  60 */     this.charMap.put("a", "а");
/*  61 */     this.charMap.put("ā", "а̄");

                this.charMap.put("ɨ", "ы");
/*  62 */     this.charMap.put("b", "б");
/*  63 */     this.charMap.put("č", "ч");
/*  64 */     this.charMap.put("d", "д");
/*  65 */     this.charMap.put("ď", "дь");
/*  66 */     this.charMap.put("d'", "дь");
/*  67 */     this.charMap.put("e", "э");
/*  68 */     this.charMap.put("ə", "э");
/*  69 */     this.charMap.put("f", "ф");
/*  70 */     this.charMap.put("g", "г");
/*  71 */     this.charMap.put("γ", "г");
/*  72 */     this.charMap.put("h", "h");
/*  73 */     this.charMap.put("i", "и");
/*  74 */     this.charMap.put("ī", "ӣ");
/*  75 */     this.charMap.put("j", "й");
/*  76 */     this.charMap.put("k", "к");
/*  77 */     this.charMap.put("l", "л");
/*  78 */     this.charMap.put("m", "м");
/*  79 */     this.charMap.put("n", "н");
/*  80 */     this.charMap.put("ń", "нь");
/*  81 */     this.charMap.put("n'", "нь");
/*  82 */     this.charMap.put("ŋ", "ӈ");
/*  83 */     this.charMap.put("o", "о");
              this.charMap.put("ɒ", "о");
/*  84 */     this.charMap.put("ō", "о̄");
/*  85 */     this.charMap.put("p", "п");
/*  86 */     this.charMap.put("r", "р");
/*  87 */     this.charMap.put("s", "с");
/*  88 */     this.charMap.put("ś", "сь");
/*  89 */     this.charMap.put("s'", "сь");
/*  90 */     this.charMap.put("š", "ш");
/*  91 */     this.charMap.put("t", "т");
/*  92 */     this.charMap.put("ť", "ть");
/*  93 */     this.charMap.put("t'", "ть");
/*  94 */     this.charMap.put("u", "у");
/*  95 */     this.charMap.put("ū", "ӯ");
/*  96 */     this.charMap.put("v", "в");
/*  97 */     this.charMap.put("w", "в");
/*  98 */     this.charMap.put("y", "ы");
/*  99 */     this.charMap.put("z", "з");
/* 100 */     this.charMap.put("ž", "ж");
/*     */   }
/*     */ 
/*     */   public String convert(String evenkiFon)
/*     */   {

/* 106 */     String res = evenkiFon.toLowerCase().replaceAll("’", "'");
/*     */ 
/* 108 */     Set<String>iotisedVowels = this.iotisedMap.keySet();
/* 109 */     for (String vowel : iotisedVowels) {
/* 110 */       for (String palatalisedConsonant : this.evenkiPalatalisedConsonants) {
/* 111 */         res = res.replaceAll(palatalisedConsonant + vowel, (String)this.beforeIotisedCharMap.get(palatalisedConsonant) + (String)this.iotisedMap.get(vowel));
/*     */       }
/*     */     }
/*     */ 
/* 115 */     Set<String>notPalatalised = this.notIotisedMap.keySet();
/* 116 */     for (String vowel : notPalatalised) {
/* 117 */       for (String notPalatalisedConsonant : this.evenkiNotPalatalisedI) {
/* 118 */         res = res.replaceAll(notPalatalisedConsonant + vowel, (String)this.charMap.get(notPalatalisedConsonant) + (String)this.notIotisedMap.get(vowel));
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 123 */     Set<String>allKeys = this.charMap.keySet();
/* 124 */     for (String vowel : allKeys) {
/* 125 */       res = res.replaceAll(vowel, (String)this.charMap.get(vowel));
/*     */     }
/* 127 */     res = res.replaceAll("'", "");
/* 128 */     return res;
/*     */   }
/*     */ }

/* Location:           E:\ELAN2011\ElanUtils\ElanUtils\out\production\ElanUtils\
 * Qualified Name:     EvenkiConverter
 * JD-Core Version:    0.6.2
 */