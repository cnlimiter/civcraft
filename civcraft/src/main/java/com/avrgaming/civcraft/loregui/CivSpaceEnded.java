
package com.avrgaming.civcraft.loregui;

import com.avrgaming.civcraft.command.civ.CivSpaceCommand;
import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.config.ConfigSpaceMissions;
import com.avrgaming.civcraft.lorestorage.LoreGuiItem;
import com.avrgaming.civcraft.lorestorage.LoreGuiItemListener;
import com.avrgaming.civcraft.main.CivCraft;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.object.Civilization;
import com.avrgaming.civcraft.object.Resident;
import com.avrgaming.civcraft.threading.TaskMaster;
import com.avrgaming.civcraft.util.ItemManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public class CivSpaceEnded
implements GuiAction {
    public static Inventory guiInventory;

    @Override
    public void performAction(InventoryClickEvent event, ItemStack stack) {
        Player player = (Player)event.getWhoClicked();
        Resident interactor = CivGlobal.getResident(player);
        Civilization civ = interactor.getCiv();
        int ended = civ.getCurrentMission();
        guiInventory = Bukkit.getServer().createInventory((InventoryHolder)player, 9, CivSettings.localize.localizedString("bookReborn_civSpaceEndedHeading"));
        for (int i = 1; i < ended; ++i) {
            ConfigSpaceMissions configSpaceMissions = CivSettings.spacemissions_levels.get(i);
            ItemStack itemStack = LoreGuiItem.build("§a" + configSpaceMissions.name, ItemManager.getId(Material.STAINED_GLASS_PANE), CivCraft.civRandom.nextInt(15), "§6" + CivSettings.localize.localizedString("click_to_view"));
            itemStack = LoreGuiItem.setAction(itemStack, "CivSpaceComponentsNormal");
            itemStack = LoreGuiItem.setActionData(itemStack, "i", String.valueOf(i));
            itemStack = LoreGuiItem.setActionData(itemStack, "b", "true");
            guiInventory.addItem(itemStack);
        }
        ItemStack backButton = LoreGuiItem.build(CivSettings.localize.localizedString("loreGui_recipes_back"), ItemManager.getId(Material.MAP), 0, CivSettings.localize.localizedString("loreGui_recipes_backMsg"));
        backButton = LoreGuiItem.setAction(backButton, "OpenInventory");
        backButton = LoreGuiItem.setActionData(backButton, "invType", "showGuiInv");
        backButton = LoreGuiItem.setActionData(backButton, "invName", CivSpaceCommand.guiInventory.getName());
        guiInventory.setItem(8, backButton);
        LoreGuiItemListener.guiInventories.put(guiInventory.getName(), guiInventory);
        TaskMaster.syncTask(new OpenInventoryTask(player, guiInventory));
    }
}

