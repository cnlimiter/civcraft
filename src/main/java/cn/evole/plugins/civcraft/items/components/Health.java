package cn.evole.plugins.civcraft.items.components;

import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.loreenhancements.LoreEnhancement;
import cn.evole.plugins.civcraft.loreenhancements.LoreEnhancementDefense;
import cn.evole.plugins.civcraft.lorestorage.LoreCraftableMaterial;
import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.main.CivMessage;
import cn.evole.plugins.civcraft.object.Resident;
import cn.evole.plugins.civcraft.util.CivColor;
import gpl.AttributeUtil;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;

public class Health extends ItemComponent {
    @Override
    public void onPrepareCreate(AttributeUtil attrs) {
        attrs.addLore(CivColor.Blue + this.getDouble("value") + " " + CivSettings.localize.localizedString("newItemLore_Defense"));
    }

    @Override
    public void onHold(PlayerItemHeldEvent event) {
        Resident resident = CivGlobal.getResident(event.getPlayer());
        if (!resident.hasTechForItem(event.getPlayer().getInventory().getItem(event.getNewSlot()))) {
            CivMessage.send((Object) resident, CivColor.Red + CivSettings.localize.localizedString("itemLore_Warning") + " - " + CivColor.LightGray + CivSettings.localize.localizedString("itemLore_defenseHalfPower"));
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
            extraDef += ((LoreEnhancementDefense) enh).getExtraDefense(attrs);
        }
        defValue += extraDef;
        double damage = event.getDamage();
        if (event.getEntity() instanceof Player && !(CivGlobal.getResident((Player) event.getEntity())).hasTechForItem(stack)) {
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

