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


import cn.evole.plugins.civcraft.threading.TaskMaster;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;

public class ChunkGenerateTask implements Runnable {

    int startX;
    int startZ;
    int stopX;
    int stopZ;

    public ChunkGenerateTask(int startx, int startz, int stopx, int stopz) {
        this.startX = startx;
        this.startZ = startz;
        this.stopX = stopx;
        this.stopZ = stopz;
    }

    @Override
    public void run() {

        int maxgen = 10;
        int i = 0;

        for (int x = startX; x <= stopX; x++) {
            for (int z = startZ; z <= stopZ; z++) {
                i++;

                Chunk chunk = Bukkit.getWorld("world").getChunkAt(x, z);
                if (!chunk.load(true)) {
                }

                if (!chunk.unload(true)) {
                }

                if (i > maxgen) {
                    TaskMaster.syncTask(new ChunkGenerateTask(x, z, stopX, stopZ));
                    return;
                }

            }
        }


    }


}
