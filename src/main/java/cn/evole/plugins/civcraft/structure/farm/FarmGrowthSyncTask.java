/**
 * [2023] Cnlimiter LLC
 * All Rights Reserved.
 * Name: civcraft
 * Author: cnlimiter
 * UpdateTime: 2023/10/14 16:15
 * Description:
 * License: ARR
 */
package cn.evole.plugins.civcraft.structure.farm;

import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.threading.CivAsyncTask;

import java.util.concurrent.TimeUnit;

public class FarmGrowthSyncTask extends CivAsyncTask {

    // XXX Despite being named a sync timer, this task is now actually asynchronous

    public void processFarmChunks() {
        if (!CivGlobal.growthEnabled) {
            return;
        }

        //Queue<FarmChunk> regrow = new LinkedList<FarmChunk>();
        for (FarmChunk fc : CivGlobal.getFarmChunks()) {
            if (fc.getTown() == null || fc.getStruct() == null) {
                System.out.println("FarmChunkError: Could not process farm chunk, town or struct was null. Orphan?");
                continue;
            }

            /* Since we're now async, we can wait on this lock. */
            try {
                if (!fc.lock.tryLock(TIMEOUT, TimeUnit.MILLISECONDS)) {
                    System.out.println("FarmChunkError: Lock Error");
                    continue;
                }
            } catch (InterruptedException e1) {
                e1.printStackTrace();
                continue;
            }
            try {
                try {
                    fc.processGrowth(this);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    continue;
                }
            } finally {
                fc.lock.unlock();
            }
            //} else {
            //	regrow.add(fc);
            //}
        }

        //if (regrow.size() > 0) {
        //		TaskMaster.syncTask(new FarmGrowthRegrowTask(regrow), 0);
        //	}
    }


    @Override
    public void run() {
        try {
            processFarmChunks();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}