/**
 * [2023] Cnlimiter LLC
 * All Rights Reserved.
 * Name: civcraft
 * Author: cnlimiter
 * UpdateTime: 2023/10/14 16:15
 * Description:
 * License: ARR
 */
package cn.evole.plugins.civcraft.command;

import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.main.CivMessage;
import cn.evole.plugins.civcraft.object.CultureChunk;
import cn.evole.plugins.civcraft.object.TownChunk;
import cn.evole.plugins.civcraft.util.ChunkCoord;
import cn.evole.plugins.civcraft.util.CivColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HereCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {

        if (sender instanceof Player) {
            Player player = (Player) sender;

            ChunkCoord coord = new ChunkCoord(player.getLocation());

            CultureChunk cc = CivGlobal.getCultureChunk(coord);
            if (cc != null) {
                CivMessage.send(sender, CivColor.LightPurple + CivSettings.localize.localizedString("var_cmd_here_inCivAndTown",
                        CivColor.Yellow + cc.getCiv().getName() + CivColor.LightPurple, CivColor.Yellow + cc.getTown().getName()));
            }

            TownChunk tc = CivGlobal.getTownChunk(coord);
            if (tc != null) {
                CivMessage.send(sender, CivColor.Green + CivSettings.localize.localizedString("var_cmd_here_inTown", CivColor.LightGreen + tc.getTown().getName()));
                if (tc.isOutpost()) {
                    CivMessage.send(sender, CivColor.Yellow + CivSettings.localize.localizedString("cmd_here_outPost"));
                }
            }

            if (cc == null && tc == null) {
                CivMessage.send(sender, CivColor.Yellow + CivSettings.localize.localizedString("cmd_here_wilderness"));
            }

        }


        return false;
    }

}
