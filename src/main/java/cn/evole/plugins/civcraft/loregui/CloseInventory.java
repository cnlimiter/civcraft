package cn.evole.plugins.civcraft.loregui;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class CloseInventory
        implements GuiAction {
    @Override
    public void performAction(InventoryClickEvent event, ItemStack stack) {
        event.getWhoClicked().closeInventory();
    }
}

