
package com.avrgaming.civcraft.loreenhancements;

import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.util.CivColor;
import gpl.AttributeUtil;
import org.bukkit.inventory.ItemStack;

public class LoreEnhancementLevitate extends LoreEnhancement {
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

