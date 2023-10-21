package cn.evole.plugins.tp.cmd;

import cn.evole.plugins.tp.TpHandler;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * Name: civs-plugin / AcceptCommand
 * Author: cnlimiter
 * CreateTime: 2023/10/20 15:01
 * Description:
 */

public class AcceptCommand implements CommandExecutor {
    private final Plugin plugin;

    public AcceptCommand(Plugin plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player)sender;
            if (cmd.getName().equalsIgnoreCase("acctp")) {
                if (args.length == 0) {
                    return true;
                }

                if (args.length == 1) {
                    TpHandler.accTp(plugin, player, Bukkit.getPlayer(args[0]));
                }

                if (args.length == 2 && args[0].equalsIgnoreCase("here")) {
                    TpHandler.accTpHere(plugin, player, Bukkit.getPlayer(args[1]));
                }
            }

            return false;
        } else {
            sender.sendMessage("§4§l此命令仅游戏内玩家可用！");
            return false;
        }
    }
}
