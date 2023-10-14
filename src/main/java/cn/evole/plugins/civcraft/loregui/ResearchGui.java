package cn.evole.plugins.civcraft.loregui;

import cn.evole.plugins.civcraft.lorestorage.LoreGuiItem;
import cn.evole.plugins.civcraft.main.CivGlobal;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class ResearchGui
        implements GuiAction {
    @Override
    public void performAction(InventoryClickEvent event, ItemStack stack) {
        Player player = (Player) event.getWhoClicked();
        String oh = LoreGuiItem.getActionData(stack, "info");
        String startResearch = "civ research on " + oh;
        String addToQueue = "civ research queueadd " + oh;
        if (CivGlobal.getResident(player).getCiv().getResearchTech() == null) {
            Bukkit.dispatchCommand((CommandSender) player, (String) startResearch);
        } else {
            Bukkit.dispatchCommand((CommandSender) player, (String) addToQueue);
        }
        player.closeInventory();
    }
}

