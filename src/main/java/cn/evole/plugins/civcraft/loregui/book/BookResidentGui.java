package cn.evole.plugins.civcraft.loregui.book;

import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.loregui.GuiAction;
import cn.evole.plugins.civcraft.loregui.OpenInventoryTask;
import cn.evole.plugins.civcraft.lorestorage.LoreGuiItem;
import cn.evole.plugins.civcraft.lorestorage.LoreGuiItemListener;
import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.object.Resident;
import cn.evole.plugins.civcraft.threading.TaskMaster;
import cn.evole.plugins.civcraft.tutorial.Book;
import cn.evole.plugins.civcraft.util.CivColor;
import cn.evole.plugins.civcraft.util.ItemManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.text.SimpleDateFormat;

public class BookResidentGui implements GuiAction {
    public static String Civilization(Resident resident) {
        if (resident.getCiv() == null) {
            return "";
        }
        return resident.getCiv().getName();
    }

    public static String Town(Resident resident) {
        if (resident.getTown() == null) {
            return "";
        }
        return resident.getTown().getName();
    }

    public static String Camp(Resident resident) {
        if (resident.getCamp() == null) {
            return "";
        }
        return resident.getCamp().getName();
    }

    @Override
    public void performAction(InventoryClickEvent event, ItemStack stack) {
        SimpleDateFormat sdf = new SimpleDateFormat("M/dd h:mm:ss a z");
        Player player = (Player) event.getWhoClicked();
        Resident resident = CivGlobal.getResident(player);
        Inventory guiInventory = Bukkit.getServer().createInventory((InventoryHolder) player, 9, CivSettings.localize.localizedString("bookReborn_resInfoHeading"));
        ItemStack playerInfo = LoreGuiItem.build(CivSettings.localize.localizedString("bookReborn_infoMenu_name"), ItemManager.getId(Material.SKULL_ITEM), 3,
                CivColor.LightGray + "Player: " + resident.getName(),
                "§6" + CivSettings.CURRENCY_NAME + ": " + "§a" + resident.getTreasury().getBalance(),
                "§2" + CivSettings.localize.localizedString("cmd_res_showRegistrationDate", " §a" + sdf.format(resident.getRegistered())),
                "§b" + CivSettings.localize.localizedString("Civilization") + " " + BookResidentGui.Civilization(resident),
                "§d" + CivSettings.localize.localizedString("Town") + " " + BookResidentGui.Town(resident),
                CivColor.Red + CivSettings.localize.localizedString("Camp") + " " + BookResidentGui.Camp(resident));
        guiInventory.setItem(0, playerInfo);
        ItemStack backButton = LoreGuiItem.build(CivSettings.localize.localizedString("loreGui_recipes_back"), ItemManager.getId(Material.MAP), 0, CivSettings.localize.localizedString("bookReborn_backToDashBoard"));
        backButton = LoreGuiItem.setAction(backButton, "OpenInventory");
        backButton = LoreGuiItem.setActionData(backButton, "invType", "showGuiInv");
        backButton = LoreGuiItem.setActionData(backButton, "invName", Book.guiInventory.getName());
        guiInventory.setItem(8, backButton);
        LoreGuiItemListener.guiInventories.put(guiInventory.getName(), guiInventory);
        TaskMaster.syncTask(new OpenInventoryTask(player, guiInventory));
    }
}

