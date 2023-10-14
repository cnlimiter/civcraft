package cn.evole.plugins.civcraft.questions;

import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.main.CivMessage;
import cn.evole.plugins.civcraft.object.Resident;
import cn.evole.plugins.civcraft.object.Town;
import cn.evole.plugins.civcraft.util.CivColor;

public class CapitulateRequest implements QuestionResponseInterface {

    public Town capitulator;
    public String from;
    public String to;
    public String playerName;

    @Override
    public void processResponse(String param) {
        if (param.equalsIgnoreCase("accept")) {
            capitulator.capitulate();
            CivMessage.global(CivSettings.localize.localizedString("var_capitulateAccept", from, to));
        } else {
            CivMessage.send(playerName, CivColor.LightGray + CivSettings.localize.localizedString("var_RequestDecline", to));
        }
    }

    @Override
    public void processResponse(String response, Resident responder) {
        processResponse(response);
    }
}
