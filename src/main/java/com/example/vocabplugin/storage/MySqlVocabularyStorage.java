package com.example.vocabplugin.storage;

import com.example.vocabplugin.VocabularyEntry;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MySqlVocabularyStorage implements VocabularyStorage {

    private final HikariDataSource dataSource;
    private final String tableName;

    public MySqlVocabularyStorage(FileConfiguration config) {
        HikariConfig hikari = new HikariConfig();
        String host = config.getString("storage.mysql.host", "127.0.0.1");
        int port = config.getInt("storage.mysql.port", 3306);
        String database = config.getString("storage.mysql.database", "minecraft");
        String user = config.getString("storage.mysql.username", "root");
        String password = config.getString("storage.mysql.password", "");

        hikari.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&allowPublicKeyRetrieval=true");
        hikari.setUsername(user);
        hikari.setPassword(password);
        hikari.setMaximumPoolSize(config.getInt("storage.mysql.pool-size", 5));
        hikari.setPoolName("VocabSignPool");
        this.dataSource = new HikariDataSource(hikari);
        this.tableName = config.getString("storage.mysql.table", "vocab_entries");
        initTable(this.tableName);
    }

    @Override
    public Map<String, VocabularyEntry> load(UUID playerId) {
        String table = table();
        String sql = "SELECT word, translation, source, added_at FROM " + table + " WHERE player_uuid = ?";
        Map<String, VocabularyEntry> result = new HashMap<>();
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, playerId.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String word = rs.getString("word");
                    String translation = rs.getString("translation");
                    String source = rs.getString("source");
                    long addedAt = rs.getLong("added_at");
                    result.put(word, new VocabularyEntry(word, translation, source, addedAt));
                }
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to load vocabulary for " + playerId, ex);
        }
        return result;
    }

    @Override
    public void save(UUID playerId, Map<String, VocabularyEntry> entries) {
        String table = table();
        String deleteSql = "DELETE FROM " + table + " WHERE player_uuid = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement delete = conn.prepareStatement(deleteSql)) {
            delete.setString(1, playerId.toString());
            delete.executeUpdate();
            for (VocabularyEntry entry : entries.values()) {
                insert(conn, table, playerId, entry);
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to save vocabulary for " + playerId, ex);
        }
    }

    @Override
    public void saveEntry(UUID playerId, VocabularyEntry entry) {
        String table = table();
        try (Connection conn = dataSource.getConnection()) {
            insert(conn, table, playerId, entry);
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to upsert entry for " + playerId, ex);
        }
    }

    @Override
    public void close() {
        dataSource.close();
    }

    private void insert(Connection conn, String table, UUID playerId, VocabularyEntry entry) throws SQLException {
        String sql = "INSERT INTO " + table + "(player_uuid, word, translation, source, added_at) VALUES(?,?,?,?,?) "
                + "ON DUPLICATE KEY UPDATE translation=VALUES(translation), source=VALUES(source), added_at=VALUES(added_at)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, playerId.toString());
            ps.setString(2, entry.word());
            ps.setString(3, entry.translation());
            ps.setString(4, entry.source());
            ps.setLong(5, entry.addedAt());
            ps.executeUpdate();
        }
    }

    private void initTable(String table) {
        String sql = "CREATE TABLE IF NOT EXISTS " + table + " ("
                + "player_uuid VARCHAR(36) NOT NULL,"
                + "word VARCHAR(80) NOT NULL,"
                + "translation VARCHAR(255) NOT NULL,"
                + "source VARCHAR(32) NOT NULL,"
                + "added_at BIGINT NOT NULL,"
                + "PRIMARY KEY (player_uuid, word)"
                + ")";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.execute();
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to init MySQL table", ex);
        }
    }

    private String table() {
        return tableName;
    }
}
