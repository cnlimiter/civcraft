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


import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.exception.CivException;
import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.object.Resident;
import cn.evole.plugins.civcraft.template.Template;
import cn.evole.plugins.civcraft.threading.CivAsyncTask;
import cn.evole.plugins.civcraft.util.BlockCoord;
import cn.evole.plugins.civcraft.util.ItemManager;
import cn.evole.plugins.civcraft.util.SimpleBlock;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;


public class BuildPreviewAsyncTask extends CivAsyncTask {
    /*
     * This task slow-builds a struct block-by-block based on the
     * town's hammer rate. This task is per-structure building and will
     * use the CivAsynTask interface to send synchronous requests to the main
     * thread to build individual blocks.
     */

    public Template tpl;
    public Block centerBlock;
    public UUID playerUUID;
    public Boolean aborted = false;
    public ReentrantLock lock = new ReentrantLock();
    private int blocksPerTick;
    private int speed;
    private Resident resident;

    public BuildPreviewAsyncTask(Template t, Block center, UUID playerUUID) {
        tpl = t;
        centerBlock = center;
        this.playerUUID = playerUUID;
        resident = CivGlobal.getResidentViaUUID(playerUUID);
        //this.blocksPerTick = getBlocksPerTick();
        //this.speed = getBuildSpeed();
        this.blocksPerTick = 100;
        this.speed = 600;
    }

    public Player getPlayer() throws CivException {
        Player player = Bukkit.getPlayer(playerUUID);
        if (player == null) {
            throw new CivException("Player offline");
        }
        return player;
    }

    @Override
    public void run() {

        try {
            int count = 0;

            for (int y = 0; y < tpl.size_y; y++) {
                for (int x = 0; x < tpl.size_x; x++) {
                    for (int z = 0; z < tpl.size_z; z++) {
                        Block b = centerBlock.getRelative(x, y, z);

                        if (tpl.blocks[x][y][z].isAir()) {
                            continue;
                        }

                        lock.lock();
                        try {
                            if (aborted) {
                                return;
                            }

                            ItemManager.sendBlockChange(getPlayer(), b.getLocation(), ItemManager.getId(CivSettings.previewMaterial), 5);
                            resident.previewUndo.put(new BlockCoord(b.getLocation()),
                                    new SimpleBlock(ItemManager.getId(b), ItemManager.getData(b)));
                            count++;
                        } finally {
                            lock.unlock();
                        }


                        if (count < blocksPerTick) {
                            continue;
                        }

                        count = 0;
                        int timeleft = speed;
                        while (timeleft > 0) {
                            int min = Math.min(10000, timeleft);
                            Thread.sleep(min);
                            timeleft -= 10000;
                        }
                    }
                }
            }
        } catch (CivException | InterruptedException e) {
            //abort task.
        }
    }


}