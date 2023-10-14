/**
 * [2023] Cnlimiter LLC
 * All Rights Reserved.
 * Name: civcraft
 * Author: cnlimiter
 * UpdateTime: 2023/10/14 16:15
 * Description:
 * License: ARR
 */
package cn.evole.plugins.civcraft.util;

import cn.evole.plugins.civcraft.exception.CivException;
import cn.evole.plugins.civcraft.main.CivGlobal;
import org.bukkit.Location;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;

public class InventoryHolderStorage {

    /*
     * An inventory holder can be a 'block'or a player.
     */
    private Location blockLocation;

    private String playerName;

    public InventoryHolderStorage(InventoryHolder holder, Location holderLocation) {
        if (holder instanceof Player) {
            Player player = (Player) holder;
            playerName = player.getName();
            blockLocation = null;
        } else {
            blockLocation = holderLocation;
        }
    }

    public InventoryHolderStorage(Location blockLoc) {
        blockLocation = blockLoc;
        playerName = null;
    }

    public InventoryHolderStorage(Player player) {
        blockLocation = null;
        playerName = player.getName();
    }

    public InventoryHolder getHolder() throws CivException {
        if (playerName != null) {
            Player player = CivGlobal.getPlayer(playerName);
            return (InventoryHolder) player;
        }

        if (blockLocation != null) {
            /* Make sure the chunk is loaded. */

            if (!blockLocation.getChunk().isLoaded()) {
                if (!blockLocation.getChunk().load()) {
                    throw new CivException("Couldn't load chunk at " + blockLocation + " where holder should reside.");
                }
            }
            if (!(blockLocation.getBlock().getState() instanceof Chest)) {
                throw new CivException("Holder location is not a chest, invalid.");
            }

            Chest chest = (Chest) blockLocation.getBlock().getState();
            return chest.getInventory().getHolder();
        }

        throw new CivException("Invalid holder.");
    }

    public void setHolder(InventoryHolder holder) throws CivException {
        if (holder instanceof Player) {
            Player player = (Player) holder;
            playerName = player.getName();
            blockLocation = null;
            return;
        }

        if (holder instanceof Chest) {
            Chest chest = (Chest) holder;
            playerName = null;
            blockLocation = chest.getLocation();
            return;
        }

        if (holder instanceof DoubleChest) {
            DoubleChest dchest = (DoubleChest) holder;
            playerName = null;
            blockLocation = dchest.getLocation();
            return;
        }

        throw new CivException("Invalid holder passed to set holder:" + holder.toString());
    }

}
