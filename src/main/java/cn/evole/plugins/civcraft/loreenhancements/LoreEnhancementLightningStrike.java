package cn.evole.plugins.civcraft.loreenhancements;

import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.util.CivColor;
import gpl.AttributeUtil;
import org.bukkit.inventory.ItemStack;

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

