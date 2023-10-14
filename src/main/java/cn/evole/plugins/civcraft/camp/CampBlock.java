/**
 * [2023] Cnlimiter LLC
 * All Rights Reserved.
 * Name: civcraft
 * Author: cnlimiter
 * UpdateTime: 2023/10/14 16:15
 * Description:
 * License: ARR
 */
package cn.evole.plugins.civcraft.camp;

import cn.evole.plugins.civcraft.util.BlockCoord;

public class CampBlock {
    //XXX TODO merge this with structure block?
    private BlockCoord coord;
    private Camp camp;
    private boolean friendlyBreakable = false;

    public CampBlock(BlockCoord coord, Camp camp) {
        this.coord = coord;
        this.camp = camp;
    }

    public CampBlock(BlockCoord coord, Camp camp, boolean friendlyBreakable) {
        this.coord = coord;
        this.camp = camp;
        this.friendlyBreakable = friendlyBreakable;
    }

    public BlockCoord getCoord() {
        return coord;
    }

    public void setCoord(BlockCoord coord) {
        this.coord = coord;
    }

    public Camp getCamp() {
        return camp;
    }

    public void setCamp(Camp camp) {
        this.camp = camp;
    }

    public int getX() {
        return this.coord.getX();
    }

    public int getY() {
        return this.coord.getY();
    }

    public int getZ() {
        return this.coord.getZ();
    }

    public String getWorldname() {
        return this.coord.getWorldname();
    }

    public boolean canBreak(String playerName) {
        if (this.friendlyBreakable == false) {
            return false;
        }

        if (camp.hasMember(playerName)) {
            return true;
        }

        return false;
    }

}
