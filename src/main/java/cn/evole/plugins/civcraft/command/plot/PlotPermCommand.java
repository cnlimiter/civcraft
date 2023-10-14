/**
 * [2023] Cnlimiter LLC
 * All Rights Reserved.
 * Name: civcraft
 * Author: cnlimiter
 * UpdateTime: 2023/10/14 16:15
 * Description:
 * License: ARR
 */
package cn.evole.plugins.civcraft.command.plot;

import cn.evole.plugins.civcraft.command.CommandBase;
import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.exception.CivException;
import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.main.CivMessage;
import cn.evole.plugins.civcraft.object.TownChunk;
import cn.evole.plugins.civcraft.permission.PermissionNode;
import cn.evole.plugins.civcraft.util.CivColor;
import org.bukkit.entity.Player;

public class PlotPermCommand extends CommandBase {

    @Override
    public void init() {
        command = "/plot perm";
        displayName = CivSettings.localize.localizedString("cmd_plot_perm_name");

        commands.put("set", CivSettings.localize.localizedString("cmd_plot_perm_setDesc"));
    }

    public void set_cmd() throws CivException {
        Player player = (Player) sender;

        TownChunk tc = CivGlobal.getTownChunk(player.getLocation());
        if (tc == null) {
            throw new CivException(CivSettings.localize.localizedString("cmd_plot_perm_setnotInTown"));
        }

        if (args.length < 4) {
            showPermCmdHelp();
            throw new CivException(CivSettings.localize.localizedString("cmd_plot_perm_setBadArg"));
        }

        PermissionNode node = null;
        switch (args[1].toLowerCase()) {
            case "build":
                node = tc.perms.build;
                break;
            case "destroy":
                node = tc.perms.destroy;
                break;
            case "interact":
                node = tc.perms.interact;
                break;
            case "itemuse":
                node = tc.perms.itemUse;
                break;
            case "reset":
                //TODO implement permissions reset.
                break;
            default:
                showPermCmdHelp();
                throw new CivException(CivSettings.localize.localizedString("cmd_plot_perm_setBadArg"));
        }

        if (node == null) {
            throw new CivException(CivSettings.localize.localizedString("cmd_plot_perm_setInternalError"));
        }

        boolean on;
        if (args[3].equalsIgnoreCase("on") || args[3].equalsIgnoreCase("yes") || args[3].equalsIgnoreCase("1")) {
            on = true;
        } else if (args[3].equalsIgnoreCase("off") || args[3].equalsIgnoreCase("no") || args[3].equalsIgnoreCase("0")) {
            on = false;
        } else {
            showPermCmdHelp();
            throw new CivException(CivSettings.localize.localizedString("cmd_plot_perm_setBadArg"));
        }

        switch (args[2].toLowerCase()) {
            case "owner":
                node.setPermitOwner(on);
                break;
            case "group":
                node.setPermitGroup(on);
                break;
            case "others":
                node.setPermitOthers(on);
        }

        tc.save();

        CivMessage.sendSuccess(sender, CivSettings.localize.localizedString("var_cmd_plot_perm_setSuccess", node.getType(), on, args[2]));
    }

    private void showPermCmdHelp() {
        CivMessage.send(sender, CivColor.LightGray + CivSettings.localize.localizedString("cmd_plot_perm_help1"));
        CivMessage.send(sender, CivColor.LightGray + "    " + CivSettings.localize.localizedString("cmd_plot_perm_help2"));
        CivMessage.send(sender, CivColor.LightGray + "    " + CivSettings.localize.localizedString("cmd_plot_perm_help3"));
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
        if (args.length != 0) {
            validPlotOwner();
        }

        return;
    }

}
