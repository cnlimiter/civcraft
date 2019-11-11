
package com.avrgaming.civcraft.loregui.book;

import org.bukkit.Bukkit;
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
import com.avrgaming.civcraft.object.Resident;
import com.avrgaming.civcraft.threading.TaskMaster;
import com.avrgaming.civcraft.util.ItemManager;

public class BookShowPerkGui
implements GuiAction {
    public static Inventory inv;

    @Override
    public void performAction(InventoryClickEvent event, ItemStack stack) {
        Resident resident = CivGlobal.getResident((Player)event.getWhoClicked());
        Player player = (Player)event.getWhoClicked();
        inv = Bukkit.getServer().createInventory((InventoryHolder)player, 54, CivSettings.localize.localizedString("resident_perksGuiHeading"));
        resident.perks.values().stream().forEachOrdered(perk -> {
            if (perk.getIdent().startsWith("temp")) {
                ItemStack itemStack = LoreGuiItem.build(perk.configPerk.display_name, perk.configPerk.type_id, perk.configPerk.data, "§b" + CivSettings.localize.localizedString("resident_perksGuiClickToView"), "§b" + CivSettings.localize.localizedString("resident_perksGuiTheseTemplates"));
                itemStack = LoreGuiItem.setAction(itemStack, "ShowTemplateType");
                itemStack = LoreGuiItem.setActionData(itemStack, "perk", perk.configPerk.id);
                inv.addItem(itemStack);
            } else if (perk.getIdent().startsWith("perk")) {
                ItemStack itemStack = LoreGuiItem.build(perk.getDisplayName(), perk.configPerk.type_id, perk.configPerk.data, "§6" + CivSettings.localize.localizedString("resident_perksGui_clickToActivate"), CivSettings.localize.localizedString("unlimited"));
                itemStack = LoreGuiItem.setAction(itemStack, "ActivatePerk");
                itemStack = LoreGuiItem.setActionData(itemStack, "perk", perk.configPerk.id);
                inv.addItem(itemStack);
            }
        }
        );
        ItemStack backButton = LoreGuiItem.build(CivSettings.localize.localizedString("bookReborn_back"), ItemManager.getId(Material.MAP), 0, CivSettings.localize.localizedString("bookReborn_backToDashBoard"));
        backButton = LoreGuiItem.setAction(backButton, "OpenInventory");
        backButton = LoreGuiItem.setActionData(backButton, "invType", "showGuiInv");
        backButton = LoreGuiItem.setActionData(backButton, "invName", Book.guiInventory.getName());
        inv.setItem(53, backButton);
        LoreGuiItemListener.guiInventories.put(inv.getName(), inv);
        TaskMaster.syncTask(new OpenInventoryTask(player, inv));
    }
}

