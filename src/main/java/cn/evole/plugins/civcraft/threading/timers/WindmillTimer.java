package cn.evole.plugins.civcraft.threading.timers;

import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.structure.Structure;
import cn.evole.plugins.civcraft.structure.Windmill;
import cn.evole.plugins.civcraft.util.BlockCoord;
import cn.evole.plugins.civcraft.war.War;

import java.util.Iterator;
import java.util.Map.Entry;

/**
 * 风车的定时任务
 */
public class WindmillTimer implements Runnable {

    @Override
    public void run() {
        if (War.isWarTime()) {
            return;
        }

        Iterator<Entry<BlockCoord, Structure>> iter = CivGlobal.getStructureIterator();
        while (iter.hasNext()) {
            Structure struct = iter.next().getValue();
            if (struct instanceof Windmill) {
                ((Windmill) struct).processWindmill();
            }
        }
    }

}
