package cn.evole.plugins.civcraft.loregui;

import cn.evole.plugins.civcraft.lorestorage.LoreGuiItem;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class BuildFromIdCr
        implements GuiAction {
    @Override
    public void performAction(InventoryClickEvent event, ItemStack stack) {
        String buildableName = LoreGuiItem.getActionData(stack, "buildableName");
        event.getWhoClicked().closeInventory();
        Bukkit.dispatchCommand((CommandSender) event.getWhoClicked(), (String) ("build " + buildableName));
    }
}

