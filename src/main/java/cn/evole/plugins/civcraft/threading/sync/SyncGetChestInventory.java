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
import cn.evole.plugins.civcraft.main.CivLog;
import cn.evole.plugins.civcraft.threading.sync.request.GetChestRequest;
import cn.evole.plugins.civcraft.util.ItemManager;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReentrantLock;

public class SyncGetChestInventory implements Runnable {

    public static final int TIMEOUT_SECONDS = 2;
    public static final int UPDATE_LIMIT = 250;

    public static ReentrantLock lock = new ReentrantLock();
    public static ConcurrentLinkedQueue<GetChestRequest> requestQueue =
            new ConcurrentLinkedQueue<>();
    private Object synchronizer = new Object();

    public SyncGetChestInventory() {
    }

    public static boolean add(GetChestRequest request) {
        return requestQueue.add(request);
    }

    @Override
    public void run() {
        if (lock.tryLock()) {
            try {
                for (int i = 0; i < UPDATE_LIMIT; i++) {
                    GetChestRequest request = requestQueue.poll();
                    if (request == null) {
                        continue;
                    }
                    Chest chest = null;
                    //Bukkit.getWorld(request.worldName).getChunkAt(request.block_x,request.block_y).isLoaded()
                    if (Bukkit.getWorld(request.worldName).getChunkAt(request.block_x, request.block_y).isLoaded()) {
                        Block b = Bukkit.getWorld(request.worldName).getBlockAt(request.block_x, request.block_y, request.block_z);
                        try {
                            chest = (Chest) b.getState();
                        } catch (ClassCastException e) {
                            ItemManager.setTypeId(b, CivData.CHEST);
                            ItemManager.setTypeId(b.getState(), CivData.CHEST);
                            b.getState().update();
                            chest = (Chest) b.getState();
                        }
                    } else {
                        CivLog.info("chunk is unload");
                    }
                    request.result = chest.getBlockInventory();
                    request.finished = true;
                    request.condition.signalAll();
                }
            } finally {
                lock.unlock();
            }
        } else {
            CivLog.warning("SyncGetChestInventory: lock was busy, try again next tick.");
        }
    }

}
