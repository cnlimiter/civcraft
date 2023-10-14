/**
 * [2023] Cnlimiter LLC
 * All Rights Reserved.
 * Name: civcraft
 * Author: cnlimiter
 * UpdateTime: 2023/10/14 16:15
 * Description:
 * License: ARR
 */
package cn.evole.plugins.civcraft.structure;

import cn.evole.plugins.civcraft.components.ProjectileArrowComponent;
import cn.evole.plugins.civcraft.exception.CivException;
import cn.evole.plugins.civcraft.object.Buff;
import cn.evole.plugins.civcraft.object.Town;
import cn.evole.plugins.civcraft.util.BlockCoord;
import org.bukkit.Location;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ArrowTower extends Structure {

    ProjectileArrowComponent arrowComponent;

    protected ArrowTower(Location center, String id, Town town) throws CivException {
        super(center, id, town);
        this.hitpoints = this.getMaxHitPoints();
    }

    protected ArrowTower(ResultSet rs) throws SQLException, CivException {
        super(rs);
    }

    @Override
    public void loadSettings() {
        super.loadSettings();
        arrowComponent = new ProjectileArrowComponent(this, this.getCenterLocation().getLocation());
        arrowComponent.createComponent(this);
    }

    /**
     * @return the damage
     */
    public int getDamage() {
        double rate = 1;
        rate += this.getTown().getBuffManager().getEffectiveDouble(Buff.FIRE_BOMB);
        return (int) (arrowComponent.getDamage() * rate);
    }

    @Override
    public int getMaxHitPoints() {
        double rate = 1.0;
        if (this.getTown().getBuffManager().hasBuff("buff_chichen_itza_tower_hp")) {
            rate += this.getTown().getBuffManager().getEffectiveDouble("buff_chichen_itza_tower_hp");
        }
        if (this.getTown().getBuffManager().hasBuff("buff_barricade")) {
            rate += this.getTown().getBuffManager().getEffectiveDouble("buff_barricade");
        }
        if (this.getCiv().getCapitol() != null && this.getCiv().getCapitol().getBuffManager().hasBuff("level6_extraTowerHPTown")) {
            rate *= this.getCiv().getCapitol().getBuffManager().getEffectiveDouble("level6_extraTowerHPTown");
        }
        return (int) ((double) this.info.max_hitpoints * rate);
    }

    /**
     * @param power the power to set
     */
    public void setPower(double power) {
        arrowComponent.setPower(power);
    }

    public void setTurretLocation(BlockCoord absCoord) {
        arrowComponent.setTurretLocation(absCoord);
    }

}
