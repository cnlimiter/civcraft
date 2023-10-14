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

import cn.evole.plugins.civcraft.exception.CivException;
import cn.evole.plugins.civcraft.main.CivLog;
import cn.evole.plugins.civcraft.threading.sync.request.UpdateInventoryRequest;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.ReentrantLock;

public class SyncUpdateInventory implements Runnable {

    //	public static final int QUEUE_SIZE = 4096;
    public static final int UPDATE_LIMIT = 200;

    /*
     * Performs the desired action on a provided multi-inventory.
     */
    public static Queue<UpdateInventoryRequest> requestQueue = new LinkedList<UpdateInventoryRequest>();
    public static ReentrantLock lock;

    public SyncUpdateInventory() {
        lock = new ReentrantLock();
    }

    @Override
    public void run() {

        Boolean retBool = false;
        if (lock.tryLock()) {
            try {
                for (int i = 0; i < UPDATE_LIMIT; i++) {
                    UpdateInventoryRequest request = requestQueue.poll();
                    if (request == null) {
                        return;
                    }


                    switch (request.action) {
                        case ADD:
                            int leftovers = request.multiInv.addItem(request.stack);
                            retBool = !(leftovers > 0);
                            break;
                        case REMOVE:
                            try {
                                retBool = request.multiInv.removeItem(request.stack, true);
                            } catch (CivException e) {
                                e.printStackTrace();
                            }
                            break;
                        case SET:
                            retBool = true;
                            request.inv.setContents(request.cont);
                            break;
                        case REPLACE:
                            retBool = true;
                            request.inv.setItem(request.index, request.stack);
                            break;
                    }

                    request.result = retBool;
                    request.finished = true;
                    request.condition.signalAll();
                }
            } finally {
                lock.unlock();
            }
        } else {
            CivLog.warning("Sync update inventory lock is busy, trying again next tick");
        }
    }
}
