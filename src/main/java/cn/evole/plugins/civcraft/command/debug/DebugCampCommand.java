/**
 * [2023] Cnlimiter LLC
 * All Rights Reserved.
 * Name: civcraft
 * Author: cnlimiter
 * UpdateTime: 2023/10/14 16:15
 * Description:
 * License: ARR
 */
package cn.evole.plugins.civcraft.command.debug;

import cn.evole.plugins.civcraft.camp.Camp;
import cn.evole.plugins.civcraft.command.CommandBase;
import cn.evole.plugins.civcraft.exception.CivException;
import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.main.CivMessage;
import cn.evole.plugins.civcraft.object.Resident;
import cn.evole.plugins.civcraft.util.BlockCoord;

public class DebugCampCommand extends CommandBase {

    @Override
    public void init() {
        command = "/dbg test ";
        displayName = "Test Commands";

        commands.put("growth", "[name] - Shows a list of this player's camp growth spots.");

    }

    public void growth_cmd() throws CivException {
        Resident resident = getNamedResident(1);

        if (!resident.hasCamp()) {
            throw new CivException("这家伙没有营地.");
        }

        Camp camp = resident.getCamp();

        CivMessage.sendHeading(sender, "Growth locations");

        String out = "";
        for (BlockCoord coord : camp.growthLocations) {
            boolean inGlobal = CivGlobal.vanillaGrowthLocations.contains(coord);
            out += coord.toString() + " in global:" + inGlobal;
        }

        CivMessage.send(sender, out);

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
