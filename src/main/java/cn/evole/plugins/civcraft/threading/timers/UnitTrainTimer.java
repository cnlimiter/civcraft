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

import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.structure.Barracks;
import cn.evole.plugins.civcraft.structure.Structure;
import cn.evole.plugins.civcraft.util.BlockCoord;

import java.util.Iterator;
import java.util.Map.Entry;

/**
 * 1s一次
 * 军政处研究物品?
 */
public class UnitTrainTimer implements Runnable {

    @Override
    public void run() {

        Iterator<Entry<BlockCoord, Structure>> iter = CivGlobal.getStructureIterator();
        while (iter.hasNext()) {
            Structure struct = iter.next().getValue();
            if (struct instanceof Barracks) {
                ((Barracks) struct).updateTraining();
            }
        }
    }

}
