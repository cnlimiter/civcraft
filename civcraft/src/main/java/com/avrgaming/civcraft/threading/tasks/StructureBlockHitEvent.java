/*************************************************************************
 * 
 * AVRGAMING LLC
 * __________________
 * 
 *  [2013] AVRGAMING LLC
 *  All Rights Reserved.
 * 
 * NOTICE:  All information contained herein is, and remains
 * the property of AVRGAMING LLC and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to AVRGAMING LLC
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from AVRGAMING LLC.
 */
package com.avrgaming.civcraft.threading.tasks;

import java.util.Random;

import gpl.AttributeUtil;
import net.minecraft.server.v1_11_R1.Material;

import org.bukkit.World;
import org.bukkit.entity.Player;

import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.exception.CivException;
import com.avrgaming.civcraft.items.units.Unit;
import com.avrgaming.civcraft.loreenhancements.LoreEnhancement;
import com.avrgaming.civcraft.lorestorage.LoreMaterial;
import com.avrgaming.civcraft.main.CivCraft;
import com.avrgaming.civcraft.main.CivData;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.object.BuildableDamageBlock;
import com.avrgaming.civcraft.object.Resident;
import com.avrgaming.civcraft.structure.Capitol;
import com.avrgaming.civcraft.structure.TownHall;
import com.avrgaming.civcraft.util.BlockCoord;
import com.avrgaming.civcraft.util.CivColor;

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
        }
        catch (CivException e) {
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
                if (resident.getCiv().getCapitol().getBuffManager().hasBuff("level6_extraCPdmgTown") && (CivGlobal.getNearestBuildable(player.getLocation()) instanceof Capitol || CivGlobal.getNearestBuildable(player.getLocation()) instanceof TownHall)) {
                    addinationalDamage += this.getAddinationalBreak();
                }
                if (resident.getCiv().getCapitol().getBuffManager().hasBuff("level6_extraStrucutreDmgTown") && !(CivGlobal.getNearestBuildable(player.getLocation()) instanceof Capitol) && !(CivGlobal.getNearestBuildable(player.getLocation()) instanceof TownHall)) {
                    addinationalDamage += this.getAddinationalBreak();
                }
            }
			if (damage > 1 && dmgBlock.isDamageable()) {
				CivMessage.send(player, CivColor.LightGray+CivSettings.localize.localizedString("var_StructureBlockHitEvent_punchoutDmg",(damage-1)));
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
                CivMessage.send(player, "§a" + CivSettings.localize.localizedString("var_StructureBlockHitEvent_invaderDmg", "§2" + invader + "§a", "§2" +  CivSettings.localize.localizedString("Damage") + "§a", "§c" + (CivGlobal.getNearestBuildable(player.getLocation()).getName().contains("Town Hall") ? "Town Hall" : "Capitol")));
                damage += invader;
            }	
			dmgBlock.getOwner().onDamage(damage, world, player, dmgBlock.getCoord(), dmgBlock);
		} else {
			CivMessage.sendErrorNoRepeat(player, 
					CivSettings.localize.localizedString("var_StructureBlockHitEvent_Invulnerable",dmgBlock.getOwner().getDisplayName()));
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

