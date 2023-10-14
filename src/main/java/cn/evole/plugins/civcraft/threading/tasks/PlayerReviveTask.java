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
import cn.evole.plugins.civcraft.main.CivMessage;
import cn.evole.plugins.civcraft.structure.TownHall;
import cn.evole.plugins.civcraft.util.BlockCoord;
import cn.evole.plugins.civcraft.util.CivColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class PlayerReviveTask implements Runnable {

    String playerName;
    int timeout;
    TownHall townhall;
    Location alternativeLocation;

    public PlayerReviveTask(Player player, int timeout, TownHall townhall, Location alt) {
        this.playerName = player.getName();
        this.timeout = timeout;
        this.townhall = townhall;
        this.alternativeLocation = alt;
    }

    public void setRespawnViaSessionDB() {
        //Player was logged out when the respawn event fired. Create a sessionDB entry
        //to respawn the player when they login.
        BlockCoord revive = townhall.getRandomRevivePoint();
        CivGlobal.getSessionDB().add("global:respawnPlayer", playerName + ":" + revive.toString(), 0, 0, 0);
    }

    @Override
    public void run() {

        Player player;
        try {
            player = CivGlobal.getPlayer(playerName);
        } catch (CivException e1) {
            setRespawnViaSessionDB();
            return;
        }

        CivMessage.send(player, CivColor.LightGray + CivSettings.localize.localizedString("var_PlayerReviveTask_repawnIn", timeout));

        try {
            synchronized (this) {
                this.wait(timeout * 1000);
            }
        } catch (InterruptedException e) {
        }

        try {
            player = CivGlobal.getPlayer(playerName);
        } catch (CivException e1) {
            setRespawnViaSessionDB();
            return;
        }

        BlockCoord revive = townhall.getRandomRevivePoint();
        Location loc;
        if (revive == null) {
            loc = alternativeLocation;
        } else {
            loc = revive.getLocation();
        }

        CivMessage.send(player, CivColor.LightGreen + CivSettings.localize.localizedString("PlayerReviveTask_respawning"));

        try {
            synchronized (this) {
                this.wait(500);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        player.teleport(loc);

    }


}
