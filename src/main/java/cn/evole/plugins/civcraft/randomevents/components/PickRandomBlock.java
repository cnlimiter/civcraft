package cn.evole.plugins.civcraft.randomevents.components;

import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.exception.InvalidConfiguration;
import cn.evole.plugins.civcraft.main.CivData;
import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.main.CivLog;
import cn.evole.plugins.civcraft.randomevents.RandomEvent;
import cn.evole.plugins.civcraft.randomevents.RandomEventComponent;
import cn.evole.plugins.civcraft.threading.TaskMaster;
import cn.evole.plugins.civcraft.util.BlockCoord;
import cn.evole.plugins.civcraft.util.ChunkCoord;
import cn.evole.plugins.civcraft.util.ItemManager;
import org.bukkit.block.Block;

import java.util.Random;

public class PickRandomBlock extends RandomEventComponent {

    @Override
    public void process() {

        class SyncTask implements Runnable {
            RandomEvent event;

            public SyncTask(RandomEvent event) {
                this.event = event;
            }

            @Override
            public void run() {
                try {
                    int max_x = CivSettings.getInteger(CivSettings.randomEventsConfig, "max_x");
                    int max_z = CivSettings.getInteger(CivSettings.randomEventsConfig, "max_z");
                    int min_x = CivSettings.getInteger(CivSettings.randomEventsConfig, "min_x");
                    int min_z = CivSettings.getInteger(CivSettings.randomEventsConfig, "min_z");

                    int range_x = max_x - min_x;
                    int range_z = max_z - min_z;

                    /* pick a chunk instead. */
                    range_x /= 16;
                    range_z /= 16;

                    BlockCoord bcoord = null;
                    ChunkCoord coord = null;
                    for (int i = 0; i < 10; i++) {

                        Random rand = new Random();
                        int randX = rand.nextInt(range_x) - (max_x / 16);
                        int randZ = rand.nextInt(range_z) - (max_z / 16);

                        coord = new ChunkCoord("world", randX, randZ);

                        /* Make sure its not in someone's culture. */
                        if (CivGlobal.getCultureChunk(coord) != null) {
                            continue;
                        }

                        /* Search for a 'valid' block to be used as breakable in the chunk. */
                        int startY = rand.nextInt(20) + 4; /* Select a y somewhere between 4 and 24. */
                        for (int x = 0; x < 16; x++) {
                            for (int z = 0; z < 16; z++) {
                                for (int y = startY; y < 50; y++) { /* no need to check higher than 128, unlikely to find a good block. */
                                    Block block = coord.getChunk().getBlock(x, y, z);
                                    //CivLog.debug("checking a block:"+block.toString());

                                    if (ItemManager.getId(block) == CivData.STONE || ItemManager.getId(block) == CivData.GRAVEL) {
                                        bcoord = new BlockCoord(block);
                                        break;
                                    }
                                }
                            }
                        }

                        if (bcoord != null) {
                            /* found our block. */
                            break;
                        }
                    }

                    if (bcoord == null || coord == null) {
                        CivLog.warning("Couldn't find a suitable block for PickRandomBlock after 10 retries.");
                        return;
                    }

                    /* Save location for requirement component */
                    this.event.componentVars.put(getString("varname"), bcoord.toString());

                    /* Obfuscate and display block location to players */
                    Random rand = new Random();
                    int y_min = bcoord.getY() - rand.nextInt(10);
                    int y_max = bcoord.getY() + rand.nextInt(10);

                    sendMessage(CivSettings.localize.localizedString("var_re_pickBlock1", (coord.getX() + "," + coord.getZ()), y_min, y_max));
                    sendMessage(CivSettings.localize.localizedString("re_pickBlock2"));
                } catch (InvalidConfiguration e) {
                    e.printStackTrace();
                }
            }
        }
        TaskMaster.syncTask(new SyncTask(this.getParent()));
    }

}
