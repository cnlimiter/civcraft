package cn.evole.plugins.civcraft.loregui;

import cn.evole.plugins.civcraft.exception.CivException;
import cn.evole.plugins.civcraft.lorestorage.LoreGuiItem;
import cn.evole.plugins.civcraft.lorestorage.LoreGuiItemListener;
import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.main.CivLog;
import cn.evole.plugins.civcraft.threading.TaskMaster;
import cn.evole.plugins.civcraft.tutorial.Book;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class OpenInventory implements GuiAction {

    @Override
    public void performAction(InventoryClickEvent event, ItemStack stack) {
        Player player = (Player) event.getWhoClicked();
        player.closeInventory();

        class SyncTaskDelayed implements Runnable {
            String playerName;
            ItemStack stack;

            public SyncTaskDelayed(String playerName, ItemStack stack) {
                this.playerName = playerName;
                this.stack = stack;
            }

            @Override
            public void run() {
                Player player;
                try {
                    player = CivGlobal.getPlayer(playerName);
                } catch (CivException e) {
                    e.printStackTrace();
                    return;
                }

                switch (LoreGuiItem.getActionData(stack, "invType")) {
                    case "showTutorialInventory":
                        Book.showTutorialInventory(player);
                        break;
                    case "showCraftingHelp":
                        Book.showCraftingHelp(player);
                        break;
                    case "showGuiInv":
                        String invName = LoreGuiItem.getActionData(stack, "invName");
                        Inventory inv = LoreGuiItemListener.guiInventories.get(invName);
                        if (inv != null) {
                            player.openInventory(inv);
                        } else {
                            CivLog.error("Couldn't find GUI inventory:" + invName);
                        }
                        break;
                    default:
                        break;
                }
            }
        }

        TaskMaster.syncTask(new SyncTaskDelayed(player.getName(), stack));
    }

}
