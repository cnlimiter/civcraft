/**
 * [2023] Cnlimiter LLC
 * All Rights Reserved.
 * Name: civcraft
 * Author: cnlimiter
 * UpdateTime: 2023/10/14 16:15
 * Description:
 * License: ARR
 */
package cn.evole.plugins.civcraft.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;


public class DebugListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerInteractEntityEvent(PlayerInteractEntityEvent event) {
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDamageEvent(EntityDamageEvent event) {
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityInteractEvent(EntityInteractEvent event) {
    }

//	@EventHandler(priority = EventPriority.NORMAL)
//	public void OnBlockBreakEvent(BlockBreakEvent event) {
//		CivLog.debug("block break! setting blocs..");
//		
//		Block downBlock = event.getBlock().getRelative(BlockFace.DOWN);
//		while (downBlock.getTypeId() == CivData.AIR) {
//			downBlock = downBlock.getRelative(BlockFace.DOWN);
//		}
//		
//		Block nextBlock = event.getBlock().getRelative(BlockFace.UP);
//		downBlock.getRelative(BlockFace.UP).setTypeIdAndData(nextBlock.getTypeId(), nextBlock.getData(), false);
//		nextBlock = event.getBlock().getRelative(BlockFace.UP);
//
//		World world = nextBlock.getLocation().getWorld();
//		while (nextBlock.getTypeId() != CivData.AIR) {
//			int type = nextBlock.getTypeId();
//			byte data = nextBlock.getData();
//			
//			nextBlock.setTypeIdAndData(CivData.AIR, (byte)0, false);
//			world.spawnFallingBlock(nextBlock.getLocation(), type, data);
//			
//			nextBlock = nextBlock.getRelative(BlockFace.UP);
//		}
//	}


}
