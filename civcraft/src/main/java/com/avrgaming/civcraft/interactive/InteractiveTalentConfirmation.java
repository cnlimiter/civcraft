
package com.avrgaming.civcraft.interactive;

import java.sql.SQLException;

import org.bukkit.entity.Player;

import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.interactive.InteractiveResponse;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.exception.CivException;
import com.avrgaming.civcraft.object.Civilization;
import com.avrgaming.civcraft.object.Resident;
import com.avrgaming.civcraft.object.Talent;
import com.avrgaming.civcraft.object.Town;
import com.avrgaming.civcraft.util.CivColor;

public class InteractiveTalentConfirmation
implements InteractiveResponse {
    public Civilization target;
    public Player leader;
    public Talent talent;
    public String succesesMessage;

    public InteractiveTalentConfirmation(Civilization target, Player leader, Talent talent, String succesesMessage) {
        this.target = target;
        this.leader = leader;
        this.talent = talent;
        this.succesesMessage = succesesMessage;
    }

    @Override
    public void respond(String message, Resident resident) {
        if (!(message.equalsIgnoreCase("yes"))) {
            CivMessage.sendError(resident, CivSettings.localize.localizedString("interactive_confirmTalent_cancel", CivColor.GoldBold + this.target.getName() + CivColor.Red));
            resident.clearInteractiveMode();
            return;
        }
        Town capitol = this.target.getCapitol();
        Civilization civ = capitol.getCiv();
        if (capitol.highestTalentLevel() >= talent.level) {
            CivMessage.sendError(resident, CivSettings.localize.localizedString("cmd_civ_talent_choose_notNow", civ.getCapitol().getName(), civ.getCapitol().getCultureLevel() + 1));
            return;
        }
        try {
            capitol.addTalent(talent);
            capitol.saveNow();
        }
        catch (SQLException e) {
            e.printStackTrace();
        } catch (CivException e) {
			e.printStackTrace();
		}
        CivMessage.sendCiv(this.target, this.succesesMessage);
        CivMessage.send(resident, CivColor.Green + CivSettings.localize.localizedString("cmd_civ_talent_choose_sucussesSender"));
    }

    protected void addBuffToTown(Town town, String id) {
        try {
            town.getBuffManager().addBuff(id, id, "Talent in " + town.getName());
        }
        catch (CivException e) {
            e.printStackTrace();
        }
    }
}

