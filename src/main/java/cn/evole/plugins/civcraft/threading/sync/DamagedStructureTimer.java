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

import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.structure.Structure;
import cn.evole.plugins.civcraft.util.BlockCoord;
import org.bukkit.Effect;
import org.bukkit.World;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Random;

public class DamagedStructureTimer implements Runnable {

    @Override
    public void run() {

        Iterator<Entry<BlockCoord, Structure>> iter = CivGlobal.getStructureIterator();
        while (iter.hasNext()) {
            Structure struct = iter.next().getValue();

            if (struct.isDestroyed()) {
                int size = struct.getStructureBlocks().size();
                World world = struct.getCorner().getBlock().getWorld();

                for (int i = 0; i < size / 10; i++) {
                    Random rand = new Random();
                    int index = rand.nextInt(size);

                    // slower but uses less memory.
                    int j = 0;
                    for (BlockCoord coord : struct.getStructureBlocks().keySet()) {

                        if (j < index) {
                            j++;
                            continue;
                        }

                        world.playEffect(coord.getLocation(), Effect.MOBSPAWNER_FLAMES, 0);
                        break;
                    }
                    //BlockCoord coord = (BlockCoord) struct.getStructureBlocks().keySet().toArray()[index];
                }
            }
        }
    }

}
