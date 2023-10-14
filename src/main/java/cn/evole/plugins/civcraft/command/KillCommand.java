package cn.evole.plugins.civcraft.command;

import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.main.CivMessage;
import cn.evole.plugins.civcraft.util.CivColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class KillCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {

        if (!(sender instanceof Player)) {
            CivMessage.sendError(sender, CivSettings.localize.localizedString("cmd_MustBePlayer"));
            return false;
        }

        Player player = (Player) sender;
        player.setHealth(0);

        CivMessage.send(sender, CivColor.Yellow + CivColor.BOLD + CivSettings.localize.localizedString("cmd_kill_Mesage"));

        return true;
    }

}
