package cn.evole.plugins.civcraft.loregui.book;

import cn.evole.plugins.civcraft.loregui.GuiAction;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class BookGoodsListAll
        implements GuiAction {
    @Override
    public void performAction(InventoryClickEvent event, ItemStack stack) {
        Player player = (Player) event.getWhoClicked();
        Bukkit.dispatchCommand((CommandSender) player, (String) "c trade");
        player.closeInventory();
    }
}

