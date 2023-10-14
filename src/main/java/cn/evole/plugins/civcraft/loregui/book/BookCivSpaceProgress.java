package cn.evole.plugins.civcraft.loregui.book;

import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.loregui.GuiAction;
import cn.evole.plugins.civcraft.loregui.OpenInventoryTask;
import cn.evole.plugins.civcraft.lorestorage.LoreGuiItem;
import cn.evole.plugins.civcraft.lorestorage.LoreGuiItemListener;
import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.main.CivMessage;
import cn.evole.plugins.civcraft.object.Civilization;
import cn.evole.plugins.civcraft.object.Resident;
import cn.evole.plugins.civcraft.threading.TaskMaster;
import cn.evole.plugins.civcraft.util.CivColor;
import cn.evole.plugins.civcraft.util.ItemManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public class BookCivSpaceProgress
        implements GuiAction {
    @Override
    public void performAction(InventoryClickEvent event, ItemStack stack) {
        Player player = (Player) event.getWhoClicked();
        Resident interactor = CivGlobal.getResident(player);
        if (interactor.getCiv() == null) {
            CivMessage.sendError((Object) player, CivSettings.localize.localizedString("var_bookcivspacegui_noCiv"));
            return;
        }
        Civilization civ = interactor.getCiv();
        if (!civ.getLeaderGroup().hasMember(interactor)) {
            CivMessage.sendError((Object) player, CivSettings.localize.localizedString("var_bookcivspacegui_noLeader", civ.getName()));
            return;
        }
        if (!civ.getMissionActive()) {
            CivMessage.sendError((Object) player, CivSettings.localize.localizedString("var_spaceshuttle_noProgress"));
            return;
        }
        Inventory guiInventory = Bukkit.getServer().createInventory((InventoryHolder) player, 9, CivSettings.localize.localizedString("bookReborn_civSpaceProgressHeading"));
        String[] split = civ.getMissionProgress().split(":");
        String missionName = CivSettings.spacemissions_levels.get((Object) Integer.valueOf((int) civ.getCurrentMission())).name;
        double beakers = Math.round(Double.parseDouble(split[0]));
        double hammers = Math.round(Double.parseDouble(split[1]));
        int percentageCompleteBeakers = (int) ((double) Math.round(Double.parseDouble(split[0])) / Double.parseDouble(CivSettings.spacemissions_levels.get((Object) Integer.valueOf((int) civ.getCurrentMission())).require_beakers) * 100.0);
        int percentageCompleteHammers = (int) ((double) Math.round(Double.parseDouble(split[1])) / Double.parseDouble(CivSettings.spacemissions_levels.get((Object) Integer.valueOf((int) civ.getCurrentMission())).require_hammers) * 100.0);
        ItemStack progress = LoreGuiItem.build("§b" + missionName, ItemManager.getId(Material.DIAMOND_SWORD), 0, "§6" + CivSettings.localize.localizedString("Beakers") + " " + beakers + CivColor.Red + " (" + percentageCompleteBeakers + "%)", "§d" + CivSettings.localize.localizedString("Hammers") + " " + hammers + CivColor.Red + " (" + percentageCompleteHammers + "%)");
        guiInventory.setItem(0, progress);
        ItemStack backButton = LoreGuiItem.build(CivSettings.localize.localizedString("loreGui_recipes_back"), ItemManager.getId(Material.MAP), 0, CivSettings.localize.localizedString("loreGui_recipes_backMsg"));
        backButton = LoreGuiItem.setAction(backButton, "OpenInventory");
        backButton = LoreGuiItem.setActionData(backButton, "invType", "showGuiInv");
        backButton = LoreGuiItem.setActionData(backButton, "invName", BookCivSpace.guiInventory.getName());
        guiInventory.setItem(8, backButton);
        LoreGuiItemListener.guiInventories.put(guiInventory.getName(), guiInventory);
        TaskMaster.syncTask(new OpenInventoryTask(player, guiInventory));
    }
}

