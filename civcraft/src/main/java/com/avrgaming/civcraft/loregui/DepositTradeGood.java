
package com.avrgaming.civcraft.loregui;

import java.sql.SQLException;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.loregui.GuiAction;
import com.avrgaming.civcraft.lorestorage.LoreGuiItem;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.exception.CivException;
import com.avrgaming.civcraft.object.Civilization;
import com.avrgaming.civcraft.object.Resident;
import com.avrgaming.civcraft.object.Town;
import com.avrgaming.civcraft.util.CivColor;

public class DepositTradeGood
implements GuiAction {
    @Override
    public void performAction(InventoryClickEvent event, ItemStack stack) throws CivException {
        Player player = (Player)event.getWhoClicked();
        Resident resident = CivGlobal.getResident(player);
        String townName = LoreGuiItem.getActionData(stack, "townName");
        String tradeGoodID = LoreGuiItem.getActionData(stack, "tradeGoodID");
        Civilization from = resident.getCiv();
        Town to = CivGlobal.getTown(townName);
        try {
            to.depositTradeGood(tradeGoodID);
            from.withdrawTradeGood(tradeGoodID);
            to.saveNow();
            from.saveNow();
        }
        catch (CivException e) {
            String message = e.getMessage();
            CivMessage.sendError((Object)player, message);
            if (message.contains("Your civilization does not have")) {
                to.withdrawTradeGood(tradeGoodID);
            }
            try {
                to.saveNow();
            }
            catch (SQLException e1) {
                e1.printStackTrace();
            }
            player.closeInventory();
            return;
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        CivMessage.sendCiv(from, CivSettings.localize.localizedString("cmd_civ_trade_deposit_succusessMessageFrom", player.getDisplayName(), "§a" + to.getName() + CivColor.RESET, CivColor.Red + CivSettings.goods.get((Object)tradeGoodID).name + CivColor.RESET));
        CivMessage.sendTown(to, "§e" + CivSettings.localize.localizedString("cmd_civ_trade_deposit_succusessMessageFrom", new StringBuilder().append(player.getDisplayName()).append("§e").toString(), new StringBuilder().append("§a").append(from.getName()).append("§e").toString(), new StringBuilder().append(CivColor.Red).append(CivSettings.goods.get((Object)tradeGoodID).name).append("§e").toString()));
        player.closeInventory();
    }
}

