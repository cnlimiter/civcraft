
package com.avrgaming.civcraft.loregui.book;

import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.config.ConfigTech;
import com.avrgaming.civcraft.loregui.GuiAction;
import com.avrgaming.civcraft.lorestorage.LoreGuiItem;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.object.Civilization;
import com.avrgaming.civcraft.object.Resident;
import com.avrgaming.civcraft.tutorial.Book;
import com.avrgaming.civcraft.util.ItemManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public class BookTechsGui
implements GuiAction {
    @Override
    public void performAction(InventoryClickEvent event, ItemStack stack) {
        Player player = (Player)event.getWhoClicked();
        Resident whoClicked = CivGlobal.getResident(player);
        if (whoClicked.getTown() == null) {
            Book.spawnGuiBook(player);
            CivMessage.send((Object)player, "§c"+CivSettings.localize.localizedString("res_gui_noTown"));
            return;
        }
        Civilization civ = whoClicked.getCiv();
        if (!civ.getLeaderGroup().hasMember(whoClicked) && !civ.getAdviserGroup().hasMember(whoClicked)) {
            Book.spawnGuiBook(player);
            CivMessage.send((Object)player, "§c"+CivSettings.localize.localizedString("cmd_NeedHigherCivRank"));
            return;
        }
        int type = ItemManager.getId(Material.EMERALD_BLOCK);
        ArrayList<ConfigTech> techs = ConfigTech.getAvailableTechs(civ);
        Inventory inv = Bukkit.getServer().createInventory((InventoryHolder)player, 54, CivSettings.localize.localizedString("resident_techsGuiHeading"));
        for (ConfigTech tech : techs) {
            String techh = tech.name;
            ItemStack itemStack = LoreGuiItem.build(tech.name, type, 0, "§6" + CivSettings.localize.localizedString("clicktoresearch"), "§b" + CivSettings.localize.localizedString("money_req", tech.getAdjustedTechCost(civ)), "§a" + CivSettings.localize.localizedString("bealers_req", tech.getAdjustedBeakerCost(civ)), "§d" + CivSettings.localize.localizedString("era_this", tech.era));
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

