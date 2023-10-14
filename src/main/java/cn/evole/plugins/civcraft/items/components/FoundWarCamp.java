package cn.evole.plugins.civcraft.items.components;

import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.config.ConfigBuildableInfo;
import cn.evole.plugins.civcraft.exception.CivException;
import cn.evole.plugins.civcraft.exception.InvalidConfiguration;
import cn.evole.plugins.civcraft.interactive.InteractiveWarCampFound;
import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.main.CivMessage;
import cn.evole.plugins.civcraft.object.Resident;
import cn.evole.plugins.civcraft.structure.Buildable;
import cn.evole.plugins.civcraft.threading.TaskMaster;
import cn.evole.plugins.civcraft.util.CallbackInterface;
import cn.evole.plugins.civcraft.util.CivColor;
import cn.evole.plugins.civcraft.war.War;
import gpl.AttributeUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class FoundWarCamp extends ItemComponent implements CallbackInterface {

    public static ConfigBuildableInfo info = new ConfigBuildableInfo();

    static {
        info.id = "warcamp";
        info.displayName = "War Camp";
        info.ignore_floating = false;
        info.template_base_name = "warcamp";
        info.tile_improvement = false;
        info.templateYShift = -1;
        info.max_hitpoints = 100;
    }

    @Override
    public void onPrepareCreate(AttributeUtil attrUtil) {
        attrUtil.addLore(ChatColor.RESET + CivColor.Gold + CivSettings.localize.localizedString("buildWarCamp_lore1"));
        attrUtil.addLore(ChatColor.RESET + CivColor.Rose + CivSettings.localize.localizedString("itemLore_RightClickToUse"));
    }

    public void foundCamp(Player player) throws CivException {
        Resident resident = CivGlobal.getResident(player);

        if (!resident.hasTown()) {
            throw new CivException(CivSettings.localize.localizedString("buildWarCamp_errorNotInCiv"));
        }

        if (!resident.getCiv().getLeaderGroup().hasMember(resident) &&
                !resident.getCiv().getAdviserGroup().hasMember(resident)) {
            throw new CivException(CivSettings.localize.localizedString("buildWarCamp_errorNotPerms"));
        }

        if (!War.isWarTime()) {
            throw new CivException(CivSettings.localize.localizedString("buildWarCamp_errorNotWarTime"));
        }

        /*
         * Build a preview for the Capitol structure.
         */
        CivMessage.send(player, CivColor.LightGreen + CivColor.BOLD + CivSettings.localize.localizedString("build_checking_position"));


        Buildable.buildVerifyStatic(player, info, player.getLocation(), this);
    }

    public void onInteract(PlayerInteractEvent event) {

        event.setCancelled(true);
        if (!event.getAction().equals(Action.RIGHT_CLICK_AIR) &&
                !event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            return;
        }

        try {
            foundCamp(event.getPlayer());
        } catch (CivException e) {
            CivMessage.sendError(event.getPlayer(), e.getMessage());
        }

        class SyncTask implements Runnable {
            String name;

            public SyncTask(String name) {
                this.name = name;
            }

            @Override
            public void run() {
                Player player;
                try {
                    player = CivGlobal.getPlayer(name);
                } catch (CivException e) {
                    return;
                }
                player.updateInventory();
            }
        }
        TaskMaster.syncTask(new SyncTask(event.getPlayer().getName()));

        return;

    }

    @Override
    public void execute(String playerName) {
        Player player;
        try {
            player = CivGlobal.getPlayer(playerName);
        } catch (CivException e) {
            return;
        }
        Resident resident = CivGlobal.getResident(playerName);
        int warTimeout;
        try {
            warTimeout = CivSettings.getInteger(CivSettings.warConfig, "warcamp.rebuild_timeout");
        } catch (InvalidConfiguration e) {
            e.printStackTrace();
            return;
        }

        CivMessage.sendHeading(player, CivSettings.localize.localizedString("buildWarCamp_heading"));
        CivMessage.send(player, CivColor.LightGreen + CivSettings.localize.localizedString("buildWarCamp_prompt1"));
        CivMessage.send(player, CivColor.LightGreen + "   -" + CivSettings.localize.localizedString("buildWarCamp_prompt2"));
        CivMessage.send(player, CivColor.LightGreen + "   -" + CivSettings.localize.localizedString("var_buildWarCamp_prompt3", warTimeout));
        CivMessage.send(player, " ");
        CivMessage.send(player, CivColor.LightGreen + ChatColor.BOLD + CivSettings.localize.localizedString("buildWarCamp_prompt5"));
        CivMessage.send(player, CivColor.LightGray + CivSettings.localize.localizedString("buildWarCamp_prompt6"));

        resident.setInteractiveMode(new InteractiveWarCampFound(info));
    }
}
