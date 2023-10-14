/**
 * [2023] Cnlimiter LLC
 * All Rights Reserved.
 * Name: civcraft
 * Author: cnlimiter
 * UpdateTime: 2023/10/14 16:15
 * Description:
 * License: ARR
 */
package cn.evole.plugins.civcraft.command.market;

import cn.evole.plugins.civcraft.command.CommandBase;
import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.exception.CivException;

public class MarketCommand extends CommandBase {

    @Override
    public void init() {
        command = "/market";
        displayName = CivSettings.localize.localizedString("cmd_market_Name");

        commands.put("buy", CivSettings.localize.localizedString("cmd_market_buyDesc"));

    }

    public void buy_cmd() {
        MarketBuyCommand cmd = new MarketBuyCommand();
        cmd.onCommand(sender, null, "buy", this.stripArgs(args, 1));
    }

    @Override
    public void doDefaultAction() throws CivException {
        showHelp();
    }

    @Override
    public void showHelp() {
        showBasicHelp();
    }

    @Override
    public void permissionCheck() throws CivException {

    }

}
