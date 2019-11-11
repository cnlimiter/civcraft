
package com.avrgaming.civcraft.command.civ;

import org.bukkit.inventory.Inventory;
import com.avrgaming.civcraft.command.CommandBase;
import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.config.ConfigSpaceMissions;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.exception.CivException;
import com.avrgaming.civcraft.object.Civilization;
import com.avrgaming.civcraft.util.CivColor;

public class CivSpaceCommand
extends CommandBase {
    public static Inventory guiInventory;

    @Override
    public void init() {
        this.command = "/civ space";
        this.displayName = CivSettings.localize.localizedString("cmd_civ_space_name");
        this.commands.put("gui", CivSettings.localize.localizedString("cmd_civ_space_guiDesc"));
        this.commands.put("complete", CivSettings.localize.localizedString("cmd_civ_space_succusessDesc"));
        this.commands.put("future", CivSettings.localize.localizedString("cmd_civ_space_futureDesc"));
        this.commands.put("progress", CivSettings.localize.localizedString("cmd_civ_space_progressDesc"));
    }

    public void progress_cmd() throws CivException {
        Civilization civ = this.getSenderCiv();
        if (!civ.getMissionActive()) {
            throw new CivException(CivSettings.localize.localizedString("var_spaceshuttle_noProgress"));
        }
        String[] split = civ.getMissionProgress().split(":");
        String missionName = CivSettings.spacemissions_levels.get((Object)Integer.valueOf((int)civ.getCurrentMission())).name;
        double beakers = Math.round(Double.parseDouble(split[0]));
        double hammers = Math.round(Double.parseDouble(split[1]));
        int percentageCompleteBeakers = (int)((double)Math.round(Double.parseDouble(split[0])) / Double.parseDouble(CivSettings.spacemissions_levels.get((Object)Integer.valueOf((int)civ.getCurrentMission())).require_beakers) * 100.0);
        int percentageCompleteHammers = (int)((double)Math.round(Double.parseDouble(split[1])) / Double.parseDouble(CivSettings.spacemissions_levels.get((Object)Integer.valueOf((int)civ.getCurrentMission())).require_hammers) * 100.0);
        String message = CivColor.LightBlue + missionName + ":" + CivColor.RESET + "\n" + CivColor.Gold + "Beakers: " + beakers + CivColor.Red + " (" + percentageCompleteBeakers + "%)" + CivColor.LightPurple + "Hammers: " + hammers + CivColor.Red + " (" + percentageCompleteHammers + "%)";
        CivMessage.sendSuccess(sender, message);
    }

    public void future_cmd() throws CivException {
        Civilization civ = this.getSenderCiv();
        if (civ.getCurrentMission() >= 8) {
            throw new CivException(CivSettings.localize.localizedString("var_spaceshuttle_end", CivSettings.spacemissions_levels.get((Object)Integer.valueOf((int)7)).name));
        }
        int current = civ.getCurrentMission();
        StringBuilder futureMissions = new StringBuilder(CivSettings.localize.localizedString("cmd_space_future")+": \n" + CivColor.LightPurple);
        if (current == 7 && civ.getMissionActive()) {
            throw new CivException(CivSettings.localize.localizedString("var_spaceshuttle_end", CivSettings.spacemissions_levels.get((Object)Integer.valueOf((int)7)).name));
        }
        if (civ.getMissionActive()) {
            ++current;
        }
        for (int i = current; i <= 7; ++i) {
            ConfigSpaceMissions configSpaceMissions = CivSettings.spacemissions_levels.get(i);
            futureMissions.append(configSpaceMissions.name).append("\n");
        }
        CivMessage.sendSuccess(sender, futureMissions.toString());
    }

    public void complete_cmd() throws CivException {
        Civilization civ = this.getSenderCiv();
        int ended = civ.getCurrentMission();
        CivMessage.sendHeading(sender, CivSettings.localize.localizedString("cmd_space_completed"));
        StringBuilder endedMissions = new StringBuilder();
        for (int i = 1; i < ended; ++i) {
            ConfigSpaceMissions configSpaceMissions = CivSettings.spacemissions_levels.get(i);
            endedMissions.append(configSpaceMissions.name).append("\n");
        }
        CivMessage.sendSuccess(sender, endedMissions.toString());
    }

    @Override
    public void doDefaultAction() throws CivException {
        this.showHelp();
    }

    @Override
    public void showHelp() {
        this.showBasicHelp();
    }

    @Override
    public void permissionCheck() throws CivException {
        this.validLeader();
    }
}

