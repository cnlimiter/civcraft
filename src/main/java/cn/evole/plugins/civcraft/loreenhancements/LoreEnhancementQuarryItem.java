package cn.evole.plugins.civcraft.loreenhancements;

import cn.evole.plugins.civcraft.util.CivColor;
import gpl.AttributeUtil;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public class LoreEnhancementQuarryItem
        extends LoreEnhancement
        implements Listener {
    @Override
    public String getDisplayName() {
        return "Quarry Item";
    }

    @Override
    public AttributeUtil add(AttributeUtil attrs) {
        attrs.addEnhancement("LoreEnhancementQuarryItem", null, null);
        attrs.addLore(CivColor.RoseBold + this.getDisplayName());
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

