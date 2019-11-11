
package com.avrgaming.civcraft.loregui.book;

import java.text.SimpleDateFormat;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.loregui.GuiAction;
import com.avrgaming.civcraft.loregui.OpenInventoryTask;
import com.avrgaming.civcraft.loregui.book.BookRelationsGui;
import com.avrgaming.civcraft.lorestorage.LoreGuiItem;
import com.avrgaming.civcraft.lorestorage.LoreGuiItemListener;
import com.avrgaming.civcraft.main.CivGlobal;

import com.avrgaming.civcraft.object.Civilization;
import com.avrgaming.civcraft.object.Relation;
import com.avrgaming.civcraft.threading.TaskMaster;
import com.avrgaming.civcraft.util.ItemManager;

public class RelationGuiAllies
implements GuiAction {
    @Override
    public void performAction(InventoryClickEvent event, ItemStack stack) {
        Civilization civ = CivGlobal.getCiv(LoreGuiItem.getActionData(stack, "civilization"));
        SimpleDateFormat sdf = new SimpleDateFormat("M/dd h:mm:ss a z");
        ItemStack itemStack = null;
        Inventory inventory = Bukkit.getServer().createInventory((InventoryHolder)event.getWhoClicked(), 54, CivSettings.localize.localizedString("resident_relationsGui_ally"));
        for (Relation relation : civ.getDiplomacyManager().getRelations()) {
            if (relation.getStatus() == Relation.Status.ALLY) {
                itemStack = LoreGuiItem.build("", ItemManager.getId(Material.EMERALD_BLOCK), 0, (Object)ChatColor.RESET + CivSettings.localize.localizedString("resident_relationsGui_relationToString", relation.toString()), "§6" + CivSettings.localize.localizedString("relation_creationDate", sdf.format(relation.getCreatedDate())));
            }
            if (itemStack == null) continue;
            inventory.addItem(itemStack);
        }
        ItemStack backButton = LoreGuiItem.build(CivSettings.localize.localizedString("bookReborn_back"), ItemManager.getId(Material.MAP), 0, CivSettings.localize.localizedString("bookReborn_backTo", BookRelationsGui.inventory.getName()));
        backButton = LoreGuiItem.setAction(backButton, "OpenInventory");
        backButton = LoreGuiItem.setActionData(backButton, "invType", "showGuiInv");
        backButton = LoreGuiItem.setActionData(backButton, "invName", BookRelationsGui.inventory.getName());
        inventory.setItem(53, backButton);
        LoreGuiItemListener.guiInventories.put(inventory.getName(), inventory);
        TaskMaster.syncTask(new OpenInventoryTask((Player)event.getWhoClicked(), inventory));
    }
}

