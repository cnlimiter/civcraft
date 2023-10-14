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
import cn.evole.plugins.civcraft.threading.tasks.PlayerQuestionTask;
import cn.evole.plugins.civcraft.threading.tasks.TemplateSelectQuestionTask;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SelectCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (!(sender instanceof Player)) {
            CivMessage.sendError(sender, CivSettings.localize.localizedString("cmd_MustBePlayer"));
            return false;
        }


        if (args.length < 1) {
            CivMessage.sendError(sender, CivSettings.localize.localizedString("cmd_enterNumber"));
            return false;
        }

        Player player = (Player) sender;

        PlayerQuestionTask task = (PlayerQuestionTask) CivGlobal.getQuestionTask(player.getName());
        if (task == null) {
            CivMessage.sendError(sender, CivSettings.localize.localizedString("cmd_acceptError"));
            return false;
        }

        if (!(task instanceof TemplateSelectQuestionTask)) {
            CivMessage.sendError(sender, CivSettings.localize.localizedString("cmd_acceptSomethingWentWrong"));
            return false;
        }

        /* We have a question, and the answer was "Accepted" so notify the task. */
        synchronized (task) {
            task.setResponse(args[0]);
            task.notifyAll();
        }

        return true;
    }

}
