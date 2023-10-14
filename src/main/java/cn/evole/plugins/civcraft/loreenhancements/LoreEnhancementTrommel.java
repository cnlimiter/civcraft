package cn.evole.plugins.civcraft.loreenhancements;

import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.util.CivColor;
import gpl.AttributeUtil;
import org.bukkit.inventory.ItemStack;

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

