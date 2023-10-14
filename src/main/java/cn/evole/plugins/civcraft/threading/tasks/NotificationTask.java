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
import org.bukkit.entity.Player;

public class NotificationTask implements Runnable {
    //private Server server;
    String message;
    String playerName;

    public NotificationTask(String playerName, String msg) {
        message = msg;
        this.playerName = playerName;
    }

    @Override
    public void run() {
        try {
            Player player = CivGlobal.getPlayer(playerName);
            CivMessage.send(player, message);
        } catch (CivException e) {
            //Player not online
        }

    }
}