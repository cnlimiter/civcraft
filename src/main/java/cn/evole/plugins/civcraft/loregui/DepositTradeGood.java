package cn.evole.plugins.civcraft.loregui;

import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.exception.CivException;
import cn.evole.plugins.civcraft.lorestorage.LoreGuiItem;
import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.main.CivMessage;
import cn.evole.plugins.civcraft.object.Civilization;
import cn.evole.plugins.civcraft.object.Resident;
import cn.evole.plugins.civcraft.object.Town;
import cn.evole.plugins.civcraft.util.CivColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.sql.SQLException;

public class DepositTradeGood
        implements GuiAction {
    @Override
    public void performAction(InventoryClickEvent event, ItemStack stack) throws CivException {
        Player player = (Player) event.getWhoClicked();
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
        } catch (CivException e) {
            String message = e.getMessage();
            CivMessage.sendError((Object) player, message);
            if (message.contains("Your civilization does not have")) {
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
        CivMessage.sendCiv(from, CivSettings.localize.localizedString("cmd_civ_trade_deposit_succusessMessageFrom", player.getDisplayName(), "§a" + to.getName() + CivColor.RESET, CivColor.Red + CivSettings.goods.get((Object) tradeGoodID).name + CivColor.RESET));
        CivMessage.sendTown(to, "§e" + CivSettings.localize.localizedString("cmd_civ_trade_deposit_succusessMessageFrom", new StringBuilder().append(player.getDisplayName()).append("§e").toString(), new StringBuilder().append("§a").append(from.getName()).append("§e").toString(), new StringBuilder().append(CivColor.Red).append(CivSettings.goods.get((Object) tradeGoodID).name).append("§e").toString()));
        player.closeInventory();
    }
}

