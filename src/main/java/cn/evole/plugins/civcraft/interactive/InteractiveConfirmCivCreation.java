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

import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.exception.CivException;
import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.main.CivMessage;
import cn.evole.plugins.civcraft.object.Resident;
import cn.evole.plugins.civcraft.threading.TaskMaster;
import cn.evole.plugins.civcraft.threading.tasks.FoundCivSync;
import cn.evole.plugins.civcraft.util.CivColor;
import org.bukkit.entity.Player;

public class InteractiveConfirmCivCreation implements InteractiveResponse {

    @Override
    public void respond(String message, Resident resident) {

        Player player;
        try {
            player = CivGlobal.getPlayer(resident);
        } catch (CivException e) {
            return;
        }

        resident.clearInteractiveMode();

        if (!message.equalsIgnoreCase("yes")) {
            CivMessage.send(player, CivSettings.localize.localizedString("interactive_civ_cancelcreate"));
            return;
        }

        if (resident.desiredCapitolName == null || resident.desiredCivName == null) {
            CivMessage.send(player, CivColor.Rose + CivSettings.localize.localizedString("interactive_civ_createError"));
            return;
        }

        TaskMaster.syncTask(new FoundCivSync(resident));

    }

}
