package cn.evole.plugins.civcraft.loregui.book;

import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.exception.CivException;
import cn.evole.plugins.civcraft.loregui.GuiAction;
import cn.evole.plugins.civcraft.lorestorage.LoreGuiItem;
import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.main.CivMessage;
import cn.evole.plugins.civcraft.object.Civilization;
import cn.evole.plugins.civcraft.object.Town;
import cn.evole.plugins.civcraft.util.CivColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.sql.SQLException;

public class WithdrawTradeGood
        implements GuiAction {
    @Override
    public void performAction(InventoryClickEvent event, ItemStack stack) throws CivException {
        Player player = (Player) event.getWhoClicked();
        String civilizationName = LoreGuiItem.getActionData(stack, "civilizationName");
        String townName = LoreGuiItem.getActionData(stack, "townName");
        String tradeGoodID = LoreGuiItem.getActionData(stack, "tradeGoodID");
        Town from = CivGlobal.getTown(townName);
        Civilization to = CivGlobal.getCiv(civilizationName);
        try {
            from.withdrawTradeGood(tradeGoodID);
            to.depositTradeGood(tradeGoodID);
            to.saveNow();
            from.saveNow();
        } catch (CivException e) {
            String message = e.getMessage();
            CivMessage.sendError((Object) player, message);
            if (message.contains("You can not have more")) {
                from.depositTradeGood(tradeGoodID);
            }
            try {
                from.saveNow();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
            player.closeInventory();
            return;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        CivMessage.sendCiv(to, CivSettings.localize.localizedString("cmd_civ_trade_withdraw_succusessMessageTo", player.getDisplayName(), "§a" + from.getName() + "§e", CivColor.Red + CivSettings.goods.get((Object) tradeGoodID).name + CivColor.RESET));
        CivMessage.sendTown(from, "§e" + CivSettings.localize.localizedString("cmd_civ_trade_withdraw_succusessMessageFrom", new StringBuilder().append(player.getDisplayName()).append("§e").toString(), new StringBuilder().append(CivColor.Red).append(CivSettings.goods.get((Object) tradeGoodID).name).append("§e").toString()));
        player.closeInventory();
    }
}

