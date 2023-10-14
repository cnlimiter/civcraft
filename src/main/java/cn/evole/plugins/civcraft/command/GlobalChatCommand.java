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
import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.main.CivMessage;
import cn.evole.plugins.civcraft.object.Resident;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GlobalChatCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {

        //TODO let non players use this command
        if ((sender instanceof Player) == false) {
            return false;
        }

        Player player = (Player) sender;
        Resident resident = CivGlobal.getResident(player);
        if (resident == null) {
            CivMessage.sendError(sender, CivSettings.localize.localizedString("cmd_gc_notResident"));
            return false;
        }

        if (args.length == 0) {
            resident.setCivChat(false);
            resident.setTownChat(false);
            CivMessage.sendSuccess(sender, CivSettings.localize.localizedString("cmd_gc_enabled"));
            return true;
        }

        CivMessage.sendError(sender, CivSettings.localize.localizedString("cmd_gc_disabled"));
        return true;

//		String fullArgs = "";
//		for (String arg : args) {
//			fullArgs += arg + " ";
//		}
//		
//		CivMessage.sendChat(resident, "<%s> %s", fullArgs);
//		return true;
    }
}
