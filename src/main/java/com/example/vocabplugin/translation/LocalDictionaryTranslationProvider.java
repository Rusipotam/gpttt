package com.example.vocabplugin.translation;

import com.example.vocabplugin.VocabSignPlugin;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class LocalDictionaryTranslationProvider implements TranslationProvider {

    private final VocabSignPlugin plugin;

    public LocalDictionaryTranslationProvider(VocabSignPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public Optional<TranslationResult> translate(String word) {
        Map<String, String> dictionary = plugin.getFallbackTranslations();
        String translation = dictionary.get(word.toLowerCase(Locale.ROOT));
        if (translation == null || translation.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(new TranslationResult(translation, "local"));
    }
}
