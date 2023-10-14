/**
 * [2023] Cnlimiter LLC
 * All Rights Reserved.
 * Name: civcraft
 * Author: cnlimiter
 * UpdateTime: 2023/10/14 16:15
 * Description:
 * License: ARR
 */
package cn.evole.plugins.civcraft.items;

import cn.evole.plugins.civcraft.exception.CivException;
import cn.evole.plugins.civcraft.listener.CustomItemManager;
import cn.evole.plugins.civcraft.main.CivGlobal;
import org.bukkit.entity.Player;

import java.util.LinkedList;

public class ItemDuraSyncTask implements Runnable {

    @Override
    public void run() {

        for (String playerName : CustomItemManager.itemDuraMap.keySet()) {
            Player player;
            try {
                player = CivGlobal.getPlayer(playerName);
            } catch (CivException e) {
                continue;
            }

            LinkedList<ItemDurabilityEntry> entries = CustomItemManager.itemDuraMap.get(playerName);

            for (ItemDurabilityEntry entry : entries) {
                entry.stack.setDurability(entry.oldValue);
            }

            player.updateInventory();
        }

        CustomItemManager.duraTaskScheduled = false;
    }
}