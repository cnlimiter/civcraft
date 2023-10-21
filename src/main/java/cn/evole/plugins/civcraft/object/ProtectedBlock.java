/**
 * [2023] Cnlimiter LLC
 * All Rights Reserved.
 * Name: civcraft
 * Author: cnlimiter
 * UpdateTime: 2023/10/14 16:15
 * Description:
 * License: ARR
 */
package cn.evole.plugins.civcraft.object;

import cn.evole.plugins.civcraft.database.SQL;
import cn.evole.plugins.civcraft.database.SQLUpdate;
import cn.evole.plugins.civcraft.exception.InvalidNameException;
import cn.evole.plugins.civcraft.main.CivLog;
import cn.evole.plugins.civcraft.util.BlockCoord;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

public class ProtectedBlock extends SQLObject {

    public static final String TABLE_NAME = "PROTECTED_BLOCKS";
    private BlockCoord coord;
    //private Structure owner;
    private Type type;

    public ProtectedBlock(BlockCoord coord, Type type) {
        this.coord = coord;
        this.type = type;
    }

    public ProtectedBlock(ResultSet rs) throws SQLException, InvalidNameException {
        //	this.coord = new BlockCoord(rs.getString("coord"));
        //	this.type = Type.TRADE_MARKER;
        //this.owner = rs.getInt(getId());
        this.load(rs);
    }

    public static void init() throws SQLException {
        if (!SQL.hasTable(TABLE_NAME)) {
            String table_create = "CREATE TABLE " + SQL.tb_prefix + TABLE_NAME + " (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                    "`coord` mediumtext NOT NULL," +
                    "`type` mediumtext NOT NULL," +
                    "`structure_id` int(11) DEFAULT 0" +
                    ")";

            SQL.makeTable(table_create);
            CivLog.info("Created " + TABLE_NAME + " table");
        } else {
            CivLog.info(TABLE_NAME + " table OK!");
        }
    }

    @Override
    public void load(ResultSet rs) throws SQLException, InvalidNameException {
        this.coord = new BlockCoord(rs.getString("coord"));
        this.type = Type.valueOf(rs.getString("type"));
        //	int structure_id = rs.getInt("structure_id");
//		if (structure_id == 0) {
//			this.owner = null;
//		} else {
//			this.owner = CivGlobal.getStructureById(structure_id);
//		}
    }

    @Override
    public void save() {
        SQLUpdate.add(this);
    }

    @Override
    public void saveNow() throws SQLException {
        HashMap<String, Object> hashmap = new HashMap<String, Object>();

        hashmap.put("coord", this.coord.toString());
        hashmap.put("type", this.type.name());

//		if (this.owner == null) {
//			hashmap.put("structure_id", 0);
//		} else {
//			hashmap.put("structure_id", this.owner.getId());
//		}
//
        SQL.updateNamedObject(this, hashmap, TABLE_NAME);
    }

    @Override
    public void delete() throws SQLException {
    }

    public Type getType() {
        return type;
    }

//	public Structure getOwner() {
//		return owner;
//	}
//
//	public void setOwner(Structure owner) {
//		this.owner = owner;
//	}

    public void setType(Type type) {
        this.type = type;
    }

    public BlockCoord getCoord() {
        return coord;
    }

    public void setCoord(BlockCoord coord) {
        this.coord = coord;
    }

    public static enum Type {
        NONE,
        TRADE_MARKER,
        PROTECTED_RAILWAY,
    }

}
