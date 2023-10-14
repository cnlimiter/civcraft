package cn.evole.plugins.civcraft.interactive;

import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.exception.CivException;
import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.main.CivMessage;
import cn.evole.plugins.civcraft.object.Report;
import cn.evole.plugins.civcraft.object.Resident;
import cn.evole.plugins.civcraft.util.CivColor;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;

public class InteractiveReportBug
        implements InteractiveResponse {
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
        Report report = new Report(resident.getName(), message, resident.getName(), true);
        try {
            report.saveNow();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        CivGlobal.addReport(report);
        CivMessage.sendSuccess((CommandSender) player, CivSettings.localize.localizedString("var_interactive_reportbug_success", CivColor.Red + message + CivColor.RESET, "§b" + report.getId()));
        resident.clearInteractiveMode();
    }
}

