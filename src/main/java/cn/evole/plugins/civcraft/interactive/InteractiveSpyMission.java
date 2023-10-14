package cn.evole.plugins.civcraft.interactive;

import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.config.ConfigMission;
import cn.evole.plugins.civcraft.exception.CivException;
import cn.evole.plugins.civcraft.items.units.MissionBook;
import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.main.CivMessage;
import cn.evole.plugins.civcraft.object.Resident;
import cn.evole.plugins.civcraft.object.Town;
import cn.evole.plugins.civcraft.threading.TaskMaster;
import cn.evole.plugins.civcraft.threading.tasks.EspionageMissionTask;
import cn.evole.plugins.civcraft.util.CivColor;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;

public class InteractiveSpyMission implements InteractiveResponse {

    public ConfigMission mission;
    public String playerName;
    public Location playerLocation;
    public Town target;

    public InteractiveSpyMission(ConfigMission mission, String playerName, Location playerLocation, Town target) {
        this.mission = mission;
        this.playerName = playerName;
        this.playerLocation = playerLocation;
        this.target = target;
        displayQuestion();
    }

    public void displayQuestion() {
        Player player;
        try {
            player = CivGlobal.getPlayer(playerName);
        } catch (CivException e) {
            return;
        }

        CivMessage.sendHeading(player, CivSettings.localize.localizedString("interactive_spy_heading") + " " + mission.name);

        double failChance = MissionBook.getMissionFailChance(mission, target);
        double compChance = MissionBook.getMissionCompromiseChance(mission, target);
        DecimalFormat df = new DecimalFormat();

        String successChance = df.format((1 - failChance) * 100) + "%";
        String compromiseChance = df.format(compChance) + "%";
        String length = "";

        int mins = mission.length / 60;
        int seconds = mission.length % 60;
        if (mins > 0) {
            length += CivSettings.localize.localizedString("var_interactive_spy_mins", mins);
            if (seconds > 0) {
                length += " & ";
            }
        }

        if (seconds > 0) {
            length += CivSettings.localize.localizedString("var_interactive_spy_seconds", seconds);
        }

        CivMessage.send(player, CivColor.Green + CivColor.BOLD + CivSettings.localize.localizedString("var_interactive_spy_prompt1", CivColor.LightGreen + successChance + CivColor.Green + CivColor.BOLD));
        CivMessage.send(player, CivColor.Green + CivColor.BOLD + CivSettings.localize.localizedString("var_interactive_spy_prompt2", CivColor.LightGreen + compromiseChance + CivColor.Green + CivColor.BOLD));
        CivMessage.send(player, CivColor.Green + CivColor.BOLD + CivSettings.localize.localizedString("var_interactive_spy_prompt3", CivColor.Yellow + mission.cost + CivColor.Green + CivColor.BOLD, CivSettings.CURRENCY_NAME));
        CivMessage.send(player, CivColor.Green + CivColor.BOLD + CivSettings.localize.localizedString("var_interactive_spy_prompt4", CivColor.Yellow + length + CivColor.Green + CivColor.BOLD));
        CivMessage.send(player, CivColor.Green + CivColor.BOLD + CivSettings.localize.localizedString("interactive_spy_prompt5"));
        CivMessage.send(player, CivColor.Green + CivColor.BOLD + CivSettings.localize.localizedString("interactive_spy_prompt6"));
        CivMessage.send(player, CivColor.Green + ChatColor.BOLD + CivSettings.localize.localizedString("interactive_spy_prompt7"));
    }


    @Override
    public void respond(String message, Resident resident) {
        Player player;
        try {
            player = CivGlobal.getPlayer(resident);
        } catch (CivException e) {
            return;
        }
        resident.clearInteractiveMode();

        if (!message.equalsIgnoreCase("yes")) {
            CivMessage.sendError(player, CivSettings.localize.localizedString("interactive_spy_aborted"));
            return;
        }

        if (!TaskMaster.hasTask("missiondelay:" + playerName)) {
            TaskMaster.asyncTask("missiondelay:" + playerName, (new EspionageMissionTask(mission, playerName, playerLocation, target, mission.length)), 0);
        } else {
            CivMessage.sendError(player, CivSettings.localize.localizedString("interactive_spy_waiting"));
            return;
        }
    }
}
