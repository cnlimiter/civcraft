/**
 * [2023] Cnlimiter LLC
 * All Rights Reserved.
 * Name: civcraft
 * Author: cnlimiter
 * UpdateTime: 2023/10/14 16:15
 * Description:
 * License: ARR
 */
package cn.evole.plugins.civcraft.threading.tasks;

import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.exception.CivException;
import cn.evole.plugins.civcraft.items.units.Unit;
import cn.evole.plugins.civcraft.loreenhancements.LoreEnhancement;
import cn.evole.plugins.civcraft.lorestorage.LoreMaterial;
import cn.evole.plugins.civcraft.main.CivCraft;
import cn.evole.plugins.civcraft.main.CivData;
import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.main.CivMessage;
import cn.evole.plugins.civcraft.object.BuildableDamageBlock;
import cn.evole.plugins.civcraft.object.Resident;
import cn.evole.plugins.civcraft.structure.Capitol;
import cn.evole.plugins.civcraft.structure.TownHall;
import cn.evole.plugins.civcraft.util.BlockCoord;
import cn.evole.plugins.civcraft.util.CivColor;
import gpl.AttributeUtil;
import net.minecraft.server.v1_12_R1.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Random;

public class StructureBlockHitEvent implements Runnable {

    /*
     * Called when a structure block is hit, this async task quickly determines
     * if the block hit should take damage during war.
     *
     */
    String playerName;
    BlockCoord coord;
    BuildableDamageBlock dmgBlock;
    World world;

    public StructureBlockHitEvent(String player, BlockCoord coord, BuildableDamageBlock dmgBlock, World world) {
        this.playerName = player;
        this.coord = coord;
        this.dmgBlock = dmgBlock;
        this.world = world;
    }

    @Override
    public void run() {

        if (playerName == null) {
            return;
        }
        Player player;
        Resident resident;
        try {
            player = CivGlobal.getPlayer(this.playerName);
            resident = CivGlobal.getResident(player);
        } catch (CivException e) {
            return;
        }
        if (dmgBlock.allowDamageNow(player)) {
            /* Do our damage. */
            int damage = 1;
            LoreMaterial material = LoreMaterial.getMaterial(player.getInventory().getItemInMainHand());
            if (material != null) {
                damage = material.onStructureBlockBreak(dmgBlock, damage);
            }

            if (player.getInventory().getItemInMainHand() != null && !player.getInventory().getItemInMainHand().getType().equals(Material.AIR)) {
                AttributeUtil attrs = new AttributeUtil(player.getInventory().getItemInMainHand());
                for (LoreEnhancement enhance : attrs.getEnhancements()) {
                    damage = enhance.onStructureBlockBreak(dmgBlock, damage);
                }
            }
            int addinationalDamage = 0;
            if (resident.getCiv() != null && resident.getCiv().getCapitol() != null) {
                if (resident.getCiv().getCapitol().getBuffManager().hasBuff("level9_extraCPdmgTown") && (CivGlobal.getNearestBuildable(player.getLocation()) instanceof Capitol || CivGlobal.getNearestBuildable(player.getLocation()) instanceof TownHall)) {
                    addinationalDamage += this.getAddinationalBreak();
                }
                if (resident.getCiv().getCapitol().getBuffManager().hasBuff("level9_extraStrucutreDmgTown") && !(CivGlobal.getNearestBuildable(player.getLocation()) instanceof Capitol) && !(CivGlobal.getNearestBuildable(player.getLocation()) instanceof TownHall)) {
                    addinationalDamage += this.getAddinationalBreak();
                }
            }
            if (damage > 1 && dmgBlock.isDamageable()) {
                CivMessage.send(player, CivColor.LightGray + CivSettings.localize.localizedString("var_StructureBlockHitEvent_punchoutDmg", (damage - 1)));
            }
            if (addinationalDamage != 0) {
                CivMessage.send(player, "§a" + CivSettings.localize.localizedString("var_StructureBlockHitEvent_talentDmg", "§2" + addinationalDamage + "§a", "§2" + CivSettings.localize.localizedString("Damage")));
                damage += addinationalDamage;
            }
            final int engineer = this.getDamageFromEngineer(player);
            if (engineer != 0 && !(CivGlobal.getNearestBuildable(player.getLocation()) instanceof Capitol) && !(CivGlobal.getNearestBuildable(player.getLocation()) instanceof TownHall)) {
                CivMessage.send(player, "§a" + CivSettings.localize.localizedString("var_StructureBlockHitEvent_engineerDmg", "§2" + engineer + "§a", "§2" + CivSettings.localize.localizedString("Damage")));
                damage += engineer;
            }
            final int invader = this.getDamageFromInvader(player);
            if (invader != 0 && (CivGlobal.getNearestBuildable(player.getLocation()) instanceof Capitol || CivGlobal.getNearestBuildable(player.getLocation()) instanceof TownHall)) {
                CivMessage.send(player, "§a" + CivSettings.localize.localizedString("var_StructureBlockHitEvent_invaderDmg", "§2" + invader + "§a", "§2" + CivSettings.localize.localizedString("Damage") + "§a", "§c" + (CivGlobal.getNearestBuildable(player.getLocation()).getName().contains("Town Hall") ? "Town Hall" : "Capitol")));
                damage += invader;
            }
            dmgBlock.getOwner().onDamage(damage, world, player, dmgBlock.getCoord(), dmgBlock);
        } else {
            CivMessage.sendErrorNoRepeat(player,
                    CivSettings.localize.localizedString("var_StructureBlockHitEvent_Invulnerable", dmgBlock.getOwner().getDisplayName()));
        }
    }

    public int getAddinationalBreak() {
        final Random rand = CivCraft.civRandom;
        int damage = 0;
        if (rand.nextInt(100) <= 50) {
            damage += rand.nextInt(2);
        }
        return damage;
    }

    public int getDamageFromEngineer(final Player player) {
        final String units = Unit.getUnitStringIds(player);
        int chance = 100;
        if (!units.contains("ax_engineer")) {
            return 0;
        }
        if (units.contains("ax_invader")) {
            chance /= 2;
        }
        if (chance == 100) {
            return 1;
        }
        if (CivData.randChance(40)) {
            return 1;
        }
        return 0;
    }

    public int getDamageFromInvader(final Player player) {
        final String units = Unit.getUnitStringIds(player);
        int chance = 100;
        if (!units.contains("ax_invader")) {
            return 0;
        }
        if (units.contains("ax_engineer")) {
            chance /= 2;
        }
        if (chance == 100) {
            return 1;
        }
        if (CivData.randChance(40)) {
            return 1;
        }
        return 0;
    }
}

