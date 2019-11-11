
package com.avrgaming.civcraft.items.components;

import gpl.AttributeUtil;

import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;

import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.items.components.ItemComponent;
import com.avrgaming.civcraft.loreenhancements.LoreEnhancement;
import com.avrgaming.civcraft.loreenhancements.LoreEnhancementDefense;
import com.avrgaming.civcraft.lorestorage.LoreCraftableMaterial;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.object.Resident;
import com.avrgaming.civcraft.util.CivColor;

public class Health
extends ItemComponent {
    @Override
    public void onPrepareCreate(AttributeUtil attrs) {
        attrs.addLore(CivColor.Blue + this.getDouble("value") + " " + CivSettings.localize.localizedString("newItemLore_Defense"));
    }

    @Override
    public void onHold(PlayerItemHeldEvent event) {
        Resident resident = CivGlobal.getResident(event.getPlayer());
        if (!resident.hasTechForItem(event.getPlayer().getInventory().getItem(event.getNewSlot()))) {
            CivMessage.send((Object)resident, CivColor.Red + CivSettings.localize.localizedString("itemLore_Warning") + " - " + CivColor.LightGray + CivSettings.localize.localizedString("itemLore_defenseHalfPower"));
        }
    }

    @Override
    public void onDefense(EntityDamageByEntityEvent event, ItemStack stack) {
        double defValue = this.getDouble("value");
        LoreCraftableMaterial craftMat = LoreCraftableMaterial.getCraftMaterial(stack);
        if (craftMat == null) {
            return;
        }
        double extraDef = 0.0;
        AttributeUtil attrs = new AttributeUtil(stack);
        for (LoreEnhancement enh : attrs.getEnhancements()) {
            if (!(enh instanceof LoreEnhancementDefense)) continue;
            extraDef += ((LoreEnhancementDefense)enh).getExtraDefense(attrs);
        }
        defValue += extraDef;
        double damage = event.getDamage();
        if (event.getEntity() instanceof Player && !(CivGlobal.getResident((Player)event.getEntity())).hasTechForItem(stack)) {
            defValue /= 2.0;
        }
        if ((damage -= defValue) < 0.5) {
            damage = 0.5;
        }
        if (event.getEntity() instanceof Arrow && !(event.getEntity() instanceof Skeleton) && !(event.getEntity() instanceof Player) && damage < 0.5) {
            damage = 3.5;
        }
        event.setDamage(damage);
    }
}

