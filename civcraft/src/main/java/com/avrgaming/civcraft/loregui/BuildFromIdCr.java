
package com.avrgaming.civcraft.loregui;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import com.avrgaming.civcraft.loregui.GuiAction;
import com.avrgaming.civcraft.lorestorage.LoreGuiItem;

public class BuildFromIdCr
implements GuiAction {
    @Override
    public void performAction(InventoryClickEvent event, ItemStack stack) {
        String buildableName = LoreGuiItem.getActionData(stack, "buildableName");
        event.getWhoClicked().closeInventory();
        Bukkit.dispatchCommand((CommandSender)event.getWhoClicked(), (String)("build " + buildableName));
    }
}

