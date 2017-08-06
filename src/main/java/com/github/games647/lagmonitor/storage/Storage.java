package com.github.games647.lagmonitor.storage;

import com.github.games647.lagmonitor.LagMonitor;
import com.google.common.collect.Lists;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.logging.Level;

public class Storage {

    private static final String MYSQL_DRIVER = "com.mysql.jdbc.Driver";

    private static final String TPS_TABLE = "tps";
    private static final String PLAYERS_TABLE = "players";
    private static final String MONITOR_TABLE = "monitor";
    private static final String WORLDS_TABLE = "worlds";
    private static final String NATIVE_TABLE = "native";

    private final LagMonitor plugin;
    private final String jdbcUrl;
    private final String username;
    private final String password;
    private final String tablePrefix;

    public Storage(LagMonitor plugin, String host, int port, String database, String username, String password
            , String tablePrefix) {
        this.plugin = plugin;

        this.username = username;
        this.password = password;
        this.tablePrefix = tablePrefix;

        this.jdbcUrl = "jdbc:mysql://" + host + ':' + port + '/' + database;

        try {
            Class.forName(MYSQL_DRIVER);
        } catch (ClassNotFoundException ex) {
            plugin.getLogger().log(Level.SEVERE, null, ex);
        }
    }

    public void createTables() throws SQLException {
        Connection con = null;
        Statement createTpsStmt = null;
        try {
            con = getConnection();

            createTpsStmt = con.createStatement();
            createTpsStmt.execute("CREATE TABLE IF NOT EXISTS " + tablePrefix + TPS_TABLE + " ("
                    + "tps_id INTEGER UNSIGNED PRIMARY KEY AUTO_INCREMENT, "
                    + "tps FLOAT UNSIGNED NOT NULL, "
                    + "updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP"
                    + ')');

            createTpsStmt.execute("CREATE TABLE IF NOT EXISTS " + tablePrefix + MONITOR_TABLE + " ("
                    + "monitor_id INTEGER UNSIGNED PRIMARY KEY AUTO_INCREMENT, "
                    + "process_usage FLOAT UNSIGNED NOT NULL, "
                    + "os_usage FLOAT UNSIGNED NOT NULL, "
                    + "free_ram SMALLINT UNSIGNED NOT NULL, "
                    + "free_ram_pct FLOAT UNSIGNED NOT NULL, "
                    + "os_free_ram SMALLINT UNSIGNED NOT NULL, "
                    + "os_free_ram_pct FLOAT UNSIGNED NOT NULL, "
                    + "load_avg FLOAT UNSIGNED NOT NULL, "
                    + "updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP"
                    + ')');

            createTpsStmt.execute("CREATE TABLE IF NOT EXISTS " + tablePrefix + WORLDS_TABLE + " ("
                    + "world_id INTEGER UNSIGNED PRIMARY KEY AUTO_INCREMENT, "
                    + "monitor_id INTEGER UNSIGNED NOT NULL, "
                    + "world_name VARCHAR(255) NOT NULL, "
                    + "chunks_loaded SMALLINT UNSIGNED NOT NULL, "
                    + "tile_entities SMALLINT UNSIGNED NOT NULL, "
                    + "world_size SMALLINT UNSIGNED NOT NULL, "
                    + "entities INT UNSIGNED NOT NULL, "
                    + "FOREIGN KEY (monitor_id) REFERENCES " + tablePrefix + MONITOR_TABLE + "(monitor_id) "
                    + ')');

            createTpsStmt.execute("CREATE TABLE IF NOT EXISTS " + tablePrefix + PLAYERS_TABLE + " ("
                    + "world_id INTEGER UNSIGNED, "
                    + "uuid CHAR(40) NOT NULL, "
                    + "name VARCHAR(16) NOT NULL, "
                    + "ping SMALLINT UNSIGNED NOT NULL, "
                    + "PRIMARY KEY (world_id, uuid), "
                    + "FOREIGN KEY (world_id) REFERENCES " + tablePrefix + WORLDS_TABLE + "(world_id) "
                    + ')');
            
            createTpsStmt.execute("CREATE TABLE IF NOT EXISTS " + tablePrefix + NATIVE_TABLE + " ("
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
        } finally {
            closeQuietly(createTpsStmt);
            closeQuietly(con);
        }
    }

    public int saveMonitor(float procUsage, float osUsage, int freeRam, float freeRamPct, int osRam, float osRamPct
            , float loadAvg) {
        Connection con = null;
        PreparedStatement saveMonitorStmt = null;
        ResultSet generatedKeys = null;
        try {
            con = getConnection();

            saveMonitorStmt = con.prepareStatement("INSERT INTO " + tablePrefix + MONITOR_TABLE
                    + " (process_usage, os_usage, free_ram, free_ram_pct, os_free_ram, os_free_ram_pct, load_avg)"
                    + " VALUES (?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            saveMonitorStmt.setFloat(1, procUsage);
            saveMonitorStmt.setFloat(2, osUsage);
            saveMonitorStmt.setInt(3, freeRam);
            saveMonitorStmt.setFloat(4, freeRamPct);
            saveMonitorStmt.setInt(5, osRam);
            saveMonitorStmt.setFloat(6, osRamPct);
            saveMonitorStmt.setFloat(7, loadAvg);
            saveMonitorStmt.execute();

            generatedKeys = saveMonitorStmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                return generatedKeys.getInt(1);
            }
        } catch (SQLException sqlEx) {
            plugin.getLogger().log(Level.SEVERE, "Error saving monitor data to database", sqlEx);
            plugin.getLogger().log(Level.SEVERE, "Using this data {0}"
                    , Lists.newArrayList(procUsage, osUsage, freeRam, freeRamPct, osRam, osRamPct, loadAvg));
        } finally {
            closeQuietly(generatedKeys);
            closeQuietly(saveMonitorStmt);
            closeQuietly(con);
        }

        return -1;
    }

    public boolean saveWorlds(int monitorId, Collection<WorldData> worldsData) {
        if (worldsData.isEmpty()) {
            return false;
        }

        Connection con = null;
        PreparedStatement saveMonitorStmt = null;
        ResultSet generatedKeys = null;
        try {
            con = getConnection();

            saveMonitorStmt = con.prepareStatement("INSERT INTO " + tablePrefix + WORLDS_TABLE
                    + " (monitor_id, world_name, chunks_loaded, tile_entities, entities, world_size)"
                    + " VALUES (?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);

            for (WorldData worldData : worldsData) {
                saveMonitorStmt.setInt(1, monitorId);
                saveMonitorStmt.setString(2, worldData.getWorldName());
                saveMonitorStmt.setInt(3, worldData.getLoadedChunks());
                saveMonitorStmt.setInt(4, worldData.getTileEntities());
                saveMonitorStmt.setInt(5, worldData.getEntities());
                saveMonitorStmt.setInt(6, worldData.getWorldSize());
                saveMonitorStmt.addBatch();
            }

            saveMonitorStmt.executeBatch();
            generatedKeys = saveMonitorStmt.getGeneratedKeys();
            for (WorldData worldData : worldsData) {
                if (generatedKeys.next()) {
                    worldData.setRowId(generatedKeys.getInt(1));
                }
            }

            return true;
        } catch (SQLException sqlEx) {
            plugin.getLogger().log(Level.SEVERE, "Error saving worlds data to database", sqlEx);
            plugin.getLogger().log(Level.SEVERE, "Using this data {0}", worldsData);
        } finally {
            closeQuietly(generatedKeys);
            closeQuietly(saveMonitorStmt);
            closeQuietly(con);
        }

        return false;
    }

    public boolean savePlayers(int monitorId, Collection<PlayerData> playerData) {
        if (playerData.isEmpty()) {
            return false;
        }

        Connection con = null;
        PreparedStatement saveMonitorStmt = null;
        ResultSet generatedKeys = null;
        try {
            con = getConnection();

            saveMonitorStmt = con.prepareStatement("INSERT INTO " + tablePrefix + PLAYERS_TABLE
                    + " (world_id, uuid, name, ping) "
                    + "VALUES (?, ?, ?, ?)");

            for (PlayerData data : playerData) {
                saveMonitorStmt.setInt(1, data.getWorldId());
                saveMonitorStmt.setString(2, data.getUuid().toString());
                saveMonitorStmt.setString(3, data.getPlayerName());
                saveMonitorStmt.setInt(4, data.getPing());
                saveMonitorStmt.addBatch();
            }

            saveMonitorStmt.executeBatch();
            return true;
        } catch (SQLException sqlEx) {
            plugin.getLogger().log(Level.SEVERE, "Error saving player data to database", sqlEx);
            plugin.getLogger().log(Level.SEVERE, "Using this data {0}", playerData);
        } finally {
            closeQuietly(generatedKeys);
            closeQuietly(saveMonitorStmt);
            closeQuietly(con);
        }

        return false;
    }

    public void saveNative(int mcRead, int mcWrite, long freeSpace, float freePct, int diskRead, int diskWrite
            , int netRead, int netWrite) {
        Connection con = null;
        PreparedStatement saveNativeStmt = null;
        try {
            con = getConnection();

            saveNativeStmt = con.prepareStatement("INSERT INTO " + tablePrefix + NATIVE_TABLE
                    + " (mc_read, mc_write, free_space, free_space_pct, disk_read, disk_write, net_read, net_write)"
                    + " VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
            saveNativeStmt.setInt(1, mcRead);
            saveNativeStmt.setInt(2, mcWrite);

            saveNativeStmt.setInt(3, (int) freeSpace);

            saveNativeStmt.setFloat(4, freePct);

            saveNativeStmt.setInt(5, diskRead);
            saveNativeStmt.setInt(6, diskWrite);

            saveNativeStmt.setInt(7, netRead);
            saveNativeStmt.setInt(8, netWrite);
            saveNativeStmt.execute();
        } catch (SQLException sqlEx) {
            plugin.getLogger().log(Level.SEVERE, "Error saving native stats to database", sqlEx);
            plugin.getLogger().log(Level.SEVERE, "Using this data {0}"
                    , Lists.newArrayList(mcRead, mcWrite, freeSpace, freePct, diskRead, diskWrite, netRead, netWrite));
        } finally {
            closeQuietly(saveNativeStmt);
            closeQuietly(con);
        }
    }

    public void saveTps(float tps) {
        Connection con = null;
        PreparedStatement saveTpsStmt = null;
        try {
            con = getConnection();

            saveTpsStmt = con.prepareStatement("INSERT INTO " + tablePrefix + TPS_TABLE + " (tps) VALUES (?)");
            saveTpsStmt.setFloat(1, tps);
            saveTpsStmt.execute();
        } catch (SQLException sqlEx) {
            plugin.getLogger().log(Level.SEVERE, "Error saving tps to database", sqlEx);
            plugin.getLogger().log(Level.SEVERE, "Using this data {0}", new Object[] {tps});
        } finally {
            closeQuietly(saveTpsStmt);
            closeQuietly(con);
        }
    }

    private Connection getConnection() throws SQLException {

        return DriverManager.getConnection(jdbcUrl, username, password);
    }

    private void closeQuietly(AutoCloseable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception closeEx) {
                plugin.getLogger().log(Level.SEVERE, "Failed to close connection", closeEx);
            }
        }
    }
}
