package com.example.vocabplugin.translation;

import com.example.vocabplugin.VocabSignPlugin;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class OverrideTranslationProvider implements TranslationProvider {

    private final VocabSignPlugin plugin;

    public OverrideTranslationProvider(VocabSignPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public Optional<TranslationResult> translate(String word) {
        Map<String, String> overrides = plugin.getOverrideTranslations();
        String translation = overrides.get(word.toLowerCase(Locale.ROOT));
        if (translation == null || translation.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(new TranslationResult(translation, "override"));
    }
}
