
package com.avrgaming.civcraft.loregui;

import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.exception.CivException;
import com.avrgaming.civcraft.lorestorage.LoreGuiItem;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.object.Civilization;
import com.avrgaming.civcraft.object.Resident;
import com.avrgaming.civcraft.util.CivColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.sql.SQLException;

public class GiftTradeGood implements GuiAction {
    @Override
    public void performAction(InventoryClickEvent event, ItemStack stack) throws CivException {
        Player player = (Player) event.getWhoClicked();
        Resident resident = CivGlobal.getResident(player);
        String civilizationName = LoreGuiItem.getActionData(stack, "civilizationName");
        String tradeGoodID = LoreGuiItem.getActionData(stack, "tradeGoodID");
        Civilization from = resident.getCiv();
        Civilization to = CivGlobal.getCiv(civilizationName);
        try {
            to.depositTradeGood(tradeGoodID);
            from.withdrawTradeGood(tradeGoodID);
            to.saveNow();
            from.saveNow();
        } catch (CivException e) {
            String message = e.getMessage();
            if (message.contains("You can not have more")) {
                message = CivSettings.localize.localizedString("cmd_civ_trade_gift_errorFullSlots");
            }
            CivMessage.sendError((Object) player, message);
            if (e.getMessage().contains("Your civilization does not have a")) {
                to.withdrawTradeGood(tradeGoodID);
            }
            try {
                to.saveNow();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
            player.closeInventory();
            return;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        CivMessage.sendCiv(from, CivSettings.localize.localizedString("cmd_civ_trade_gift_succusessMessageFrom", player.getDisplayName(), "�a" + to.getName() + CivColor.RESET, CivColor.Red + CivSettings.goods.get((Object) tradeGoodID).name + CivColor.RESET));
        CivMessage.sendCiv(to, CivSettings.localize.localizedString("cmd_civ_trade_gift_succusessMessageTo", player.getDisplayName(), "�a" + from.getName() + CivColor.RESET, CivColor.Red + CivSettings.goods.get((Object) tradeGoodID).name + CivColor.RESET));
        player.closeInventory();
    }
}

