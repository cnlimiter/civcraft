
package com.avrgaming.civcraft.loregui.book;

import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.config.ConfigSpaceMissions;
import com.avrgaming.civcraft.loregui.GuiAction;
import com.avrgaming.civcraft.loregui.OpenInventoryTask;
import com.avrgaming.civcraft.lorestorage.LoreGuiItem;
import com.avrgaming.civcraft.lorestorage.LoreGuiItemListener;
import com.avrgaming.civcraft.main.CivCraft;
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

public class BookCivSpaceFuture
implements GuiAction {
    public static Inventory guiInventory;

    @Override
    public void performAction(InventoryClickEvent event, ItemStack stack) {
        Player player = (Player)event.getWhoClicked();
        Resident interactor = CivGlobal.getResident(player);
        if (interactor.getCiv() == null) {
            CivMessage.sendError((Object)player, CivSettings.localize.localizedString("var_bookcivspacegui_noCiv"));
            return;
        }
        Civilization civ = interactor.getCiv();
        if (!civ.getLeaderGroup().hasMember(interactor)) {
            CivMessage.sendError((Object)player, CivSettings.localize.localizedString("var_bookcivspacegui_noLeader", civ.getName()));
            return;
        }
        if (civ.getCurrentMission() >= 8) {
            CivMessage.sendError((Object)player, CivSettings.localize.localizedString("var_spaceshuttle_end", CivSettings.spacemissions_levels.get((Object)Integer.valueOf((int)7)).name));
            return;
        }
        int current = civ.getCurrentMission();
        if (current == 7 && civ.getMissionActive()) {
            CivMessage.sendError((Object)player, CivSettings.localize.localizedString("var_spaceshuttle_end", CivSettings.spacemissions_levels.get((Object)Integer.valueOf((int)7)).name));
            return;
        }
        if (civ.getMissionActive()) {
            ++current;
        }
        guiInventory = Bukkit.getServer().createInventory((InventoryHolder)player, 9, CivSettings.localize.localizedString("bookReborn_civSpaceFutureHeading"));
        for (int i = current; i <= 7; ++i) {
            ConfigSpaceMissions configSpaceMissions = CivSettings.spacemissions_levels.get(i);
            ItemStack itemStack = LoreGuiItem.build(CivColor.Red + configSpaceMissions.name, ItemManager.getId(Material.GLASS), CivCraft.civRandom.nextInt(15), "§6" + CivSettings.localize.localizedString("click_to_view"));
            itemStack = LoreGuiItem.setAction(itemStack, "CivSpaceComponents");
            itemStack = LoreGuiItem.setActionData(itemStack, "i", String.valueOf(i));
            itemStack = LoreGuiItem.setActionData(itemStack, "b", "false");
            guiInventory.addItem(itemStack);
        }
        ItemStack backButton = LoreGuiItem.build(CivSettings.localize.localizedString("loreGui_recipes_back"), ItemManager.getId(Material.MAP), 0, CivSettings.localize.localizedString("loreGui_recipes_backMsg"));
        backButton = LoreGuiItem.setAction(backButton, "OpenInventory");
        backButton = LoreGuiItem.setActionData(backButton, "invType", "showGuiInv");
        backButton = LoreGuiItem.setActionData(backButton, "invName", BookCivSpace.guiInventory.getName());
        guiInventory.setItem(8, backButton);
        LoreGuiItemListener.guiInventories.put(guiInventory.getName(), guiInventory);
        TaskMaster.syncTask(new OpenInventoryTask(player, guiInventory));
    }
}

