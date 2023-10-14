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

import cn.evole.plugins.civcraft.exception.CivException;
import cn.evole.plugins.civcraft.main.CivData;
import cn.evole.plugins.civcraft.structure.Windmill;
import cn.evole.plugins.civcraft.util.BlockCoord;
import cn.evole.plugins.civcraft.util.ItemManager;
import cn.evole.plugins.civcraft.util.MultiInventory;

import java.util.ArrayList;
import java.util.Random;

public class WindmillPostProcessSyncTask implements Runnable {

    ArrayList<BlockCoord> plantBlocks;
    Windmill windmill;
    int breadCount;
    int carrotCount;
    int potatoCount;
    MultiInventory source_inv;

    public WindmillPostProcessSyncTask(Windmill windmill, ArrayList<BlockCoord> plantBlocks,
                                       int breadCount, int carrotCount, int potatoCount, MultiInventory source_inv) {
        this.plantBlocks = plantBlocks;
        this.windmill = windmill;
        this.breadCount = breadCount;
        this.carrotCount = carrotCount;
        this.potatoCount = potatoCount;
        this.source_inv = source_inv;
    }

    @Override
    public void run() {
        Random rand = new Random();

        for (BlockCoord coord : plantBlocks) {

            int randomCropType = rand.nextInt(3);

            switch (randomCropType) {
                case 0:
                    if (breadCount > 0) {
                        /* bread seed */
                        try {
                            source_inv.removeItem(CivData.BREAD_SEED, 1, true);
                        } catch (CivException e) {
                            e.printStackTrace();
                        }
                        breadCount--;
                        ItemManager.setTypeId(coord.getBlock(), CivData.WHEAT);
                        ItemManager.setData(coord.getBlock(), 0, true);
                        continue;
                    }
                case 1:
                    if (carrotCount > 0) {
                        /* carrots */
                        try {
                            source_inv.removeItem(CivData.CARROT_ITEM, 1, true);
                        } catch (CivException e) {
                            e.printStackTrace();
                        }
                        carrotCount--;
                        ItemManager.setTypeId(coord.getBlock(), CivData.CARROTS);
                        ItemManager.setData(coord.getBlock(), 0, true);

                        continue;
                    }
                    break;
                case 2:
                    if (potatoCount > 0) {
                        /* potatoes */
                        try {
                            source_inv.removeItem(CivData.POTATO_ITEM, 1, true);
                        } catch (CivException e) {
                            e.printStackTrace();
                        }
                        potatoCount--;
                        ItemManager.setTypeId(coord.getBlock(), CivData.POTATOES);
                        ItemManager.setData(coord.getBlock(), 0, true);

                        continue;
                    }
            }

            /* our randomly selected crop couldn't be placed, try them all now. */
            if (breadCount > 0) {
                /* bread seed */
                try {
                    source_inv.removeItem(CivData.BREAD_SEED, 1, true);
                } catch (CivException e) {
                    e.printStackTrace();
                }
                breadCount--;
                ItemManager.setTypeId(coord.getBlock(), CivData.WHEAT);
                ItemManager.setData(coord.getBlock(), 0, true);

                continue;
            }
            if (carrotCount > 0) {
                /* carrots */
                try {
                    source_inv.removeItem(CivData.CARROT_ITEM, 1, true);
                } catch (CivException e) {
                    e.printStackTrace();
                }
                carrotCount--;
                ItemManager.setTypeId(coord.getBlock(), CivData.CARROTS);
                ItemManager.setData(coord.getBlock(), 0, true);

                continue;
            }
            if (potatoCount > 0) {
                /* potatoes */
                try {
                    source_inv.removeItem(CivData.POTATO_ITEM, 1, true);
                } catch (CivException e) {
                    e.printStackTrace();
                }
                potatoCount--;
                ItemManager.setTypeId(coord.getBlock(), CivData.POTATOES);
                ItemManager.setData(coord.getBlock(), 0, true);
                continue;
            }

        }

    }

}
