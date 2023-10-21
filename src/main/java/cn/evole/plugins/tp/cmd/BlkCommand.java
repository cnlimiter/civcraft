package cn.evole.plugins.tp.cmd;

import cn.evole.plugins.tp.TpHandler;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * Name: civs-plugin / BlkCommand
 * Author: cnlimiter
 * CreateTime: 2023/10/20 15:08
 * Description:
 */

public class BlkCommand implements CommandExecutor {
    private final Plugin plugin;

    public BlkCommand(Plugin plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player)sender;
            if (cmd.getName().equalsIgnoreCase("bklist")) {
                if (args.length == 0) {
                    return true;
                }

                if (args.length == 1) {
                    TpHandler.bkList(player, Bukkit.getPlayer(args[0]));
                }
            }

            return false;
        } else {
            sender.sendMessage("§4§l此命令仅游戏内玩家可用！");
            return false;
        }
    }
}
