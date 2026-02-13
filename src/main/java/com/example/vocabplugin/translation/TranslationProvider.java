package com.example.vocabplugin.translation;

import java.util.Optional;

public interface TranslationProvider {
    Optional<TranslationResult> translate(String word);
}
