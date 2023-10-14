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

import cn.evole.plugins.civcraft.main.CivLog;
import cn.evole.plugins.civcraft.threading.CivAsyncTask;
import cn.evole.plugins.civcraft.threading.TaskMaster;

import java.util.LinkedList;
import java.util.Queue;

public class FarmGrowthRegrowTask extends CivAsyncTask {

    Queue<FarmChunk> farmsToGrow;


    public FarmGrowthRegrowTask(Queue<FarmChunk> farms) {
        this.farmsToGrow = farms;
    }

    @Override
    public void run() {

        Queue<FarmChunk> regrow = new LinkedList<FarmChunk>();
        CivLog.info("Regrowing " + farmsToGrow.size() + " farms due to locking failures.");

        FarmChunk fc;
        while ((fc = farmsToGrow.poll()) != null) {
            if (fc.lock.tryLock()) {
                try {
                    try {
                        fc.processGrowth(this);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        return;
                    }
                } finally {
                    fc.lock.unlock();
                }
            } else {
                regrow.add(fc);
            }
        }

        if (regrow.size() > 0) {
            TaskMaster.syncTask(new FarmGrowthRegrowTask(regrow));
        }
    }

}
