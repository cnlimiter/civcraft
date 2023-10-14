package cn.evole.plugins.civcraft.interactive;

import cn.evole.plugins.civcraft.camp.WarCamp;
import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.config.ConfigBuildableInfo;
import cn.evole.plugins.civcraft.main.CivMessage;
import cn.evole.plugins.civcraft.object.Resident;

public class InteractiveWarCampFound implements InteractiveResponse {

    ConfigBuildableInfo info;

    public InteractiveWarCampFound(ConfigBuildableInfo info) {
        this.info = info;
    }

    @Override
    public void respond(String message, Resident resident) {
        resident.clearInteractiveMode();

        if (!message.equalsIgnoreCase("yes")) {
            CivMessage.send(resident, CivSettings.localize.localizedString("interactive_warcamp_Cancel"));
            return;
        }

        WarCamp.newCamp(resident, info);
    }

}
