package cn.evole.plugins.civcraft.loregui;

import cn.evole.plugins.civcraft.lorestorage.LoreGuiItem;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class WonderGuiBuild
        implements GuiAction {
    @Override
    public void performAction(InventoryClickEvent event, ItemStack stack) {
        Player player = (Player) event.getWhoClicked();
        String oh = LoreGuiItem.getActionData(stack, "info");
        String toBuildStart = "build " + oh;
        Bukkit.dispatchCommand((CommandSender) player, (String) toBuildStart);
    }
}

