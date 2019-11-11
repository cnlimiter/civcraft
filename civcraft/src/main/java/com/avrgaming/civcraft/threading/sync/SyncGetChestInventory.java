/*************************************************************************
 *
 * AVRGAMING LLC
 * __________________
 *
 *  [2013] AVRGAMING LLC
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of AVRGAMING LLC and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to AVRGAMING LLC
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from AVRGAMING LLC.
 */
package com.avrgaming.civcraft.threading.sync;

import com.avrgaming.civcraft.main.CivData;
import com.avrgaming.civcraft.threading.sync.request.GetChestRequest;
import com.avrgaming.civcraft.util.ItemManager;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.craftbukkit.v1_11_R1.CraftWorld;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReentrantLock;

public class SyncGetChestInventory implements Runnable {

    public static final int TIMEOUT_SECONDS = 2;
    public static final int UPDATE_LIMIT = 250;

    public static ReentrantLock lock;
    private Object synchronizer = new Object();

    public static ConcurrentLinkedQueue<GetChestRequest> requestQueue =
            new ConcurrentLinkedQueue<>();

    public static boolean add(GetChestRequest request) {
        return requestQueue.add(request);
    }

    public SyncGetChestInventory() {
        lock = new ReentrantLock();
    }

    @Override
    public void run() {
        for (int i = 0; i < UPDATE_LIMIT; i++) {
            GetChestRequest request = requestQueue.poll();
            if (request == null) {
                continue;
            }

            Chest chest = null;

            if (((CraftWorld) Bukkit.getWorld(request.worldName)).isChunkLoaded(request.block_x >> 4, request.block_z >> 4)) {
                Block b = Bukkit.getWorld(request.worldName).getBlockAt(request.block_x, request.block_y, request.block_z);

                try {
                    chest = (Chest) b.getState();
                } catch (ClassCastException e) {
                    ItemManager.setTypeId(b, CivData.CHEST);
                    ItemManager.setTypeId(b.getState(), CivData.CHEST);
                    b.getState().update();
                    chest = (Chest) b.getState();
                }
            }


            request.result = chest.getBlockInventory();
            request.finished = true;

            synchronized (synchronizer) {
                request.condition.signalAll();
            }
        }
    }
}
