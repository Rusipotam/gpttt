package com.example.vocabplugin;

public record VocabularyEntry(
        String word,
        String translation,
        String source,
        long addedAt
) {
}
