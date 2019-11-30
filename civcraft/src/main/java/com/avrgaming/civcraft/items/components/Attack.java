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
package com.avrgaming.civcraft.items.components;

import gpl.AttributeUtil;
import gpl.AttributeUtil.Attribute;
import gpl.AttributeUtil.AttributeType;

import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.config.ConfigUnit;
import com.avrgaming.civcraft.items.units.Unit;
import com.avrgaming.civcraft.loreenhancements.LoreEnhancement;
import com.avrgaming.civcraft.loreenhancements.LoreEnhancementAttack;
import com.avrgaming.civcraft.loreenhancements.LoreEnhancementLevitate;
import com.avrgaming.civcraft.loreenhancements.LoreEnhancementLightningStrike;
import com.avrgaming.civcraft.loreenhancements.LoreEnhancementPoison;
import com.avrgaming.civcraft.main.CivCraft;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.object.Resident;
import com.avrgaming.civcraft.util.CivColor;
import com.avrgaming.civcraft.util.Levitate;
import com.avrgaming.civcraft.util.TimeTools;

public class Attack extends ItemComponent {

    @Override
    public void onPrepareCreate(AttributeUtil attrs) {

        // Add generic attack damage of 0 to clear the default lore on item.
        attrs.add(Attribute.newBuilder().name("Attack").
                type(AttributeType.GENERIC_ATTACK_DAMAGE).
                amount(0).
                build());
        attrs.addLore(CivColor.Rose + "" + this.getDouble("value") + " " + CivSettings.localize.localizedString("itemLore_Attack"));
        return;
    }

    @Override
    public void onHold(PlayerItemHeldEvent event) {

        Resident resident = CivGlobal.getResident(event.getPlayer());
        if (!resident.hasTechForItem(event.getPlayer().getInventory().getItem(event.getNewSlot()))) {
            CivMessage.send(resident, CivColor.Rose + CivSettings.localize.localizedString("itemLore_Warning") + " - " + CivColor.LightGray + CivSettings.localize.localizedString("itemLore_attackHalfDamage"));
        }
    }

    @Override
    public void onAttack(EntityDamageByEntityEvent event, ItemStack inHand) {
        AttributeUtil attrs = new AttributeUtil(inHand);
        double dmg = this.getDouble("value");
        ConfigUnit artifact = Unit.getPlayerUnit((Player) event.getDamager());
        double extraAtt = 0.0;
        for (LoreEnhancement loreEnhancement : attrs.getEnhancements()) {
            Resident resident;
            Player player;
            if (loreEnhancement instanceof LoreEnhancementAttack) {
                extraAtt += ((LoreEnhancementAttack) loreEnhancement).getExtraAttack(attrs);
            }
            if (loreEnhancement instanceof LoreEnhancementPoison && !event.isCancelled() && event.getEntity() instanceof LivingEntity && !(event.getEntity() instanceof ArmorStand) && !(event.getEntity() instanceof Slime)) {
                if (!(event.getEntity() instanceof Player)) {
                    ((LivingEntity) event.getEntity()).addPotionEffect(new PotionEffect(PotionEffectType.POISON, 50, 1));
                } else {
                    player = (Player) event.getEntity();
                    resident = CivGlobal.getResident(player);
                    if (!resident.isPoisonImmune()) {
                        resident.addPosionImmune();
                        player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 40, 0));
                    }
                }
            }
            if (loreEnhancement instanceof LoreEnhancementLevitate && !event.isCancelled() && event.getEntity() instanceof LivingEntity && !(event.getEntity() instanceof ArmorStand) && !(event.getEntity() instanceof Slime) && event.getEntity() instanceof Player && !(resident = CivGlobal.getResident(player = (Player) event.getEntity())).isLevitateImmune()) {
                resident.addLevitateImmune();
                new Levitate(player, 3000L).start();
            }
            int lightning_chance = CivCraft.civRandom.nextInt(1000000);
            if (!(loreEnhancement instanceof LoreEnhancementLightningStrike) || event.isCancelled() || lightning_chance > 55000)
                continue;
            if (event.getEntity() instanceof LivingEntity && !(event.getEntity() instanceof ArmorStand) && !(event.getEntity() instanceof Slime)) {
                LivingEntity toDamage = (LivingEntity) event.getEntity();
                toDamage.getWorld().strikeLightningEffect(toDamage.getLocation());
                if (toDamage.getHealth() - 6.0 > 0.0) {
                    toDamage.setHealth(toDamage.getHealth() - 6.0);
                    toDamage.setFireTicks((int) TimeTools.toTicks(3L));
                    event.setDamage(0.5);
                } else {
                    toDamage.setHealth(0.1);
                    event.setDamage(1.0);
                }
                Object[] arrobject = new Object[2];
                arrobject[0] = toDamage;
                arrobject[1] = !(toDamage instanceof Player) ? CivColor.RoseBold + CivColor.ITALIC + "Player " + CivColor.RESET + CivColor.LightBlueItalic : "";
                CivMessage.send((Object) event.getDamager(), CivColor.LightBlueItalic + CivSettings.localize.localizedString("loreEnh_LightStrike_Sucusses", arrobject));
                if (!(toDamage instanceof Player)) continue;
                CivMessage.send((Object) event.getEntity(), CivSettings.localize.localizedString("loreEnh_LightStrike_Sucusses2", event.getDamager().getName()));
                continue;
            }
            CivMessage.send((Object) event.getDamager(), CivColor.LightBlueItalic + CivSettings.localize.localizedString("loreEnh_LightStrike_warning"));
        }
        dmg += extraAtt;
        if (artifact != null && artifact.id.equals("a_archer")) {
            dmg -= dmg * 0.1;
        } else if (artifact != null && artifact.id.equals("a_warrior")) {
            dmg *= 1.25;
        } else if (artifact != null && artifact.id.equals("a_berserker")) {
            dmg *= 1.05;
        } else if (artifact != null && artifact.id.equals("a_crossbowman")) {
            dmg -= dmg * 0.2;
        } else if (artifact != null && artifact.id.equals("a_swordsman")) {
            dmg *= 1.4;
        } else if (artifact != null && artifact.id.equals("a_spearman")) {
            dmg *= 1.05;
        } else if (artifact != null && artifact.id.equals("a_slinger")) {
            dmg -= dmg * 0.25;
        } else if (artifact != null && artifact.id.equals("a_musketman")) {
            dmg *= 1.5;
        } else if (artifact != null && artifact.id.equals("a_knight")) {
            dmg *= 1.1;
        }

        if (event.getDamager() instanceof Player) {
            Resident resident = CivGlobal.getResident(((Player) event.getDamager()));
            if (!resident.hasTechForItem(inHand)) {
                dmg = dmg / 2;
            }
        }

        if (dmg < 0.5) {
            dmg = 0.5;
        }

        event.setDamage(dmg);
    }

}
