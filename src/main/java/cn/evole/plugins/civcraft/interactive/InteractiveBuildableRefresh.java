package cn.evole.plugins.civcraft.interactive;

import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.exception.CivException;
import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.main.CivMessage;
import cn.evole.plugins.civcraft.object.Resident;
import cn.evole.plugins.civcraft.structure.Buildable;
import cn.evole.plugins.civcraft.threading.TaskMaster;
import cn.evole.plugins.civcraft.util.CivColor;
import org.bukkit.entity.Player;

import java.io.IOException;

public class InteractiveBuildableRefresh implements InteractiveResponse {

    String playerName;
    Buildable buildable;

    public InteractiveBuildableRefresh(Buildable buildable, String playerName) {
        this.playerName = playerName;
        this.buildable = buildable;
        displayMessage();
    }

    public void displayMessage() {
        Player player;
        try {
            player = CivGlobal.getPlayer(playerName);
        } catch (CivException e) {
            return;
        }

        CivMessage.sendHeading(player, CivSettings.localize.localizedString("interactive_refresh_Heading"));
        CivMessage.send(player, CivColor.LightGreen + CivColor.BOLD + CivSettings.localize.localizedString("var_interactive_refresh_prompt1", buildable.getDisplayName()));
        CivMessage.send(player, CivColor.LightGreen + CivColor.BOLD + CivSettings.localize.localizedString("interactive_refresh_prompt2"));
        CivMessage.send(player, CivColor.LightGreen + CivColor.BOLD + CivSettings.localize.localizedString("interactive_refresh_prompt3"));

    }


    @Override
    public void respond(String message, Resident resident) {
        resident.clearInteractiveMode();

        if (!"yes".equalsIgnoreCase(message)) {
            CivMessage.send(resident, CivColor.LightGray + CivSettings.localize.localizedString("interactive_refresh_cancel"));
            return;
        }

        class SyncTask implements Runnable {
            Buildable buildable;
            Resident resident;

            public SyncTask(Buildable buildable, Resident resident) {
                this.buildable = buildable;
                this.resident = resident;
            }

            @Override
            public void run() {
                try {
                    try {
                        buildable.repairFromTemplate();
                        buildable.getTown().markLastBuildableRefeshAsNow();
                        CivMessage.sendSuccess(resident, CivSettings.localize.localizedString("var_interactive_refresh_success", buildable.getDisplayName()));
                    } catch (IOException e) {
                        e.printStackTrace();
                        throw new CivException(CivSettings.localize.localizedString("interactive_refresh_exception") + " " + buildable.getSavedTemplatePath() + " ?");
                    }
                } catch (CivException e) {
                    CivMessage.sendError(resident, e.getMessage());
                }
            }
        }

        TaskMaster.syncTask(new SyncTask(buildable, resident));
    }
}
