package cn.evole.plugins.civcraft.loregui.book;

import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.config.ConfigTech;
import cn.evole.plugins.civcraft.loregui.GuiAction;
import cn.evole.plugins.civcraft.lorestorage.LoreGuiItem;
import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.main.CivMessage;
import cn.evole.plugins.civcraft.object.Civilization;
import cn.evole.plugins.civcraft.object.Resident;
import cn.evole.plugins.civcraft.tutorial.Book;
import cn.evole.plugins.civcraft.util.ItemManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public class BookTechsGui implements GuiAction {
    @Override
    public void performAction(InventoryClickEvent event, ItemStack stack) {
        Player player = (Player) event.getWhoClicked();
        Resident whoClicked = CivGlobal.getResident(player);
        if (whoClicked.getTown() == null) {
            Book.spawnGuiBook(player);
            CivMessage.send((Object) player, "§c" + CivSettings.localize.localizedString("res_gui_noTown"));
            return;
        }
        Civilization civ = whoClicked.getCiv();
        if (!civ.getLeaderGroup().hasMember(whoClicked) && !civ.getAdviserGroup().hasMember(whoClicked)) {
            Book.spawnGuiBook(player);
            CivMessage.send((Object) player, "§c" + CivSettings.localize.localizedString("cmd_NeedHigherCivRank"));
            return;
        }
        int type = ItemManager.getId(Material.EMERALD_BLOCK);
        ArrayList<ConfigTech> techs = ConfigTech.getAvailableTechs(civ);
        Inventory inv = Bukkit.getServer().createInventory((InventoryHolder) player, 54, CivSettings.localize.localizedString("resident_techsGuiHeading"));
        for (ConfigTech tech : techs) {
            String techh = tech.name;
            ItemStack itemStack = LoreGuiItem.build(tech.name, type, 0,
                    "§6" + CivSettings.localize.localizedString("clicktoresearch"),
                    "§b" + CivSettings.localize.localizedString("money_requ", tech.getAdjustedTechCost(civ)),
                    "§a" + CivSettings.localize.localizedString("beakers_req", tech.getAdjustedBeakerCost(civ)),
                    "§d" + CivSettings.localize.localizedString("era_this", tech.era));
            itemStack = LoreGuiItem.setAction(itemStack, "ResearchGui");
            itemStack = LoreGuiItem.setActionData(itemStack, "info", techh);
            inv.addItem(itemStack);
        }
        ItemStack backButton = LoreGuiItem.build(CivSettings.localize.localizedString("bookReborn_back"), ItemManager.getId(Material.MAP), 0, CivSettings.localize.localizedString("bookReborn_backToDashBoard"));
        backButton = LoreGuiItem.setAction(backButton, "OpenInventory");
        backButton = LoreGuiItem.setActionData(backButton, "invType", "showGuiInv");
        backButton = LoreGuiItem.setActionData(backButton, "invName", Book.guiInventory.getName());
        inv.setItem(53, backButton);
        player.openInventory(inv);
    }
}

