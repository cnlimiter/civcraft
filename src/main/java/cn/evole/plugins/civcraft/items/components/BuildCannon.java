package cn.evole.plugins.civcraft.items.components;

import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.exception.CivException;
import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.main.CivMessage;
import cn.evole.plugins.civcraft.object.Resident;
import cn.evole.plugins.civcraft.siege.Cannon;
import cn.evole.plugins.civcraft.util.CivColor;
import cn.evole.plugins.civcraft.war.War;
import gpl.AttributeUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class BuildCannon extends ItemComponent {

    public void onInteract(PlayerInteractEvent event) {
        try {

            if (!War.isWarTime()) {
                throw new CivException(CivSettings.localize.localizedString("buildCannon_NotWar"));
            }

            Resident resident = CivGlobal.getResident(event.getPlayer());
            Cannon.newCannon(resident);

            CivMessage.sendCiv(resident.getCiv(), CivSettings.localize.localizedString("var_buildCannon_Success",
                    (event.getPlayer().getLocation().getBlockX() + "," +
                            event.getPlayer().getLocation().getBlockY() + "," +
                            event.getPlayer().getLocation().getBlockZ())));

            ItemStack newStack = new ItemStack(Material.AIR);
            event.getPlayer().getInventory().setItemInMainHand(newStack);
        } catch (CivException e) {
            CivMessage.sendError(event.getPlayer(), e.getMessage());
        }

    }

    @Override
    public void onPrepareCreate(AttributeUtil attrUtil) {
        attrUtil.addLore(ChatColor.RESET + CivColor.Gold + CivSettings.localize.localizedString("buildCannon_Lore1"));
        attrUtil.addLore(ChatColor.RESET + CivColor.Rose + CivSettings.localize.localizedString("itemLore_RightClickToUse"));
    }

}
