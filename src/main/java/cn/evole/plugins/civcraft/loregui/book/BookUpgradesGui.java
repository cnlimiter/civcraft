package cn.evole.plugins.civcraft.loregui.book;

import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.config.ConfigBuildableInfo;
import cn.evole.plugins.civcraft.config.ConfigTownUpgrade;
import cn.evole.plugins.civcraft.loregui.GuiAction;
import cn.evole.plugins.civcraft.loregui.OpenInventoryTask;
import cn.evole.plugins.civcraft.lorestorage.LoreGuiItem;
import cn.evole.plugins.civcraft.lorestorage.LoreGuiItemListener;
import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.main.CivMessage;
import cn.evole.plugins.civcraft.object.Civilization;
import cn.evole.plugins.civcraft.object.Resident;
import cn.evole.plugins.civcraft.object.Town;
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

public class BookUpgradesGui implements GuiAction {
    @Override
    public void performAction(InventoryClickEvent event, ItemStack stack) {
        Player player = (Player) event.getWhoClicked();
        Resident whoClicked = CivGlobal.getResident(player);
        if (whoClicked.getTown() == null) {
            Book.spawnGuiBook(player);
            CivMessage.send(player, "§c" + CivSettings.localize.localizedString("res_gui_noTown"));
            return;
        }
        Civilization civ = whoClicked.getCiv();
        Town town = whoClicked.getSelectedTown();
        if (!(town.getMayorGroup().hasMember(whoClicked) || town.getAssistantGroup().hasMember(whoClicked) || civ.getLeaderGroup().hasMember(whoClicked))) {
            Book.spawnGuiBook(player);
            CivMessage.send((Object) player, "§c" + CivSettings.localize.localizedString("cmd_NeedHigherTownOrCivRank"));
            return;
        }
        Inventory inv = Bukkit.getServer().createInventory((InventoryHolder) player, 54, CivSettings.localize.localizedString("resident_upgradesGuiHeading"));
        for (ConfigTownUpgrade upgrade : ConfigTownUpgrade.getAllUpgrades(town)) {
            double cost = upgrade.cost;
            if (town.getCiv().getGovernment().id.equalsIgnoreCase("gov_theocracy")) {
                cost *= 0.9;
            }
            ItemStack is = null;
            if (upgrade.isAvailable(town)) {
                is = LoreGuiItem.build(upgrade.name, ItemManager.getId(Material.EMERALD_BLOCK), 0, "§b" + CivSettings.localize.localizedString("money_requ", Math.round(cost)), "§6" + CivSettings.localize.localizedString("tutorial_lore_clicktoView"));
                is = LoreGuiItem.setAction(is, "UpgradeGuiBuy");
                is = LoreGuiItem.setActionData(is, "info", upgrade.name);
            } else if (!town.hasStructure(upgrade.require_structure)) {
                ConfigBuildableInfo structure = CivSettings.structures.get(upgrade.require_structure);
                is = LoreGuiItem.build(upgrade.name, ItemManager.getId(Material.EMERALD), 0, "§b" + CivSettings.localize.localizedString("money_requ", Math.round(cost)), CivColor.Red + CivSettings.localize.localizedString("requ") + structure.displayName, "§3" + CivSettings.localize.localizedString("clicktobuild"));
                is = LoreGuiItem.setAction(is, "WonderGuiBuild");
                is = LoreGuiItem.setActionData(is, "info", structure.displayName);
            } else if (!town.hasUpgrade(upgrade.require_upgrade)) {
                ConfigTownUpgrade upgrade1 = CivSettings.getUpgradeById(upgrade.require_upgrade);
                is = LoreGuiItem.build(upgrade.name, ItemManager.getId(Material.GLOWSTONE_DUST), 0, "§b" + CivSettings.localize.localizedString("money_requ", Math.round(cost)), CivColor.Red + CivSettings.localize.localizedString("requ") + upgrade1.name, "§3" + CivSettings.localize.localizedString("tutorial_lore_clicktoView"));
                is = LoreGuiItem.setAction(is, "UpgradeGuiBuy");
                is = LoreGuiItem.setActionData(is, "info", upgrade1.name);
            }
            if (is == null) continue;
            inv.addItem(new ItemStack[]{is});
        }
        ItemStack backButton = LoreGuiItem.build(CivSettings.localize.localizedString("bookReborn_back"), ItemManager.getId(Material.MAP), 0, CivSettings.localize.localizedString("bookReborn_backToDashBoard"));
        backButton = LoreGuiItem.setAction(backButton, "OpenInventory");
        backButton = LoreGuiItem.setActionData(backButton, "invType", "showGuiInv");
        backButton = LoreGuiItem.setActionData(backButton, "invName", Book.guiInventory.getName());
        inv.setItem(53, backButton);
        LoreGuiItemListener.guiInventories.put(inv.getName(), inv);
        TaskMaster.syncTask(new OpenInventoryTask(player, inv));
    }
}

