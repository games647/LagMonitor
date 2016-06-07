package com.github.games647.lagmonitor.storage;

import com.github.games647.lagmonitor.LagMonitor;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.List;
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

    public Storage(LagMonitor plugin, String host, int port, String database, String username, String password) {
        this.plugin = plugin;

        this.username = username;
        this.password = password;

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
            createTpsStmt.executeQuery("CREATE TABLE IF NOT EXISTS " + TPS_TABLE + " ("
                    + "tps_id INTEGER UNSIGNED PRIMARY KEY AUTO_INCREMENT, "
                    + "tps UNSIGNED FLOAT NOT NULL, "
                    + "updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP"
                    + ")");

            createTpsStmt.executeQuery("CREATE TABLE IF NOT EXISTS " + MONITOR_TABLE + " ("
                    + "monitor_id INTEGER UNSIGNED PRIMARY KEY AUTO_INCREMENT, "
                    + "process_usage UNSIGNED FLOAT NOT NULL, "
                    + "os_usage UNSIGNED FLOAT NOT NULL, "
                    + "free_ram UNSIGNED SMALLINT NOT NULL, "
                    + "free_ram_pct UNSIGNED FLOAT NOT NULL, "
                    + "os_free_ram UNSIGNED SMALLINT NOT NULL, "
                    + "os_free_ram_pct UNSIGNED FLOAT NOT NULL, "
                    + "load_avg UNSIGNED FLOAT NOT NULL, "
                    + "updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP"
                    + ")");

            createTpsStmt.executeQuery("CREATE TABLE IF NOT EXISTS " + WORLDS_TABLE + " ("
                    + "world_id INTEGER UNSIGNED PRIMARY KEY AUTO_INCREMENT, "
                    + "monitor_id INTEGER UNSIGNED, "
                    + "world_name UNSIGNED VARCHAR NOT NULL, "
                    + "chunks_loaded UNSIGNED SMALLINT NOT NULL, "
                    + "tile_entities UNSIGNED SMALLINT NOT NULL, "
                    + "world_size UNSIGNED SMALLINT NOT NULL, "
                    + "entities UNSIGNED INT NOT NULL, "
                    + "FOREIGN KEY (monitor_id) REFERENCES " + MONITOR_TABLE + "(monitor_id) "
                    + ")");

            createTpsStmt.executeQuery("CREATE TABLE IF NOT EXISTS " + PLAYERS_TABLE + " ("
                    + "monitor_id INTEGER UNSIGNED, "
                    + "world_id INTEGER UNSIGNED, "
                    + "uuid UNSIGNED CHAR(40) NOT NULL, "
                    + "name UNSIGNED VARCHAR(16) NOT NULL, "
                    + "ping UNSIGNED SMALLINT NOT NULL, "
                    + "PRIMARY KEY (monitor_id, uuid), "
                    + "FOREIGN KEY (monitor_id) REFERENCES " + MONITOR_TABLE + "(monitor_id) "
                    + ")");
            
            createTpsStmt.executeQuery("CREATE TABLE IF NOT EXISTS " + NATIVE_TABLE + " ("
                    + "native_id INTEGER UNSIGNED PRIMARY KEY AUTO_INCREMENT, "
                    + "mc_read UNSIGNED SMALLINT, "
                    + "mc_write UNSIGNED SMALLINT, "
                    + "free_space UNSIGNED INT, "
                    + "free_space_pct UNSIGNED FLOAT, "
                    + "disk_read UNSIGNED SMALLINT, "
                    + "disk_write UNSIGNED SMALLINT, "
                    + "net_read UNSIGNED SMALLINT, "
                    + "net_write UNSIGNED SMALLINT, "
                    + "updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP"
                    + ")");
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

            saveMonitorStmt = con.prepareStatement("INSERT INTO " + MONITOR_TABLE
                    + " (process_usage, os_usage, free_ram, free_ram_pct, os_free_ram, os_free_ram_pct, load_avg)"
                    + " VALUES (?, ?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
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
            plugin.getLogger().log(Level.SEVERE, "Error saving tps to database", sqlEx);
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

            saveMonitorStmt = con.prepareStatement("INSERT INTO " + WORLDS_TABLE
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
            plugin.getLogger().log(Level.SEVERE, "Error saving tps to database", sqlEx);
        } finally {
            closeQuietly(generatedKeys);
            closeQuietly(saveMonitorStmt);
            closeQuietly(con);
        }

        return false;
    }

    public boolean savePlayers(int monitorId, List<PlayerData> playerData) {
        if (playerData.isEmpty()) {
            return false;
        }

        Connection con = null;
        PreparedStatement saveMonitorStmt = null;
        ResultSet generatedKeys = null;
        try {
            con = getConnection();

            saveMonitorStmt = con.prepareStatement("INSERT INTO " + MONITOR_TABLE
                    + " (world_id, uuid, name, ping) VALUES (?, ?, ?, ?)");

            for (PlayerData data : playerData) {
                saveMonitorStmt.setInt(1, data.getWorldId());
                saveMonitorStmt.setString(2, data.getUuid().toString());
                saveMonitorStmt.setString(3, data.getPlayerName());
                saveMonitorStmt.setInt(3, data.getPing());
                saveMonitorStmt.addBatch();
            }

            saveMonitorStmt.executeBatch();
            return true;
        } catch (SQLException sqlEx) {
            plugin.getLogger().log(Level.SEVERE, "Error saving tps to database", sqlEx);
        } finally {
            closeQuietly(generatedKeys);
            closeQuietly(saveMonitorStmt);
            closeQuietly(con);
        }

        return false;
    }

    public void saveNative(int mcRead, int mcWrite, int freeSpace, float freePct, int diskRead, int diskWrite
            , int netRead, int netWrite) {
        Connection con = null;
        PreparedStatement saveNativeStmt = null;
        try {
            con = getConnection();

            saveNativeStmt = con.prepareStatement("INSERT INTO " + NATIVE_TABLE 
                    + " (mc_read, mc_write, free_space, free_space_pct, disk_read, disk_write, net_read, net_write)"
                    + " VALUES (?, ?, ?, , ?, ?, ?, ?, ?)");
            saveNativeStmt.setInt(1, mcRead);
            saveNativeStmt.setInt(2, mcWrite);

            saveNativeStmt.setInt(3, freeSpace);

            saveNativeStmt.setFloat(4, freePct);

            saveNativeStmt.setInt(5, diskRead);
            saveNativeStmt.setInt(6, diskWrite);

            saveNativeStmt.setInt(7, netRead);
            saveNativeStmt.setInt(8, netWrite);
            saveNativeStmt.execute();
        } catch (SQLException sqlEx) {
            plugin.getLogger().log(Level.SEVERE, "Error saving native stats to database", sqlEx);
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

            saveTpsStmt = con.prepareStatement("INSERT INTO " + TPS_TABLE + " (tps) VALUES (?)");
            saveTpsStmt.setFloat(1, tps);
            saveTpsStmt.execute();
        } catch (SQLException sqlEx) {
            plugin.getLogger().log(Level.SEVERE, "Error saving tps to database", sqlEx);
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
