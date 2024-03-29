/**
 * [2023] Cnlimiter LLC
 * All Rights Reserved.
 * Name: civcraft
 * Author: cnlimiter
 * UpdateTime: 2023/10/14 16:15
 * Description:
 * License: ARR
 */
package cn.evole.plugins.civcraft.threading.sync;

import cn.evole.plugins.civcraft.main.CivData;
import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.main.CivLog;
import cn.evole.plugins.civcraft.structure.Farm;
import cn.evole.plugins.civcraft.structure.farm.FarmChunk;
import cn.evole.plugins.civcraft.structure.farm.GrowBlock;
import cn.evole.plugins.civcraft.threading.sync.request.GrowRequest;
import cn.evole.plugins.civcraft.util.ItemManager;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.ReentrantLock;

public class SyncGrowTask implements Runnable {

    public static final int UPDATE_LIMIT = 200;
    public static Queue<GrowRequest> requestQueue = new LinkedList<GrowRequest>();
    public static ReentrantLock lock;

    public SyncGrowTask() {
        lock = new ReentrantLock();
    }

    @Override
    public void run() {
        if (!CivGlobal.growthEnabled) {
            return;
        }

        HashSet<FarmChunk> unloadedFarms = new HashSet<FarmChunk>();

        if (lock.tryLock()) {
            try {
                for (int i = 0; i < UPDATE_LIMIT; i++) {
                    GrowRequest request = requestQueue.poll();
                    if (request == null) {
                        return;
                    }

                    if (request.farmChunk == null) {
                        request.result = false;
                    } else if (!request.farmChunk.getChunk().isLoaded()) {
                        // This farm's chunk isn't loaded so we can't update
                        // the crops. Add the missed growths to the farms to
                        // process later.
                        unloadedFarms.add(request.farmChunk);
                        request.result = false;

                    } else {

                        for (GrowBlock growBlock : request.growBlocks) {
                            switch (growBlock.typeId) {
                                case CivData.CARROTS:
                                case CivData.WHEAT:
                                case CivData.POTATOES:
                                    if ((growBlock.data - 1) != ItemManager.getData(growBlock.bcoord.getBlock())) {
                                        // replanted??
                                        continue;
                                    }
                                    break;
                            }

                            if (!growBlock.spawn && ItemManager.getId(growBlock.bcoord.getBlock()) != growBlock.typeId) {
                                continue;
                            } else {
                                if (growBlock.spawn) {
                                    // Only allow block to change its type if its marked as spawnable.
                                    ItemManager.setTypeId(growBlock.bcoord.getBlock(), growBlock.typeId);
                                }
                                ItemManager.setData(growBlock.bcoord.getBlock(), growBlock.data);
                                request.result = true;
                            }

                        }
                    }

                    request.finished = true;
                    request.condition.signalAll();
                }

                // increment any farms that were not loaded.
                for (FarmChunk fc : unloadedFarms) {
                    fc.incrementMissedGrowthTicks();
                    Farm farm = (Farm) fc.getStruct();
                    farm.saveMissedGrowths();
                }


            } finally {
                lock.unlock();
            }
        } else {
            CivLog.warning("SyncGrowTask: lock busy, retrying next tick.");
        }
    }
}

