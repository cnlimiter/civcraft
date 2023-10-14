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
import cn.evole.plugins.civcraft.loreenhancements.LoreEnhancement;
import cn.evole.plugins.civcraft.loreenhancements.LoreEnhancementDefense;
import cn.evole.plugins.civcraft.lorestorage.LoreCraftableMaterial;
import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.main.CivMessage;
import cn.evole.plugins.civcraft.object.Resident;
import cn.evole.plugins.civcraft.util.CivColor;
import gpl.AttributeUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;

public class Defense extends ItemComponent {

    @Override
    public void onPrepareCreate(AttributeUtil attrs) {
        attrs.addLore(CivColor.Blue + "" + this.getDouble("value") + " " + CivSettings.localize.localizedString("newItemLore_Defense"));
    }

    @Override
    public void onHold(PlayerItemHeldEvent event) {

        Resident resident = CivGlobal.getResident(event.getPlayer());
        if (!resident.hasTechForItem(event.getPlayer().getInventory().getItem(event.getNewSlot()))) {
            CivMessage.send(resident, CivColor.Rose + CivSettings.localize.localizedString("itemLore_Warning") + " - " + CivColor.LightGray + CivSettings.localize.localizedString("itemLore_defenseHalfPower"));
        }
    }

    @Override
    public void onDefense(EntityDamageByEntityEvent event, ItemStack stack) {
        double defValue = this.getDouble("value");
        ConfigUnit artifact = Unit.getPlayerUnit((Player) event.getEntity());
        /* Try to get any extra defense enhancements from this item. */
        LoreCraftableMaterial craftMat = LoreCraftableMaterial.getCraftMaterial(stack);
        if (craftMat == null) {
            return;
        }

        double extraDef = 0;
        AttributeUtil attrs = new AttributeUtil(stack);

        for (LoreEnhancement enh : attrs.getEnhancements()) {
            if (enh instanceof LoreEnhancementDefense) {
                extraDef += ((LoreEnhancementDefense) enh).getExtraDefense(attrs);
            }
        }

        defValue += extraDef;
        // TODO :护甲额外增伤
        if (artifact != null) {
            switch (artifact.id) {
                case "a_berserker": {
                    defValue *= 1.15;
                    break;
                }
                case "a_spearman": {
                    defValue *= 1.3;
                    break;
                }
                case "a_slinger": {
                    defValue *= 1.15;
                    break;
                }
                case "a_musketman": {
                    defValue *= 1.15;
                    break;
                }
                case "a_knight": {
                    defValue *= 1.45;
                    break;
                }
            }
        }
        double damage = event.getDamage();

        if (event.getEntity() instanceof Player) {
            Resident resident = CivGlobal.getResident(((Player) event.getEntity()));
            if (!resident.hasTechForItem(stack)) {
                defValue = defValue / 2;
            }
        }

        damage -= defValue;
        if (damage < 0.5) {
            /* Always do at least 0.5 damage. */
            damage = 0.5;
        }

        event.setDamage(damage);
    }

}
