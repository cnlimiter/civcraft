package cn.evole.plugins.civcraft.loregui.book;

import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.config.ConfigTradeGood;
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
import cn.evole.plugins.civcraft.util.CivColor;
import cn.evole.plugins.civcraft.util.ItemManager;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class BookGoodsTown implements GuiAction {
    @Override
    public void performAction(InventoryClickEvent event, ItemStack stack) {
        Player player = (Player) event.getWhoClicked();
        Resident resident = CivGlobal.getResident(player);
        Civilization civ = resident.getCiv();
        Town town = resident.getSelectedTown();
        if (civ == null) {
            CivMessage.sendError(player, CivSettings.localize.localizedString("var_virtualTG_noCiv"));
            return;
        }
        if (!civ.getLeaderGroup().hasMember(resident) && !resident.getSelectedTown().getMayorGroup().hasMember(resident)) {
            CivMessage.sendError(player, CivSettings.localize.localizedString("var_virtualTG_noPermM", "§6" + civ.getName() + CivColor.Red, "§6" + town.getName() + CivColor.Red));
            return;
        }
        if (StringUtils.isBlank(town.tradeGoods)) {
            CivMessage.sendError(player, CivSettings.localize.localizedString("cmd_civ_trade_listtown_noGoods",
                    "§6" + town.getName() + CivColor.Red));
            return;
        }
        Inventory listInventory = Bukkit.getServer().createInventory(player, 9,
                CivColor.GoldBold + CivSettings.localize.localizedString("cmd_civ_trade_listtown_listInvName",
                        CivColor.RoseBold + town.getName()));
        int i = 0;
        for (String goodID : town.tradeGoods.split(", ")) {
            ConfigTradeGood configTradeGood = CivSettings.goods.get(goodID);
            if (configTradeGood == null) continue;
            String[] split = CivSettings.getBonusDisplayString(configTradeGood, null).split(";");
            ItemStack tradeGood = LoreGuiItem.build(configTradeGood.name, configTradeGood.material, configTradeGood.material_data, split);
            tradeGood = LoreGuiItem.setAction(tradeGood, "ShowTradeGoodInfo");
            listInventory.setItem(i, tradeGood);
            ++i;
        }
        ItemStack backButton = LoreGuiItem.build(CivSettings.localize.localizedString("loreGui_recipes_back"), ItemManager.getId(Material.MAP), 0, CivSettings.localize.localizedString("var_virtualTG_backToMain"));
        backButton = LoreGuiItem.setAction(backButton, "OpenInventory");
        backButton = LoreGuiItem.setActionData(backButton, "invType", "showGuiInv");
        backButton = LoreGuiItem.setActionData(backButton, "invName", BookGoodsGui.guiInventory.getName());
        listInventory.setItem(8, backButton);
        LoreGuiItemListener.guiInventories.put(listInventory.getName(), listInventory);
        TaskMaster.syncTask(new OpenInventoryTask(player, listInventory));
    }
}

