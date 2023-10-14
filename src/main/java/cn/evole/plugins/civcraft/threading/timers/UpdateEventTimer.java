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

import cn.evole.plugins.civcraft.camp.Camp;
import cn.evole.plugins.civcraft.camp.CampUpdateTick;
import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.structure.Structure;
import cn.evole.plugins.civcraft.structure.wonders.Wonder;
import cn.evole.plugins.civcraft.threading.CivAsyncTask;
import cn.evole.plugins.civcraft.threading.TaskMaster;
import cn.evole.plugins.civcraft.threading.tasks.QuarryAsyncTask;
import cn.evole.plugins.civcraft.threading.tasks.TrommelAsyncTask;
import cn.evole.plugins.civcraft.util.BlockCoord;
import cn.hutool.core.util.StrUtil;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReentrantLock;

public class UpdateEventTimer extends CivAsyncTask {

    public static ReentrantLock lock = new ReentrantLock();

    public UpdateEventTimer() {
    }

    @Override
    public void run() {
        if (!lock.tryLock()) {
            return;
        }
        try {
            // Loop through each structure, if it has an update function call it in another async process
            Iterator<Entry<BlockCoord, Structure>> iter = CivGlobal.getStructureIterator();

            while (iter.hasNext()) {
                Structure struct = iter.next().getValue();

                if (!struct.isActive())
                    continue;

                try {
                    if (StrUtil.isNotBlank(struct.getUpdateEvent())) {
                        if ("trommel_process".equals(struct.getUpdateEvent())) {
                            if (!CivGlobal.trommelsEnabled) {
                                continue;
                            }

                            TaskMaster.asyncTask("trommel-" + struct.getCorner().toString(), new TrommelAsyncTask(struct), 0);
                        } else if ("quarry_process".equals(struct.getUpdateEvent())) {
                            if (!CivGlobal.quarriesEnabled) {
                                continue;
                            }
                            TaskMaster.asyncTask("quarry-" + struct.getCorner().toString(), new QuarryAsyncTask(struct), 0);
                        }
                    }


                    struct.onUpdate();
                } catch (Exception e) {
                    e.printStackTrace();
                    //We need to catch any exception so that an error in one town/structure/good does not
                    //break things for everybody.
                    //TODO log exception into a file or something...
                    //				if (struct.getTown() == null) {
                    //					RJ.logException("TownUnknown struct:"+struct.config.displayName, e);
                    //				} else {
                    //					RJ.logException(struct.town.getName()+":"+struct.config.displayName, e);
                    //				}
                }
            }

            for (Wonder wonder : CivGlobal.getWonders()) {
                wonder.onUpdate();
            }


            for (Camp camp : CivGlobal.getCamps()) {
                if (!camp.sifterLock.isLocked()) {
                    TaskMaster.asyncTask(new CampUpdateTick(camp), 0);
                }
            }

        } finally {
            lock.unlock();
        }

    }

}
