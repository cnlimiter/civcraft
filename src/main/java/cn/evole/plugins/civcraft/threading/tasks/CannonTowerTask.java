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

import cn.evole.plugins.civcraft.main.CivGlobal;

public class CannonTowerTask implements Runnable {
    @Override
    public void run() {
        try {
            if (!CivGlobal.towersEnabled) {
                return;
            }
//			Iterator<Entry<BlockCoord, Structure>> iter = CivGlobal.getStructureIterator();
//			while(iter.hasNext()) {
//				Structure struct = iter.next().getValue();
//				if (struct instanceof CannonTower) {
//					((CannonTower)struct).process();
//				}
//				
//			}

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
