package cn.evole.plugins.global.perks;

import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.config.ConfigPerk;
import cn.evole.plugins.civcraft.database.SQL;
import cn.evole.plugins.civcraft.exception.CivException;
import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.main.CivLog;
import cn.evole.plugins.civcraft.object.Resident;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

public class PerkManagerSimple extends PerkManager {

    /*
     * Create required tables in the global database if they cannot be found.
     */
    public static String USER_PERKS_TABLE_NAME = "USER_PERKS";

    @Override
    public void init() throws SQLException {
        System.out.println("================= USER_PERKS INIT ======================");

        // Check/Build SessionDB tables
        if (!SQL.hasGlobalTable(USER_PERKS_TABLE_NAME)) {
            String table_create = "CREATE TABLE " + USER_PERKS_TABLE_NAME + " (" +
                    "`id` int(11) unsigned NOT NULL auto_increment," +
                    "`uuid` varchar(256)," +
                    "`perk_id` mediumtext," +
                    "`used_phase` mediumtext," +
                    "PRIMARY KEY (`id`)" + ")";

            SQL.makeGlobalTable(table_create);
            CivLog.info("Created " + USER_PERKS_TABLE_NAME + " table");
        } else {
            CivLog.info(USER_PERKS_TABLE_NAME + " table OK!");
        }

        System.out.println("==================================================");
    }

    @Override
    public void loadPerksForResident(Resident resident) throws SQLException, NotVerifiedException, CivException {
        String sql;
        Connection context = null;
        ResultSet rs = null;
        PreparedStatement s = null;
        HashMap<String, Integer> perkCounts = new HashMap<String, Integer>();


        try {
            context = SQL.getGlobalConnection();

            String uuid = resident.getUUIDString();

            try {
                /* Lookup join table for perks and users. */
                sql = "SELECT `perk_id`,`used_phase` FROM `" + USER_PERKS_TABLE_NAME + "` WHERE `uuid` = ?";
                s = context.prepareStatement(sql);
                s.setString(1, uuid);

                rs = s.executeQuery();

                while (rs.next()) {
                    /* 'used' is now deprecated. */
                    String usedPhase = rs.getString("used_phase");
                    String id = rs.getString("perk_id");
                    if ((usedPhase == null) || !usedPhase.equals(CivGlobal.getPhase())) {
                        perkCounts.merge(id, 1, Integer::sum);
                    }
                }
            } finally {
                SQL.close(rs, s, null);
            }

//            for (String perkID : perkCounts.keySet()) {
//                ConfigPerk configPerk = CivSettings.perks.get(perkID);
//                if (configPerk == null) {
//                    continue;
//                }
//
//                int count = perkCounts.get(perkID);
//
//                Perk p = new Perk(configPerk);
//                p.count = count;
//                resident.perks.put(p.getIdent(), p);
//            }

            for (ConfigPerk configPerk : CivSettings.perks.values()) {//免费使用所有模板
                Perk p = new Perk(configPerk);
                p.count = 100000;
                resident.perks.put(p.getIdent(), p);
            }

        } finally {
            SQL.close(rs, s, context);
        }
    }

    @Override
    public int addPerkToResident(Resident resident, String perk_id, Integer count) throws SQLException, CivException {
        String sql;
        Connection context = null;
        ResultSet rs = null;
        PreparedStatement s = null;

        try {
            context = SQL.getGlobalConnection();

            String uuid = resident.getUUIDString();

            int added = 0;
            for (int i = 0; i < count; i++) {
                /* Lookup join table for perks and users. */
                sql = "INSERT INTO `" + USER_PERKS_TABLE_NAME + "` (uuid, perk_id) VALUES(?, ?)";
                s = context.prepareStatement(sql);
                s.setString(1, uuid);
                s.setString(2, perk_id);
                added += s.executeUpdate();
            }
            return added;
        } finally {
            SQL.close(rs, s, context);
        }
    }

    @Override
    public int removePerkFromResident(Resident resident, String perk_id, Integer count) throws SQLException, CivException {
        String sql;
        Connection context = null;
        ResultSet rs = null;
        PreparedStatement s = null;

        try {
            context = SQL.getGlobalConnection();

            String uuid = resident.getUUIDString();

            /* Lookup join table for perks and users. */
            sql = "DELETE FROM `" + USER_PERKS_TABLE_NAME + "` WHERE `uuid` = ? AND `perk_id` = ? LIMIT ?";
            s = context.prepareStatement(sql);
            s.setString(1, uuid);
            s.setString(2, perk_id);
            s.setInt(3, count);
            return s.executeUpdate();
        } finally {
            SQL.close(rs, s, context);
        }
    }

}
