/**
 * [2023] Cnlimiter LLC
 * All Rights Reserved.
 * Name: civcraft
 * Author: cnlimiter
 * UpdateTime: 2023/10/14 16:15
 * Description:
 * License: ARR
 */
package cn.evole.plugins.civcraft.lorestorage;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;

public class LoreGuiItemListener implements Listener {

    public static HashMap<String, Inventory> guiInventories = new HashMap<String, Inventory>();

    public static boolean isGUIInventory(Inventory inv) {
        return guiInventories.containsKey(inv.getName());
    }

    /*
     * First phase of inventory click that cancels any
     * event that was clicked on a gui item.
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void OnInventoryClick(InventoryClickEvent event) {
        if (LoreGuiItem.isGUIItem(event.getCurrentItem())
                || LoreGuiItem.isGUIItem(event.getCursor())) {
            event.setCancelled(true);

            if (event.getCurrentItem() != null) {
                String action = LoreGuiItem.getAction(event.getCurrentItem());
                if (action != null) {
                    LoreGuiItem.processAction(action, event.getCurrentItem(), event);
                }
                return;
            }

            if (event.getCursor() != null) {
                String action = LoreGuiItem.getAction(event.getCursor());
                if (action != null) {
                    LoreGuiItem.processAction(action, event.getCursor(), event);
                }
                return;
            }

        }
    }

    /*
     * The second phase cancels the event if a non-gui item has been
     * dropped into a gui inventory.
     */
    @EventHandler(priority = EventPriority.LOW)
    public void OnInventoryClickSecondPhase(InventoryClickEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (event.getRawSlot() < event.getView().getTopInventory().getSize()) {
            if (guiInventories.containsKey(event.getView().getTopInventory().getName())) {
                event.setCancelled(true);
                return;
            }
        } else if (event.isShiftClick()) {
            if (guiInventories.containsKey(event.getView().getTopInventory().getName())) {
                event.setCancelled(true);
                return;
            }
        }

    }

    @EventHandler(priority = EventPriority.LOW)
    public void OnInventoryDragEvent(InventoryDragEvent event) {
        if (event.isCancelled()) {
            return;
        }

        for (int slot : event.getRawSlots()) {
            if (slot < event.getView().getTopInventory().getSize()) {
                if (guiInventories.containsKey(event.getView().getTopInventory().getName())) {
                    event.setCancelled(true);
                    return;
                }
            }
        }

    }

}
