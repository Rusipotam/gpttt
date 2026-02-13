package com.example.vocabplugin.storage;

import com.example.vocabplugin.VocabularyEntry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class YamlVocabularyStorage implements VocabularyStorage {

    private final File folder;

    public YamlVocabularyStorage(File dataFolder) {
        this.folder = new File(dataFolder, "playerdata");
        if (!folder.exists()) {
            folder.mkdirs();
        }
    }

    @Override
    public Map<String, VocabularyEntry> load(UUID playerId) {
        File file = file(playerId);
        if (!file.exists()) {
            return new HashMap<>();
        }
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection entriesSection = yaml.getConfigurationSection("entries");
        Map<String, VocabularyEntry> result = new HashMap<>();
        if (entriesSection == null) {
            return result;
        }
        for (String word : entriesSection.getKeys(false)) {
            String base = "entries." + word;
            String translation = yaml.getString(base + ".translation", word);
            String source = yaml.getString(base + ".source", "unknown");
            long addedAt = yaml.getLong(base + ".added-at", System.currentTimeMillis());
            result.put(word, new VocabularyEntry(word, translation, source, addedAt));
        }
        return result;
    }

    @Override
    public void save(UUID playerId, Map<String, VocabularyEntry> entries) {
        File file = file(playerId);
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("player-id", playerId.toString());
        for (VocabularyEntry entry : entries.values()) {
            String base = "entries." + entry.word();
            yaml.set(base + ".translation", entry.translation());
            yaml.set(base + ".source", entry.source());
            yaml.set(base + ".added-at", entry.addedAt());
        }
        try {
            yaml.save(file);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to save vocabulary file for " + playerId, e);
        }
    }

    private File file(UUID playerId) {
        return new File(folder, playerId + ".yml");
    }
}
