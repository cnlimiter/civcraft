/**
 * [2023] Cnlimiter LLC
 * All Rights Reserved.
 * Name: civcraft
 * Author: cnlimiter
 * UpdateTime: 2023/10/14 16:15
 * Description:
 * License: ARR
 */
package cn.evole.plugins.civcraft.command.town;

import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.main.CivMessage;
import cn.evole.plugins.civcraft.object.Resident;
import cn.evole.plugins.civcraft.util.CivColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TownChatCommand implements CommandExecutor {

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
            resident.setTownChat(!resident.isTownChat());
            resident.setCivChat(false);
            CivMessage.sendSuccess(sender, CivSettings.localize.localizedString("cmd_town_chat_mode") + " " + resident.isTownChat());
            return true;
        }


        String fullArgs = "";
        for (String arg : args) {
            fullArgs += arg + " ";
        }

        if (resident.getTown() == null) {
            player.sendMessage(CivColor.Rose + CivSettings.localize.localizedString("cmd_town_chat_NoTown"));
            return false;
        }
        CivMessage.sendTownChat(resident.getTown(), resident, "<%s> %s", fullArgs);
        return true;
    }

}
