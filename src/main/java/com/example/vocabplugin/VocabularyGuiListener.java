package com.example.vocabplugin;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class VocabularyGuiListener implements Listener {

    private final VocabSignPlugin plugin;

    public VocabularyGuiListener(VocabSignPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory inventory = event.getInventory();
        if (!(inventory.getHolder() instanceof VocabularyGuiHolder)) {
            return;
        }
        event.setCancelled(true);

        ItemStack current = event.getCurrentItem();
        if (current == null || !current.hasItemMeta()) {
            return;
        }

        String word = current.getItemMeta().getPersistentDataContainer()
                .get(plugin.getAddWordKey(), PersistentDataType.STRING);
        if (word == null) {
            return;
        }

        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        player.closeInventory();
        player.sendMessage(Component.text("Перевожу и сохраняю слово: " + word + "..."));

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            VocabularyEntry entry = plugin.addWord(player.getUniqueId(), word);
            plugin.getServer().getScheduler().runTask(plugin, () ->
                    player.sendMessage(Component.text("Добавлено: " + entry.word() + " -> " + entry.translation() + " [" + entry.source() + "]")));
        });
    }
}
