package cn.evole.plugins.civcraft.items.components;

import cn.evole.plugins.civcraft.lorestorage.LoreCraftableMaterial;
import cn.evole.plugins.civcraft.main.CivData;
import cn.evole.plugins.civcraft.util.ItemManager;
import gpl.AttributeUtil;
import org.bukkit.inventory.ItemStack;

public class Tagged extends ItemComponent {

    public static String matrixHasSameTag(ItemStack[] matrix) {
        String tag = null;

        for (ItemStack stack : matrix) {
            if ((stack == null) || (ItemManager.getId(stack) == CivData.AIR)) {
                continue;
            }

            LoreCraftableMaterial craftMat = LoreCraftableMaterial.getCraftMaterial(stack);
            if (craftMat == null) {
                return null;
            }

            Tagged tagged = (Tagged) craftMat.getComponent("Tagged");
            if (tagged == null) {
                return null;
            }

            if (tag == null) {
                tag = tagged.getTag(stack);
                continue;
            } else {
                if (!tagged.getTag(stack).equals(tag)) {
                    return null;
                }
            }
        }

        return tag;

    }

    @Override
    public void onPrepareCreate(AttributeUtil attrUtil) {
    }

    public ItemStack addTag(ItemStack src, String tag) {
        AttributeUtil attrs = new AttributeUtil(src);
        attrs.setCivCraftProperty("tag", tag);
        return attrs.getStack();
    }

    public String getTag(ItemStack src) {
        AttributeUtil attrs = new AttributeUtil(src);
        return attrs.getCivCraftProperty("tag");
    }
}
