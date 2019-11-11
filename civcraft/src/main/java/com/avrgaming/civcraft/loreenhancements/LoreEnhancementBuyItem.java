
package com.avrgaming.civcraft.loreenhancements;

import gpl.AttributeUtil;
import org.bukkit.inventory.ItemStack;

public class LoreEnhancementBuyItem
extends LoreEnhancement {
    @Override
    public AttributeUtil add(AttributeUtil attrs) {
        attrs.addEnhancement("LoreEnhancementBuyItem", null, null);
        attrs.addLore("§b" + this.getDisplayName());
        return attrs;
    }

    @Override
    public String getDisplayName() {
        return "Buy Item";
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

