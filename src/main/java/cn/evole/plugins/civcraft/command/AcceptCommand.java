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
import cn.evole.plugins.civcraft.threading.tasks.CivLeaderQuestionTask;
import cn.evole.plugins.civcraft.threading.tasks.PlayerQuestionTask;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AcceptCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {

        if (!(sender instanceof Player)) {
            CivMessage.sendError(sender, CivSettings.localize.localizedString("cmd_MustBePlayer"));
            return false;
        }

        Player player = (Player) sender;

        PlayerQuestionTask task = (PlayerQuestionTask) CivGlobal.getQuestionTask(player.getName());
        if (task != null) {
            /* We have a question, and the answer was "Accepted" so notify the task. */
            synchronized (task) {
                task.setResponse("accept");
                task.notifyAll();
            }
            return true;
        }

        Resident resident = CivGlobal.getResident(player);
        if (resident.hasTown()) {
            if (resident.getCiv().getLeaderGroup().hasMember(resident)) {
                CivLeaderQuestionTask civTask = (CivLeaderQuestionTask) CivGlobal.getQuestionTask("civ:" + resident.getCiv().getName());
                if (civTask != null) {
                    synchronized (civTask) {
                        civTask.setResponse("accept");
                        civTask.setResponder(resident);
                        civTask.notifyAll();
                    }
                }
                return true;
            }
        }


        CivMessage.sendError(sender, CivSettings.localize.localizedString("cmd_acceptError"));
        return false;
    }

}
