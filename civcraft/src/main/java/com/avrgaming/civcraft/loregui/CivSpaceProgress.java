
package com.avrgaming.civcraft.loregui;

import com.avrgaming.civcraft.command.civ.CivSpaceCommand;
import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.lorestorage.LoreGuiItem;
import com.avrgaming.civcraft.lorestorage.LoreGuiItemListener;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.object.Civilization;
import com.avrgaming.civcraft.object.Resident;
import com.avrgaming.civcraft.threading.TaskMaster;
import com.avrgaming.civcraft.util.CivColor;
import com.avrgaming.civcraft.util.ItemManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public class CivSpaceProgress
implements GuiAction {
    @Override
    public void performAction(InventoryClickEvent event, ItemStack stack) {
        Player player = (Player)event.getWhoClicked();
        Resident interactor = CivGlobal.getResident(player);
        Civilization civ = interactor.getCiv();
        if (!civ.getMissionActive()) {
            CivMessage.sendError((Object)player, CivSettings.localize.localizedString("var_spaceshuttle_noProgress"));
            return;
        }
        Inventory guiInventory = Bukkit.getServer().createInventory((InventoryHolder)player, 9, CivSettings.localize.localizedString("bookReborn_civSpaceProgressHeading"));
        String[] split = civ.getMissionProgress().split(":");
        String missionName = CivSettings.spacemissions_levels.get((Object)Integer.valueOf((int)civ.getCurrentMission())).name;
        double beakers = Math.round(Double.parseDouble(split[0]));
        double hammers = Math.round(Double.parseDouble(split[1]));
        int percentageCompleteBeakers = (int)((double)Math.round(Double.parseDouble(split[0])) / Double.parseDouble(CivSettings.spacemissions_levels.get((Object)Integer.valueOf((int)civ.getCurrentMission())).require_beakers) * 100.0);
        int percentageCompleteHammers = (int)((double)Math.round(Double.parseDouble(split[1])) / Double.parseDouble(CivSettings.spacemissions_levels.get((Object)Integer.valueOf((int)civ.getCurrentMission())).require_hammers) * 100.0);
        ItemStack progress = LoreGuiItem.build("§b" + missionName, ItemManager.getId(Material.DIAMOND_SWORD), 0, "§6"+CivSettings.localize.localizedString("beakers")+" " + beakers + CivColor.Red + "(" + percentageCompleteBeakers + "%)", "§d"+ CivSettings.localize.localizedString("hammers")+" " + hammers + CivColor.Red + "(" + percentageCompleteHammers + "%)");
        guiInventory.setItem(0, progress);
        ItemStack backButton = LoreGuiItem.build(CivSettings.localize.localizedString("loreGui_recipes_back"), ItemManager.getId(Material.MAP), 0, CivSettings.localize.localizedString("loreGui_recipes_backMsg"));
        backButton = LoreGuiItem.setAction(backButton, "OpenInventory");
        backButton = LoreGuiItem.setActionData(backButton, "invType", "showGuiInv");
        backButton = LoreGuiItem.setActionData(backButton, "invName", CivSpaceCommand.guiInventory.getName());
        guiInventory.setItem(8, backButton);
        LoreGuiItemListener.guiInventories.put(guiInventory.getName(), guiInventory);
        TaskMaster.syncTask(new OpenInventoryTask(player, guiInventory));
    }
}

