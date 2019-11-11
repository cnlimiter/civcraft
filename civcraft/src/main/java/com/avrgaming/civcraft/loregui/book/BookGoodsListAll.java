
package com.avrgaming.civcraft.loregui.book;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import com.avrgaming.civcraft.loregui.GuiAction;

public class BookGoodsListAll
implements GuiAction {
    @Override
    public void performAction(InventoryClickEvent event, ItemStack stack) {
        Player player = (Player)event.getWhoClicked();
        Bukkit.dispatchCommand((CommandSender)player, (String)"c trade");
        player.closeInventory();
    }
}

