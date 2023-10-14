/**
 * [2023] Cnlimiter LLC
 * All Rights Reserved.
 * Name: civcraft
 * Author: cnlimiter
 * UpdateTime: 2023/10/14 16:15
 * Description:
 * License: ARR
 */
package cn.evole.plugins.civcraft.interactive;

import cn.evole.plugins.civcraft.command.town.TownCommand;
import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.exception.CivException;
import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.main.CivMessage;
import cn.evole.plugins.civcraft.object.Resident;
import cn.evole.plugins.civcraft.threading.TaskMaster;
import cn.evole.plugins.civcraft.util.CivColor;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class InteractiveCapitolName implements InteractiveResponse {

    @Override
    public void respond(String message, Resident resident) {

        Player player;
        try {
            player = CivGlobal.getPlayer(resident);
        } catch (CivException e) {
            return;
        }

        if ("cancel".equalsIgnoreCase(message)) {
            CivMessage.send(player, CivSettings.localize.localizedString("interactive_capitol_cancel"));
            resident.clearInteractiveMode();
            return;
        }

        //|| !StringUtils.isAsciiPrintable(message)
        // 检查是否只包含unicode字母
        if (!valid(message)) {
            CivMessage.send(player, CivColor.Rose + ChatColor.BOLD + CivSettings.localize.localizedString("interactive_capitol_invalidname"));
            return;
        }

        message = message.replace(" ", "_");
        message = message.replace("\"", "");
        message = message.replace("\'", "");

        resident.desiredCapitolName = message;
        CivMessage.send(player, CivColor.LightGreen + CivSettings.localize.localizedString("var_interactive_capitol_confirm1", CivColor.Yellow + resident.desiredCivName + CivColor.LightGreen, CivColor.Yellow + resident.desiredCapitolName + CivColor.LightGreen));
        CivMessage.sendHeading(player, CivSettings.localize.localizedString("interactive_capitol_confirmSurvey"));

        class SyncTask implements Runnable {
            String playerName;


            public SyncTask(String name) {
                this.playerName = name;
            }

            @Override
            public void run() {
                Player player;
                try {
                    player = CivGlobal.getPlayer(playerName);
                } catch (CivException e) {
                    return;
                }

                Resident resident = CivGlobal.getResident(playerName);
                if (resident == null) {
                    return;
                }

                CivMessage.send(player, TownCommand.survey(player.getLocation()));
                CivMessage.send(player, "");
                CivMessage.send(player, CivColor.LightGreen + ChatColor.BOLD + CivSettings.localize.localizedString("interactive_capitol_confirmPrompt"));
                resident.setInteractiveMode(new InteractiveConfirmCivCreation());
            }

        }

        TaskMaster.syncTask(new SyncTask(resident.getName()));

        return;
    }

}
