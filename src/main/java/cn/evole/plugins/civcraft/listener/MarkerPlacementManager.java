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

import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.exception.CivException;
import cn.evole.plugins.civcraft.main.CivData;
import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.main.CivMessage;
import cn.evole.plugins.civcraft.structure.Structure;
import cn.evole.plugins.civcraft.util.ItemManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;

//import org.bukkit.event.inventory.InventoryClickEvent;

public class MarkerPlacementManager implements Listener {

    private static HashMap<String, Structure> playersInPlacementMode = new HashMap<String, Structure>();
    private static HashMap<String, ArrayList<Location>> markers = new HashMap<String, ArrayList<Location>>();


    public static void addToPlacementMode(Player player, Structure structure, String markerName) throws CivException {

        if (player.getInventory().getItemInMainHand() != null && ItemManager.getId(player.getInventory().getItemInMainHand()) != CivData.AIR) {
            throw new CivException(CivSettings.localize.localizedString("placement_errorHolding"));
        }

        playersInPlacementMode.put(player.getName(), structure);
        markers.put(player.getName(), new ArrayList<Location>());

        ItemStack stack = ItemManager.createItemStack(CivData.REDSTONE_TORCH_OFF, 2);
        ItemMeta meta = stack.getItemMeta();
        if (markerName != null) {
            meta.setDisplayName(markerName);
        } else {
            meta.setDisplayName("Marker");
        }
        stack.setItemMeta(meta);
        player.getInventory().setItemInMainHand(stack);

        CivMessage.send(player, CivSettings.localize.localizedString("var_placement_enabled", structure.getDisplayName()));
    }

    public static void removeFromPlacementMode(Player player, boolean canceled) {
        if (canceled) {
            Structure struct = playersInPlacementMode.get(player.getName());
            struct.getTown().removeStructure(struct);
            CivGlobal.removeStructure(struct);
        }
        playersInPlacementMode.remove(player.getName());
        markers.remove(player.getName());
        player.getInventory().setItemInMainHand(ItemManager.createItemStack(CivData.AIR, 1));
        CivMessage.send(player, CivSettings.localize.localizedString("placement_ended"));
    }

    public static boolean isPlayerInPlacementMode(Player player) {
        return isPlayerInPlacementMode(player.getName());
    }

    public static boolean isPlayerInPlacementMode(String name) {
        return playersInPlacementMode.containsKey(name);
    }

    public static void setMarker(Player player, Location location) throws CivException {
        ArrayList<Location> locs = markers.get(player.getName());

        Structure struct = playersInPlacementMode.get(player.getName());
        int amount = player.getInventory().getItemInMainHand().getAmount();
        if (amount == 1) {
            player.getInventory().setItemInMainHand(null);
        } else {
            player.getInventory().getItemInMainHand().setAmount((amount - 1));
        }

        locs.add(location);
        struct.onMarkerPlacement(player, location, locs);

    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void OnItemHeldChange(PlayerItemHeldEvent event) {
        if (isPlayerInPlacementMode(event.getPlayer())) {
            removeFromPlacementMode(event.getPlayer(), true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void OnPlayerDropItemEvent(PlayerDropItemEvent event) {
        if (isPlayerInPlacementMode(event.getPlayer())) {
            event.setCancelled(true);
            removeFromPlacementMode(event.getPlayer(), true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void OnPlayerQuit(PlayerQuitEvent event) {
        if (isPlayerInPlacementMode(event.getPlayer())) {
            removeFromPlacementMode(event.getPlayer(), true);
        }
    }

//	@EventHandler(priority = EventPriority.MONITOR) 
//	public void OnInventoryClick(InventoryClickEvent event) {
//		Player player;
//		try {
//			player = CivGlobal.getPlayer(event.getWhoClicked().getName());
//		} catch (CivException e) {
//			//Not a valid player or something, forget it.
//			return;
//		}
//
//		if (isPlayerInPlacementMode(player)) {
//			removeFromPlacementMode(player, true);
//		}
//	}
//	
}
