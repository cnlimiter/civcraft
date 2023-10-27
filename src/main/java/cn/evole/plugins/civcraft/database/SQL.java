/**
 * [2023] Cnlimiter LLC
 * All Rights Reserved.
 * Name: civcraft
 * Author: cnlimiter
 * UpdateTime: 2023/10/14 16:15
 * Description:
 * License: ARR
 */
package cn.evole.plugins.civcraft.database;

import cn.evole.plugins.civcraft.camp.Camp;
import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.config.ConfigMarketItem;
import cn.evole.plugins.civcraft.event.EventTimer;
import cn.evole.plugins.civcraft.exception.InvalidConfiguration;
import cn.evole.plugins.civcraft.items.BonusGoodie;
import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.main.CivLog;
import cn.evole.plugins.civcraft.object.*;
import cn.evole.plugins.civcraft.permission.PermissionGroup;
import cn.evole.plugins.civcraft.randomevents.RandomEvent;
import cn.evole.plugins.civcraft.road.RoadBlock;
import cn.evole.plugins.civcraft.sessiondb.SessionDatabase;
import cn.evole.plugins.civcraft.structure.Structure;
import cn.evole.plugins.civcraft.structure.wonders.Wonder;
import cn.evole.plugins.civcraft.threading.TaskMaster;
import cn.evole.plugins.civcraft.util.BiomeCache;
import cn.evole.plugins.global.perks.PerkManager;
import cn.evole.plugins.global.perks.PerkManagerSimple;
import cn.evole.plugins.global.scores.ScoreManager;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class SQL {

    public static String hostname = "";
    public static String port = "";
    public static String db_name = "";
    public static String username = "";
    public static String password = "";
    public static boolean useSSL = false;
    public static String tb_prefix = "";
    private static String dsn = "";


    public static String global_dsn = "";
    public static String global_hostname = "";
    public static String global_port = "";
    public static String global_username = "";
    public static String global_password = "";
    public static String global_db = "";
    public static boolean global_useSSL = false;

    public static String perks_dsn = "";
    public static String perks_hostname = "";
    public static String perks_port = "";
    public static String perks_username = "";
    public static String perks_password = "";
    public static String perks_db = "";
    public static boolean perks_useSSL = false;

    public static String driver = "";
    public static String type = "";
    public static ConnectionPool gameDatabase;
    public static ConnectionPool globalDatabase;
    public static ConnectionPool perkDatabase;

    public static void initialize(Plugin plugin) throws InvalidConfiguration, SQLException, ClassNotFoundException {
        CivLog.heading("Initializing SQL");
        SQL.type = CivSettings.getStringBase("sql_type");
        SQL.driver = CivSettings.getStringBase("sql_driver");

        SQL.useSSL = CivSettings.getBooleanBase("game.useSSL");
        SQL.hostname = CivSettings.getStringBase("game.hostname");
        SQL.port = CivSettings.getStringBase("game.port");
        SQL.db_name = CivSettings.getStringBase("game.database");
        SQL.username = CivSettings.getStringBase("game.username");
        SQL.password = CivSettings.getStringBase("game.password");
        SQL.tb_prefix = CivSettings.getStringBase("game.table_prefix");
        SQL.dsn = "jdbc:"+ SQL.type +"://" + hostname + ":" + port + "/" + tb_prefix + db_name + "?useSSL=" + SQL.useSSL + "&requireSSL=" + SQL.useSSL;
        CivLog.info("\t Using " + SQL.hostname + ":" + SQL.port + " user:" + SQL.username + " DB:" + SQL.db_name);
        CivLog.info("\t Building Connection Pool for GAME database.");
        gameDatabase = new ConnectionPool(SQL.driver, SQL.dsn, SQL.username, SQL.password);
        CivLog.info("\t Connected to GAME database");

        CivLog.heading("Initializing Global SQL Database");
        SQL.global_useSSL = CivSettings.getBooleanBase("global.useSSL");
        SQL.global_hostname = CivSettings.getStringBase("global.hostname");
        SQL.global_port = CivSettings.getStringBase("global.port");
        SQL.global_username = CivSettings.getStringBase("global.username");
        SQL.global_password = CivSettings.getStringBase("global.password");
        SQL.global_db = CivSettings.getStringBase("global.database");

        SQL.global_dsn = "jdbc:"+ SQL.type +"://" + SQL.global_hostname + ":" + SQL.global_port + "/" + SQL.global_db + "?useSSL=" + SQL.global_useSSL + "&requireSSL=" + SQL.global_useSSL;
        CivLog.info("\t Using GLOBAL db at:" + SQL.global_hostname + ":" + SQL.global_port + " user:" + SQL.global_username + " DB:" + SQL.global_db);
        CivLog.info("\t Building Connection Pool for GLOBAL database.");
        globalDatabase = new ConnectionPool(SQL.driver, SQL.global_dsn, SQL.global_username, SQL.global_password);
        CivLog.info("\t Connected to GLOBAL database");

        //CivGlobal.perkManager = new PerkManager();
//		if (PlatinumManager.isLegacyEnabled()) {
//        CivLog.heading("Initializing PERKS SQL Database");
//        SQL.perks_useSSL = CivSettings.getBooleanBase("perks.useSSL");
//        SQL.perks_hostname = CivSettings.getStringBase("perks.hostname");
//        SQL.perks_port = CivSettings.getStringBase("perks.port");
//        SQL.perks_username = CivSettings.getStringBase("perks.username");
//        SQL.perks_password = CivSettings.getStringBase("perks.password");
//        SQL.perks_db = CivSettings.getStringBase("perks.database");
//
//        SQL.perks_dsn = "jdbc:"+ SQL.type +"://" + SQL.perks_hostname + ":" + SQL.perks_port + "/" + SQL.perks_db + "?useSSL=" + SQL.perks_useSSL + "&requireSSL=" + SQL.perks_useSSL;
//        CivLog.info("\t Using PERKS db at:" + SQL.perks_hostname + ":" + SQL.perks_port + " user:" + SQL.perks_username + " DB:" + SQL.perks_db);
//        CivLog.info("\t Building Connection Pool for PERKS database.");
//        perkDatabase = new ConnectionPool(SQL.driver, SQL.perks_dsn, SQL.perks_hostname, SQL.perks_password);
//        CivLog.info("\t Connected to PERKS database");
        //} else if (PlatinumManager.isEnabled()) {

        CivGlobal.perkManager = new PerkManagerSimple();
        CivGlobal.perkManager.init();
        CivLog.info("Enabled SIMPLE PerkManager");
        //}

        CivLog.heading("Initializing SQL Finished");
    }


    public static void initCivObjectTables() throws SQLException {
        CivLog.heading("Building Civ Object Tables.");

        SessionDatabase.init();
        BiomeCache.init();
        Civilization.init();
        Town.init();
        Resident.init();
        Relation.init();
        TownChunk.init();
        Structure.init();
        Wonder.init();
        WallBlock.init();
        RoadBlock.init();
        PermissionGroup.init();
        TradeGood.init();
        ProtectedBlock.init();
        BonusGoodie.init();
        MissionLogger.init();
        EventTimer.init();
        Camp.init();
        ConfigMarketItem.init();
        RandomEvent.init();
        StructureSign.init();
        Report.init();

        CivLog.heading("Building Global Tables!!");
        ScoreManager.init();

        CivLog.info("----- Done Building Tables ----");

    }

    public static Connection getGameConnection() throws SQLException {
        return gameDatabase.getConnection();
    }

    public static Connection getGlobalConnection() throws SQLException {
        return globalDatabase.getConnection();
    }

    public static Connection getPerkConnection() throws SQLException {
        //CivLog.debug("get connection ----> free conns:"+SQL.getGameDatabaseStats().getTotalFree()+" leased:"+SQL.getGameDatabaseStats().getTotalLeased());
        return perkDatabase.getConnection();
    }

    public static boolean hasTable(String name) throws SQLException {
        Connection context = null;
        ResultSet result = null;
        try {
            context = getGameConnection();
            DatabaseMetaData dbm = context.getMetaData();
            String[] types = {"TABLE"};

            result = dbm.getTables(null, null, SQL.tb_prefix + name, types);
            return result.next();
        } finally {
            SQL.close(result, null, context);
        }
    }

    public static boolean hasGlobalTable(String name) throws SQLException {
        Connection global_context = null;
        ResultSet rs = null;

        try {
            global_context = getGlobalConnection();
            DatabaseMetaData dbm = global_context.getMetaData();
            String[] types = {"TABLE"};
            rs = dbm.getTables(null, null, name, types);
            if (rs.next()) {
                return true;
            }
            return false;

        } finally {
            SQL.close(rs, null, global_context);
        }
    }

    public static boolean hasColumn(String tablename, String columnName) throws SQLException {
        Connection context = null;
        ResultSet result = null;

        try {
            context = getGameConnection();
            DatabaseMetaData dbm = context.getMetaData();
            result = dbm.getColumns(null, null, SQL.tb_prefix + tablename, columnName);
            boolean found = result.next();
            return found;
        } finally {
            SQL.close(result, null, context);
        }
    }

    public static void addColumn(String tablename, String columnDef) throws SQLException {
        Connection context = null;
        PreparedStatement ps = null;

        try {
            String table_alter = "ALTER TABLE " + SQL.tb_prefix + tablename + " ADD " + columnDef;
            context = getGameConnection();
            ps = context.prepareStatement(table_alter);
            ps.execute();
            CivLog.info("\tADDED:" + columnDef);
        } finally {
            SQL.close(null, ps, context);
        }

    }

    public static boolean hasGlobalColumn(String tablename, String columnName) throws SQLException {
        Connection global_context = null;
        ResultSet rs = null;

        try {
            global_context = getGlobalConnection();
            DatabaseMetaData dbm = global_context.getMetaData();
            rs = dbm.getColumns(null, null, tablename, columnName);

            try {
                boolean found = rs.next();
                return found;
            } finally {
                rs.close();
            }

        } finally {
            SQL.close(rs, null, global_context);
        }
    }

    public static void addGlobalColumn(String tablename, String columnDef) throws SQLException {
        Connection global_context = null;
        PreparedStatement ps = null;

        try {
            global_context = SQL.getGlobalConnection();
            String table_alter = "ALTER TABLE " + tablename + " ADD " +
                    columnDef;

            ps = global_context.prepareStatement(table_alter);
            ps.execute();
            CivLog.info("\tADDED GLOBAL:" + columnDef);
        } finally {
            SQL.close(null, ps, global_context);
        }
    }

    public static void updateNamedObjectAsync(NamedObject obj, HashMap<String, Object> hashmap, String tablename) {
        TaskMaster.asyncTask("", new SQLUpdateNamedObjectTask(obj, hashmap, tablename), 0);
    }

    public static void updateNamedObject(SQLObject obj, HashMap<String, Object> hashmap, String tablename) throws SQLException {
        if (obj.isDeleted()) {
            return;
        }

        if (obj.getId() == 0) {
            obj.setId(SQL.insertNow(hashmap, tablename));
        } else {
            SQL.update(obj.getId(), hashmap, tablename);
        }
    }

    public static void update(int id, HashMap<String, Object> hashmap, String tablename) throws SQLException {
        hashmap.put("id", id);
        update(hashmap, "id", tablename);
    }


    public static void update(HashMap<String, Object> hashmap, String keyname, String tablename) throws SQLException {
        Connection context = null;
        PreparedStatement ps = null;

        try {
            String sql = "UPDATE `" + SQL.tb_prefix + tablename + "` SET ";
            String where = " WHERE `" + keyname + "` = ?;";
            ArrayList<Object> values = new ArrayList<Object>();

            Object keyValue = hashmap.get(keyname);
            hashmap.remove(keyname);

            Iterator<String> keyIter = hashmap.keySet().iterator();
            while (keyIter.hasNext()) {
                String key = keyIter.next();

                sql += "`" + key + "` = ?";
                sql += "" + (keyIter.hasNext() ? ", " : " ");
                values.add(hashmap.get(key));
            }

            sql += where;

            context = SQL.getGameConnection();
            ps = context.prepareStatement(sql);

            int i = 1;
            for (Object value : values) {
                if (value instanceof String) {
                    ps.setString(i, (String) value);
                } else if (value instanceof Integer) {
                    ps.setInt(i, (Integer) value);
                } else if (value instanceof Boolean) {
                    ps.setBoolean(i, (Boolean) value);
                } else if (value instanceof Double) {
                    ps.setDouble(i, (Double) value);
                } else if (value instanceof Float) {
                    ps.setFloat(i, (Float) value);
                } else if (value instanceof Long) {
                    ps.setLong(i, (Long) value);
                } else {
                    ps.setObject(i, value);
                }
                i++;
            }

            ps.setObject(i, keyValue);

            if (ps.executeUpdate() == 0) {
                insertNow(hashmap, tablename);
            }
        } finally {
            SQL.close(null, ps, context);
        }
    }

    public static void insert(HashMap<String, Object> hashmap, String tablename) {
        TaskMaster.asyncTask(new SQLInsertTask(hashmap, tablename), 0);
    }

    public static int insertNow(HashMap<String, Object> hashmap, String tablename) throws SQLException {
        Connection context = null;
        ResultSet rs = null;
        PreparedStatement ps = null;

        try {
            String sql = "INSERT INTO " + SQL.tb_prefix + tablename + " ";
            String keycodes = "(";
            String valuecodes = " VALUES ( ";
            ArrayList<Object> values = new ArrayList<Object>();

            Iterator<String> keyIter = hashmap.keySet().iterator();
            while (keyIter.hasNext()) {
                String key = keyIter.next();

                keycodes += key;
                keycodes += "" + (keyIter.hasNext() ? "," : ")");

                valuecodes += "?";
                valuecodes += "" + (keyIter.hasNext() ? "," : ")");

                values.add(hashmap.get(key));
            }

            sql += keycodes;
            sql += valuecodes;

            context = SQL.getGameConnection();
            ps = context.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            int i = 1;
            for (Object value : values) {
                if (value instanceof String) {
                    ps.setString(i, (String) value);
                } else if (value instanceof Integer) {
                    ps.setInt(i, (Integer) value);
                } else if (value instanceof Boolean) {
                    ps.setBoolean(i, (Boolean) value);
                } else if (value instanceof Double) {
                    ps.setDouble(i, (Double) value);
                } else if (value instanceof Float) {
                    ps.setFloat(i, (Float) value);
                } else if (value instanceof Long) {
                    ps.setLong(i, (Long) value);
                } else {
                    ps.setObject(i, value);
                }
                i++;
            }

            ps.execute();
            int id = 0;
            rs = ps.getGeneratedKeys();

            while (rs.next()) {
                id = rs.getInt(1);
                break;
            }

            if (id == 0) {
                String name = (String) hashmap.get("name");
                if (name == null) {
                    name = "Unknown";
                }

                CivLog.error("SQL ERROR: Saving an SQLObject returned a 0 ID! Name:" + name + " Table:" + tablename);
            }
            return id;

        } finally {
            SQL.close(rs, ps, context);
        }
    }


    public static void deleteNamedObject(SQLObject obj, String tablename) throws SQLException {
        Connection context = null;
        PreparedStatement ps = null;

        try {
            String sql = "DELETE FROM " + SQL.tb_prefix + tablename + " WHERE `id` = ?";
            context = SQL.getGameConnection();
            ps = context.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, obj.getId());
            ps.execute();
            ps.close();
            obj.setDeleted(true);
        } finally {
            SQL.close(null, ps, context);
        }
    }

    public static void deleteByName(String name, String tablename) throws SQLException {
        Connection context = null;
        PreparedStatement ps = null;

        try {
            String sql = "DELETE FROM " + SQL.tb_prefix + tablename + " WHERE `name` = ?";
            context = SQL.getGameConnection();
            ps = context.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, name);
            ps.execute();
            ps.close();
        } finally {
            SQL.close(null, ps, context);
        }
    }

    public static void makeCol(String colname, String type, String TABLE_NAME) throws SQLException {
        if (!SQL.hasColumn(TABLE_NAME, colname)) {
            CivLog.info("\tCouldn't find " + colname + " column for " + TABLE_NAME);
            SQL.addColumn(TABLE_NAME, "`" + colname + "` " + type);
        }
    }

    public static void makeTable(String table_create) throws SQLException {
        Connection context = null;
        PreparedStatement ps = null;

        try {
            context = SQL.getGameConnection();
            ps = context.prepareStatement(table_create);
            ps.execute();
        } finally {
            SQL.close(null, ps, context);
        }

    }

    public static void makeGlobalTable(String table_create) throws SQLException {
        Connection context = null;
        PreparedStatement ps = null;

        try {
            context = SQL.getGlobalConnection();
            ps = context.prepareStatement(table_create);
            ps.execute();
        } finally {
            SQL.close(null, ps, context);
        }
    }

    public static void close(ResultSet rs, PreparedStatement ps, Connection context) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        if (ps != null) {
            try {
                ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        if (context != null) {
            try {
                context.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

}
