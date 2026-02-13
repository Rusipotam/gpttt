package com.example.vocabplugin.translation;

import com.example.vocabplugin.VocabSignPlugin;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GoogleTranslationProvider implements TranslationProvider {

    private static final Pattern TRANSLATED_TEXT_PATTERN = Pattern.compile("\\\"translatedText\\\"\\s*:\\s*\\\"(.*?)\\\"");
    private final VocabSignPlugin plugin;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public GoogleTranslationProvider(VocabSignPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public Optional<TranslationResult> translate(String word) {
        FileConfiguration cfg = plugin.getConfig();
        if (!cfg.getBoolean("translation.google.enabled", false)) {
            return Optional.empty();
        }
        String apiKey = cfg.getString("translation.google.api-key", "");
        if (apiKey == null || apiKey.isBlank()) {
            return Optional.empty();
        }

        String source = cfg.getString("translation.google.source-language", "en");
        String target = cfg.getString("translation.google.target-language", "ru");
        String body = "q=" + urlEncode(word)
                + "&source=" + urlEncode(source)
                + "&target=" + urlEncode(target)
                + "&format=text"
                + "&key=" + urlEncode(apiKey);

        HttpRequest request = HttpRequest.newBuilder(URI.create("https://translation.googleapis.com/language/translate/v2"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                Matcher matcher = TRANSLATED_TEXT_PATTERN.matcher(response.body());
                if (matcher.find()) {
                    String translated = matcher.group(1)
                            .replace("\\u003c", "<")
                            .replace("\\u003e", ">")
                            .replace("\\n", "\n");
                    return Optional.of(new TranslationResult(translated, "google"));
                }
            } else {
                plugin.getLogger().warning("Google Translate returned status " + response.statusCode());
            }
        } catch (IOException | InterruptedException ex) {
            plugin.getLogger().warning("Google translate failed: " + ex.getMessage());
            if (ex instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
        }

        return Optional.empty();
    }

    private String urlEncode(String raw) {
        return URLEncoder.encode(raw, StandardCharsets.UTF_8);
    }
}
