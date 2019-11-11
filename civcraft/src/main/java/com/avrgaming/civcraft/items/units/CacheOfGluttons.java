
package com.avrgaming.civcraft.items.units;

import gpl.AttributeUtil;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.config.ConfigUnit;
import com.avrgaming.civcraft.items.units.Unit;
import com.avrgaming.civcraft.items.units.UnitMaterial;
import com.avrgaming.civcraft.lorestorage.LoreMaterial;

import com.avrgaming.civcraft.exception.CivException;
import com.avrgaming.civcraft.object.Town;
import com.avrgaming.civcraft.util.CivColor;

public class CacheOfGluttons
extends UnitMaterial {
    public CacheOfGluttons(String id, ConfigUnit configUnit) {
        super(id, configUnit);
    }

    public static void spawn(Inventory inv, Town town) throws CivException {
        ItemStack is = LoreMaterial.spawn(Unit.CACHEOFGLUTTONS_ARTIFACT);
        CacheOfGluttons.setOwningTown(town, is);
        AttributeUtil attrs = new AttributeUtil(is);
        attrs.addEnhancement("LoreEnhancementSoulBound", null, null);
        attrs.addLore(CivColor.Gold+CivSettings.localize.localizedString("itemLore_Souldbound"));
        attrs.addLore(CivColor.LightGray + "Effect:");
        attrs.addLore(CivColor.LightGray + "Passive");
        attrs.addLore(CivColor.LightGray + "Removes Hunger");
        attrs.addLore(CivColor.LightGray + "Reusable: No");
        is = attrs.getStack();
        if (!Unit.addItemNoStack(inv, is)) {
            throw new CivException(CivSettings.localize.localizedString("var_arrtifacts_errorBarracksFull", Unit.CACHEOFGLUTTONS_ARTIFACT.getUnit().name));
        }
    }
}

