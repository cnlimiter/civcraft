/**
 * [2023] Cnlimiter LLC
 * All Rights Reserved.
 * Name: civcraft
 * Author: cnlimiter
 * UpdateTime: 2023/10/14 16:15
 * Description:
 * License: ARR
 */
package cn.evole.plugins.civcraft.threading.timers;

import cn.evole.plugins.civcraft.cache.PlayerLocationCache;
import cn.evole.plugins.civcraft.exception.CivException;
import cn.evole.plugins.civcraft.main.CivGlobal;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import java.util.LinkedList;
import java.util.Queue;

public class PlayerLocationCacheUpdate implements Runnable {

    public static int UPDATE_LIMIT = 20;
    public static Queue<String> playerQueue = new LinkedList<String>();

    @Override
    public void run() {

        //	if (PlayerLocationCache.lock.tryLock()) {
        try {
            for (int i = 0; i < UPDATE_LIMIT; i++) {
                String playerName = playerQueue.poll();
                if (playerName == null) {
                    return;
                }

                try {
                    Player player = CivGlobal.getPlayer(playerName);
                    if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) {
                        //Don't leave creative or Spectator players in the cache.
                        PlayerLocationCache.remove(playerName);
                        continue;
                    }
                    PlayerLocationCache.updateLocation(player);
                    playerQueue.add(playerName);

                } catch (CivException e) {
                    // player not online. remove from queue by not re-adding.
                    PlayerLocationCache.remove(playerName);
                    continue;
                }
            }
        } finally {
            //	PlayerLocationCache.lock.unlock();
        }
        //}
    }

}
