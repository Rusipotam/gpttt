package com.example.vocabplugin.translation;

import java.util.List;

public class TranslationService {

    private final List<TranslationProvider> providers;

    public TranslationService(List<TranslationProvider> providers) {
        this.providers = providers;
    }

    public TranslationResult resolveTranslation(String word) {
        for (TranslationProvider provider : providers) {
            var result = provider.translate(word);
            if (result.isPresent()) {
                return result.get();
            }
        }
        return new TranslationResult(word, "self");
    }
}
