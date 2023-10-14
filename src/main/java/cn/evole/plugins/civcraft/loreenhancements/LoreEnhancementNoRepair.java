package cn.evole.plugins.civcraft.loreenhancements;

import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.util.CivColor;
import gpl.AttributeUtil;
import org.bukkit.inventory.ItemStack;

public class LoreEnhancementNoRepair
        extends LoreEnhancement {
    @Override
    public String getDisplayName() {
        return CivColor.GrayBold + CivSettings.localize.localizedString("itemLore_noRepair");
    }

    @Override
    public AttributeUtil add(AttributeUtil attrs) {
        attrs.addEnhancement("LoreEnhancementNoRepair", null, null);
        attrs.addLore(CivColor.GrayBold + this.getDisplayName());
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

