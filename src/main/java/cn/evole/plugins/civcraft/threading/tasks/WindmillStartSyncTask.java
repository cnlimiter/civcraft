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

import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.structure.Windmill;
import cn.evole.plugins.civcraft.structure.farm.FarmChunk;
import cn.evole.plugins.civcraft.threading.TaskMaster;
import cn.evole.plugins.civcraft.util.ChunkCoord;
import org.bukkit.ChunkSnapshot;

import java.util.ArrayList;

public class WindmillStartSyncTask implements Runnable {

    Windmill windmill;

    public WindmillStartSyncTask(Windmill windmill) {
        this.windmill = windmill;
    }

    @Override
    public void run() {
        /* Find adjacent farms, get their chunk snapshots and continue processing in our thread. */
        ChunkCoord cc = new ChunkCoord(windmill.getCorner());
        ArrayList<ChunkSnapshot> snapshots = new ArrayList<ChunkSnapshot>();

        int[][] offset = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}, {1, 1}, {-1, -1}, {-1, 1}, {1, -1}};
        for (int i = 0; i < 8; i++) {
            cc.setX(cc.getX() + offset[i][0]);
            cc.setZ(cc.getZ() + offset[i][1]);

            FarmChunk farmChunk = CivGlobal.getFarmChunk(cc);
            if (farmChunk != null) {
                snapshots.add(farmChunk.getChunk().getChunkSnapshot());
            }

            cc.setFromLocation(windmill.getCorner().getLocation());
        }


        if (snapshots.size() == 0) {
            return;
        }

        /* Fire off an async task to do some post processing. */
        TaskMaster.asyncTask("", new WindmillPreProcessTask(windmill, snapshots), 0);

    }

}
