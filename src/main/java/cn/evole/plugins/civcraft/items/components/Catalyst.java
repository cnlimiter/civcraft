package cn.evole.plugins.civcraft.items.components;

import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.exception.InvalidConfiguration;
import cn.evole.plugins.civcraft.loreenhancements.LoreEnhancement;
import cn.evole.plugins.civcraft.lorestorage.LoreCraftableMaterial;
import cn.evole.plugins.civcraft.lorestorage.LoreMaterial;
import cn.evole.plugins.civcraft.main.CivData;
import cn.evole.plugins.civcraft.main.CivLog;
import cn.evole.plugins.civcraft.object.Town;
import cn.evole.plugins.civcraft.util.CivColor;
import gpl.AttributeUtil;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class Catalyst extends ItemComponent {

    @Override
    public void onPrepareCreate(AttributeUtil attrUtil) {
        attrUtil.addLore(ChatColor.RESET + CivColor.Gold + CivSettings.localize.localizedString("itemLore_Catalyst"));
    }

    public ItemStack getEnchantedItem(ItemStack stack) {

        LoreCraftableMaterial craftMat = LoreCraftableMaterial.getCraftMaterial(stack);
        if (craftMat == null) {
            return null;
        }

        String[] materials = this.getString("allowed_materials").split(",");
        boolean found = false;
        for (String mat : materials) {
            mat = mat.replaceAll(" ", "");
            if (mat.equals(LoreMaterial.getMID(stack))) {
                found = true;
                break;
            }
        }

        if (!found) {
            return null;
        }

        String enhStr = this.getString("enhancement");

        LoreEnhancement enhance = LoreEnhancement.enhancements.get(enhStr);
        if (enhance == null) {
            CivLog.error("Couldn't find enhancement titled:" + enhStr);
            return null;
        }


        if (enhance.canEnchantItem(stack)) {
            AttributeUtil attrs = new AttributeUtil(stack);
            enhance.variables.put("amount", getString("amount"));
            attrs = enhance.add(attrs);
            return attrs.getStack();

        }

        return null;
    }

    public int getEnhancedLevel(ItemStack stack) {
        String enhStr = this.getString("enhancement");

        LoreEnhancement enhance = LoreEnhancement.enhancements.get(enhStr);
        if (enhance == null) {
            CivLog.error("Couldn't find enhancement titled:" + enhStr);
            return 0;
        }

        return (int) enhance.getLevel(new AttributeUtil(stack));
    }

    public boolean enchantSuccess(ItemStack stack) {
        try {
            int free_catalyst_amount = CivSettings.getInteger(CivSettings.civConfig, "global.free_catalyst_amount");
            int extra_catalyst_amount = CivSettings.getInteger(CivSettings.civConfig, "global.extra_catalyst_amount");
            double extra_catalyst_percent = CivSettings.getDouble(CivSettings.civConfig, "global.extra_catalyst_percent");

            int level = getEnhancedLevel(stack);

            if (level <= free_catalyst_amount) {
                return true;
            }

            int chance = Integer.parseInt(getString("chance"));
            Random rand = new Random();
            int extra = 0;
            int n = rand.nextInt(100);

            if (level <= extra_catalyst_amount) {
                n -= (int) (extra_catalyst_percent * 100);
            }

            n += extra;

            if (n <= chance) {
                return true;
            }

            return false;
        } catch (InvalidConfiguration e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean enchantSuccess(String catalystId, int bonusAttack, double bonusDefence, Town owner) {
        boolean isAttack = catalystId.contains("_attack_catalyst");
        boolean superCatalyst = catalystId.contains("_super_catalyst");
        if (superCatalyst) {
            return true;
        }
        if (isAttack && bonusAttack <= 1) {
            return true;
        }
        if (!isAttack && bonusDefence <= 0.25) {
            return true;
        }
        int x = owner.getXChance();
        if (isAttack) {
            return CivData.randChance(60 + x);
        }
        return CivData.randChance(70 + x);
    }


}
