
package com.avrgaming.civcraft.loregui.book;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.tutorial.Book;
import com.avrgaming.civcraft.loregui.GuiAction;
import com.avrgaming.civcraft.loregui.OpenInventoryTask;
import com.avrgaming.civcraft.lorestorage.LoreGuiItem;
import com.avrgaming.civcraft.lorestorage.LoreGuiItemListener;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivMessage;

import com.avrgaming.civcraft.object.Resident;
import com.avrgaming.civcraft.threading.TaskMaster;
import com.avrgaming.civcraft.util.CivColor;
import com.avrgaming.civcraft.util.ItemManager;

public class BookRelationsGui
implements GuiAction {
    public static Inventory inventory = null;

    @Override
    public void performAction(InventoryClickEvent event, ItemStack stack) {
        Player player = (Player)event.getWhoClicked();
        Resident resident = CivGlobal.getResident(player);
        if (resident.getTown() == null) {
            Book.spawnGuiBook(player);
            CivMessage.send((Object)player, "§c"+CivSettings.localize.localizedString("res_gui_noTown"));
            return;
        }
        inventory = Bukkit.getServer().createInventory((InventoryHolder)player, 9, CivSettings.localize.localizedString("resident_relationsGuiHeading"));
        ItemStack relation = LoreGuiItem.build(CivColor.LightGreenBold + CivSettings.localize.localizedString("resident_relationsGui_ally"), ItemManager.getId(Material.EMERALD_BLOCK), 0, (Object)ChatColor.RESET + CivSettings.localize.localizedString("resident_relationsGui_allyInfo"), "§6§6" + CivSettings.localize.localizedString("bookReborn_clickToView"));
        relation = LoreGuiItem.setAction(relation, "RelationGuiAllies");
        relation = LoreGuiItem.setActionData(relation, "civilization", resident.getCiv().getName());
        inventory.addItem(relation);
        relation = LoreGuiItem.build(CivColor.LightGreenBold + CivSettings.localize.localizedString("resident_relationsGui_peace"), ItemManager.getId(Material.LAPIS_BLOCK), 0, (Object)ChatColor.RESET + CivSettings.localize.localizedString("resident_relationsGui_peaceInfo"), "§6§6" + CivSettings.localize.localizedString("bookReborn_clickToView"));
        relation = LoreGuiItem.setAction(relation, "RelationGuiPeaces");
        relation = LoreGuiItem.setActionData(relation, "civilization", resident.getCiv().getName());
        inventory.addItem(relation);
        relation = LoreGuiItem.build(CivColor.LightGreenBold + CivSettings.localize.localizedString("resident_relationsGui_hostile"), ItemManager.getId(Material.GOLD_BLOCK), 0, (Object)ChatColor.RESET + CivSettings.localize.localizedString("resident_relationsGui_hostileInfo"), "§6§6" + CivSettings.localize.localizedString("bookReborn_clickToView"));
        relation = LoreGuiItem.setAction(relation, "RelationGuiHostiles");
        relation = LoreGuiItem.setActionData(relation, "civilization", resident.getCiv().getName());
        inventory.addItem(relation);
        relation = LoreGuiItem.build(CivColor.LightGreenBold + CivSettings.localize.localizedString("resident_relationsGui_war"), ItemManager.getId(Material.REDSTONE_BLOCK), 0, (Object)ChatColor.RESET + CivSettings.localize.localizedString("resident_relationsGui_warInfo"), "§6§6" + CivSettings.localize.localizedString("bookReborn_clickToView"));
        relation = LoreGuiItem.setAction(relation, "RelationGuiWars");
        relation = LoreGuiItem.setActionData(relation, "civilization", resident.getCiv().getName());
        inventory.addItem(relation);
        ItemStack backButton = LoreGuiItem.build(CivSettings.localize.localizedString("bookReborn_backToDashBoard"), ItemManager.getId(Material.MAP), 0, CivSettings.localize.localizedString("bookReborn_backToDashBoard"));
        backButton = LoreGuiItem.setAction(backButton, "OpenInventory");
        backButton = LoreGuiItem.setActionData(backButton, "invType", "showGuiInv");
        backButton = LoreGuiItem.setActionData(backButton, "invName", Book.guiInventory.getName());
        inventory.setItem(8, backButton);
        LoreGuiItemListener.guiInventories.put(inventory.getName(), inventory);
        TaskMaster.syncTask(new OpenInventoryTask(player, inventory));
    }
}

