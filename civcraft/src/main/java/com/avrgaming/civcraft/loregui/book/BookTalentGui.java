
package com.avrgaming.civcraft.loregui.book;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.loregui.GuiAction;
import com.avrgaming.civcraft.loregui.OpenInventoryTask;
import com.avrgaming.civcraft.lorestorage.LoreGuiItem;
import com.avrgaming.civcraft.lorestorage.LoreGuiItemListener;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.object.Civilization;
import com.avrgaming.civcraft.object.Resident;
import com.avrgaming.civcraft.threading.TaskMaster;
import com.avrgaming.civcraft.tutorial.Book;
import com.avrgaming.civcraft.util.ItemManager;

public class BookTalentGui implements GuiAction {
    public static Inventory inventory = null;

    @Override
    public void performAction(InventoryClickEvent event, ItemStack stack) {
        Player player = (Player) event.getWhoClicked();
        inventory = Bukkit.getServer().createInventory((InventoryHolder) player, 9, CivSettings.localize.localizedString("talentGui_heading"));
        Resident whoClicked = CivGlobal.getResident(player);
        if (whoClicked.getTown() == null) {
            Book.spawnGuiBook(player);
            CivMessage.send((Object) player, "§c" + CivSettings.localize.localizedString("res_gui_noTown"));
            return;
        }
        Civilization civ = whoClicked.getCiv();
        if (!civ.getLeaderGroup().hasMember(whoClicked) && !civ.getAdviserGroup().hasMember(whoClicked)) {
            Book.spawnGuiBook(player);
            CivMessage.send((Object) player, "§c" + CivSettings.localize.localizedString("cmd_NeedHigherCivRank"));
            return;
        }
        ItemStack talentList = LoreGuiItem.build(CivSettings.localize.localizedString("talentGui_talentList"), ItemManager.getId(Material.PAPER), 0, "§6" + CivSettings.localize.localizedString("bookReborn_clickToView"));
        talentList = LoreGuiItem.setAction(talentList, "TalentList");
        inventory.setItem(0, talentList);
        ItemStack talentChoose = LoreGuiItem.build(CivSettings.localize.localizedString("talentGui_talentChoose"), ItemManager.getId(Material.BOOK_AND_QUILL), 0, "§6" + CivSettings.localize.localizedString("bookReborn_clickToView"));
        talentChoose = LoreGuiItem.setAction(talentChoose, "TalentChoose");
        inventory.setItem(1, talentChoose);
        ItemStack backButton = LoreGuiItem.build(CivSettings.localize.localizedString("bookReborn_back"), ItemManager.getId(Material.MAP), 0, CivSettings.localize.localizedString("bookReborn_backToDashBoard"));
        backButton = LoreGuiItem.setAction(backButton, "OpenInventory");
        backButton = LoreGuiItem.setActionData(backButton, "invType", "showGuiInv");
        backButton = LoreGuiItem.setActionData(backButton, "invName", Book.guiInventory.getName());
        inventory.setItem(8, backButton);
        LoreGuiItemListener.guiInventories.put(inventory.getName(), inventory);
        TaskMaster.syncTask(new OpenInventoryTask(player, inventory));
    }
}

