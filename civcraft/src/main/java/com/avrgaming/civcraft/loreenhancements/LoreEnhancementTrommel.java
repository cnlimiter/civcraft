
package com.avrgaming.civcraft.loreenhancements;

import gpl.AttributeUtil;
import org.bukkit.inventory.ItemStack;
import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.loreenhancements.LoreEnhancement;
import com.avrgaming.civcraft.util.CivColor;

public class LoreEnhancementTrommel
extends LoreEnhancement {
    @Override
    public String getDisplayName() {
        return CivColor.LightGreenBold + CivSettings.localize.localizedString("itemLore_trommel");
    }

    @Override
    public AttributeUtil add(AttributeUtil attrs) {
        attrs.addEnhancement("LoreEnhancementTrommel", null, null);
        attrs.addLore(CivColor.LightGreenBold + this.getDisplayName());
        return attrs;
    }

    @Override
    public String serialize(ItemStack stack) {
        return "";
    }

    @Override
    public ItemStack deserialize(ItemStack stack, String data) {
        return stack;
    }
}

