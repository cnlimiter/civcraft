package cn.evole.plugins.global.perks.components;


import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.interactive.InteractiveRenameCivOrTown;
import cn.evole.plugins.civcraft.main.CivMessage;
import cn.evole.plugins.civcraft.object.Resident;

public class RenameCivOrTown extends PerkComponent {

    @Override
    public void onActivate(Resident resident) {

        if (!resident.hasTown()) {
            CivMessage.sendError(resident, CivSettings.localize.localizedString("RenameCivOrTown_NotResident"));
            return;
        }

        resident.setInteractiveMode(new InteractiveRenameCivOrTown(resident, this));
    }

}
