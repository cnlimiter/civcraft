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

import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.main.CivMessage;
import cn.evole.plugins.civcraft.structure.Buildable;
import cn.evole.plugins.civcraft.util.BlockCoord;
import cn.evole.plugins.civcraft.war.War;
import org.bukkit.entity.Player;

public class StructureBlock implements BuildableDamageBlock {

    private BlockCoord coord = null;
    private Buildable owner = null;
    private boolean damageable = true;
    private boolean alwaysDamage = false;

    /* This is a block that can be damaged. */
    public StructureBlock(BlockCoord coord, Buildable owner) {
        this.coord = coord;
        this.owner = owner;
    }

    public Buildable getOwner() {
        return owner;
    }

    public void setOwner(Buildable owner) {
        this.owner = owner;
    }

    public Town getTown() {
        return this.owner.getTown();
    }

    public Civilization getCiv() {
        return this.owner.getCiv();
    }

    public BlockCoord getCoord() {
        return coord;
    }

    public void setCoord(BlockCoord coord) {
        this.coord = coord;
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

    public boolean isDamageable() {
        return damageable;
    }

    public void setDamageable(boolean damageable) {
        this.damageable = damageable;
    }

    public boolean canDestroyOnlyDuringWar() {
        return true;
    }

    @Override
    public boolean allowDamageNow(Player player) {
        // Dont bother making any checks if we're not at war
        if (War.isWarTime()) {
            // Structures with max hitpoints of 0 cannot be damaged.
            if (this.getOwner().getMaxHitPoints() != 0) {
                Resident res = CivGlobal.getResident(player.getName());
                if (res == null) {
                    return false;
                }

                // Make sure the resident has a town
                if (res.hasTown()) {
                    if (res.getTown().defeated) {
                        CivMessage.sendError(player, CivSettings.localize.localizedString("structBlock_errorDefeated"));
                        return false;
                    }

                    Civilization civ = res.getTown().getCiv();
                    // Make sure we are at war with this civilization.
                    // Cant be at war with our own, will be false if our own structure.
                    if (civ.getDiplomacyManager().atWarWith(this.getCiv())) {
                        if (this.alwaysDamage) {
                            return true;
                        }

                        if (!this.isDamageable()) {
                            CivMessage.sendError(player, CivSettings.localize.localizedString("structBlock_error1"));
                        } else if (CivGlobal.willInstantBreak(this.getCoord().getBlock().getType())) {
                            CivMessage.sendError(player, CivSettings.localize.localizedString("structBlock_error2"));
                        } else {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public boolean isAlwaysDamage() {
        return alwaysDamage;
    }

    public void setAlwaysDamage(boolean alwaysDamage) {
        this.alwaysDamage = alwaysDamage;
    }
}
