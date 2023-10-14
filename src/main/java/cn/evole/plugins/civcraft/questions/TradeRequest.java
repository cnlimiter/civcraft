package cn.evole.plugins.civcraft.questions;

import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.main.CivMessage;
import cn.evole.plugins.civcraft.object.Resident;
import cn.evole.plugins.civcraft.trade.TradeInventoryListener;
import cn.evole.plugins.civcraft.trade.TradeInventoryPair;
import cn.evole.plugins.civcraft.util.CivColor;

public class TradeRequest implements QuestionResponseInterface {

    public Resident resident;
    public Resident trader;

    @Override
    public void processResponse(String param) {
        if (param.equalsIgnoreCase("accept")) {
            TradeInventoryPair pair = new TradeInventoryPair();
            pair.inv = trader.startTradeWith(resident);
            if (pair.inv == null) {
                return;
            }

            pair.otherInv = resident.startTradeWith(trader);
            if (pair.otherInv == null) {
                return;
            }

            pair.resident = trader;
            pair.otherResident = resident;
            TradeInventoryListener.tradeInventories.put(TradeInventoryListener.getTradeInventoryKey(trader), pair);

            TradeInventoryPair otherPair = new TradeInventoryPair();
            otherPair.inv = pair.otherInv;
            otherPair.otherInv = pair.inv;
            otherPair.resident = pair.otherResident;
            otherPair.otherResident = pair.resident;
            TradeInventoryListener.tradeInventories.put(TradeInventoryListener.getTradeInventoryKey(resident), otherPair);
        } else {
            CivMessage.send(trader, CivColor.LightGray + CivSettings.localize.localizedString("var_trade_declined", resident.getName()));
        }
    }

    @Override
    public void processResponse(String response, Resident responder) {
        processResponse(response);
    }
}
