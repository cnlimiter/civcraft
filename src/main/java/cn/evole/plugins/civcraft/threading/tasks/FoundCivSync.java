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

import cn.evole.plugins.civcraft.exception.CivException;
import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.main.CivMessage;
import cn.evole.plugins.civcraft.object.Civilization;
import cn.evole.plugins.civcraft.object.Resident;
import cn.evole.plugins.civcraft.util.CivColor;
import org.bukkit.entity.Player;

public class FoundCivSync implements Runnable {

    Resident resident;

    public FoundCivSync(Resident resident) {
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
            Civilization.newCiv(resident.desiredCivName, resident.desiredCapitolName, resident, player, resident.desiredTownLocation);
        } catch (CivException e) {
            CivMessage.send(player, CivColor.Rose + e.getMessage());
        }

    }


}
