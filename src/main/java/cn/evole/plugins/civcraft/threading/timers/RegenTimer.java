package cn.evole.plugins.civcraft.threading.timers;

import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.structure.Structure;
import cn.evole.plugins.civcraft.structure.wonders.Wonder;
import cn.evole.plugins.civcraft.util.BlockCoord;

import java.util.Iterator;
import java.util.Map.Entry;

/**
 * 5s一次
 * 大概是建筑回血的亚子
 */
public class RegenTimer implements Runnable {

    @Override
    public void run() {
        Iterator<Entry<BlockCoord, Structure>> iter = CivGlobal.getStructureIterator();

        while (iter.hasNext()) {
            Structure struct = iter.next().getValue();
            struct.processRegen();
        }

        for (Wonder wonder : CivGlobal.getWonders()) {
            wonder.processRegen();
        }
    }

}
