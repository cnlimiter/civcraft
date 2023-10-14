/**
 * [2023] Cnlimiter LLC
 * All Rights Reserved.
 * Name: civcraft
 * Author: cnlimiter
 * UpdateTime: 2023/10/14 16:15
 * Description:
 * License: ARR
 */
package cn.evole.plugins.civcraft.structure.wonders;

import cn.evole.plugins.civcraft.exception.CivException;
import cn.evole.plugins.civcraft.object.Town;
import org.bukkit.Location;

import java.sql.ResultSet;
import java.sql.SQLException;

public class TheColossus extends Wonder {

    public TheColossus(Location center, String id, Town town)
            throws CivException {
        super(center, id, town);
    }

    public TheColossus(ResultSet rs) throws SQLException, CivException {
        super(rs);
    }

    @Override
    public void onLoad() {
        if (this.isActive()) {
            addBuffs();
        }
    }

    @Override
    public void onComplete() {
        addBuffs();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        removeBuffs();
    }

    @Override
    protected void removeBuffs() {
        this.removeBuffFromTown(this.getTown(), "buff_colossus_reduce_upkeep");
        this.removeBuffFromTown(this.getTown(), "buff_colossus_coins_from_culture");
    }

    @Override
    protected void addBuffs() {
        this.addBuffToTown(this.getTown(), "buff_colossus_reduce_upkeep");
        this.addBuffToTown(this.getTown(), "buff_colossus_coins_from_culture");

    }

}
