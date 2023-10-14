/**
 * [2023] Cnlimiter LLC
 * All Rights Reserved.
 * Name: civcraft
 * Author: cnlimiter
 * UpdateTime: 2023/10/14 16:15
 * Description:
 * License: ARR
 */
package cn.evole.plugins.civcraft.util;

import cn.evole.plugins.civcraft.structure.Buildable;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.HashMap;
import java.util.Map;

public class SimpleBlock {

    //public static final int SIGN = 1;
    //public static final int CHEST = 2;
    //public static final int SIGN_LITERAL = 3;

    //public int special = 0;
//	public int special_id = -1;
    public int x;
    public int y;
    public int z;
    public Type specialType;
    public String command;
    public String[] message = new String[4];
    public String worldname;
    public Buildable buildable;
    public Map<String, String> keyvalues = new HashMap<String, String>();
    private int type = 0;
    private byte data = 0;
    /**
     * Construct the block with its type.
     */
    public SimpleBlock(Block block) {
        this.x = block.getX();
        this.y = block.getY();
        this.z = block.getZ();
        this.worldname = block.getWorld().getName();
        this.type = ItemManager.getId(block);
        this.data = ItemManager.getData(block);
        this.specialType = Type.NORMAL;
    }

    public SimpleBlock(String hash, int type, byte data) {
        String[] split = hash.split(",");
        this.worldname = split[0];
        this.x = Integer.parseInt(split[1]);
        this.y = Integer.parseInt(split[2]);
        this.z = Integer.parseInt(split[3]);
        this.type = type;
        this.data = data;
        this.specialType = Type.NORMAL;
    }

    /**
     * Construct the block with its type and data.
     *
     * @param type
     * @param data
     */
    public SimpleBlock(int type, int data) {
        this.type = (short) type;
        this.data = (byte) data;
        this.specialType = Type.NORMAL;

    }

    public static String getKeyFromBlockCoord(BlockCoord coord) {
        return coord.getWorldname() + "," + coord.getX() + "," + coord.getY() + "," + coord.getZ();
    }

    public String getKey() {
        return this.worldname + "," + this.x + "," + this.y + "," + this.z;
    }

    /**
     * @return the type
     */
    public int getType() {
        return (int) type;
    }

    /**
     * @param type the type to set
     */
    public void setType(int type) {
        this.type = (short) type;
    }

    @SuppressWarnings("deprecation")
    public Material getMaterial() {
        return Material.getMaterial(type);
    }

    public void setTypeAndData(int type, int data) {
        this.type = (short) type;
        this.data = (byte) data;
    }

    /**
     * @return the data
     */
    public int getData() {
        return (int) data;
    }

    /**
     * @param data the data to set
     */
    public void setData(int data) {
        this.data = (byte) data;
    }

    /**
     * Returns true if it's air.
     *
     * @return if air
     */
    public boolean isAir() {
        return type == (byte) 0x0;
    }

    public String getKeyValueString() {
        String out = "";

        for (String key : keyvalues.keySet()) {
            String value = keyvalues.get(key);
            out += key + ":" + value + ",";
        }

        return out;
    }

    public Location getLocation() {
        return new Location(Bukkit.getWorld(this.worldname), this.x, this.y, this.z);
    }

    public enum Type {
        NORMAL,
        COMMAND,
        LITERAL,
    }

}
