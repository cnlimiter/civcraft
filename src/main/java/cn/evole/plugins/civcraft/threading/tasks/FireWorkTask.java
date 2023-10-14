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

import cn.evole.plugins.civcraft.util.FireworkEffectPlayer;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.World;

public class FireWorkTask implements Runnable {

    FireworkEffectPlayer fplayer = new FireworkEffectPlayer();
    FireworkEffect fe;
    int repeats;
    World world;
    Location loc;

    public FireWorkTask(FireworkEffect fe, World world, Location loc, int repeats) {
        this.fe = fe;
        this.repeats = repeats;
        this.world = world;
        this.loc = loc;
    }

    @Override
    public void run() {
        for (int i = 0; i < repeats; i++) {
            try {
                fplayer.playFirework(world, loc, fe);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

}
