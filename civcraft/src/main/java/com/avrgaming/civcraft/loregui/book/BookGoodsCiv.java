
package com.avrgaming.civcraft.loregui.book;

import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.config.ConfigTradeGood;
import com.avrgaming.civcraft.loregui.GuiAction;
import com.avrgaming.civcraft.loregui.OpenInventoryTask;
import com.avrgaming.civcraft.lorestorage.LoreGuiItem;
import com.avrgaming.civcraft.lorestorage.LoreGuiItemListener;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.object.Civilization;
import com.avrgaming.civcraft.object.Resident;
import com.avrgaming.civcraft.threading.TaskMaster;
import com.avrgaming.civcraft.util.CivColor;
import com.avrgaming.civcraft.util.ItemManager;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class BookGoodsCiv
implements GuiAction {
    @Override
    public void performAction(InventoryClickEvent event, ItemStack stack) {
        Player player = (Player)event.getWhoClicked();
        Resident resident = CivGlobal.getResident(player);
        Civilization civ = resident.getCiv();
        if (civ == null) {
            CivMessage.sendError((Object)player, CivSettings.localize.localizedString("var_virtualTG_noCiv"));
            return;
        }
        if (!civ.getLeaderGroup().hasMember(resident)) {
            CivMessage.sendError((Object)player, CivSettings.localize.localizedString("var_virtualTG_noPerm", "§6" + civ.getName() + CivColor.Red));
            return;
        }
        if (StringUtils.isBlank((String)civ.tradeGoods)) {
            CivMessage.sendError((Object)player, CivSettings.localize.localizedString("cmd_civ_trade_gift_noGoods"));
            return;
        }
        Inventory listInventory = Bukkit.getServer().createInventory(player, 54, CivColor.GoldBold + CivSettings.localize.localizedString("cmd_civ_trade_listciv_listInvName", CivColor.RoseBold + civ.getName()));
        int i = 0;
        for (String goodID : civ.tradeGoods.split(", ")) {
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
        listInventory.setItem(53, backButton);
        LoreGuiItemListener.guiInventories.put(listInventory.getName(), listInventory);
        TaskMaster.syncTask(new OpenInventoryTask(player, listInventory));
    }
}

