package com.avrgaming.civcraft.loregui;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import com.avrgaming.civcraft.exception.CivException;


public interface GuiAction {
    public void performAction(InventoryClickEvent event, ItemStack stack) throws CivException;
}
