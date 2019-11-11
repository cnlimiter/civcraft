
package com.avrgaming.civcraft.items.components;

import gpl.AttributeUtil;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerLeashEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.interactive.InteractiveConfirmScroll;
import com.avrgaming.civcraft.items.components.ItemComponent;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.object.Civilization;
import com.avrgaming.civcraft.object.Resident;
import com.avrgaming.civcraft.util.CivColor;

public class ProcessScroll
extends ItemComponent {
    @Override
    public void onPrepareCreate(AttributeUtil attrUtil) {
    }

    @Override
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction().equals((Object)Action.RIGHT_CLICK_AIR)) {
            event.getPlayer().updateInventory();
            event.setCancelled(true);
        } else if (event.getAction().equals((Object)Action.RIGHT_CLICK_BLOCK) && event.getClickedBlock().getType() != Material.CHEST) {
            event.getPlayer().updateInventory();
            event.setCancelled(true);
        }
        Player player = event.getPlayer();
        Resident resident = CivGlobal.getResident(player);
        Civilization civ = resident.getCiv();
        if (civ == null) {
            CivMessage.sendError(resident, CivSettings.localize.localizedString("var_processScroll_noCiv"));
            return;
        }
        AttributeUtil attr = new AttributeUtil(event.getItem());
        CivMessage.sendSuccess(resident, CivSettings.localize.localizedString("var_processScroll_interactive1"));
        for (String lore : attr.getLore()) {
            CivMessage.send((Object)resident, lore);
        }
        CivMessage.send((Object)resident, CivColor.LightGray + CivSettings.localize.localizedString("var_processScroll_interactive2"));
        InteractiveConfirmScroll interactiveConfirmScroll = new InteractiveConfirmScroll();
        resident.setInteractiveMode(interactiveConfirmScroll);
    }

    @Override
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        event.setCancelled(true);
    }

    @Override
    public void onPlayerLeashEvent(PlayerLeashEntityEvent event) {
    }
}

