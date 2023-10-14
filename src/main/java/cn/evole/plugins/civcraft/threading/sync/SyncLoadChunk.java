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

import cn.evole.plugins.civcraft.main.CivLog;
import cn.evole.plugins.civcraft.threading.sync.request.LoadChunkRequest;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.ReentrantLock;

public class SyncLoadChunk implements Runnable {
    /*
     * Loads a chunk synchronously and notifies the thread that was waiting on it when it is loaded.
     */
    public static final int UPDATE_LIMIT = 2048;

    public static Queue<LoadChunkRequest> requestQueue = new LinkedList<LoadChunkRequest>();
    public static ReentrantLock lock;

    public SyncLoadChunk() {
        lock = new ReentrantLock();
    }

    @Override
    public void run() {
        if (lock.tryLock()) {
            try {
                for (int i = 0; i < UPDATE_LIMIT; i++) {
                    LoadChunkRequest request = requestQueue.poll();
                    if (request == null) {
                        continue;
                    }

                    Chunk chunk = Bukkit.getWorld(request.worldName).getChunkAt(request.x, request.z);
                    if (!chunk.isLoaded()) {
                        if (!chunk.load()) {
                            CivLog.error("Couldn't load chunk at " + request.x + "," + request.z);
                            continue;
                        }
                    }

                    request.finished = true;
                    request.condition.signalAll();
                }
            } finally {
                lock.unlock();
            }

        } else {
            CivLog.warning("SyncLoadChunk: lock was busy, try again next tick.");
        }
    }


}
