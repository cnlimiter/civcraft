package cn.evole.plugins.civcraft.loregui.book;

import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.loregui.GuiAction;
import cn.evole.plugins.civcraft.loregui.OpenInventoryTask;
import cn.evole.plugins.civcraft.lorestorage.LoreGuiItem;
import cn.evole.plugins.civcraft.lorestorage.LoreGuiItemListener;
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

public class BookGoodsGui
        implements GuiAction {
    public static Inventory guiInventory;

    @Override
    public void performAction(InventoryClickEvent event, ItemStack stack) {
        Player player = (Player) event.getWhoClicked();
        guiInventory = Bukkit.getServer().createInventory((InventoryHolder) player, 9, CivColor.Red + CivSettings.localize.localizedString("bookReborn_goodsMenuHeading"));
        ItemStack is = LoreGuiItem.build(CivSettings.localize.localizedString("bookReborn_goodsMenu_town"), ItemManager.getId(Material.PAPER), 0, "§6" + CivSettings.localize.localizedString("click_to_view"));
        is = LoreGuiItem.setAction(is, "BookGoodsTown");
        guiInventory.setItem(0, is);
        is = LoreGuiItem.build(CivSettings.localize.localizedString("bookReborn_goodsMenu_civ"), ItemManager.getId(Material.BOW), 0, "§6" + CivSettings.localize.localizedString("click_to_view"));
        is = LoreGuiItem.setAction(is, "BookGoodsCiv");
        guiInventory.setItem(1, is);
        is = LoreGuiItem.build(CivSettings.localize.localizedString("bookReborn_goodsMenu_deposit"), ItemManager.getId(Material.GOLD_INGOT), 0, "§6" + CivSettings.localize.localizedString("click_to_view"));
        is = LoreGuiItem.setAction(is, "BookGoodsDeposit");
        guiInventory.setItem(2, is);
        is = LoreGuiItem.build(CivSettings.localize.localizedString("bookReborn_goodsMenu_withdraw"), ItemManager.getId(Material.GOLD_NUGGET), 0, "§6" + CivSettings.localize.localizedString("click_to_view"));
        is = LoreGuiItem.setAction(is, "BookGoodsWithdraw");
        guiInventory.setItem(3, is);
        is = LoreGuiItem.build(CivSettings.localize.localizedString("bookReborn_goodsMenu_listAll"), ItemManager.getId(Material.DIAMOND_SWORD), 0, "§6" + CivSettings.localize.localizedString("click_to_view"));
        is = LoreGuiItem.setAction(is, "BookGoodsListAll");
        guiInventory.setItem(4, is);
        ItemStack backButton = LoreGuiItem.build(CivSettings.localize.localizedString("loreGui_recipes_back"), ItemManager.getId(Material.MAP), 0, CivSettings.localize.localizedString("bookReborn_backToDashBoard"));
        backButton = LoreGuiItem.setAction(backButton, "OpenInventory");
        backButton = LoreGuiItem.setActionData(backButton, "invType", "showGuiInv");
        backButton = LoreGuiItem.setActionData(backButton, "invName", Book.guiInventory.getName());
        guiInventory.setItem(8, backButton);
        LoreGuiItemListener.guiInventories.put(guiInventory.getName(), guiInventory);
        TaskMaster.syncTask(new OpenInventoryTask(player, guiInventory));
    }
}

