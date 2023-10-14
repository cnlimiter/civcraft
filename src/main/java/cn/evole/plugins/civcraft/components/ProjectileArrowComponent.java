/**
 * [2023] Cnlimiter LLC
 * All Rights Reserved.
 * Name: civcraft
 * Author: cnlimiter
 * UpdateTime: 2023/10/14 16:15
 * Description:
 * License: ARR
 */
package cn.evole.plugins.civcraft.components;

import cn.evole.plugins.civcraft.cache.ArrowFiredCache;
import cn.evole.plugins.civcraft.cache.CivCache;
import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.exception.InvalidConfiguration;
import cn.evole.plugins.civcraft.object.Buff;
import cn.evole.plugins.civcraft.object.Town;
import cn.evole.plugins.civcraft.structure.Buildable;
import cn.evole.plugins.civcraft.util.BlockCoord;
import org.bukkit.Location;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

public class ProjectileArrowComponent extends ProjectileComponent {

    private double power;
    private boolean isActive = true;
    public ProjectileArrowComponent(Buildable buildable, Location turretCenter) {
        super(buildable, turretCenter);
    }

    @Override
    public void loadSettings() {
        try {
            setDamage(CivSettings.getInteger(CivSettings.warConfig, "arrow_tower.damage"));
            power = CivSettings.getDouble(CivSettings.warConfig, "arrow_tower.power");
            range = CivSettings.getDouble(CivSettings.warConfig, "arrow_tower.range");
            if (this.getTown().getBuffManager().hasBuff("buff_great_lighthouse_tower_range") && this.getBuildable().getConfigId().equals("s_arrowtower")) {
                range *= this.getTown().getBuffManager().getEffectiveDouble("buff_great_lighthouse_tower_range");
            } else if (this.getTown().getBuffManager().hasBuff("buff_ingermanland_water_range") &&
                    (this.getBuildable().getConfigId().equals("w_grand_ship_ingermanland") || this.getBuildable().getConfigId().equals("s_arrowship"))) {
                range *= this.getTown().getBuffManager().getEffectiveDouble("buff_ingermanland_water_range");
            }
            if (this.getTown().getCiv().getCapitol() != null) {
                this.range += this.getTown().getCiv().getCapitol().getBuffManager().getEffectiveDouble("level6_extraRangeTown");
            }
            min_range = CivSettings.getDouble(CivSettings.warConfig, "arrow_tower.min_range");

            this.proximityComponent.setBuildable(buildable);
            this.proximityComponent.setCenter(new BlockCoord(getTurretCenter()));
            this.proximityComponent.setRadius(range);

        } catch (InvalidConfiguration e) {
            e.printStackTrace();
        }
    }

    @Override
    public void fire(Location turretLoc, Entity targetEntity) {
        if (!buildable.isValid() || !isActive) {
            return;
        }

        Location playerLoc = targetEntity.getLocation();
        playerLoc.setY(playerLoc.getY() + 1); //Target the head instead of feet.

        turretLoc = adjustTurretLocation(turretLoc, playerLoc);
        Vector dir = getVectorBetween(playerLoc, turretLoc).normalize();
        Arrow arrow = buildable.getCorner().getLocation().getWorld().spawnArrow(turretLoc, dir, (float) power, 0.0f);
        arrow.setVelocity(dir.multiply(power));

        if (buildable.getTown().getBuffManager().hasBuff(Buff.FIRE_BOMB)) {
            arrow.setFireTicks(1000);
        }

        CivCache.arrowsFired.put(arrow.getUniqueId(), new ArrowFiredCache(this, targetEntity, arrow));
    }

    public double getPower() {
        return power;
    }

    public void setPower(double power) {
        this.power = power;
    }

    public Town getTown() {
        return buildable.getTown();
    }

}
