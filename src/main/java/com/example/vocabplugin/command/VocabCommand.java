package com.example.vocabplugin.command;

import com.example.vocabplugin.VocabSignPlugin;
import com.example.vocabplugin.VocabularyEntry;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class VocabCommand implements CommandExecutor, TabCompleter {

    private final VocabSignPlugin plugin;

    public VocabCommand(VocabSignPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("/vocab override <set|remove|list> ... | /vocab export <player> [csv|json] | /vocab reload");
            return true;
        }

        return switch (args[0].toLowerCase()) {
            case "reload" -> handleReload(sender);
            case "override" -> handleOverride(sender, args);
            case "export" -> handleExport(sender, args);
            default -> false;
        };
    }

    private boolean handleReload(CommandSender sender) {
        plugin.reloadPluginData();
        sender.sendMessage("Vocab plugin reloaded.");
        return true;
    }

    private boolean handleOverride(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("/vocab override <set|remove|list>");
            return true;
        }
        return switch (args[1].toLowerCase()) {
            case "set" -> {
                if (args.length < 4) {
                    sender.sendMessage("/vocab override set <word> <translation>");
                    yield true;
                }
                String word = args[2].toLowerCase();
                String translation = String.join(" ", java.util.Arrays.copyOfRange(args, 3, args.length));
                plugin.setOverride(word, translation);
                sender.sendMessage("Override set: " + word + " -> " + translation);
                yield true;
            }
            case "remove" -> {
                if (args.length < 3) {
                    sender.sendMessage("/vocab override remove <word>");
                    yield true;
                }
                plugin.removeOverride(args[2]);
                sender.sendMessage("Override removed: " + args[2]);
                yield true;
            }
            case "list" -> {
                sender.sendMessage("Overrides:");
                plugin.getOverrideTranslations().forEach((k, v) -> sender.sendMessage("- " + k + " -> " + v));
                yield true;
            }
            default -> false;
        };
    }

    private boolean handleExport(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("/vocab export <player> [csv|json]");
            return true;
        }
        UUID playerId = parsePlayer(args[1]);
        if (playerId == null) {
            sender.sendMessage("Unknown player: " + args[1]);
            return true;
        }
        String format = args.length >= 3 ? args[2].toLowerCase() : "csv";
        Map<String, VocabularyEntry> entries = plugin.getPlayerVocabulary(playerId);

        File exportDir = new File(plugin.getDataFolder(), "exports");
        exportDir.mkdirs();

        try {
            if ("json".equals(format)) {
                File file = new File(exportDir, playerId + ".json");
                Files.writeString(file.toPath(), toJson(entries), StandardCharsets.UTF_8);
                sender.sendMessage("Exported JSON: " + file.getName());
            } else {
                File file = new File(exportDir, playerId + ".csv");
                Files.writeString(file.toPath(), toCsv(entries), StandardCharsets.UTF_8);
                sender.sendMessage("Exported CSV: " + file.getName());
            }
        } catch (IOException ex) {
            sender.sendMessage("Export failed: " + ex.getMessage());
        }

        return true;
    }

    private UUID parsePlayer(String input) {
        try {
            return UUID.fromString(input);
        } catch (IllegalArgumentException ignored) {
        }
        OfflinePlayer player = Bukkit.getOfflinePlayer(input);
        return player.getUniqueId();
    }

    private String toCsv(Map<String, VocabularyEntry> entries) {
        StringBuilder sb = new StringBuilder("word,translation,source,added_at\n");
        for (VocabularyEntry entry : entries.values()) {
            sb.append(escape(entry.word())).append(',')
                    .append(escape(entry.translation())).append(',')
                    .append(escape(entry.source())).append(',')
                    .append(entry.addedAt()).append('\n');
        }
        return sb.toString();
    }

    private String toJson(Map<String, VocabularyEntry> entries) {
        StringBuilder sb = new StringBuilder("[\n");
        boolean first = true;
        for (VocabularyEntry e : entries.values()) {
            if (!first) {
                sb.append(",\n");
            }
            first = false;
            sb.append("  {\"word\":\"").append(json(e.word())).append("\",\"translation\":\"")
                    .append(json(e.translation())).append("\",\"source\":\"")
                    .append(json(e.source())).append("\",\"added_at\":").append(e.addedAt()).append("}");
        }
        sb.append("\n]");
        return sb.toString();
    }

    private String escape(String input) {
        return '"' + input.replace("\"", "\"\"") + '"';
    }

    private String json(String input) {
        return input.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return List.of("override", "export", "reload");
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("override")) {
            return List.of("set", "remove", "list");
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("export")) {
            return List.of("csv", "json");
        }
        return new ArrayList<>();
    }
}
