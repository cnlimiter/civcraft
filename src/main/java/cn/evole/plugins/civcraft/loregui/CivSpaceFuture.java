package cn.evole.plugins.civcraft.loregui;

import cn.evole.plugins.civcraft.command.civ.CivSpaceCommand;
import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.config.ConfigSpaceMissions;
import cn.evole.plugins.civcraft.lorestorage.LoreGuiItem;
import cn.evole.plugins.civcraft.lorestorage.LoreGuiItemListener;
import cn.evole.plugins.civcraft.main.CivCraft;
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

public class CivSpaceFuture
        implements GuiAction {
    public static Inventory guiInventory;

    @Override
    public void performAction(InventoryClickEvent event, ItemStack stack) {
        Player player = (Player) event.getWhoClicked();
        Resident interactor = CivGlobal.getResident(player);
        Civilization civ = interactor.getCiv();
        if (civ.getCurrentMission() >= 8) {
            CivMessage.sendError((Object) player, CivSettings.localize.localizedString("var_spaceshuttle_end", CivSettings.spacemissions_levels.get((Object) Integer.valueOf((int) 7)).name));
            return;
        }
        int current = civ.getCurrentMission();
        if (current == 7 && civ.getMissionActive()) {
            CivMessage.sendError((Object) player, CivSettings.localize.localizedString("var_spaceshuttle_end", CivSettings.spacemissions_levels.get((Object) Integer.valueOf((int) 7)).name));
            return;
        }
        if (civ.getMissionActive()) {
            ++current;
        }
        guiInventory = Bukkit.getServer().createInventory((InventoryHolder) player, 9, CivSettings.localize.localizedString("bookReborn_civSpaceFutureHeading"));
        for (int i = current; i <= 7; ++i) {
            ConfigSpaceMissions configSpaceMissions = CivSettings.spacemissions_levels.get(i);
            ItemStack itemStack = LoreGuiItem.build(CivColor.Red + configSpaceMissions.name, ItemManager.getId(Material.GLASS), CivCraft.civRandom.nextInt(15), "§6" + CivSettings.localize.localizedString("click_to_view"));
            itemStack = LoreGuiItem.setAction(itemStack, "CivSpaceComponentsNormal");
            itemStack = LoreGuiItem.setActionData(itemStack, "i", String.valueOf(i));
            itemStack = LoreGuiItem.setActionData(itemStack, "b", "false");
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

