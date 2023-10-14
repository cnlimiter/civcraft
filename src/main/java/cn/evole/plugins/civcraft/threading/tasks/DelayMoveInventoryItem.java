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

import cn.evole.plugins.civcraft.exception.CivException;
import cn.evole.plugins.civcraft.main.CivGlobal;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class DelayMoveInventoryItem implements Runnable {

    /*
     * Sometimes we want to preform an action on an inventory after a very short delay.
     * For example, if we want to lock an item to a slot, we cannot cancel the move event since
     * that results in the item being 'dropped' on the ground. Instead we have to let the move
     * event complete, and then issue another action to move the item back.
     */

    public int fromSlot;
    public int toSlot;
    public Inventory inv;
    public String playerName;

    @Override
    public void run() {

        ItemStack fromStack = inv.getItem(fromSlot);
        ItemStack toStack = inv.getItem(toSlot);

        if (fromStack != null) {
            inv.setItem(toSlot, fromStack);
            inv.setItem(fromSlot, toStack);
            if (playerName != null) {
                Player player;
                try {
                    player = CivGlobal.getPlayer(playerName);
                    player.updateInventory();
                } catch (CivException e) {
                }
            }
        }
    }


}
