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
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class InteractiveTownName implements InteractiveResponse {

    @Override
    public void respond(String message, Resident resident) {

        Player player;
        try {
            player = CivGlobal.getPlayer(resident);
        } catch (CivException e) {
            return;
        }

        if ("cancel".equalsIgnoreCase(message)) {
            CivMessage.send(player, CivSettings.localize.localizedString("interactive_town_cancelled"));
            resident.clearInteractiveMode();
            return;
        }

        //|| !StringUtils.isAsciiPrintable(message)
        // 检查是否只包含unicode字母
        if (!valid(message)) {
            CivMessage.send(player, CivColor.Rose + ChatColor.BOLD + CivSettings.localize.localizedString("interactive_town_nameInvalid"));
            return;
        }

        message = message.replace(" ", "_");
        message = message.replace("\"", "");
        message = message.replace("\'", "");

        resident.desiredTownName = message;
        CivMessage.send(player, CivColor.LightGreen + CivSettings.localize.localizedString("var_interactive_town_confirmName", CivColor.Yellow + resident.desiredTownName + CivColor.LightGreen));

        class SyncTask implements Runnable {
            Resident resident;

            public SyncTask(Resident resident) {
                this.resident = resident;
            }


            @Override
            public void run() {
                Player player;
                try {
                    player = CivGlobal.getPlayer(resident);
                } catch (CivException e) {
                    return;
                }

                CivMessage.sendHeading(player, CivSettings.localize.localizedString("interactive_town_surveyResults"));
                CivMessage.send(player, TownCommand.survey(player.getLocation()));

                Location capLoc = resident.getCiv().getCapitolTownHallLocation();
                if (capLoc == null) {
                    CivMessage.sendError(player, CivSettings.localize.localizedString("interactive_town_noCapitol"));
                    resident.clearInteractiveMode();
                    return;
                }

                CivMessage.send(player, CivColor.LightGreen + ChatColor.BOLD + CivSettings.localize.localizedString("interactive_town_confirm"));

                resident.setInteractiveMode(new InteractiveConfirmTownCreation());
            }
        }

        TaskMaster.syncTask(new SyncTask(resident));

        return;


    }

}
