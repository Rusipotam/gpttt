package com.example.vocabplugin;

import com.example.vocabplugin.command.VocabCommand;
import com.example.vocabplugin.storage.MySqlVocabularyStorage;
import com.example.vocabplugin.storage.VocabularyStorage;
import com.example.vocabplugin.storage.YamlVocabularyStorage;
import com.example.vocabplugin.translation.GoogleTranslationProvider;
import com.example.vocabplugin.translation.LocalDictionaryTranslationProvider;
import com.example.vocabplugin.translation.OverrideTranslationProvider;
import com.example.vocabplugin.translation.TranslationResult;
import com.example.vocabplugin.translation.TranslationService;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class VocabSignPlugin extends JavaPlugin {

    private final Set<String> dictionary = new HashSet<>();
    private final ConcurrentHashMap<UUID, Map<String, VocabularyEntry>> playerVocab = new ConcurrentHashMap<>();
    private final Map<String, String> overrideTranslations = new ConcurrentHashMap<>();
    private final Map<String, String> fallbackTranslations = new ConcurrentHashMap<>();

    private NamespacedKey addWordKey;
    private GuiStyle guiStyle;
    private VocabularyStorage storage;
    private TranslationService translationService;
    private File overrideFile;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        addWordKey = new NamespacedKey(this, "add_word");
        guiStyle = loadGuiStyle();

        loadDictionary();
        loadFallbackTranslations();
        loadOverrides();

        storage = createStorage();
        translationService = new TranslationService(java.util.List.of(
                new OverrideTranslationProvider(this),
                new GoogleTranslationProvider(this),
                new LocalDictionaryTranslationProvider(this)
        ));

        getServer().getPluginManager().registerEvents(new SignInteractListener(this), this);
        getServer().getPluginManager().registerEvents(new VocabularyGuiListener(this), this);

        VocabCommand vocabCommand = new VocabCommand(this);
        if (getCommand("vocab") != null) {
            getCommand("vocab").setExecutor(vocabCommand);
            getCommand("vocab").setTabCompleter(vocabCommand);
        }

        getLogger().info("VocabSignPlugin enabled. Dictionary size: " + dictionary.size());
    }

    @Override
    public void onDisable() {
        if (storage != null) {
            storage.close();
        }
    }

    public NamespacedKey getAddWordKey() {
        return addWordKey;
    }

    public GuiStyle getGuiStyle() {
        return guiStyle;
    }

    public boolean isWordValid(String word) {
        return dictionary.contains(word.toLowerCase(Locale.ROOT));
    }

    public Map<String, VocabularyEntry> getPlayerVocabulary(UUID playerId) {
        return Collections.unmodifiableMap(playerVocab.computeIfAbsent(playerId, storage::load));
    }

    public VocabularyEntry addWord(UUID playerId, String word) {
        String normalized = word.toLowerCase(Locale.ROOT);
        TranslationResult result = translationService.resolveTranslation(normalized);
        VocabularyEntry entry = new VocabularyEntry(normalized, result.translation(), result.source(), System.currentTimeMillis());
        Map<String, VocabularyEntry> map = new LinkedHashMap<>(playerVocab.computeIfAbsent(playerId, storage::load));
        map.put(normalized, entry);
        playerVocab.put(playerId, map);
        storage.saveEntry(playerId, entry);
        return entry;
    }

    public Map<String, String> getOverrideTranslations() {
        return Collections.unmodifiableMap(overrideTranslations);
    }

    public Map<String, String> getFallbackTranslations() {
        return Collections.unmodifiableMap(fallbackTranslations);
    }

    public void setOverride(String word, String translation) {
        overrideTranslations.put(word.toLowerCase(Locale.ROOT), translation);
        persistOverrides();
    }

    public void removeOverride(String word) {
        overrideTranslations.remove(word.toLowerCase(Locale.ROOT));
        persistOverrides();
    }

    public void reloadPluginData() {
        reloadConfig();
        guiStyle = loadGuiStyle();
        loadDictionary();
        loadFallbackTranslations();
        loadOverrides();
        if (storage != null) {
            storage.close();
        }
        storage = createStorage();
        translationService = new TranslationService(java.util.List.of(
                new OverrideTranslationProvider(this),
                new GoogleTranslationProvider(this),
                new LocalDictionaryTranslationProvider(this)
        ));
        playerVocab.clear();
    }

    private VocabularyStorage createStorage() {
        String type = getConfig().getString("storage.type", "yaml").toLowerCase(Locale.ROOT);
        if ("mysql".equals(type)) {
            getLogger().info("Using MySQL vocabulary storage");
            return new MySqlVocabularyStorage(getConfig());
        }
        getLogger().info("Using YAML vocabulary storage");
        return new YamlVocabularyStorage(getDataFolder());
    }

    private GuiStyle loadGuiStyle() {
        ConfigurationSection section = getConfig().getConfigurationSection("gui");
        if (section == null) {
            return GuiStyle.defaultStyle();
        }
        return GuiStyle.fromConfig(section);
    }

    private void loadDictionary() {
        dictionary.clear();
        String path = getConfig().getString("dictionary-resource", "dictionary/en_words.txt");
        try (InputStream stream = getResource(path)) {
            if (stream == null) {
                getLogger().warning("Dictionary resource not found: " + path);
                return;
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String normalized = line.trim().toLowerCase(Locale.ROOT);
                    if (!normalized.isEmpty() && normalized.matches("[a-z]+(?:'[a-z]+)?")) {
                        dictionary.add(normalized);
                    }
                }
            }
        } catch (IOException ex) {
            getLogger().severe("Failed to load dictionary: " + ex.getMessage());
        }
    }

    private void loadFallbackTranslations() {
        fallbackTranslations.clear();
        String path = getConfig().getString("fallback-translation-resource", "dictionary/en_ru_fallback.csv");
        try (InputStream stream = getResource(path)) {
            if (stream == null) {
                getLogger().warning("Fallback translation resource not found: " + path);
                return;
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(",", 2);
                    if (parts.length == 2) {
                        fallbackTranslations.put(parts[0].trim().toLowerCase(Locale.ROOT), parts[1].trim());
                    }
                }
            }
        } catch (IOException ex) {
            getLogger().warning("Failed to load fallback translations: " + ex.getMessage());
        }
    }

    private void loadOverrides() {
        overrideFile = new File(getDataFolder(), "translations-overrides.yml");
        if (!overrideFile.exists()) {
            if (!overrideFile.getParentFile().exists()) {
                overrideFile.getParentFile().mkdirs();
            }
            try {
                overrideFile.createNewFile();
            } catch (IOException e) {
                throw new IllegalStateException("Failed to create translations-overrides.yml", e);
            }
        }
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(overrideFile);
        overrideTranslations.clear();
        ConfigurationSection section = yaml.getConfigurationSection("overrides");
        if (section == null) {
            return;
        }
        for (String key : section.getKeys(false)) {
            String translation = section.getString(key);
            if (translation != null && !translation.isBlank()) {
                overrideTranslations.put(key.toLowerCase(Locale.ROOT), translation);
            }
        }
    }

    private void persistOverrides() {
        YamlConfiguration yaml = new YamlConfiguration();
        Map<String, String> sorted = new HashMap<>(overrideTranslations);
        sorted.forEach((k, v) -> yaml.set("overrides." + k, v));
        try {
            yaml.save(overrideFile);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to save overrides", ex);
        }
    }
}
