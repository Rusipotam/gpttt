package com.example.vocabplugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignInteractListener implements Listener {

    private static final Pattern WORD_PATTERN = Pattern.compile("[A-Za-z]+(?:'[A-Za-z]+)?");
    private static final PlainTextComponentSerializer PLAIN = PlainTextComponentSerializer.plainText();
    private final VocabSignPlugin plugin;

    public SignInteractListener(VocabSignPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onSignRightClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getClickedBlock() == null) {
            return;
        }

        Block block = event.getClickedBlock();
        if (!(block.getState() instanceof Sign sign)) {
            return;
        }
        if (!sign.isWaxed()) {
            return;
        }

        Set<String> words = new LinkedHashSet<>();
        collectWords(sign.getSide(Side.FRONT).lines(), words);
        collectWords(sign.getSide(Side.BACK).lines(), words);

        if (words.isEmpty()) {
            return;
        }

        Player player = event.getPlayer();
        event.setCancelled(true);
        List<String> orderedWords = new ArrayList<>(words);
        Inventory gui = VocabularyGuiFactory.create(plugin, player, orderedWords);
        player.openInventory(gui);
    }

    private void collectWords(List<Component> lines, Set<String> destination) {
        for (Component line : lines) {
            String text = PLAIN.serialize(line);
            Matcher matcher = WORD_PATTERN.matcher(text);
            while (matcher.find()) {
                destination.add(matcher.group().toLowerCase(Locale.ROOT));
            }
        }
    }
}
