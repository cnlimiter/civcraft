package cn.evole.plugins.civcraft.loreenhancements;

import cn.evole.plugins.civcraft.util.CivColor;
import gpl.AttributeUtil;
import org.bukkit.inventory.ItemStack;

public class LoreEnhancementDropAfterDeath
        extends LoreEnhancement {
    @Override
    public AttributeUtil add(AttributeUtil attrs) {
        attrs.addEnhancement("LoreEnhancementBuyItem", null, null);
        attrs.addLore(CivColor.Red + this.getDisplayName());
        return attrs;
    }

    @Override
    public String getDisplayName() {
        return "Vanishing";
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

