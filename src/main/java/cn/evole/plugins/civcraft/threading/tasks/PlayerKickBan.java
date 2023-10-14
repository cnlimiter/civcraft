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
import cn.evole.plugins.civcraft.object.Resident;
import org.bukkit.entity.Player;

public class PlayerKickBan implements Runnable {

    String name;
    boolean kick;
    boolean ban;
    String reason;

    public PlayerKickBan(String name, boolean kick, boolean ban, String reason) {
        this.name = name;
        this.kick = kick;
        this.ban = ban;
        this.reason = reason;
    }

    @Override
    public void run() {
        Player player;
        try {
            player = CivGlobal.getPlayer(name);
        } catch (CivException e) {
            return;
        }

        if (ban) {
            Resident resident = CivGlobal.getResident(player);
            resident.setBanned(true);
            resident.save();
        }

        if (kick) {
            player.kickPlayer(reason);
        }
    }

}
