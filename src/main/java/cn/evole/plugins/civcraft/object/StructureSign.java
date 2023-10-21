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
import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.main.CivLog;
import cn.evole.plugins.civcraft.structure.Buildable;
import cn.evole.plugins.civcraft.structure.Structure;
import cn.evole.plugins.civcraft.structure.wonders.Wonder;
import cn.evole.plugins.civcraft.util.BlockCoord;
import org.bukkit.block.Sign;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

public class StructureSign extends SQLObject {

    public static String TABLE_NAME = "STRUCTURE_SIGNS";
    private String text;
    private Buildable owner;
    private String type;
    private String action;
    private BlockCoord coord;
    private int direction;
    private boolean allowRightClick = false;

    public StructureSign(BlockCoord coord, Buildable owner) {
        this.coord = coord;
        this.owner = owner;
    }

    public StructureSign(ResultSet rs) throws SQLException {
        load(rs);
    }

    public static void init() throws SQLException {
        if (!SQL.hasTable(TABLE_NAME)) {
            String table_create = "CREATE TABLE " + SQL.tb_prefix + TABLE_NAME + " (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                    "`text` TEXT, " +
                    "`structure_id` int(11), " +
                    "`wonder_id` int(11)," +
                    "`type` TEXT, " +
                    "`action` TEXT, " +
                    "`coordHash` TEXT, " +
                    "`direction` int(11)" +
                    ")";

            SQL.makeTable(table_create);
            CivLog.info("Created " + TABLE_NAME + " table");
        } else {
            CivLog.info(TABLE_NAME + " table OK!");
        }
    }

    @Override
    public void load(ResultSet rs) throws SQLException {
        this.setId(rs.getInt("id"));
        this.text = rs.getString("text");
        this.action = rs.getString("action");
        this.type = rs.getString("type");
        int structure_id = rs.getInt("structure_id");
        int wonder_id = rs.getInt("wonder_id");
        this.owner = null;

        if (structure_id != 0) {
            this.owner = CivGlobal.getStructureById(structure_id);
        } else if (wonder_id != 0) {
            this.owner = CivGlobal.getWonderById(wonder_id);
        }


        this.coord = new BlockCoord(rs.getString("coordHash"));
        this.direction = rs.getInt("direction");

        if (this.owner != null) {
            owner.addStructureSign(this);
        }
    }

    @Override
    public void save() {
        SQLUpdate.add(this);
    }

    @Override
    public void saveNow() throws SQLException {
        HashMap<String, Object> hashmap = new HashMap<String, Object>();
        hashmap.put("text", this.getText());

        if (this.owner == null) {
            hashmap.put("structure_id", 0);
            hashmap.put("wonder_id", 0);
        } else if (this.owner instanceof Structure) {
            hashmap.put("structure_id", owner.getId());
            hashmap.put("wonder_id", 0);
        } else if (this.owner instanceof Wonder) {
            hashmap.put("structure_id", 0);
            hashmap.put("wonder_id", owner.getId());
        }

        hashmap.put("type", this.getType());
        hashmap.put("action", this.getAction());
        hashmap.put("coordHash", this.coord.toString());
        hashmap.put("direction", this.direction);

        SQL.updateNamedObject(this, hashmap, TABLE_NAME);
    }

    @Override
    public void delete() throws SQLException {
        SQL.deleteNamedObject(this, TABLE_NAME);
        CivGlobal.removeStructureSign(this);
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setText(String[] message) {
        this.text = "";
        for (String str : message) {
            text += str + "\n";
        }
    }

    public Buildable getOwner() {
        return owner;
    }

    public void setOwner(Buildable owner) {
        this.owner = owner;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public BlockCoord getCoord() {
        return coord;
    }

    public void setCoord(BlockCoord coord) {
        this.coord = coord;
    }

    public int getDirection() {
        return direction;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    public void update() {
        if (coord.getBlock().getState() instanceof Sign) {
            Sign sign = (Sign) coord.getBlock().getState();
            String[] lines = this.text.split("\\n");

            for (int i = 0; i < 4; i++) {
                if (i < lines.length) {
                    sign.setLine(i, lines[i]);
                } else {
                    sign.setLine(i, "");
                }
            }
            sign.update();
        }
    }

    public boolean isAllowRightClick() {
        return allowRightClick;
    }

    public void setAllowRightClick(boolean allowRightClick) {
        this.allowRightClick = allowRightClick;
    }

}