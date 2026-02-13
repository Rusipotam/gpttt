package com.example.vocabplugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class VocabularyGuiFactory {

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacyAmpersand();

    private VocabularyGuiFactory() {
    }

    public static Inventory create(VocabSignPlugin plugin, Player player, List<String> words) {
        int rows = Math.clamp((int) Math.ceil(words.size() / 9.0), 1, 6);
        Inventory inventory = plugin.getServer().createInventory(
                new VocabularyGuiHolder(words),
                rows * 9,
                LEGACY.deserialize(plugin.getGuiStyle().title())
        );

        Map<String, VocabularyEntry> userWords = plugin.getPlayerVocabulary(player.getUniqueId());

        for (int i = 0; i < words.size() && i < inventory.getSize(); i++) {
            String word = words.get(i);
            boolean valid = plugin.isWordValid(word);
            boolean alreadyAdded = userWords.containsKey(word);
            inventory.setItem(i, buildItem(plugin, word, valid, alreadyAdded));
        }

        return inventory;
    }

    private static ItemStack buildItem(VocabSignPlugin plugin, String word, boolean valid, boolean alreadyAdded) {
        GuiStyle style = plugin.getGuiStyle();
        Material material = valid ? style.validWordMaterial() : style.invalidWordMaterial();
        int cmd = valid ? style.validWordCustomModelData() : style.invalidWordCustomModelData();

        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();
        String displayNameTemplate = valid ? style.validWordDisplayName() : style.invalidWordDisplayName();
        meta.displayName(LEGACY.deserialize(displayNameTemplate.replace("%word%", word)));

        List<Component> lore = new ArrayList<>();
        if (!valid) {
            lore.add(LEGACY.deserialize(style.invalidLoreLine()));
        } else if (alreadyAdded) {
            lore.add(LEGACY.deserialize(style.addedLoreLine()));
        } else {
            lore.add(LEGACY.deserialize(style.clickToAddLoreLine()));
            meta.getPersistentDataContainer().set(plugin.getAddWordKey(), PersistentDataType.STRING, word);
        }
        meta.lore(lore);

        if (cmd > 0) {
            meta.setCustomModelData(cmd);
        }
        stack.setItemMeta(meta);
        return stack;
    }
}
