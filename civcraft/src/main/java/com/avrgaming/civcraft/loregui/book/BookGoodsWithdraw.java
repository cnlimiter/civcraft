
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
import com.avrgaming.civcraft.object.Town;
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

public class BookGoodsWithdraw
implements GuiAction {
    @Override
    public void performAction(InventoryClickEvent event, ItemStack stack) {
        Player player = (Player)event.getWhoClicked();
        Resident resident = CivGlobal.getResident(player);
        Town from = resident.getSelectedTown();
        Civilization to = resident.getCiv();
        if (to == null) {
            CivMessage.sendError((Object)player, CivSettings.localize.localizedString("var_virtualTG_noCiv"));
            return;
        }
        if (!to.getLeaderGroup().hasMember(resident) && !resident.getSelectedTown().getMayorGroup().hasMember(resident)) {
            CivMessage.sendError((Object)player, CivSettings.localize.localizedString("var_virtualTG_noPermM", "§6" + to.getName() + CivColor.Red, "§6" + from.getName() + CivColor.Red));
            return;
        }
        if (StringUtils.isBlank((String)from.tradeGoods)) {
            CivMessage.sendError((Object)player, CivSettings.localize.localizedString("cmd_civ_trade_listtown_noGoods", "§6" + from.getName() + CivColor.Red));
            return;
        }
        Inventory withdrawInventory = Bukkit.getServer().createInventory(player, 9, CivColor.GoldBold + CivSettings.localize.localizedString("cmd_civ_trade_gift_giftInvName", CivColor.RoseBold + from.getName()));
        int i = 0;
        for (String goodID : from.tradeGoods.split(", ")) {
            ConfigTradeGood configTradeGood = CivSettings.goods.get(goodID);
            if (configTradeGood == null) continue;
            String[] split = CivSettings.getBonusDisplayString(configTradeGood, "§a" + CivSettings.localize.localizedString("cmd_civ_trade_withdraw_clickToWithdraw", new StringBuilder().append("§2").append(to.getName()).append("§a").toString())).split(";");
            ItemStack tradeGood = LoreGuiItem.build(configTradeGood.name, configTradeGood.material, configTradeGood.material_data, split);
            tradeGood = LoreGuiItem.setAction(tradeGood, "Confirmation");
            tradeGood = LoreGuiItem.setActionData(tradeGood, "civilizationName", to.getName());
            tradeGood = LoreGuiItem.setActionData(tradeGood, "townName", from.getName());
            tradeGood = LoreGuiItem.setActionData(tradeGood, "tradeGoodID", goodID);
            tradeGood = LoreGuiItem.setActionData(tradeGood, "passFields", "civilizationName,townName,tradeGoodID");
            tradeGood = LoreGuiItem.setActionData(tradeGood, "passAction", "WithdrawTradeGood");
            tradeGood = LoreGuiItem.setActionData(tradeGood, "confirmText", CivSettings.localize.localizedString("cmd_civ_trade_withdraw_confirmText", "§2" + to.getName() + "§a"));
            withdrawInventory.setItem(i, tradeGood);
            ++i;
        }
        ItemStack backButton = LoreGuiItem.build(CivSettings.localize.localizedString("loreGui_recipes_back"), ItemManager.getId(Material.MAP), 0, CivSettings.localize.localizedString("var_virtualTG_backToMain"));
        backButton = LoreGuiItem.setAction(backButton, "OpenInventory");
        backButton = LoreGuiItem.setActionData(backButton, "invType", "showGuiInv");
        backButton = LoreGuiItem.setActionData(backButton, "invName", BookGoodsGui.guiInventory.getName());
        withdrawInventory.setItem(8, backButton);
        LoreGuiItemListener.guiInventories.put(withdrawInventory.getName(), withdrawInventory);
        TaskMaster.syncTask(new OpenInventoryTask(player, withdrawInventory));
    }
}

