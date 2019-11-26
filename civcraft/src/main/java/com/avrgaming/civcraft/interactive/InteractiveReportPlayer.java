package com.avrgaming.civcraft.interactive;

import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.exception.CivException;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.object.Report;
import com.avrgaming.civcraft.object.Resident;
import com.avrgaming.civcraft.util.CivColor;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;

public class InteractiveReportPlayer implements InteractiveResponse {

    String playerName;

    public InteractiveReportPlayer(String playerName) {
        this.playerName = playerName;
    }

    @Override
    public void respond(String message, Resident resident) {
        Player player;
        try {
            player = CivGlobal.getPlayer(resident);
        } catch (CivException e) {
            return;
        }
        if (message.equalsIgnoreCase("cancel")) {
            CivMessage.send((Object) player, "§a" + (Object) ChatColor.BOLD + CivSettings.localize.localizedString("interactive_report_cancel"));
            resident.clearInteractiveMode();
            return;
        }
        if (message.contains("http")) {
            message = message.replace(" ", ".");
        }
        Report report = new Report(resident.getDesiredReportPlayerName(), message, resident.getName(), false);
        try {
            report.saveNow();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        CivGlobal.addReport(report);
        CivMessage.sendSuccess((CommandSender) player, CivSettings.localize.localizedString("var_interactive_report_success", resident.getDesiredReportPlayerName(), CivColor.Red + message + CivColor.RESET, "§b" + report.getId()));
        resident.clearInteractiveMode();
    }

}
