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

import cn.evole.plugins.civcraft.camp.Camp;
import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.main.CivMessage;
import cn.evole.plugins.civcraft.object.Resident;
import cn.evole.plugins.civcraft.util.CivColor;
import org.bukkit.entity.Player;

public class JoinCampResponse implements QuestionResponseInterface {

    public Camp camp;
    public Resident resident;
    public Player sender;

    @Override
    public void processResponse(String param) {
        if (param.equalsIgnoreCase("accept")) {
            CivMessage.send(sender, CivColor.LightGray + CivSettings.localize.localizedString("var_joinCamp_accepted", resident.getName()));

            if (!camp.hasMember(resident.getName())) {
                camp.addMember(resident);
                CivMessage.sendCamp(camp, CivSettings.localize.localizedString("var_joinCamp_Alert", resident.getName()));
                resident.save();
            }
        } else {
            CivMessage.send(sender, CivColor.LightGray + CivSettings.localize.localizedString("var_joinCamp_Decline", resident.getName()));
        }
    }

    @Override
    public void processResponse(String response, Resident responder) {
        processResponse(response);
    }
}
