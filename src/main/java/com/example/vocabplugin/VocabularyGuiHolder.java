package com.example.vocabplugin;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.List;

public record VocabularyGuiHolder(List<String> words) implements InventoryHolder {
    @Override
    public Inventory getInventory() {
        return null;
    }
}
