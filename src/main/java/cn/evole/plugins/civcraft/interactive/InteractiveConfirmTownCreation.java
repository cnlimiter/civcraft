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
import cn.evole.plugins.civcraft.questions.TownNewRequest;
import cn.evole.plugins.civcraft.util.CivColor;
import org.bukkit.entity.Player;

public class InteractiveConfirmTownCreation implements InteractiveResponse {

    @Override
    public void respond(String message, Resident resident) {

        Player player;
        try {
            player = CivGlobal.getPlayer(resident);
        } catch (CivException e) {
            return;
        }

        resident.clearInteractiveMode();

        if (!"yes".equalsIgnoreCase(message)) {
            CivMessage.send(player, CivSettings.localize.localizedString("interactive_town_cancel"));
            return;
        }

        if (resident.desiredTownName == null) {
            CivMessage.send(player, CivColor.Rose + CivSettings.localize.localizedString("interactive_town_createError"));
            return;
        }

        TownNewRequest join = new TownNewRequest();
        join.resident = resident;
        join.civ = resident.getCiv();
        try {
            CivGlobal.questionLeaders(player, resident.getCiv(), CivSettings.localize.localizedString("var_interactive_town_alert", player.getName(), resident.desiredTownName, (player.getLocation().getBlockX() + "," + player.getLocation().getBlockY() + "," + player.getLocation().getBlockZ())),
                    30 * 1000, join);
        } catch (CivException e) {
            CivMessage.sendError(player, e.getMessage());
            return;
        }

        CivMessage.send(player, CivColor.Yellow + CivSettings.localize.localizedString("interactive_town_request"));
//		CivGlobal.questionPlayer(player, CivGlobal.getPlayer(newResident), 
//				"Would you like to join the town of "+town.getName()+"?",
//				INVITE_TIMEOUT, join);

//		TaskMaster.syncTask(new FoundTownSync(resident));
    }

}
