package com.example.vocabplugin;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

public record GuiStyle(
        Material validWordMaterial,
        int validWordCustomModelData,
        Material invalidWordMaterial,
        int invalidWordCustomModelData,
        String validWordDisplayName,
        String invalidWordDisplayName,
        String addedLoreLine,
        String clickToAddLoreLine,
        String invalidLoreLine,
        String title
) {
    public static GuiStyle fromConfig(ConfigurationSection section) {
        return new GuiStyle(
                parseMaterial(section.getString("valid-word.material"), Material.WRITABLE_BOOK),
                section.getInt("valid-word.custom-model-data", 0),
                parseMaterial(section.getString("invalid-word.material"), Material.BARRIER),
                section.getInt("invalid-word.custom-model-data", 0),
                section.getString("valid-word.display-name", "&a%word%"),
                section.getString("invalid-word.display-name", "&c%word%"),
                section.getString("lore.already-added", "&7Уже в словаре"),
                section.getString("lore.click-to-add", "&eНажмите, чтобы добавить"),
                section.getString("lore.invalid", "&cСлово не найдено в словаре"),
                section.getString("title", "&8English Vocabulary")
        );
    }

    public static GuiStyle defaultStyle() {
        return new GuiStyle(
                Material.WRITABLE_BOOK,
                0,
                Material.BARRIER,
                0,
                "&a%word%",
                "&c%word%",
                "&7Уже в словаре",
                "&eНажмите, чтобы добавить",
                "&cСлово не найдено в словаре",
                "&8English Vocabulary"
        );
    }

    private static Material parseMaterial(String input, Material fallback) {
        if (input == null) {
            return fallback;
        }
        Material parsed = Material.matchMaterial(input);
        return parsed == null ? fallback : parsed;
    }
}
