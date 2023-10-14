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

import cn.evole.plugins.civcraft.structure.Buildable;
import cn.evole.plugins.civcraft.util.BlockCoord;

public class ControlPoint {

    /* Location of the control block. */
    private BlockCoord coord;

    /* Hitpoints for this control block. */
    private int hitpoints;

    /* Max hitpoints for this control block. */
    private int maxHitpoints;

    /* TownHall this control point belongs to. */
    private Buildable buildable;
    private String info;

    public ControlPoint(BlockCoord coord, Buildable buildable, int hitpoints, String info) {
        this.coord = coord;
        this.setBuildable(buildable);
        this.maxHitpoints = hitpoints;
        this.hitpoints = this.maxHitpoints;
        this.info = info;
    }

    /**
     * @return the coord
     */
    public BlockCoord getCoord() {
        return coord;
    }

    /**
     * @param coord the coord to set
     */
    public void setCoord(BlockCoord coord) {
        this.coord = coord;
    }

    /**
     * @return the hitpoints
     */
    public int getHitpoints() {
        return hitpoints;
    }

    /**
     * @param hitpoints the hitpoints to set
     */
    public void setHitpoints(int hitpoints) {
        this.hitpoints = hitpoints;
    }

    /**
     * @return the maxHitpoints
     */
    public int getMaxHitpoints() {
        return maxHitpoints;
    }

    /**
     * @param maxHitpoints the maxHitpoints to set
     */
    public void setMaxHitpoints(int maxHitpoints) {
        this.maxHitpoints = maxHitpoints;
    }

    public void damage(int amount) {
        if (this.hitpoints <= 0) {
            return;
        }

        this.hitpoints -= amount;

        if (this.hitpoints <= 0) {
            this.hitpoints = 0;
        }

    }

    public boolean isDestroyed() {
        if (this.hitpoints <= 0) {
            return true;
        }
        return false;
    }

    public Buildable getBuildable() {
        return buildable;
    }

    public void setBuildable(Buildable buildable) {
        this.buildable = buildable;
    }

    public String getinfo() {
        return this.info;
    }
}
