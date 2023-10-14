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

import cn.evole.plugins.civcraft.components.ProjectileCannonComponent;
import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.exception.CivException;
import cn.evole.plugins.civcraft.exception.InvalidConfiguration;
import cn.evole.plugins.civcraft.object.Buff;
import cn.evole.plugins.civcraft.object.Town;
import cn.evole.plugins.civcraft.util.BlockCoord;
import org.bukkit.Location;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CannonTower extends Structure {

    ProjectileCannonComponent cannonComponent;

    protected CannonTower(Location center, String id, Town town)
            throws CivException {
        super(center, id, town);
        this.hitpoints = this.getMaxHitPoints();
    }

    protected CannonTower(ResultSet rs) throws SQLException, CivException {
        super(rs);
    }

    @Override
    public void loadSettings() {
        super.loadSettings();
        cannonComponent = new ProjectileCannonComponent(this, this.getCenterLocation().getLocation());
        cannonComponent.createComponent(this);
    }

    public int getDamage() {
        double rate = 1;
        rate += this.getTown().getBuffManager().getEffectiveDouble(Buff.FIRE_BOMB);
        return (int) (cannonComponent.getDamage() * rate);
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

//	public void setDamage(int damage) {
//		cannonComponent.setDamage(damage);
//	}


    public void setTurretLocation(BlockCoord absCoord) {
        cannonComponent.setTurretLocation(absCoord);
    }


//	@Override
//	public void fire(Location turretLoc, Location playerLoc) {
//		turretLoc = adjustTurretLocation(turretLoc, playerLoc);
//		Vector dir = getVectorBetween(playerLoc, turretLoc);
//		
//		Fireball fb = turretLoc.getWorld().spawn(turretLoc, Fireball.class);
//		fb.setDirection(dir);
//		// NOTE cannon does not like it when the dir is normalized or when velocity is set.
//		fb.setYield((float)yield);
//		CivCache.cannonBallsFired.put(fb.getUniqueId(), new CannonFiredCache(this, playerLoc, fb));
//	}

    @Override
    public void onCheck() throws CivException {
        try {
            double build_distance = CivSettings.getDouble(CivSettings.warConfig, "cannon_tower.build_distance");

            for (Town town : this.getTown().getCiv().getTowns()) {
                for (Structure struct : town.getStructures()) {
                    if (struct instanceof CannonTower) {
                        BlockCoord center = struct.getCenterLocation();
                        double distance = center.distance(this.getCenterLocation());
                        if (distance <= build_distance) {
                            throw new CivException(CivSettings.localize.localizedString("var_buildable_tooCloseToCannonTower", (center.getX() + "," + center.getY() + "," + center.getZ())));
                        }
                    }
                    if (struct instanceof CannonShip) {
                        BlockCoord center = struct.getCenterLocation();
                        double distance = center.distance(this.getCenterLocation());
                        if (distance <= build_distance) {
                            throw new CivException(CivSettings.localize.localizedString("var_buildable_tooCloseToCannonShip", (center.getX() + "," + center.getY() + "," + center.getZ())));
                        }
                    }
                }
            }
        } catch (InvalidConfiguration e) {
            e.printStackTrace();
            throw new CivException(e.getMessage());
        }

    }

}
