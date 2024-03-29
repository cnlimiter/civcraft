package cn.evole.plugins.civcraft.loreenhancements;

import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.object.BuildableDamageBlock;
import cn.evole.plugins.civcraft.util.CivColor;
import gpl.AttributeUtil;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class LoreEnhancementPunchout extends LoreEnhancement {

    public String getDisplayName() {
        return CivSettings.localize.localizedString("itemLore_Punchout");
    }

    public AttributeUtil add(AttributeUtil attrs) {
        attrs.addEnhancement("LoreEnhancementPunchout", null, null);
        attrs.addLore(CivColor.Gold + getDisplayName());
        return attrs;
    }

    @Override
    public int onStructureBlockBreak(BuildableDamageBlock sb, int damage) {
        Random rand = new Random();

        if (damage <= 1) {
            if (rand.nextInt(100) <= 50) {
                damage += rand.nextInt(5) + 1;
            }
        }

        return damage;
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