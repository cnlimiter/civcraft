package cn.evole.plugins.global.perks;

import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.config.ConfigPerk;
import cn.evole.plugins.civcraft.database.SQL;
import cn.evole.plugins.civcraft.exception.CivException;
import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.object.Resident;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

//import org.bukkit.entity.Player;

public class PerkManager {

    private static final HashMap<String, Integer> userIdCache = new HashMap<String, Integer>();
    public static String hostname = "";
    public static String port = "";
    public static String db_name = "";
    public static String username = "";
    public static String password = "";
    //public static Connection context = null;
    public static String dsn = "";

    private static Integer getUserWebsiteId(Resident resident) throws SQLException, NotVerifiedException {
        Connection context = null;
        ResultSet rs = null;
        PreparedStatement s = null;

        try {
            context = SQL.getPerkConnection();
            /* User id's don't change, if we've looked it up before, dont look it up again. */
            Integer userId = userIdCache.get(resident.getName());
            if (userId != null) {
                return userId;
            }

            String sql = "SELECT `id`, `game_name`, `verified` FROM `users` WHERE `game_name` = ?";
            s = context.prepareStatement(sql);
            s.setString(1, resident.getName());

            rs = s.executeQuery();
            if (!rs.next()) {
                throw new NotVerifiedException();
            }

            /* Double check resident is verified. */
            boolean verified = rs.getBoolean("verified");
            if (!verified) {
                throw new NotVerifiedException();
            }

            userId = rs.getInt("id");
            userIdCache.put(resident.getName(), userId);
            return userId;
        } finally {
            SQL.close(rs, s, context);
        }
    }

    public void init() throws SQLException {
    }

    public int addPerkToResident(Resident resident, String perk_id, Integer count) throws SQLException, CivException {
        return 0;
    }

    public int removePerkFromResident(Resident resident, String perk_id, Integer count) throws SQLException, CivException {
        return 0;
    }

    public void loadPerksForResident(Resident resident) throws SQLException, NotVerifiedException, CivException {
        LinkedList<String> perkIdents = new LinkedList<String>();
        String sql;
        Connection context = null;
        ResultSet rs = null;
        PreparedStatement s = null;
        HashSet<Integer> perkIDs = new HashSet<Integer>();
        HashMap<Integer, Integer> perkCounts = new HashMap<Integer, Integer>();

        try {
            context = SQL.getPerkConnection();

            /* XXX TODO Get better with JOIN statements and do this faster. */
            Integer userID = getUserWebsiteId(resident);

            try {
                /* Lookup join table for perks and users. */
                sql = "SELECT `perk_id`,`used`,`used_phase` FROM `userperks` WHERE `user_id` = ?";
                s = context.prepareStatement(sql);
                s.setInt(1, userID);

                rs = s.executeQuery();

                while (rs.next()) {
                    /* 'used' is now deprecated. */
                    //Boolean used = rs.getBoolean("used");
                    String usedPhase = rs.getString("used_phase");
                    if (usedPhase == null) {
                        usedPhase = "old";
                    }

                    int id = rs.getInt("perk_id");
                    if (!usedPhase.equals(CivGlobal.getPhase())) {
                        Integer count = perkCounts.get(id);
                        if (count == null) {
                            perkCounts.put(id, 1);
                        } else {
                            perkCounts.put(id, count + 1);
                        }

                        perkIDs.add(id);
                    }
                }
            } finally {
                SQL.close(rs, s, null);
            }

            try {
                if (!perkIDs.isEmpty()) {
                    /* Finally, look up perk idents. */
                    StringBuilder sqlBuild = new StringBuilder("SELECT `id`, `ident` FROM `perks` WHERE id IN (");
                    for (Integer id : perkIDs) {
                        sqlBuild.append("" + id + ",");
                    }
                    sqlBuild.setCharAt(sqlBuild.length() - 1, ')');
                    s = context.prepareStatement(sqlBuild.toString());
                    rs = s.executeQuery();

                    /* Put all of the perk id's into a list that we'll return. */
                    while (rs.next()) {
                        Integer count = perkCounts.get(rs.getInt("id"));
                        for (int i = 0; i < count; i++) {
                            perkIdents.add(rs.getString("ident"));
                        }
                    }

                    s.close();
                    rs.close();
                }
            } finally {
                SQL.close(rs, s, null);
            }

            for (String perkID : perkIdents) {
                ConfigPerk configPerk = CivSettings.perks.get(perkID);
                if (configPerk == null) {
                    continue;
                }

                Perk p2 = resident.perks.get(configPerk.id);
                if (p2 != null) {
                    p2.count++;
                    resident.perks.put(p2.getIdent(), p2);
                } else {
                    Perk p = new Perk(configPerk);
                    resident.perks.put(p.getIdent(), p);
                }
            }

            return;
        } finally {
            SQL.close(rs, s, context);
        }
    }

    public void markAsUsed(Resident resident, Perk parent) throws SQLException, NotVerifiedException {
        return;
    }

}
