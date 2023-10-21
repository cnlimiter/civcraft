/**
 * [2023] Cnlimiter LLC
 * All Rights Reserved.
 * Name: civcraft
 * Author: cnlimiter
 * UpdateTime: 2023/10/14 16:15
 * Description:
 * License: ARR
 */
package cn.evole.plugins.civcraft.permission;

import cn.evole.plugins.civcraft.database.SQL;
import cn.evole.plugins.civcraft.database.SQLUpdate;
import cn.evole.plugins.civcraft.exception.InvalidNameException;
import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.main.CivLog;
import cn.evole.plugins.civcraft.object.Civilization;
import cn.evole.plugins.civcraft.object.Resident;
import cn.evole.plugins.civcraft.object.SQLObject;
import cn.evole.plugins.civcraft.object.Town;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PermissionGroup extends SQLObject {

    public static final String TABLE_NAME = "GROUPS";
    private Map<String, Resident> members = new ConcurrentHashMap<String, Resident>();
    /* Only cache towns as the 'civ' can change when a town gets conquered or gifted/moved. */
    private Town cacheTown = null;
    private int civId;
    private int townId;

    public PermissionGroup(Civilization civ, String name) throws InvalidNameException {
        this.civId = civ.getId();
        this.setName(name);
    }

    public PermissionGroup(Town town, String name) throws InvalidNameException {
        this.townId = town.getId();
        this.cacheTown = town;
        this.setName(name);
    }

    public PermissionGroup(ResultSet rs) throws SQLException, InvalidNameException {
        this.load(rs);
    }

    public static void init() throws SQLException {
        if (!SQL.hasTable(TABLE_NAME)) {
            String table_create = "CREATE TABLE " + SQL.tb_prefix + TABLE_NAME + " (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                    "`name` VARCHAR(64) NOT NULL," +
                    "`town_id` int(11)," +
                    "`civ_id` int(11)," +
                    "`members` TEXT" +
                    //"FOREIGN KEY (town_id) REFERENCES "+SQL.tb_prefix+"TOWN(id),"+
                    //"FOREIGN KEY (civ_id) REFERENCES "+SQL.tb_prefix+"CIVILIZATIONS(id),"+
                    ")";

            SQL.makeTable(table_create);
            CivLog.info("Created " + TABLE_NAME + " table");
        } else {
            CivLog.info(TABLE_NAME + " table OK!");
        }
    }

    public static boolean isProtectedGroupName(String name) {
        return isTownProtectedGroup(name) || isCivProtectedGroup(name);
    }

    private static boolean isTownProtectedGroup(String name) {
        switch (name.toLowerCase()) {
            case "mayors":
            case "assistants":
            case "residents":
                return true;
        }
        return false;
    }

    private static boolean isCivProtectedGroup(String name) {
        switch (name.toLowerCase()) {
            case "leaders":
            case "advisers":
                return true;
        }
        return false;
    }

    public void addMember(Resident res) {
        members.put(res.getUUIDString(), res);
    }

    public void removeMember(Resident res) {
        members.remove(res.getUUIDString());
    }

    public boolean hasMember(Resident res) {
        return members.containsKey(res.getUUIDString());
    }

    public void clearMembers() {
        members.clear();
    }

    @Override
    public void load(ResultSet rs) throws SQLException, InvalidNameException {
        this.setId(rs.getInt("id"));
        this.setName(rs.getString("name"));
        this.setTownId(rs.getInt("town_id"));
        this.setCivId(rs.getInt("civ_id"));
        loadMembersFromSaveString(rs.getString("members"));

        if (this.getTownId() != 0) {
            this.cacheTown = CivGlobal.getTownFromId(this.getTownId());
            this.getTown().addGroup(this);
        } else {
            Civilization civ = CivGlobal.getCivFromId(this.getCivId());
            if (civ == null) {
                civ = CivGlobal.getConqueredCivFromId(this.getCivId());
                if (civ == null) {
                    CivLog.warning("COUlD NOT FIND CIV ID:" + this.getCivId() + " for group: " + this.getName() + " to load.");
                    return;
                }
            }

            civ.addGroup(this);
        }
    }

    @Override
    public void save() {
        SQLUpdate.add(this);
    }

    @Override
    public void saveNow() throws SQLException {
        HashMap<String, Object> hashmap = new HashMap<String, Object>();

        hashmap.put("name", this.getName());
        hashmap.put("members", this.getMembersSaveString());
        hashmap.put("town_id", this.getTownId());
        hashmap.put("civ_id", this.getCivId());

        SQL.updateNamedObject(this, hashmap, TABLE_NAME);
    }

    @Override
    public void delete() throws SQLException {
        SQL.deleteNamedObject(this, TABLE_NAME);
    }

    private String getMembersSaveString() {
        String ret = "";

        for (String name : members.keySet()) {
            ret += name + ",";
        }

        return ret;
    }

    private void loadMembersFromSaveString(String src) {
        String[] names = src.split(",");

        for (String n : names) {
            Resident res;

            if (n.length() >= 1) {
                res = CivGlobal.getResidentViaUUID(UUID.fromString(n));

                if (res != null) {
                    members.put(n, res);
                }
            }
        }
    }

    public Town getTown() {
        return cacheTown;
    }

    public void setTown(Town town) {
        this.cacheTown = town;
    }

    public int getMemberCount() {
        return members.size();
    }

    public Collection<Resident> getMemberList() {
        return members.values();
    }

    public Civilization getCiv() {
        if (cacheTown == null) {
            return null;
        }

        return cacheTown.getCiv();
    }

    public boolean isProtectedGroup() {
        return isTownProtectedGroup(this.getName()) || isCivProtectedGroup(this.getName());
    }

    public boolean isTownProtectedGroup() {
        return isTownProtectedGroup(this.getName());
    }

    public boolean isCivProtectedGroup() {
        return isCivProtectedGroup(this.getName());
    }

    public String getMembersString() {
        String out = "";

        for (String uuid : members.keySet()) {
            Resident res = CivGlobal.getResidentViaUUID(UUID.fromString(uuid));
            out += res.getName() + ", ";
        }
        return out;
    }

    public int getCivId() {
        return civId;
    }

    public void setCivId(int civId) {
        this.civId = civId;
    }

    public int getTownId() {
        return townId;
    }

    public void setTownId(int townId) {
        this.townId = townId;
    }
}
