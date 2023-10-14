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

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

public class DelayItemDrop implements Runnable {

    Location loc;
    ItemStack stack;

    public DelayItemDrop(ItemStack stack, Location loc) {
        this.loc = loc;
        this.stack = stack;
    }

    @Override
    public void run() {
        loc.getWorld().dropItem(loc, stack);
    }

}
