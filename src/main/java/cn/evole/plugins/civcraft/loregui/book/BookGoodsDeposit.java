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

public class BookGoodsDeposit
        implements GuiAction {
    @Override
    public void performAction(InventoryClickEvent event, ItemStack stack) {
        Player player = (Player) event.getWhoClicked();
        Resident resident = CivGlobal.getResident(player);
        Civilization from = resident.getCiv();
        Town to = resident.getSelectedTown();
        if (from == null) {
            CivMessage.sendError((Object) player, CivSettings.localize.localizedString("var_virtualTG_noCiv"));
            return;
        }
        if (!from.getLeaderGroup().hasMember(resident)) {
            CivMessage.sendError((Object) player, CivSettings.localize.localizedString("var_virtualTG_noPerm", "§6" + from.getName() + CivColor.Red));
            return;
        }
        if (StringUtils.isBlank((String) from.tradeGoods)) {
            CivMessage.sendError((Object) player, CivSettings.localize.localizedString("cmd_civ_trade_gift_noGoods"));
            return;
        }
        Inventory gepositInventory = Bukkit.getServer().createInventory(player, 54, CivColor.GoldBold + CivSettings.localize.localizedString("cmd_civ_trade_gift_giftInvName", CivColor.RoseBold + from.getName()));
        int i = 0;
        for (String goodID : from.tradeGoods.split(", ")) {
            ConfigTradeGood configTradeGood = CivSettings.goods.get(goodID);
            if (configTradeGood == null) continue;
            String[] split = CivSettings.getBonusDisplayString(configTradeGood, "§a" + CivSettings.localize.localizedString("cmd_civ_trade_deposit_clickToDeposit", new StringBuilder().append("§2").append(to.getName()).append("§a").toString())).split(";");
            ItemStack tradeGood = LoreGuiItem.build(configTradeGood.name, configTradeGood.material, configTradeGood.material_data, split);
            tradeGood = LoreGuiItem.setAction(tradeGood, "Confirmation");
            tradeGood = LoreGuiItem.setActionData(tradeGood, "townName", to.getName());
            tradeGood = LoreGuiItem.setActionData(tradeGood, "tradeGoodID", goodID);
            tradeGood = LoreGuiItem.setActionData(tradeGood, "passFields", "townName,tradeGoodID");
            tradeGood = LoreGuiItem.setActionData(tradeGood, "passAction", "DepositTradeGood");
            tradeGood = LoreGuiItem.setActionData(tradeGood, "confirmText", CivSettings.localize.localizedString("cmd_civ_trade_deposit_confirmText", "§2" + to.getName() + "§a"));
            gepositInventory.setItem(i, tradeGood);
            ++i;
        }
        ItemStack backButton = LoreGuiItem.build(CivSettings.localize.localizedString("loreGui_recipes_back"), ItemManager.getId(Material.MAP), 0, CivSettings.localize.localizedString("var_virtualTG_backToMain"));
        backButton = LoreGuiItem.setAction(backButton, "OpenInventory");
        backButton = LoreGuiItem.setActionData(backButton, "invType", "showGuiInv");
        backButton = LoreGuiItem.setActionData(backButton, "invName", BookGoodsGui.guiInventory.getName());
        gepositInventory.setItem(53, backButton);
        LoreGuiItemListener.guiInventories.put(gepositInventory.getName(), gepositInventory);
        TaskMaster.syncTask(new OpenInventoryTask(player, gepositInventory));
    }
}

