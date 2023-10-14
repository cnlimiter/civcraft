/**
 * [2023] Cnlimiter LLC
 * All Rights Reserved.
 * Name: civcraft
 * Author: cnlimiter
 * UpdateTime: 2023/10/14 16:15
 * Description:
 * License: ARR
 */
package cn.evole.plugins.civcraft.command;

import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.exception.CivException;
import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.main.CivMessage;
import cn.evole.plugins.civcraft.object.Resident;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PayCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {

        try {
            Player player = CivGlobal.getPlayer(sender.getName());
            Resident resident = CivGlobal.getResident(player);
            if (resident == null) {
                CivMessage.sendError(sender, CivSettings.localize.localizedString("cmd_pay_missingPlayer"));
                return false;
            }

            if (args.length < 2) {
                throw new CivException(CivSettings.localize.localizedString("cmd_pay_prompt"));
            }

            Resident payTo = CivGlobal.getResident(args[0]);
            if (payTo == null) {
                throw new CivException(CivSettings.localize.localizedString("cmd_NameNoResults"));
            }

            if (resident == payTo) {
                throw new CivException(CivSettings.localize.localizedString("cmd_pay_yourself"));
            }

            Double amount;
            try {
                amount = Double.valueOf(args[1]);
                if (!resident.getTreasury().hasEnough(amount)) {
                    throw new CivException(CivSettings.localize.localizedString("var_cmd_pay_InsufficentFunds", CivSettings.CURRENCY_NAME));
                }
            } catch (NumberFormatException e) {
                throw new CivException(CivSettings.localize.localizedString("EnterNumber"));
            }

            if (amount < 1) {
                throw new CivException(CivSettings.localize.localizedString("cmd_pay_WholeNumbers"));
            }
            amount = Math.floor(amount);

            resident.getTreasury().withdraw(amount);
            payTo.getTreasury().deposit(amount);

            CivMessage.sendSuccess(player, CivSettings.localize.localizedString("var_cmd_pay_PaidSuccess", amount, CivSettings.CURRENCY_NAME, payTo.getName()));

            try {
                Player payToPlayer = CivGlobal.getPlayer(payTo);
                CivMessage.sendSuccess(payToPlayer, CivSettings.localize.localizedString("var_cmd_pay_PaidReceiverSuccess", resident.getName(), amount, CivSettings.CURRENCY_NAME));
            } catch (CivException e) {
                // player not online, forget it.
            }
        } catch (CivException e) {
            CivMessage.sendError(sender, e.getMessage());
            return false;
        }
        return true;
    }

}
