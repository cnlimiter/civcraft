/**
 * [2023] Cnlimiter LLC
 * All Rights Reserved.
 * Name: civcraft
 * Author: cnlimiter
 * UpdateTime: 2023/10/14 16:15
 * Description:
 * License: ARR
 */
package cn.evole.plugins.civcraft.interactive;

import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.exception.CivException;
import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.main.CivMessage;
import cn.evole.plugins.civcraft.object.Resident;
import cn.evole.plugins.civcraft.object.Town;
import cn.evole.plugins.civcraft.structure.Buildable;
import cn.evole.plugins.civcraft.structure.wonders.Wonder;
import cn.evole.plugins.civcraft.template.Template;
import cn.evole.plugins.civcraft.threading.TaskMaster;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class InteractiveBuildCommand implements InteractiveResponse {

    Town town;
    Buildable buildable;
    Location center;
    Template tpl;

    public InteractiveBuildCommand(Town town, Buildable buildable, Location center, Template tpl) {
        this.town = town;
        this.buildable = buildable;
        this.center = center.clone();
        this.tpl = tpl;
    }

    @Override
    public void respond(String message, Resident resident) {
        Player player;
        try {
            player = CivGlobal.getPlayer(resident);
        } catch (CivException e) {
            return;
        }

        if (!"yes".equalsIgnoreCase(message)) {
            CivMessage.sendError(player, CivSettings.localize.localizedString("interactive_build_cancel"));
            resident.clearInteractiveMode();
            resident.undoPreview();
            return;
        }


        if (!buildable.validated) {
            CivMessage.sendError(player, CivSettings.localize.localizedString("interactive_build_invalid"));
            return;
        }

        if (!buildable.isValid() && !player.isOp()) {
            CivMessage.sendError(player, CivSettings.localize.localizedString("interactive_build_invalidNotOP"));
            return;
        }

        class SyncTask implements Runnable {
            Resident resident;

            public SyncTask(Resident resident) {
                this.resident = resident;
            }

            @Override
            public void run() {
                Player player;
                try {
                    player = CivGlobal.getPlayer(resident);
                } catch (CivException e) {
                    return;
                }

                try {
                    if (buildable instanceof Wonder) {
                        town.buildWonder(player, buildable.getConfigId(), center, tpl);
                    } else {
                        town.buildStructure(player, buildable.getConfigId(), center, tpl);
                    }
                    resident.clearInteractiveMode();
                } catch (CivException e) {
                    CivMessage.sendError(player, e.getMessage());
                }
            }
        }

        TaskMaster.syncTask(new SyncTask(resident));

    }

}
