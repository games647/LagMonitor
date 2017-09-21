package com.github.games647.lagmonitor.storage;

import com.github.games647.lagmonitor.LagMonitor;
import com.google.common.collect.Lists;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.logging.Level;

public class Storage {

    private static final String TPS_TABLE = "tps";
    private static final String PLAYERS_TABLE = "players";
    private static final String MONITOR_TABLE = "monitor";
    private static final String WORLDS_TABLE = "worlds";
    private static final String NATIVE_TABLE = "native";

    private final MysqlDataSource dataSource;

    private final LagMonitor plugin;
    private final String tablePrefix;

    public Storage(LagMonitor plugin, String host, int port, String database, String username, String password
            , String tablePrefix) {
        this.plugin = plugin;

        this.tablePrefix = tablePrefix;

        this.dataSource = new MysqlDataSource();
        this.dataSource.setUser(username);
        this.dataSource.setPassword(password);

        this.dataSource.setServerName(host);
        this.dataSource.setPort(port);
        this.dataSource.setDatabaseName(database);
    }

    public void createTables() throws SQLException {
        try (Connection con = dataSource.getConnection(); Statement tableStmt = con.createStatement()) {
            tableStmt.execute("CREATE TABLE IF NOT EXISTS " + tablePrefix + TPS_TABLE + " ("
                    + "tps_id INTEGER UNSIGNED PRIMARY KEY AUTO_INCREMENT, "
                    + "tps FLOAT UNSIGNED NOT NULL, "
                    + "updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP"
                    + ')');

            tableStmt.execute("CREATE TABLE IF NOT EXISTS " + tablePrefix + MONITOR_TABLE + " ("
                    + "monitor_id INTEGER UNSIGNED PRIMARY KEY AUTO_INCREMENT, "
                    + "process_usage FLOAT UNSIGNED NOT NULL, "
                    + "os_usage FLOAT UNSIGNED NOT NULL, "
                    + "free_ram MEDIUMINT UNSIGNED NOT NULL, "
                    + "free_ram_pct FLOAT UNSIGNED NOT NULL, "
                    + "os_free_ram MEDIUMINT UNSIGNED NOT NULL, "
                    + "os_free_ram_pct FLOAT UNSIGNED NOT NULL, "
                    + "load_avg FLOAT UNSIGNED NOT NULL, "
                    + "updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP"
                    + ')');

            tableStmt.execute("CREATE TABLE IF NOT EXISTS " + tablePrefix + WORLDS_TABLE + " ("
                    + "world_id INTEGER UNSIGNED PRIMARY KEY AUTO_INCREMENT, "
                    + "monitor_id INTEGER UNSIGNED NOT NULL, "
                    + "world_name VARCHAR(255) NOT NULL, "
                    + "chunks_loaded SMALLINT UNSIGNED NOT NULL, "
                    + "tile_entities SMALLINT UNSIGNED NOT NULL, "
                    + "world_size SMALLINT UNSIGNED NOT NULL, "
                    + "entities INT UNSIGNED NOT NULL, "
                    + "FOREIGN KEY (monitor_id) REFERENCES " + tablePrefix + MONITOR_TABLE + "(monitor_id) "
                    + ')');

            tableStmt.execute("CREATE TABLE IF NOT EXISTS " + tablePrefix + PLAYERS_TABLE + " ("
                    + "world_id INTEGER UNSIGNED, "
                    + "uuid CHAR(40) NOT NULL, "
                    + "name VARCHAR(16) NOT NULL, "
                    + "ping SMALLINT UNSIGNED NOT NULL, "
                    + "PRIMARY KEY (world_id, uuid), "
                    + "FOREIGN KEY (world_id) REFERENCES " + tablePrefix + WORLDS_TABLE + "(world_id) "
                    + ')');

            tableStmt.execute("CREATE TABLE IF NOT EXISTS " + tablePrefix + NATIVE_TABLE + " ("
                    + "native_id INTEGER UNSIGNED PRIMARY KEY AUTO_INCREMENT, "
                    + "mc_read SMALLINT UNSIGNED , "
                    + "mc_write SMALLINT UNSIGNED, "
                    + "free_space INT UNSIGNED, "
                    + "free_space_pct FLOAT UNSIGNED, "
                    + "disk_read SMALLINT UNSIGNED, "
                    + "disk_write SMALLINT UNSIGNED, "
                    + "net_read SMALLINT UNSIGNED, "
                    + "net_write SMALLINT UNSIGNED, "
                    + "updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP"
                    + ')');
        }
    }

    public int saveMonitor(float procUsage, float osUsage, int freeRam, float freeRamPct, int osRam, float osRamPct
            , float loadAvg) {
        try (Connection con = dataSource.getConnection();
             PreparedStatement stmt = con.prepareStatement("INSERT INTO " + tablePrefix + MONITOR_TABLE
                     + " (process_usage, os_usage, free_ram, free_ram_pct, os_free_ram, os_free_ram_pct, load_avg)"
                     + " VALUES (?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
            stmt.setFloat(1, procUsage);
            stmt.setFloat(2, osUsage);
            stmt.setInt(3, freeRam);
            stmt.setFloat(4, freeRamPct);
            stmt.setInt(5, osRam);
            stmt.setFloat(6, osRamPct);
            stmt.setFloat(7, loadAvg);
            stmt.execute();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }
        } catch (SQLException sqlEx) {
            plugin.getLogger().log(Level.SEVERE, "Error saving monitor data to database", sqlEx);
            plugin.getLogger().log(Level.SEVERE, "Using this data {0}"
                    , Lists.newArrayList(procUsage, osUsage, freeRam, freeRamPct, osRam, osRamPct, loadAvg));
        }

        return -1;
    }

    public boolean saveWorlds(int monitorId, Collection<WorldData> worldsData) {
        if (worldsData.isEmpty()) {
            return false;
        }

        try (Connection con = dataSource.getConnection();
             PreparedStatement stmt = con.prepareStatement("INSERT INTO " + tablePrefix + WORLDS_TABLE
                     + " (monitor_id, world_name, chunks_loaded, tile_entities, entities, world_size)"
                     + " VALUES (?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
            for (WorldData worldData : worldsData) {
                stmt.setInt(1, monitorId);
                stmt.setString(2, worldData.getWorldName());
                stmt.setInt(3, worldData.getLoadedChunks());
                stmt.setInt(4, worldData.getTileEntities());
                stmt.setInt(5, worldData.getEntities());
                stmt.setInt(6, worldData.getWorldSize());
                stmt.addBatch();
            }

            stmt.executeBatch();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                for (WorldData worldData : worldsData) {
                    if (generatedKeys.next()) {
                        worldData.setRowId(generatedKeys.getInt(1));
                    }
                }
            }

            return true;
        } catch (SQLException sqlEx) {
            plugin.getLogger().log(Level.SEVERE, "Error saving worlds data to database", sqlEx);
            plugin.getLogger().log(Level.SEVERE, "Using this data {0}", worldsData);
        }

        return false;
    }

    public boolean savePlayers(Collection<PlayerData> playerData) {
        if (playerData.isEmpty()) {
            return false;
        }

        try (Connection con = dataSource.getConnection();
            PreparedStatement stmt = con.prepareStatement("INSERT INTO " + tablePrefix + PLAYERS_TABLE
                    + " (world_id, uuid, name, ping) "
                    + "VALUES (?, ?, ?, ?)")) {
            for (PlayerData data : playerData) {
                stmt.setInt(1, data.getWorldId());
                stmt.setString(2, data.getUuid().toString());
                stmt.setString(3, data.getPlayerName());
                stmt.setInt(4, data.getPing());
                stmt.addBatch();
            }

            stmt.executeBatch();
            return true;
        } catch (SQLException sqlEx) {
            plugin.getLogger().log(Level.SEVERE, "Error saving player data to database", sqlEx);
            plugin.getLogger().log(Level.SEVERE, "Using this data {0}", playerData);
        }

        return false;
    }

    public void saveNative(int mcRead, int mcWrite, long freeSpace, float freePct, int diskRead, int diskWrite
            , int netRead, int netWrite) {
        try (Connection con = dataSource.getConnection();
            PreparedStatement stmt = con.prepareStatement("INSERT INTO " + tablePrefix + NATIVE_TABLE
                    + " (mc_read, mc_write, free_space, free_space_pct, disk_read, disk_write, net_read, net_write)"
                    + " VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {
            stmt.setInt(1, mcRead);
            stmt.setInt(2, mcWrite);

            stmt.setInt(3, (int) freeSpace);

            stmt.setFloat(4, freePct);

            stmt.setInt(5, diskRead);
            stmt.setInt(6, diskWrite);

            stmt.setInt(7, netRead);
            stmt.setInt(8, netWrite);
            stmt.execute();
        } catch (SQLException sqlEx) {
            plugin.getLogger().log(Level.SEVERE, "Error saving native stats to database", sqlEx);
            plugin.getLogger().log(Level.SEVERE, "Using this data {0}"
                    , Lists.newArrayList(mcRead, mcWrite, freeSpace, freePct, diskRead, diskWrite, netRead, netWrite));
        }
    }

    public void saveTps(float tps) {
        try (Connection con = dataSource.getConnection();
             PreparedStatement stmt = con.prepareStatement("INSERT INTO " + tablePrefix
                     + TPS_TABLE + " (tps) VALUES (?)")) {
            stmt.setFloat(1, tps);
            stmt.execute();
        } catch (SQLException sqlEx) {
            plugin.getLogger().log(Level.SEVERE, "Error saving tps to database", sqlEx);
            plugin.getLogger().log(Level.SEVERE, "Using this data {0}", new Object[] {tps});
        }
    }
}
