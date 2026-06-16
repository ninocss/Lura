package com.nino161er.rssfeed.data.util

import java.util.Locale

object LanguageDetector {
    private val enStopWords = setOf(
        "the", "a", "an", "and", "or", "but", "in", "on", "at", "to", "for",
        "of", "with", "by", "from", "is", "are", "was", "were", "be", "been",
        "being", "have", "has", "had", "do", "does", "did", "will", "would",
        "can", "could", "shall", "should", "may", "might", "must", "this",
        "that", "these", "those", "it", "its", "they", "them", "their",
        "what", "which", "who", "whom", "how", "when", "where", "why",
        "not", "no", "nor", "so", "if", "then", "than", "as", "just",
        "about", "up", "out", "all", "each", "every", "some", "any",
        "both", "more", "most", "other", "such", "only", "own", "same",
        "here", "there", "very", "too", "also", "into", "over", "after"
    )

    private val deStopWords = setOf(
        "der", "die", "das", "den", "dem", "des", "ein", "eine", "einen",
        "einer", "einem", "eines", "und", "oder", "aber", "mit", "von",
        "für", "auf", "an", "in", "zu", "aus", "bei", "nach", "um",
        "ist", "sind", "war", "wird", "werden", "wurde", "würde",
        "hat", "haben", "hatte", "hätte", "kann", "können", "konnte",
        "soll", "sollen", "muss", "müssen", "mochte", "möchten",
        "dass", "weil", "wenn", "als", "wie", "nicht", "kein", "keine",
        "sich", "sie", "er", "ihm", "ihn", "ihr", "uns", "euch",
        "dieser", "diese", "dieses", "welcher", "welche", "welches",
        "was", "wer", "wem", "wen", "wessen", "noch", "schon", "immer",
        "sehr", "auch", "nur", "bereits", "dann", "dort", "hier", "da",
        "also", "aber", "denn", "doch", "jedoch", "trotzdem", "allerdings"
    )

    private val frStopWords = setOf(
        "le", "la", "les", "l", "un", "une", "des", "du", "de", "au",
        "aux", "ce", "cet", "cette", "ces", "mon", "ton", "son", "ma",
        "ta", "sa", "mes", "tes", "ses", "nos", "vos", "leurs",
        "et", "ou", "mais", "donc", "car", "ni", "avec", "sans",
        "dans", "sur", "sous", "entre", "pour", "par", "vers", "chez",
        "est", "sont", "était", "étaient", "être", "a", "ont", "avait",
        "avaient", "avoir", "fait", "faire", "peut", "peuvent", "pouvoir",
        "veut", "veulent", "vouloir", "doit", "doivent", "devoir",
        "que", "qui", "dont", "où", "quoi", "quel", "quelle", "quels",
        "quelles", "ne", "pas", "plus", "rien", "personne", "jamais",
        "se", "me", "te", "nous", "vous", "ils", "elles", "lui",
        "c", "d", "j", "m", "n", "s", "t", "y", "en"
    )

    private val esStopWords = setOf(
        "el", "la", "los", "las", "un", "una", "unos", "unas", "lo",
        "y", "e", "o", "u", "pero", "sino", "con", "sin", "de", "del",
        "en", "por", "para", "hacia", "entre", "hasta", "desde",
        "es", "son", "era", "eran", "ser", "estar", "estoy", "está",
        "están", "tenía", "tiene", "tienen", "tener", "había", "hay",
        "hacer", "hecho", "puede", "pueden", "poder", "quiere", "quieren",
        "querer", "debe", "deben", "deber", "va", "van", "ir",
        "que", "quien", "cual", "cuales", "como", "cuando", "donde",
        "qué", "quién", "cuál", "cuáles", "cómo", "cuándo", "dónde",
        "no", "nunca", "nadie", "nada", "también", "ya", "muy",
        "me", "te", "se", "nos", "os", "le", "les", "lo", "la",
        "mi", "tu", "su", "mis", "tus", "sus", "nuestro", "tu",
        "él", "ella", "ellos", "ellas", "usted", "ustedes", "yo",
        "tú", "vosotros", "nosotros", "este", "esta", "estos", "estas",
        "ese", "esa", "esos", "esas", "aquel", "aquella", "aquellos", "aquellas"
    )

    private val languageMap = mapOf(
        "en" to Locale.forLanguageTag("en-US"),
        "de" to Locale.forLanguageTag("de-DE"),
        "fr" to Locale.forLanguageTag("fr-FR"),
        "es" to Locale.forLanguageTag("es-ES")
    )

    private val stopWords = mapOf(
        "en" to enStopWords,
        "de" to deStopWords,
        "fr" to frStopWords,
        "es" to esStopWords
    )

    fun detect(text: String): Locale {
        val cleaned = text.lowercase().replace(Regex("[^a-zäöüßàâæçéèêëîïôœùûüÿ ]"), " ")
        val words = cleaned.split(Regex("\\s+")).filter { it.length > 1 }

        if (words.size < 3) return Locale.getDefault()

        val scores = mutableMapOf<String, Int>()

        for ((lang, wordsSet) in stopWords) {
            var count = 0
            for (word in words) {
                if (word in wordsSet) count++
            }
            scores[lang] = count
        }

        val best = scores.maxByOrNull { it.value } ?: return Locale.getDefault()
        val bestCount = best.value
        val secondBestCount = scores.filterKeys { it != best.key }.maxOfOrNull { it.value } ?: 0

        return if (bestCount > 0 && bestCount > secondBestCount * 1.5) {
            languageMap[best.key] ?: Locale.getDefault()
        } else {
            Locale.getDefault()
        }
    }

    fun flagEmoji(locale: Locale): String = when (locale.language) {
        "de" -> "\uD83C\uDDE9\uD83C\uDDEA"
        "fr" -> "\uD83C\uDDEB\uD83C\uDDF7"
        "es" -> "\uD83C\uDDEA\uD83C\uDDF8"
        else -> "\uD83C\uDDEC\uD83C\uDDE7"
    }

    fun languageCode(locale: Locale): String = when (locale.language) {
        "de" -> "DE"
        "fr" -> "FR"
        "es" -> "ES"
        else -> "EN"
    }
}
