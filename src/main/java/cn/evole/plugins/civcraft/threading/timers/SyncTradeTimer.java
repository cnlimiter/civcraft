/**
 * [2023] Cnlimiter LLC
 * All Rights Reserved.
 * Name: civcraft
 * Author: cnlimiter
 * UpdateTime: 2023/10/14 16:15
 * Description:
 * License: ARR
 */
package cn.evole.plugins.civcraft.threading.timers;

import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.main.CivMessage;
import cn.evole.plugins.civcraft.object.Town;
import cn.evole.plugins.civcraft.object.TradeGood;
import cn.evole.plugins.civcraft.util.CivColor;

import java.text.DecimalFormat;

/**
 * 贸易品结算(不应该是task？
 */
public class SyncTradeTimer implements Runnable {

    public SyncTradeTimer() {
    }

    public void processTownsTradePayments(Town town) {

        //goodies = town.getEffectiveBonusGoodies();

        //double payment = TradeGood.getTownTradePayment(town, goodies);
        double payment = TradeGood.getTownTradePayment(town);
        DecimalFormat df = new DecimalFormat();

        if (payment > 0.0) {

            double taxesPaid = payment * town.getDepositCiv().getIncomeTaxRate();
            if (taxesPaid > 0) {
                CivMessage.sendTown(town, CivColor.LightGreen +
                        CivSettings.localize.localizedString("var_syncTrade_payout", (CivColor.Yellow + df.format(payment) + CivColor.LightGreen + " " + CivSettings.CURRENCY_NAME),
                                CivSettings.localize.localizedString("var_cottage_grew_taxes", (df.format(Math.floor(taxesPaid)) + " " + CivSettings.CURRENCY_NAME), town.getDepositCiv().getName())));
            } else {
                CivMessage.sendTown(town, CivColor.LightGreen + CivSettings.localize.localizedString("var_syncTrade_payout", (CivColor.Yellow + df.format(payment) + CivColor.LightGreen + " " + CivSettings.CURRENCY_NAME), ""));
            }

            town.getTreasury().deposit(payment - taxesPaid);
            town.getDepositCiv().taxPayment(town, taxesPaid);
        }
    }

    @Override
    public void run() {
        if (!CivGlobal.tradeEnabled) {
            return;
        }

        CivGlobal.checkForDuplicateGoodies();

        for (Town town : CivGlobal.getTowns()) {
            try {
                processTownsTradePayments(town);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
