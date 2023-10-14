/**
 * [2023] Cnlimiter LLC
 * All Rights Reserved.
 * Name: civcraft
 * Author: cnlimiter
 * UpdateTime: 2023/10/14 16:15
 * Description:
 * License: ARR
 */
package cn.evole.plugins.civcraft.command.debug;

import cn.evole.plugins.civcraft.command.CommandBase;
import cn.evole.plugins.civcraft.exception.CivException;
import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.main.CivMessage;
import cn.evole.plugins.civcraft.structure.farm.FarmChunk;
import cn.evole.plugins.civcraft.structure.farm.FarmGrowthSyncTask;
import cn.evole.plugins.civcraft.structure.farm.FarmPreCachePopulateTimer;
import cn.evole.plugins.civcraft.threading.TaskMaster;
import cn.evole.plugins.civcraft.util.BlockCoord;
import cn.evole.plugins.civcraft.util.ChunkCoord;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.entity.Player;

public class DebugFarmCommand extends CommandBase {

    @Override
    public void init() {
        command = "/dbg farm ";
        displayName = "Farm Commands";

        commands.put("showgrowth", "Highlight the crops that grew last tick.");
        commands.put("grow", "[x] grows ALL farm chunks x many times.");
        commands.put("cropcache", "show the crop cache for this plot.");
        commands.put("unloadchunk", "[x] [z] unloads this farm chunk");
        commands.put("cache", "Runs the crop cache task.");

    }

    public void unloadchunk_cmd() throws CivException {

        int x = getNamedInteger(1);
        int z = getNamedInteger(2);

        Bukkit.getWorld("world").unloadChunk(x, z);
        CivMessage.sendSuccess(sender, "Chunk " + x + "," + z + " unloaded");
    }

    public void showgrowth_cmd() throws CivException {
        Player player = getPlayer();

        ChunkCoord coord = new ChunkCoord(player.getLocation());
        FarmChunk fc = CivGlobal.getFarmChunk(coord);
        if (fc == null) {
            throw new CivException("This is not a farm.");
        }

        for (BlockCoord bcoord : fc.getLastGrownCrops()) {
            bcoord.getBlock().getWorld().playEffect(bcoord.getLocation(), Effect.MOBSPAWNER_FLAMES, 1);
        }

        CivMessage.sendSuccess(player, "Flashed last grown crops");
    }


    public void cropcache_cmd() throws CivException {
        Player player = getPlayer();

        ChunkCoord coord = new ChunkCoord(player.getLocation());
        FarmChunk fc = CivGlobal.getFarmChunk(coord);
        if (fc == null) {
            throw new CivException("This is not a farm.");
        }

        for (BlockCoord bcoord : fc.cropLocationCache) {
            bcoord.getBlock().getWorld().playEffect(bcoord.getLocation(), Effect.MOBSPAWNER_FLAMES, 1);
        }
        CivMessage.sendSuccess(player, "Flashed cached crops.");
    }

    public void grow_cmd() throws CivException {

        int count = getNamedInteger(1);
        for (int i = 0; i < count; i++) {
            TaskMaster.asyncTask(new FarmGrowthSyncTask(), 0);
        }
        CivMessage.sendSuccess(sender, "Grew all farms.");
    }

    public void cache_cmd() {
        TaskMaster.syncTask(new FarmPreCachePopulateTimer());
    }

    @Override
    public void doDefaultAction() throws CivException {
        showHelp();
    }

    @Override
    public void showHelp() {
        showBasicHelp();
    }

    @Override
    public void permissionCheck() throws CivException {

    }

}
