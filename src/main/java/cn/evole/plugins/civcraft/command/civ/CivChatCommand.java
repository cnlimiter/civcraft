/**
 * [2023] Cnlimiter LLC
 * All Rights Reserved.
 * Name: civcraft
 * Author: cnlimiter
 * UpdateTime: 2023/10/14 16:15
 * Description:
 * License: ARR
 */
package cn.evole.plugins.civcraft.command.civ;

import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.main.CivMessage;
import cn.evole.plugins.civcraft.object.Resident;
import cn.evole.plugins.civcraft.util.CivColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CivChatCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {

        //TODO let non players use this command
        if ((sender instanceof Player) == false) {
            return false;
        }

        Player player = (Player) sender;
        Resident resident = CivGlobal.getResident(player);
        if (resident == null) {
            CivMessage.sendError(sender, CivSettings.localize.localizedString("cmd_civchat_notResident"));
            return false;
        }

        if (args.length == 0) {
            resident.setCivChat(!resident.isCivChat());
            resident.setTownChat(false);
            CivMessage.sendSuccess(sender, CivSettings.localize.localizedString("cmd_civchat_modeSet") + " " + resident.isCivChat());
            return true;
        }


        String fullArgs = "";
        for (String arg : args) {
            fullArgs += arg + " ";
        }

        if (resident.getTown() == null) {
            player.sendMessage(CivColor.Rose + CivSettings.localize.localizedString("cmd_civchat_error"));
            return false;
        }

        CivMessage.sendCivChat(resident.getTown().getCiv(), resident, "<%s> %s", fullArgs);
        return true;
    }
}
