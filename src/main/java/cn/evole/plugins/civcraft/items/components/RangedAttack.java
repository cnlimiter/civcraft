package cn.evole.plugins.civcraft.items.components;

import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.config.ConfigUnit;
import cn.evole.plugins.civcraft.items.units.Unit;
import cn.evole.plugins.civcraft.loreenhancements.LoreEnhancement;
import cn.evole.plugins.civcraft.loreenhancements.LoreEnhancementAttack;
import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.main.CivMessage;
import cn.evole.plugins.civcraft.object.Resident;
import cn.evole.plugins.civcraft.util.CivColor;
import gpl.AttributeUtil;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class RangedAttack extends ItemComponent {

    private static double ARROW_MAX_VEL = 6.0;

    @Override
    public void onPrepareCreate(AttributeUtil attrs) {
        attrs.addLore(CivColor.Rose + this.getDouble("value") + " " + CivSettings.localize.localizedString("itemLore_RangedAttack"));
    }

    @Override
    public void onInteract(PlayerInteractEvent event) {
        if (Unit.isWearingAnyMetal(event.getPlayer())) {
            event.setCancelled(true);
            CivMessage.sendError(event.getPlayer(), CivSettings.localize.localizedString("itemLore_RangedAttack_errorMetal"));
            return;
        }
    }

    @Override
    public void onHold(PlayerItemHeldEvent event) {

        Resident resident = CivGlobal.getResident(event.getPlayer());
        if (!resident.hasTechForItem(event.getPlayer().getInventory().getItem(event.getNewSlot()))) {
            CivMessage.send(resident, CivColor.Rose + CivSettings.localize.localizedString("itemLore_Warning") + " - " + CivColor.LightGray +
                    CivSettings.localize.localizedString("itemLore_attackHalfDamage"));
        }
    }

    public void onRangedAttack(EntityDamageByEntityEvent event, ItemStack inHand) {
        AttributeUtil attrs = new AttributeUtil(inHand);
        double dmg = this.getDouble("value");
        ConfigUnit artifact = null;
        if (event.getDamager() instanceof Arrow) {
            artifact = Unit.getPlayerUnit((Player) ((Arrow) event.getDamager()).getShooter());
            Arrow arrow = (Arrow) event.getDamager();
            if (arrow.getShooter() instanceof Player) {
                Player attacker = (Player) arrow.getShooter();
                if (Unit.isWearingAnyMetal(attacker)) {
                    event.setCancelled(true);
                    CivMessage.sendError(attacker, CivSettings.localize.localizedString("itemLore_RangedAttack_errorMetal"));
                    return;
                }
            }
        }

        double extraAtt = 0.0;
        for (LoreEnhancement enh : attrs.getEnhancements()) {
            if (enh instanceof LoreEnhancementAttack) {
                extraAtt += ((LoreEnhancementAttack) enh).getExtraAttack(attrs);
            }
        }
        dmg += extraAtt;
        // TODO: 弓的增伤
        if (artifact != null) {
            switch (artifact.id) {
                case "a_archer": {
                    dmg *= 1.25;
                    break;
                }
                case "a_warrior": {
                    dmg -= dmg * 0.1;
                    break;
                }
                case "a_berserker": {
                    dmg *= 1.1;
                    break;
                }
                case "a_swordsman": {
                    dmg -= dmg * 0.2;
                    break;
                }
                case "a_crossbowman": {
                    dmg *= 1.4;
                    break;
                }
                case "a_spearman": {
                    dmg *= 1.1;
                    break;
                }
                case "a_slinger": {
                    dmg *= 1.5;
                    break;
                }
                case "a_musketman": {
                    dmg -= dmg * 0.25;
                    break;
                }
                case "a_knight": {
                    dmg *= 1.2;
                    break;
                }
            }
        }
        Vector vel = event.getDamager().getVelocity();
        double magnitudeSquared = Math.pow(vel.getX(), 2) + Math.pow(vel.getY(), 2) + Math.pow(vel.getZ(), 2);

        double percentage = magnitudeSquared / ARROW_MAX_VEL;
        double totalDmg = percentage * dmg;

        if (totalDmg > dmg) {
            totalDmg = dmg;
        }

        if (event.getDamager() instanceof Arrow) {
            Arrow arrow = (Arrow) event.getDamager();
            if (arrow.getShooter() instanceof Player) {
                Resident resident = CivGlobal.getResident(((Player) arrow.getShooter()));
                if (!resident.hasTechForItem(inHand)) {
                    totalDmg = totalDmg / 2;
                }
            }
        }

        if (totalDmg < 0.5) {
            totalDmg = 0.5;
        }

        event.setDamage(totalDmg);
    }


}
