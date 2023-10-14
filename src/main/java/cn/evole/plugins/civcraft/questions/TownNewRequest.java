package cn.evole.plugins.civcraft.questions;

import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.main.CivMessage;
import cn.evole.plugins.civcraft.object.Civilization;
import cn.evole.plugins.civcraft.object.Resident;
import cn.evole.plugins.civcraft.threading.TaskMaster;
import cn.evole.plugins.civcraft.threading.tasks.FoundTownSync;
import cn.evole.plugins.civcraft.util.CivColor;

public class TownNewRequest implements QuestionResponseInterface {

    public Resident resident;
    public Resident leader;
    public Civilization civ;
    public String name;

    @Override
    public void processResponse(String param) {
        if (param.equalsIgnoreCase("accept")) {
            CivMessage.send(civ, CivColor.LightGreen + CivSettings.localize.localizedString("newTown_accepted1", leader.getName(), name));
            TaskMaster.syncTask(new FoundTownSync(resident));
        } else {
            CivMessage.send(resident, CivColor.LightGray + CivSettings.localize.localizedString("var_newTown_declined", leader.getName()));
        }
    }

    @Override
    public void processResponse(String response, Resident responder) {
        this.leader = responder;
        processResponse(response);
    }
}
