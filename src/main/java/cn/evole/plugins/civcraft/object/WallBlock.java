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
import cn.evole.plugins.civcraft.exception.CivException;
import cn.evole.plugins.civcraft.exception.InvalidNameException;
import cn.evole.plugins.civcraft.exception.InvalidObjectException;
import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.main.CivLog;
import cn.evole.plugins.civcraft.structure.Structure;
import cn.evole.plugins.civcraft.structure.Wall;
import cn.evole.plugins.civcraft.util.BlockCoord;
import cn.evole.plugins.civcraft.util.ChunkCoord;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

public class WallBlock extends SQLObject {

    public static final String TABLE_NAME = "WALLBLOCKS";
    int old_id;
    int old_data;
    int type_id;
    int data;
    private BlockCoord coord;
    private Wall struct;

    public WallBlock(BlockCoord coord, Structure struct, int old_id, int old_data, int type, int data) throws SQLException {
        this.coord = coord;
        this.struct = (Wall) struct;
        this.old_data = old_data;
        this.old_id = old_id;
        this.type_id = type;
        this.data = data;
    }

    public WallBlock(ResultSet rs) throws SQLException, InvalidNameException, InvalidObjectException, CivException {
        this.load(rs);
    }

    public static void init() throws SQLException {
        if (!SQL.hasTable(TABLE_NAME)) {
            String table_create = "CREATE TABLE " + SQL.tb_prefix + TABLE_NAME + " (" +
                    "`id` int(11) unsigned NOT NULL auto_increment," +
                    "`struct_id` int(11) NOT NULL DEFAULT 0," +
                    "`coord` mediumtext DEFAULT NULL," +
                    "`type_id` int(11) DEFAULT 0," +
                    "`data` int(11) DEFAULT 0," +
                    "`old_id` int(11) DEFAULT 0," +
                    "`old_data` int(11) DEFAULT 0," +
                    "PRIMARY KEY (`id`)" + ")";

            SQL.makeTable(table_create);
            CivLog.info("Created " + TABLE_NAME + " table");
        } else {
            CivLog.info(TABLE_NAME + " table OK!");

            if (!SQL.hasColumn(TABLE_NAME, "type_id")) {
                CivLog.info("\tCouldn't find type_id column for wallblock.");
                SQL.addColumn(TABLE_NAME, "`type_id` int(11) default 0");
            }

            if (!SQL.hasColumn(TABLE_NAME, "data")) {
                CivLog.info("\tCouldn't find data column for wallblock.");
                SQL.addColumn(TABLE_NAME, "`data` int(11) default 0");
            }
        }
    }

    public BlockCoord getCoord() {
        return coord;
    }

    public void setCoord(BlockCoord coord) {
        this.coord = coord;
    }

    @Override
    public void load(ResultSet rs) throws SQLException, InvalidNameException,
            InvalidObjectException, CivException {
        this.setId(rs.getInt("id"));
        this.setStruct(CivGlobal.getStructureById(rs.getInt("struct_id")));
        if (this.struct == null) {
            int id = rs.getInt("struct_id");
            this.delete();
            throw new CivException("Could not load WallBlock, could not find structure:" + id);
        }

        this.setCoord(new BlockCoord(rs.getString("coord")));

        CivGlobal.addWallChunk(this.struct, new ChunkCoord(getCoord().getLocation()));
        this.struct.addStructureBlock(this.getCoord(), true);
        this.struct.wallBlocks.put(this.getCoord(), this);
        this.old_id = rs.getInt("old_id");
        this.old_data = rs.getInt("old_data");
        this.type_id = rs.getInt("type_id");
        this.data = rs.getInt("data");

    }

    @Override
    public void save() {
        SQLUpdate.add(this);
    }

    @Override
    public void saveNow() throws SQLException {
        HashMap<String, Object> hashmap = new HashMap<String, Object>();

        hashmap.put("struct_id", this.getStruct().getId());
        hashmap.put("coord", this.getCoord().toString());
        hashmap.put("old_id", this.old_id);
        hashmap.put("old_data", this.old_data);
        hashmap.put("type_id", this.type_id);
        hashmap.put("data", this.data);

        SQL.updateNamedObject(this, hashmap, TABLE_NAME);
    }

    @Override
    public void delete() throws SQLException {
        if (this.coord != null) {
            CivGlobal.removeStructureBlock(this.coord);
        }
        SQL.deleteNamedObject(this, TABLE_NAME);
    }

    public Structure getStruct() {
        return struct;
    }

    public void setStruct(Structure struct) {
        this.struct = (Wall) struct;
    }

    public int getOldId() {
        return this.old_id;
    }

    public byte getOldData() {
        return (byte) this.old_data;
    }

    public int getTypeId() {
        return this.type_id;
    }

    public int getData() {
        return this.data;
    }

}
