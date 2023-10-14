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

import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.main.CivData;
import cn.evole.plugins.civcraft.main.CivLog;
import cn.evole.plugins.civcraft.main.CivMessage;
import cn.evole.plugins.civcraft.structure.Buildable;
import cn.evole.plugins.civcraft.structure.Structure;
import cn.evole.plugins.civcraft.structure.wonders.Wonder;
import cn.evole.plugins.civcraft.template.Template;
import cn.evole.plugins.civcraft.threading.CivAsyncTask;
import cn.evole.plugins.civcraft.threading.TaskMaster;
import cn.evole.plugins.civcraft.util.BlockCoord;
import cn.evole.plugins.civcraft.util.CivColor;
import cn.evole.plugins.civcraft.util.SimpleBlock;
import cn.evole.plugins.civcraft.util.SimpleBlock.Type;
import org.bukkit.block.Block;

import java.sql.SQLException;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;

public class BuildAsyncTask extends CivAsyncTask {
    /*
     * This task slow-builds a struct block-by-block based on the
     * town's hammer rate. This task is per-structure building and will
     * use the CivAsynTask interface to send synchronous requests to the main
     * thread to build individual blocks.
     */

    private final int SAVE_INTERVAL = 5 * 1000; /* once every 5 sec. */
    public Buildable buildable;
    public int speed;
    public int blocks_per_tick;
    public Template tpl;
    public Block centerBlock;
    public Boolean aborted = false;
    public Date lastSave;
    private int count;
    private int extra_blocks;
    private int percent_complete;
    private Queue<SimpleBlock> sbs; //Blocks to add to main sync task queue;

    public BuildAsyncTask(Buildable bld, Template t, int s, int blocks_per_tick, Block center) {
        buildable = bld;
        speed = s;
        tpl = t;
        centerBlock = center;
        this.blocks_per_tick = blocks_per_tick;
        this.percent_complete = 0;
        sbs = new LinkedList<SimpleBlock>();
    }

    @Override
    public void run() {

        try {
            start();
            // Do something if we aborted???
        } catch (Exception e) {
            CivLog.exception("BuildAsyncTask town:" + buildable.getTown() + " struct:" + buildable.getDisplayName() + " template:" + tpl.dir(), e);
        }
    }


    private boolean start() {
        lastSave = new Date();

        for (; buildable.getBuiltBlockCount() < (tpl.size_x * tpl.size_y * tpl.size_z); buildable.builtBlockCount++) {
            speed = buildable.getBuildSpeed();
            blocks_per_tick = buildable.getBlocksPerTick();

            synchronized (aborted) {
                if (aborted) {
                    return aborted;
                }
            }

            if (buildable.isComplete()) {
                break;
            }


            if (buildable instanceof Wonder) {
                if (buildable.getTown().getMotherCiv() != null) {
                    CivMessage.sendTown(buildable.getTown(), CivSettings.localize.localizedString("var_buildAsync_wonderHaltedConquered", buildable.getTown().getCiv().getName()));
                    try {
                        Thread.sleep(1800000); //30 min notify.
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                Buildable inProgress = buildable.getTown().getCurrentStructureInProgress();
                if (inProgress != null && inProgress != buildable) {
                    CivMessage.sendTown(buildable.getTown(), CivSettings.localize.localizedString("var_buildAsync_wonderHaltedOtherConstruction", inProgress.getDisplayName()));
                    try {
                        Thread.sleep(60000); //1 min notify.
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                if (buildable.getTown().getTownHall() == null) {
                    CivMessage.sendTown(buildable.getTown(), CivSettings.localize.localizedString("buildAsync_wonderHaltedNoTownHall"));
                    try {
                        Thread.sleep(600000); //10 min notify.
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            if (build()) {
                //skip to next run.
                continue;
            }

            Date now = new Date();
            if (now.getTime() > lastSave.getTime() + SAVE_INTERVAL) {
                buildable.updateBuildProgess();
                lastSave = now;
            }

            count = 0; //reset count, this tick is over.
            // Add all of the blocks from this tick to the sync task.
            synchronized (this.aborted) {
                if (!this.aborted) {
                    this.updateBlocksQueue(sbs);
                    sbs.clear();
                } else {
                    return aborted;
                }
            }

            try {
                int nextPercentComplete = (int) (((double) buildable.getBuiltBlockCount() / (double) buildable.getTotalBlockCount()) * 100);
                if (nextPercentComplete > this.percent_complete) {
                    this.percent_complete = nextPercentComplete;
                    if ((this.percent_complete % 10 == 0)) {
                        if (buildable instanceof Wonder) {
                            CivMessage.global(CivSettings.localize.localizedString("var_buildAsync_progressWonder", buildable.getDisplayName(), buildable.getTown().getName(), nextPercentComplete));
                        } else {

                            CivMessage.sendTown(buildable.getTown(),
                                    CivColor.Yellow + CivSettings.localize.localizedString("var_buildAsync_progressOther", buildable.getDisplayName(), nextPercentComplete));
                        }
                    }
                }

                int timeleft = speed;
                while (timeleft > 0) {
                    int min = Math.min(10000, timeleft);
                    Thread.sleep(min);
                    timeleft -= 10000;

                    /* Calculate our speed again in case our hammer rate has changed. */
                    int newSpeed = buildable.getBuildSpeed();
                    if (newSpeed != speed) {
                        speed = newSpeed;
                        timeleft = newSpeed;
                    }
                }

                if (buildable instanceof Wonder) {
                    if (checkOtherWonderAlreadyBuilt()) {
                        processWonderAbort();
                        return false; //wonder aborted via function above, no need to abort again.
                    }

                    if (buildable.isDestroyed()) {
                        CivMessage.sendTown(buildable.getTown(), CivSettings.localize.localizedString("var_buildAsync_destroyed", buildable.getDisplayName()));
                        abortWonder();
                        return false;
                    }

                    if (buildable.getTown().getMotherCiv() != null) {
                        // Can't build wonder while we're conquered.
                        continue;
                    }
                }
                //check if wonder was completed...
            } catch (InterruptedException e) {
                //e.printStackTrace();
            }
        }
        // Make sure the last iteration makes it on to the queue.
        if (sbs.size() > 0) {
            updateBlocksQueue(sbs);
            sbs.clear();
        }
        //structures are always available
        if (buildable instanceof Wonder) {
            if (checkOtherWonderAlreadyBuilt()) {
                processWonderAbort();
                return false;
            }
        }

        buildable.setComplete(true);
        if (buildable instanceof Wonder) {
            buildable.getTown().setCurrentWonderInProgress(null);
        } else {
            buildable.getTown().setCurrentStructureInProgress(null);
        }
        buildable.savedBlockCount = buildable.builtBlockCount;
        buildable.updateBuildProgess();
        buildable.save();

        tpl.deleteInProgessTemplate(buildable.getCorner().toString(), buildable.getTown());
        buildable.getTown().build_tasks.remove(this);
        TaskMaster.syncTask(new PostBuildSyncTask(tpl, buildable), 10);
        if (this.buildable instanceof Structure) {
            CivMessage.global(CivSettings.localize.localizedString("var_buildAsync_completed", this.buildable.getTown().getName(), "§2" + this.buildable.getDisplayName() + CivColor.RESET));
        } else if (this.buildable instanceof Wonder) {
            CivMessage.global(CivSettings.localize.localizedString("var_buildAsync_completedWonder", CivColor.Red + this.buildable.getCiv().getName() + CivColor.RESET, "§6" + this.buildable.getTown().getName() + CivColor.RESET, "§a" + this.buildable.getDisplayName() + CivColor.RESET));
        }
        buildable.onComplete();
        return false;
    }

    public boolean build() {

        boolean skipToNext = false;

        // Apply extra blocks first, then work on this blocks per tick.
        if (this.extra_blocks > 0) {
            synchronized (this) {
                extra_blocks--;
                skipToNext = true;
            }
        } else if (count < this.blocks_per_tick) {
            count++;
            skipToNext = true;
        }
        //3D mailman algorithm...

        int y = (buildable.getBuiltBlockCount() / (tpl.size_x * tpl.size_z)); //bottom to top.
        //int y = (tpl.size_y - (buildable.builtBlockCount / (tpl.size_x*tpl.size_z))) - 1; //Top to bottom
        int z = (buildable.getBuiltBlockCount() / tpl.size_x) % tpl.size_z;
        int x = buildable.getBuiltBlockCount() % tpl.size_x;

        SimpleBlock sb = tpl.blocks[x][y][z];

        // Convert relative x,y,z to real x,y,z in world.
        sb.x = x + centerBlock.getX();
        sb.y = y + centerBlock.getY();
        sb.z = z + centerBlock.getZ();
        sb.worldname = centerBlock.getWorld().getName();
        sb.buildable = buildable;


        // Add this SimpleBlock to the update queue and *assume* that all of the
        // synchronous stuff is now going to be handled later. Perform the reset
        // of the build task async.
        synchronized (this.aborted) {
            if (!this.aborted) {
                if (sb.getType() == CivData.WOOD_DOOR ||
                        sb.getType() == CivData.IRON_DOOR ||
                        sb.getType() == CivData.SPRUCE_DOOR ||
                        sb.getType() == CivData.BIRCH_DOOR ||
                        sb.getType() == CivData.JUNGLE_DOOR ||
                        sb.getType() == CivData.ACACIA_DOOR ||
                        sb.getType() == CivData.DARK_OAK_DOOR ||
                        Template.isAttachable(sb.getType())) {
                    // dont build doors, save it for post sync build.
                } else {
                    sbs.add(sb);
                }

                if (buildable.isDestroyable() == false && sb.getType() != CivData.AIR) {
                    if (sb.specialType != Type.COMMAND) {
                        BlockCoord coord = new BlockCoord(sb.worldname, sb.x, sb.y, sb.z);
                        if (sb.y == 0) {
                            buildable.addStructureBlock(coord, false);
                        } else {
                            buildable.addStructureBlock(coord, true);
                        }
                    }
                }
            } else {
                sbs.clear();
                return false;
            }
        }

        return skipToNext;
    }


    private boolean checkOtherWonderAlreadyBuilt() {
        if (buildable.isComplete()) {
            return false; //We are completed, other wonders are not already built.
        }

        return (!Wonder.isWonderAvailable(buildable.getConfigId()));
    }

    private void processWonderAbort() {
        CivMessage.sendTown(buildable.getTown(), CivColor.Rose + CivSettings.localize.localizedString("var_buildAsync_wonderFarAway", buildable.getDisplayName()));

        //Refund the town half the cost of the wonder.
        double refund = (int) (buildable.getCost() / 2);
        buildable.getTown().depositDirect(refund);

        CivMessage.sendTown(buildable.getTown(), CivColor.Yellow + CivSettings.localize.localizedString("var_buildAsync_wonderRefund", refund, CivSettings.CURRENCY_NAME));
        abortWonder();
    }

    private void abortWonder() {
        class SyncTask implements Runnable {

            @Override
            public void run() {
                //Remove build task from town..
                buildable.getTown().build_tasks.remove(this);
                buildable.unbindStructureBlocks();

                //remove wonder from town.
                synchronized (buildable.getTown()) {
                    //buildable.getTown().wonders.remove(buildable);
                    buildable.getTown().removeWonder(buildable);
                }

                //Remove the scaffolding..
                tpl.removeScaffolding(buildable.getCorner().getLocation());
                try {
                    ((Wonder) buildable).delete();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        TaskMaster.syncTask(new SyncTask());

    }

    public double setExtraHammers(double extra_hammers) {

        double leftover_hammers = 0.0;
        //Get the total number of blocks represented by the extra hammers.
        synchronized (this) {
            this.extra_blocks = (int) (buildable.getBlocksPerHammer() * extra_hammers);
            int blocks_left = buildable.getTotalBlockCount() - buildable.getBuiltBlockCount();
            if (this.extra_blocks > blocks_left) {
                leftover_hammers = (this.extra_blocks - blocks_left) / buildable.getBlocksPerHammer();
            }
        }
        return leftover_hammers;
    }

    public void abort() {
        synchronized (aborted) {
            aborted = true;
        }
    }


}
