package com.example.vocabplugin.storage;

import com.example.vocabplugin.VocabularyEntry;

import java.util.Map;
import java.util.UUID;

public interface VocabularyStorage {
    Map<String, VocabularyEntry> load(UUID playerId);

    void save(UUID playerId, Map<String, VocabularyEntry> entries);

    default void saveEntry(UUID playerId, VocabularyEntry entry) {
        Map<String, VocabularyEntry> all = load(playerId);
        all.put(entry.word(), entry);
        save(playerId, all);
    }

    default void close() {
    }
}
