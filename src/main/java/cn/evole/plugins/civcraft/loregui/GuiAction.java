package cn.evole.plugins.civcraft.loregui;

import cn.evole.plugins.civcraft.exception.CivException;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;


public interface GuiAction {
    public void performAction(InventoryClickEvent event, ItemStack stack) throws CivException;
}
