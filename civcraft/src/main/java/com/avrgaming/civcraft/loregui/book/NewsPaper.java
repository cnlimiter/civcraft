
package com.avrgaming.civcraft.loregui.book;

import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.config.ConfigNewspaper;
import com.avrgaming.civcraft.loregui.GuiAction;
import com.avrgaming.civcraft.loregui.OpenInventoryTask;
import com.avrgaming.civcraft.lorestorage.LoreGuiItem;
import com.avrgaming.civcraft.lorestorage.LoreGuiItemListener;
import com.avrgaming.civcraft.main.CivCraft;
import com.avrgaming.civcraft.threading.TaskMaster;
import com.avrgaming.civcraft.tutorial.Book;
import com.avrgaming.civcraft.util.CivColor;
import com.avrgaming.civcraft.util.ItemManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class NewsPaper
        implements GuiAction {
    static Inventory guiInventory;

    @Override
    public void performAction(InventoryClickEvent event, ItemStack stack) {
        ItemStack is;
        Player player = (Player) event.getWhoClicked();
        guiInventory = Bukkit.getServer().createInventory((InventoryHolder) player, 27, CivColor.LightGreenBold + CivSettings.localize.localizedString("bookReborn_news_heading"));
        for (int i = 0; i < 27; ++i) {
            Random rand = CivCraft.civRandom;
            is = LoreGuiItem.build("", ItemManager.getId(Material.STAINED_GLASS_PANE), rand.nextInt(15), new String[0]);
            guiInventory.setItem(i, is);
        }
        for (ConfigNewspaper news : CivSettings.newspapers.values()) {
            boolean useAllLines;
            try {
                Double version = Double.valueOf(news.version);
                useAllLines = version <= 2.2;
            } catch (NumberFormatException twoFourFive) {
                useAllLines = false;
            }
            is = useAllLines ? LoreGuiItem.build(CivColor.WhiteBold + news.headline + " " + CivColor.WhiteBold + news.lineotd, news.item, news.iData, CivColor.LightGrayItalic + news.date, CivColor.LightGreenBold + "Aura:", "§f" + news.line1, "§f" + news.line2, "§f" + news.line3, "§bAlcor:", "§f" + news.line4, "§f" + news.line5, "§f" + news.line6, CivColor.LightPurpleBold + "Orion:", "§f" + news.line7, "§f" + news.line8, "§f" + news.line9, CivColor.GoldBold + "Tauri:", "§f" + news.line10, "§f" + news.line11, "§f" + news.line12, "Version: " + news.version) : LoreGuiItem.build(CivColor.WhiteBold + news.headline + " " + CivColor.WhiteBold + news.lineotd, news.item, news.iData, CivColor.LightGrayItalic + news.date, CivColor.LightGreenBold + "Orion:", "§f" + news.line7, "§f" + news.line8, "§f" + news.line9, CivColor.LightPurpleBold + "Tauri:", "§f" + news.line10, "§f" + news.line11, "§f" + news.line12, "Version: " + news.version);
            guiInventory.setItem(news.guiData.intValue(), is);
        }
        ItemStack backButton = LoreGuiItem.build(CivSettings.localize.localizedString("bookReborn_back"), ItemManager.getId(Material.MAP), 0, CivSettings.localize.localizedString("bookReborn_backToDashBoard"));
        backButton = LoreGuiItem.setAction(backButton, "OpenInventory");
        backButton = LoreGuiItem.setActionData(backButton, "invType", "showGuiInv");
        backButton = LoreGuiItem.setActionData(backButton, "invName", Book.guiInventory.getName());
        guiInventory.setItem(26, backButton);
        LoreGuiItemListener.guiInventories.put(guiInventory.getName(), guiInventory);
        TaskMaster.syncTask(new OpenInventoryTask(player, guiInventory));
    }
}

