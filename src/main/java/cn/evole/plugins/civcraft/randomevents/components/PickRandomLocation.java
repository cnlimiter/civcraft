package cn.evole.plugins.civcraft.randomevents.components;

import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.exception.InvalidConfiguration;
import cn.evole.plugins.civcraft.randomevents.RandomEvent;
import cn.evole.plugins.civcraft.randomevents.RandomEventComponent;
import cn.evole.plugins.civcraft.threading.TaskMaster;
import cn.evole.plugins.civcraft.util.BlockCoord;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.Random;

public class PickRandomLocation extends RandomEventComponent {

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

                    Random rand = new Random();
                    int randX = rand.nextInt(range_x) - max_x;
                    int randZ = rand.nextInt(range_z) - max_z;

                    /* XXX only pick in "world" */
                    World world = Bukkit.getWorld("world");
                    int y = world.getHighestBlockYAt(randX, randZ);

                    BlockCoord bcoord = new BlockCoord(world.getName(), randX, y, randZ);

                    String varname = getString("varname");
                    event.componentVars.put(varname, bcoord.toString());

                    sendMessage(CivSettings.localize.localizedString("var_re_pickRandom", bcoord.getX() + "," + bcoord.getY() + "," + bcoord.getZ()));
                } catch (InvalidConfiguration e) {
                    e.printStackTrace();
                }
            }
        }
        TaskMaster.syncTask(new SyncTask(this.getParent()));
    }
}
