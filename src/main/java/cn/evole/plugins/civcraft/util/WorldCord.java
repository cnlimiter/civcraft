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

import org.bukkit.Location;

public class WorldCord {

    private String worldname;
    private int x;
    private int y;
    private int z;


    public WorldCord(String worldname, int x, int y, int z) {
        this.worldname = worldname;
        this.x = x;
        this.y = y;
        this.z = z;
    }


    public WorldCord(Location location) {
        this.worldname = location.getWorld().getName();
        this.x = location.getBlockX();
        this.y = location.getBlockY();
        this.z = location.getBlockZ();
    }

    public String getWorldname() {
        return worldname;
    }

    public void setWorldname(String worldname) {
        this.worldname = worldname;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getZ() {
        return z;
    }

    public void setZ(int z) {
        this.z = z;
    }


}
