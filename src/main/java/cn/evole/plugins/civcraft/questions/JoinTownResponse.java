/**
 * [2023] Cnlimiter LLC
 * All Rights Reserved.
 * Name: civcraft
 * Author: cnlimiter
 * UpdateTime: 2023/10/14 16:15
 * Description:
 * License: ARR
 */
package cn.evole.plugins.civcraft.questions;

import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.exception.AlreadyRegisteredException;
import cn.evole.plugins.civcraft.main.CivMessage;
import cn.evole.plugins.civcraft.object.Resident;
import cn.evole.plugins.civcraft.object.Town;
import cn.evole.plugins.civcraft.util.CivColor;
import org.bukkit.entity.Player;

public class JoinTownResponse implements QuestionResponseInterface {

    public Town town;
    public Resident resident;
    public Player sender;

    @Override
    public void processResponse(String param) {
        if (param.equalsIgnoreCase("accept")) {
            CivMessage.send(sender, CivColor.LightGray + CivSettings.localize.localizedString("var_joinTown_accepted", resident.getName()));

            try {
                town.addResident(resident);
            } catch (AlreadyRegisteredException e) {
                CivMessage.sendError(sender, CivSettings.localize.localizedString("var_joinTown_errorInTown", resident.getName()));
                return;
            }

            CivMessage.sendTown(town, CivSettings.localize.localizedString("var_joinTown_alert", resident.getName()));
            resident.save();
        } else {
            CivMessage.send(sender, CivColor.LightGray + CivSettings.localize.localizedString("var_joinTown_Declined", resident.getName()));
        }
    }

    @Override
    public void processResponse(String response, Resident responder) {
        processResponse(response);
    }
}
