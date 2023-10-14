package cn.evole.plugins.civcraft.items.components;

import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.interactive.InteractiveConfirmScroll;
import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.main.CivMessage;
import cn.evole.plugins.civcraft.object.Civilization;
import cn.evole.plugins.civcraft.object.Resident;
import cn.evole.plugins.civcraft.util.CivColor;
import gpl.AttributeUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerLeashEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class ProcessScroll extends ItemComponent {
    @Override
    public void onPrepareCreate(AttributeUtil attrUtil) {
    }

    @Override
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction().equals((Object) Action.RIGHT_CLICK_AIR)) {
            event.getPlayer().updateInventory();
            event.setCancelled(true);
        } else if (event.getAction().equals((Object) Action.RIGHT_CLICK_BLOCK) && event.getClickedBlock().getType() != Material.CHEST) {
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
            CivMessage.send((Object) resident, lore);
        }
        CivMessage.send((Object) resident, CivColor.LightGray + CivSettings.localize.localizedString("var_processScroll_interactive2"));
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

