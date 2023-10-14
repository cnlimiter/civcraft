/**
 * [2023] Cnlimiter LLC
 * All Rights Reserved.
 * Name: civcraft
 * Author: cnlimiter
 * UpdateTime: 2023/10/14 16:15
 * Description:
 * License: ARR
 */
package cn.evole.plugins.civcraft.threading.sync.request;

import cn.evole.plugins.civcraft.util.MultiInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.locks.ReentrantLock;

public class UpdateInventoryRequest extends AsyncRequest {

    public MultiInventory multiInv;
    public Inventory inv;
    public int index;
    public ItemStack[] cont;
    public ItemStack stack;
    public Action action;
    public UpdateInventoryRequest(ReentrantLock lock) {
        super(lock);
    }
    public enum Action {
        ADD,
        REMOVE,
        SET,
        REPLACE
    }

}
