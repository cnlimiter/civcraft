package com.avrgaming.civcraft.command;

import org.bukkit.ChatColor;

import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.exception.CivException;
import com.avrgaming.civcraft.interactive.InteractiveReportBug;
import com.avrgaming.civcraft.interactive.InteractiveReportPlayer;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.object.Resident;
import com.avrgaming.civcraft.util.CivColor;

public class ReportCommand extends CommandBase {

    @Override
    public void init() {
        command = "/report";
        displayName = CivSettings.localize.localizedString("cmd_reprot_Name");

        commands.put("player", CivSettings.localize.localizedString("cmd_report_playerDesc"));
        commands.put("bug", CivSettings.localize.localizedString("cmd_report_bugDesc"));

    }

    public void bug_cmd() throws CivException {
        Resident resident = this.getResident();
        CivMessage.sendHeading(this.sender, CivSettings.localize.localizedString("cmd_report_Heading"));
        CivMessage.send((Object) this.sender, "§e" + (Object) ChatColor.BOLD + CivSettings.localize.localizedString("cmd_report_5"));
        CivMessage.send((Object) this.sender, " ");
        CivMessage.send((Object) this.sender, "§e" + (Object) ChatColor.BOLD + CivSettings.localize.localizedString("cmd_report_6") + CivSettings.localize.localizedString("cmd_report_7"));
        CivMessage.send((Object) this.sender, "§e" + (Object) ChatColor.BOLD + CivSettings.localize.localizedString("interactive_report_descriptionBuG"));
        CivMessage.send((Object) this.sender, CivColor.LightGray + (Object) ChatColor.BOLD + CivSettings.localize.localizedString("cmd_report_8"));
        resident.setInteractiveMode(new InteractiveReportBug());
    }

    public void player_cmd() throws CivException {
        Resident resident = this.getResident();
        Resident reportedResident = this.getNamedResident(1);
        CivMessage.sendHeading(this.sender, CivSettings.localize.localizedString("cmd_report_Heading"));
        CivMessage.send((Object) this.sender, "§e" + (Object) ChatColor.BOLD + CivSettings.localize.localizedString("cmd_report_1", reportedResident.getName()));
        CivMessage.send((Object) this.sender, " ");
        CivMessage.send((Object) this.sender, "§e" + (Object) ChatColor.BOLD + CivSettings.localize.localizedString("cmd_report_2") + CivSettings.localize.localizedString("cmd_report_3"));
        CivMessage.send((Object) this.sender, "§e" + (Object) ChatColor.BOLD + CivSettings.localize.localizedString("interactive_report_description"));
        CivMessage.send((Object) this.sender, CivColor.LightGray + (Object) ChatColor.BOLD + CivSettings.localize.localizedString("cmd_report_4"));
        resident.setDesiredReportPlayerName(reportedResident.getName());
        resident.setInteractiveMode(new InteractiveReportPlayer(command));
    }

    @Override
    public void doDefaultAction() throws CivException {
        showHelp();
    }

    @Override
    public void showHelp() {
        showBasicHelp();
    }

    @Override
    public void permissionCheck() throws CivException {

    }

}
