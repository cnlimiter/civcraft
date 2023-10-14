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
import cn.evole.plugins.civcraft.object.ControlPoint;
import cn.evole.plugins.civcraft.object.Town;
import org.bukkit.Location;

import java.sql.ResultSet;
import java.sql.SQLException;


public class ChichenItza extends Wonder {

    public ChichenItza(Location center, String id, Town town)
            throws CivException {
        super(center, id, town);
    }

    public ChichenItza(ResultSet rs) throws SQLException, CivException {
        super(rs);
    }

    @Override
    protected void removeBuffs() {
        removeBuffFromCiv(this.getCiv(), "buff_chichen_itza_tower_hp");
        removeBuffFromCiv(this.getCiv(), "buff_chichen_itza_regen_rate");
        removeBuffFromTown(this.getTown(), "buff_chichen_itza_cp_bonus_hp");
        //This is where the Itza's buff to CP is removed
        for (ControlPoint cp : this.getTown().getTownHall().getControlPoints().values()) {
            cp.setMaxHitpoints((cp.getMaxHitpoints() - (int) this.getTown().getBuffManager().getEffectiveDouble("buff_chichen_itza_cp_bonus_hp")));
        }
    }

    @Override
    protected void addBuffs() {
        addBuffToCiv(this.getCiv(), "buff_chichen_itza_tower_hp");
        addBuffToCiv(this.getCiv(), "buff_chichen_itza_regen_rate");
        addBuffToTown(this.getTown(), "buff_chichen_itza_cp_bonus_hp");
        //This is where the Itza's buff to CP applies
        for (ControlPoint cp : this.getTown().getTownHall().getControlPoints().values()) {
            cp.setMaxHitpoints((cp.getMaxHitpoints() + (int) this.getTown().getBuffManager().getEffectiveDouble("buff_chichen_itza_cp_bonus_hp")));
        }
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

}
