/**
 * [2023] Cnlimiter LLC
 * All Rights Reserved.
 * Name: civcraft
 * Author: cnlimiter
 * UpdateTime: 2023/10/14 16:15
 * Description:
 * License: ARR
 */
package cn.evole.plugins.civcraft.threading.tasks;

import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.exception.CivException;
import cn.evole.plugins.civcraft.loreenhancements.LoreEnhancement;
import cn.evole.plugins.civcraft.lorestorage.LoreCraftableMaterial;
import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.main.CivLog;
import cn.evole.plugins.civcraft.object.Resident;
import cn.evole.plugins.civcraft.util.ItemManager;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class GivePlayerStartingKit implements Runnable {

    public String name;

    public GivePlayerStartingKit(String name) {
        this.name = name;
    }

    @Override
    public void run() {
        try {
            Player player = CivGlobal.getPlayer(name);

            for (String kitItems : CivSettings.kitItems) {
                String[] split = kitItems.split(":");

                ItemStack stack;
                try {
                    Integer type = Integer.valueOf(split[0]);
                    Integer amount = Integer.valueOf(split[1]);

                    stack = ItemManager.createItemStack(type, amount);


                } catch (NumberFormatException e) {
                    String customMatID = split[0];
                    LoreCraftableMaterial craftMat = LoreCraftableMaterial.getCraftMaterialFromId(customMatID);
                    if (craftMat == null) {
                        CivLog.warning("Couldn't find custom material:" + customMatID + " to give to player on first join.");
                        continue;
                    }

                    stack = LoreCraftableMaterial.spawn(craftMat);
                }
//				if (split[0] == "mat_tutorial_book" || split[0] == "mat_found_camp")
//				{
                stack = LoreCraftableMaterial.addEnhancement(stack, LoreEnhancement.enhancements.get("LoreEnhancementSoulBound"));
//				}

                player.getInventory().addItem(stack);
            }

            Resident resident = CivGlobal.getResident(name);
            if (resident != null) {
                resident.getTreasury().deposit(CivSettings.startingCoins);
                resident.setGivenKit(true);
            }


        } catch (CivException e) {
            //	e.printStackTrace();
            CivLog.warning("Tried to give starting kit to offline player:" + name);
            return;
        }

    }

}
