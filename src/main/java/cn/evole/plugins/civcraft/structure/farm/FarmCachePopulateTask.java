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

import java.util.LinkedList;


public class FarmCachePopulateTask implements Runnable {

    LinkedList<FarmChunk> farms;

    public FarmCachePopulateTask(LinkedList<FarmChunk> farms) {
        this.farms = farms;
    }

    @Override
    public void run() {
        if (!CivGlobal.growthEnabled) {
            return;
        }

        for (FarmChunk fc : farms) {
            try {
                fc.populateCropLocationCache();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

}
