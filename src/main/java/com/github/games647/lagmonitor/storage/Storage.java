package com.github.games647.lagmonitor.storage;

import com.google.common.collect.Lists;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Storage {

    private static final String TPS_TABLE = "tps";
    private static final String PLAYERS_TABLE = "players";
    private static final String MONITOR_TABLE = "monitor";
    private static final String WORLDS_TABLE = "worlds";
    private static final String NATIVE_TABLE = "native";

    private final MysqlDataSource dataSource;

    private final Logger logger;
    private final String prefix;

    public Storage(Logger logger, String host, int port, String database, boolean usessl,
                   String user, String pass, String prefix) {
        this.logger = logger;

        this.prefix = prefix;

        this.dataSource = new MysqlDataSource();
        this.dataSource.setUser(user);
        this.dataSource.setPassword(pass);

        this.dataSource.setServerName(host);
        this.dataSource.setPort(port);
        this.dataSource.setDatabaseName(database);
        this.dataSource.setUseSSL(usessl);

        this.dataSource.setCachePrepStmts(true);
        this.dataSource.setUseServerPreparedStmts(true);
    }

    public void createTables() throws SQLException {
        try (InputStream in = getClass().getResourceAsStream("/create.sql");
             BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
             Connection con = dataSource.getConnection();
             Statement stmt = con.createStatement()) {
            StringBuilder builder = new StringBuilder();

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("#")) continue;

                builder.append(line);
                if (line.endsWith(";")) {
                    stmt.addBatch(builder.toString().replace("{prefix}", prefix));
                    builder = new StringBuilder();
                }
            }

            stmt.executeBatch();
        } catch (IOException ioEx) {
            logger.log(Level.SEVERE, "Failed to load migration file", ioEx);
        }
    }

    public int saveMonitor(float procUsage, float osUsage, int freeRam, float freeRamPct, int osRam, float osRamPct
            , float loadAvg) {
        try (Connection con = dataSource.getConnection();
             PreparedStatement stmt = con.prepareStatement("INSERT INTO " + prefix + MONITOR_TABLE
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
            logger.log(Level.SEVERE, "Error saving monitor data to database", sqlEx);
            logger.log(Level.SEVERE, "Using this data {0}"
                    , Lists.newArrayList(procUsage, osUsage, freeRam, freeRamPct, osRam, osRamPct, loadAvg));
        }

        return -1;
    }

    public boolean saveWorlds(int monitorId, Collection<WorldData> worldsData) {
        if (worldsData.isEmpty()) {
            return false;
        }

        try (Connection con = dataSource.getConnection();
             PreparedStatement stmt = con.prepareStatement("INSERT INTO " + prefix + WORLDS_TABLE
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
            logger.log(Level.SEVERE, "Error saving worlds data to database", sqlEx);
            logger.log(Level.SEVERE, "Using this data {0}", worldsData);
        }

        return false;
    }

    public void savePlayers(Collection<PlayerData> playerData) {
        if (playerData.isEmpty()) {
            return;
        }

        try (Connection con = dataSource.getConnection();
             PreparedStatement stmt = con.prepareStatement("INSERT INTO " + prefix + PLAYERS_TABLE
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
        } catch (SQLException sqlEx) {
            logger.log(Level.SEVERE, "Error saving player data to database", sqlEx);
            logger.log(Level.SEVERE, "Using this data {0}", playerData);
        }

    }

    public void saveNative(int mcRead, int mcWrite, int freeSpace, float freePct, int diskRead, int diskWrite
            , int netRead, int netWrite) {
        try (Connection con = dataSource.getConnection();
             PreparedStatement stmt = con.prepareStatement("INSERT INTO " + prefix + NATIVE_TABLE
                     + " (mc_read, mc_write, free_space, free_space_pct, disk_read, disk_write, net_read, net_write)"
                     + " VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {
            stmt.setInt(1, mcRead);
            stmt.setInt(2, mcWrite);

            stmt.setInt(3, freeSpace);

            stmt.setFloat(4, freePct);

            stmt.setInt(5, diskRead);
            stmt.setInt(6, diskWrite);

            stmt.setInt(7, netRead);
            stmt.setInt(8, netWrite);
            stmt.execute();
        } catch (SQLException sqlEx) {
            logger.log(Level.SEVERE, "Error saving native stats to database", sqlEx);
            logger.log(Level.SEVERE, "Using this data {0}"
                    , Lists.newArrayList(mcRead, mcWrite, freeSpace, freePct, diskRead, diskWrite, netRead, netWrite));
        }
    }

    public void saveTps(float tps) {
        try (Connection con = dataSource.getConnection();
             PreparedStatement stmt = con.prepareStatement("INSERT INTO " + prefix
                     + TPS_TABLE + " (tps) VALUES (?)")) {
            stmt.setFloat(1, tps);
            stmt.execute();
        } catch (SQLException sqlEx) {
            logger.log(Level.SEVERE, "Error saving tps to database", sqlEx);
            logger.log(Level.SEVERE, "Using this data {0}", new Object[]{tps});
        }
    }
}
