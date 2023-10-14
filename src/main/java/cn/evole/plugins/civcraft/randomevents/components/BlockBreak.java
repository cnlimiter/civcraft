package cn.evole.plugins.civcraft.randomevents.components;

import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.main.CivCraft;
import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.main.CivMessage;
import cn.evole.plugins.civcraft.object.Resident;
import cn.evole.plugins.civcraft.randomevents.RandomEventComponent;
import cn.evole.plugins.civcraft.util.BlockCoord;
import cn.evole.plugins.civcraft.util.CivColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.plugin.PluginManager;

public class BlockBreak extends RandomEventComponent implements Listener {

    BlockCoord bcoord;
    boolean blockBroken = false;
    boolean brokenByTown = false;

    @Override
    public void onStart() {
        /* Register a listener to watch for killed mobs. */
        final PluginManager pluginManager = CivCraft.getPlugin().getServer().getPluginManager();
        pluginManager.registerEvents(this, CivCraft.getPlugin());

    }

    @Override
    public void onCleanup() {
        HandlerList.unregisterAll(this);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockBreak(BlockBreakEvent event) {
        if (blockBroken) {
            return;
        }

        String varname = this.getString("varname");
        String locString = this.getParent().componentVars.get(varname);
        if (locString == null) {
            return;
        }

        bcoord = new BlockCoord(this.getParent().componentVars.get(varname));

        BlockCoord breakCoord = new BlockCoord(event.getBlock());

        if (breakCoord.getX() == bcoord.getX() &&
                breakCoord.getY() == bcoord.getY() &&
                breakCoord.getZ() == bcoord.getZ()) {

            Resident resident = CivGlobal.getResident(event.getPlayer().getName());
            if (resident.getTown() == this.getParentTown()) {
                brokenByTown = true;
                CivMessage.send(event.getPlayer(), CivColor.LightGreen + CivSettings.localize.localizedString("re_blockBreak_success"));
            }

            blockBroken = true;

            this.getParent().componentVars.put(getString("playername_var"), event.getPlayer().getName());
        }
    }

    @Override
    public void process() {
    }

    @Override
    public boolean onCheck() {
        return blockBroken && brokenByTown;
    }
}
