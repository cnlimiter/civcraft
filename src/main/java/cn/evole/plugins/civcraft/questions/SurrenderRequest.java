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
import cn.evole.plugins.civcraft.main.CivMessage;
import cn.evole.plugins.civcraft.object.Civilization;
import cn.evole.plugins.civcraft.object.Resident;
import cn.evole.plugins.civcraft.util.CivColor;

public class SurrenderRequest implements QuestionResponseInterface {

    public Civilization fromCiv;
    public Civilization toCiv;

    @Override
    public void processResponse(String param) {
        if (param.equalsIgnoreCase("accept")) {
            fromCiv.onDefeat(toCiv);
            CivMessage.global(CivSettings.localize.localizedString("var_surrender_accepted", fromCiv.getName(), toCiv.getName()));
        } else {
            CivMessage.sendCiv(fromCiv, CivColor.LightGray + CivSettings.localize.localizedString("var_RequestDecline", toCiv.getName()));
        }
    }

    @Override
    public void processResponse(String response, Resident responder) {
        processResponse(response);
    }
}
