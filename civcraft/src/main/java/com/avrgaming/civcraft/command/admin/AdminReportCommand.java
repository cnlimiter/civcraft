
package com.avrgaming.civcraft.command.admin;

import java.text.SimpleDateFormat;
import org.bukkit.Bukkit;
import com.avrgaming.civcraft.command.CommandBase;
import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.exception.CivException;
import com.avrgaming.civcraft.object.Report;
import com.avrgaming.civcraft.object.Resident;
import com.avrgaming.civcraft.util.CivColor;

public class AdminReportCommand
extends CommandBase {
    @Override
    public void init() {
        this.command = "/ad report";
        this.displayName = CivSettings.localize.localizedString("adcmd_report_name");
        this.commands.put("buglist", CivSettings.localize.localizedString("adcmd_report_buglistDesc", Bukkit.getServer().getServerName()));
        this.commands.put("playerlist", CivSettings.localize.localizedString("adcmd_report_playerlistDesc", Bukkit.getServer().getServerName()));
        this.commands.put("close", CivSettings.localize.localizedString("adcmd_report_closelistDesc", Bukkit.getServer().getServerName()));
    }

    public void close_cmd() throws CivException {
        if (this.args.length < 2) {
            throw new CivException(CivColor.Red + CivSettings.localize.localizedString("adcmd_report_close_enterID"));
        }
        Integer reportID = Integer.parseInt(this.args[1]);
        Report report = CivGlobal.getReportById(reportID);
        if (report == null) {
            throw new CivException(CivColor.Red + CivSettings.localize.localizedString("adcmd_report_close_unkReport", reportID));
        }
        if (report.getClosed()) {
            Object[] arrobject = new Object[2];
            arrobject[0] = reportID;
            arrobject[1] = report.getBug() ? "bug" : "player";
            throw new CivException(CivColor.Red + CivSettings.localize.localizedString("adcmd_report_close_closedReport", arrobject));
        }
        if (this.args.length < 3) {
            throw new CivException(CivColor.Red + CivSettings.localize.localizedString("adcmd_report_close_enterArgs"));
        }
        StringBuilder messages = new StringBuilder();
        for (int i = 2; i < this.args.length; ++i) {
            messages.append(this.args[i]).append(i == this.args.length - 1 ? "" : " ");
        }
        report.close(this.sender.getName(), messages.toString());
        CivMessage.sendSuccess(this.sender, CivColor.Red + CivSettings.localize.localizedString("adcmd_report_close_succusess", reportID, messages.toString()));
        Resident senderResident = CivGlobal.getResident(report.getReportedBy());
        senderResident.setReportChecked(true);
        senderResident.setReportResult(this.sender.getName() + "///" + messages + "///" + report.getCloseTime());
        senderResident.save();
        CivMessage.sendSuccess(senderResident, CivColor.BOLD + CivColor.UNDERLINE + senderResident.getReportResult().split("///")[0] + " responded to your report. Use '/res report' for more infomation.");
    }

    public void buglist_cmd() {
		SimpleDateFormat sdf = new SimpleDateFormat("M/dd/yy h:mm:ss a z");
        CivMessage.sendHeading(this.sender, "Server Bugs " + Bukkit.getServerName());
        for (Report report : CivGlobal.getReports()) {
            if (report.getClosed() || !report.getBug()) continue;
            String message = CivColor.LightGray + "(" + report.getId() + ") " + "§a" + "Reporter: " + CivColor.Red + report.getReportedBy() + " " + "§d" + "Time: " + sdf.format(report.getTime()) + " " + "§b" + "Proof: " + report.getProof();
            CivMessage.send((Object)this.sender, message);
        }
    }

    public void playerlist_cmd() {
		SimpleDateFormat sdf = new SimpleDateFormat("M/dd/yy h:mm:ss a z");
        CivMessage.sendHeading(this.sender, "Player Reports " + Bukkit.getServerName());
        for (Report report : CivGlobal.getReports()) {
            if (report.getClosed() || report.getBug()) continue;
            String message = CivColor.LightGray + "(" + report.getId() + ") " + "§a" + "Reporter: " + CivColor.Red + report.getReportedBy() + " " + "§d" + "Time: " + sdf.format(report.getTime()) + " " + "§b" + "Proof: " + report.getProof() + " " + "§2" + "Player: " + report.getCause();
            CivMessage.send((Object)this.sender, message);
        }
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
    }
}

