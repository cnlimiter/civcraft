/**
 * [2023] Cnlimiter LLC
 * All Rights Reserved.
 * Name: civcraft
 * Author: cnlimiter
 * UpdateTime: 2023/10/14 16:15
 * Description:
 * License: ARR
 */
package cn.evole.plugins.civcraft.threading.tasks;

import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.exception.CivException;
import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.main.CivLog;
import cn.evole.plugins.civcraft.main.CivMessage;
import cn.evole.plugins.civcraft.object.Resident;
import cn.evole.plugins.civcraft.object.Town;
import cn.evole.plugins.civcraft.util.CivColor;
import org.bukkit.entity.Player;

import java.sql.SQLException;

public class FoundTownSync implements Runnable {

    Resident resident;

    public FoundTownSync(Resident resident) {
        this.resident = resident;
    }

    @Override
    public void run() {
        Player player;
        try {
            player = CivGlobal.getPlayer(resident);
        } catch (CivException e1) {
            return;
        }

        try {
            Town.newTown(resident, resident.desiredTownName, resident.getCiv(), false, false, resident.desiredTownLocation);
        } catch (CivException | SQLException e) {
            CivLog.error("Caught exception:" + e.getMessage());
            if (e.getMessage().contains("Duplicate entry")) {
                CivMessage.send(player, CivColor.Rose + CivSettings.localize.localizedString("town_found_databaseException"));
            } else {
                CivMessage.send(player, CivColor.Rose + e.getMessage());
            }
            return;
        }

        //CivMessage.sendSuccess(sender, "Town "+args[1]+" has been founded.");
        CivMessage.global(CivSettings.localize.localizedString("var_FoundTownSync_Success", resident.desiredTownName, resident.getCiv().getName()));
    }

}
