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
import cn.evole.plugins.civcraft.structure.ScoutShip;
import cn.evole.plugins.civcraft.structure.ScoutTower;
import cn.evole.plugins.civcraft.structure.Structure;
import cn.evole.plugins.civcraft.util.BlockCoord;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;

public class ScoutTowerTask implements Runnable {
    @Override
    public void run() {
        HashSet<String> announced = new HashSet<String>();

        try {
            if (!CivGlobal.towersEnabled) {
                return;
            }

            Iterator<Entry<BlockCoord, Structure>> iter = CivGlobal.getStructureIterator();
            while (iter.hasNext()) {
                Structure struct = iter.next().getValue();
                if (struct instanceof ScoutTower) {
                    ((ScoutTower) struct).process(announced);
                } else if (struct instanceof ScoutShip) {
                    ((ScoutShip) struct).process(announced);
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
