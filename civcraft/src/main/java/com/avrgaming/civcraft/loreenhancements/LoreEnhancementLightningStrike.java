
package com.avrgaming.civcraft.loreenhancements;

import gpl.AttributeUtil;
import org.bukkit.inventory.ItemStack;
import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.loreenhancements.LoreEnhancement;
import com.avrgaming.civcraft.util.CivColor;

public class LoreEnhancementLightningStrike
extends LoreEnhancement {
    @Override
    public AttributeUtil add(AttributeUtil attrs) {
        attrs.addEnhancement("LoreEnhancementLightningStrike", null, null);
        attrs.addLore(CivColor.RoseItalic + this.getDisplayName());
        return attrs;
    }

    @Override
    public boolean hasEnchantment(ItemStack item) {
        AttributeUtil attrs = new AttributeUtil(item);
        return attrs.hasEnhancement("LoreEnhancementLightningStrike");
    }

    @Override
    public String getDisplayName() {
        return CivSettings.localize.localizedString("loreEnh_LightStrike");
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

