
package com.avrgaming.civcraft.questions;

import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.exception.AlreadyRegisteredException;
import com.avrgaming.civcraft.exception.CivException;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.object.Civilization;
import com.avrgaming.civcraft.object.Resident;
import com.avrgaming.civcraft.object.Town;
import com.avrgaming.civcraft.util.CivColor;
import org.bukkit.entity.Player;

public class ChangeTownRequest
implements QuestionResponseInterface {
    public Resident resident;
    public Resident leader;
    public Civilization civ;
    public Town from;
    public Town to;

    @Override
    public void processResponse(String param) {
        String fullLeaderName;
        String fullResidentName;
        try {
        	if (!CivGlobal.getPlayer(resident).isOnline()) {
                return;
            }
        }
        catch (CivException e) {
            return;
        }
        try {
            Player leaderPlayer = CivGlobal.getPlayer(leader);
            Player residentPlayer = CivGlobal.getPlayer(resident);
            fullLeaderName = leaderPlayer.getDisplayName();
            fullResidentName = residentPlayer.getDisplayName();
        }
        catch (CivException e) {
            e.printStackTrace();
            CivMessage.sendError(this.leader, CivSettings.localize.localizedString("internalCommandException"));
            return;
        }
        if (param.equalsIgnoreCase("accept")) {
            CivMessage.sendCiv(this.civ, "�a" + CivSettings.localize.localizedString("var_changetownrequest_accepted", fullLeaderName, fullResidentName, new StringBuilder().append(CivColor.Red).append(this.from.getName()).toString(), new StringBuilder().append(CivColor.Red).append(this.to.getName()).toString()));
            this.changeTown();
        } else {
            CivMessage.sendCiv(this.civ, CivColor.LightGray + CivSettings.localize.localizedString("var_changetownrequest_declined", fullLeaderName, fullResidentName, new StringBuilder().append(CivColor.Red).append(this.from.getName()).toString(), new StringBuilder().append(CivColor.Red).append(this.to.getName()).toString()));
        }
    }

    protected void changeTown() {
        try {
        	if (!CivGlobal.getPlayer(resident).isOnline()) {
                return;
            }
        }
        catch (CivException e) {
            return;
        }
        try {
            if (!this.resident.getTreasury().hasEnough(50000.0)) {
                throw new CivException(CivSettings.localize.localizedString("var_switchtown_no_money", CivSettings.CURRENCY_NAME));
            }
            this.resident.getTreasury().withdraw(50000.0);
            this.from.removeResident(this.resident);
            this.to.addResident(this.resident);
            CivMessage.send((Object)this.resident, "§b" + CivSettings.localize.localizedString("sucusses_switch", this.to.getName()));
        }
        catch (AlreadyRegisteredException e) {
            e.printStackTrace();
            CivMessage.sendError(this.resident, CivSettings.localize.localizedString("var_switchtown_arleady_in_this_town"));
        }
        catch (CivException e1) {
            CivMessage.sendError(this.resident, e1.getMessage());
        }
    }

    @Override
    public void processResponse(String response, Resident responder) {
        this.leader = responder;
        this.processResponse(response);
    }
}

