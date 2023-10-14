/**
 * [2023] Cnlimiter LLC
 * All Rights Reserved.
 * Name: civcraft
 * Author: cnlimiter
 * UpdateTime: 2023/10/14 16:15
 * Description:
 * License: ARR
 */
package cn.evole.plugins.civcraft.items.components;

import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.config.ConfigUnit;
import cn.evole.plugins.civcraft.items.units.Unit;
import cn.evole.plugins.civcraft.loreenhancements.*;
import cn.evole.plugins.civcraft.main.CivCraft;
import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.main.CivMessage;
import cn.evole.plugins.civcraft.object.Resident;
import cn.evole.plugins.civcraft.util.CivColor;
import cn.evole.plugins.civcraft.util.Levitate;
import cn.evole.plugins.civcraft.util.TimeTools;
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
            //中毒？
            if (loreEnhancement instanceof LoreEnhancementPoison
                    && !event.isCancelled()
                    && event.getEntity() instanceof LivingEntity
                    && !(event.getEntity() instanceof ArmorStand)
                    && !(event.getEntity() instanceof Slime)) {
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
            //击飞？
            if (loreEnhancement instanceof LoreEnhancementLevitate
                    && !event.isCancelled()
                    && event.getEntity() instanceof LivingEntity
                    && !(event.getEntity() instanceof ArmorStand)
                    && !(event.getEntity() instanceof Slime)
                    && event.getEntity() instanceof Player
                    && !(resident = CivGlobal.getResident(player = (Player) event.getEntity())).isLevitateImmune()) {
                resident.addLevitateImmune();
                new Levitate(player, 3000L).start();
            }
            int lightning_chance = CivCraft.civRandom.nextInt(1000000);
            //雷击
            if (!(loreEnhancement instanceof LoreEnhancementLightningStrike)
                    || event.isCancelled()
                    || lightning_chance > 55000)
                continue;
            if (event.getEntity() instanceof LivingEntity
                    && !(event.getEntity() instanceof ArmorStand)
                    && !(event.getEntity() instanceof Slime)) {
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
