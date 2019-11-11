
package com.avrgaming.civcraft.loregui;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import com.avrgaming.civcraft.loregui.GuiAction;
import com.avrgaming.civcraft.lorestorage.LoreGuiItem;

public class UpgradeGuiBuy
implements GuiAction {
    @Override
    public void performAction(InventoryClickEvent event, ItemStack stack) {
        Player player = (Player)event.getWhoClicked();
        String oh = LoreGuiItem.getActionData(stack, "info");
        String toUpgrade = "town upgrade buy " + oh;
        Bukkit.dispatchCommand((CommandSender)player, (String)toUpgrade);
        player.closeInventory();
    }
}

