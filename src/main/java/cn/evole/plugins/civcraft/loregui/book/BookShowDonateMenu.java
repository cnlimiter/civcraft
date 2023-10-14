package cn.evole.plugins.civcraft.loregui.book;

import cn.evole.plugins.civcraft.loregui.GuiAction;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class BookShowDonateMenu
        implements GuiAction {
    @Override
    public void performAction(InventoryClickEvent event, ItemStack stack) {
        Bukkit.dispatchCommand((CommandSender) event.getWhoClicked(), (String) "buy");
    }
}

