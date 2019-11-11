
package com.avrgaming.civcraft.loreenhancements;

import gpl.AttributeUtil;
import org.bukkit.inventory.ItemStack;
import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.loreenhancements.LoreEnhancement;
import com.avrgaming.civcraft.util.CivColor;

public class LoreEnhancementLevitate
extends LoreEnhancement {
    @Override
    public String getDisplayName() {
        return CivColor.LightGrayBold + CivSettings.localize.localizedString("itemLore_levitate");
    }

    @Override
    public AttributeUtil add(AttributeUtil attrs) {
        attrs.addEnhancement("LoreEnhancementLevitate", null, null);
        attrs.addLore(CivColor.LightGrayBold + this.getDisplayName());
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

