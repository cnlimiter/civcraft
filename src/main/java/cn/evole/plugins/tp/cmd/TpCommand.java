package cn.evole.plugins.tp.cmd;

import cn.evole.plugins.tp.TpConfig;
import cn.evole.plugins.tp.TpHandler;
import cn.evole.plugins.tp.TpUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * Name: civs-plugin / TpCommand
 * Author: cnlimiter
 * CreateTime: 2023/10/20 15:02
 * Description:
 */

public class TpCommand implements CommandExecutor {
    private final Plugin plugin;

    public TpCommand(Plugin plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("tpa")) {
            if (!(sender instanceof Player)) {
                if (args.length == 1) {
                    if (args[0].equalsIgnoreCase("reload")) {
                        TpConfig.loading();
                        sender.sendMessage("§d+=================================================+");
                        sender.sendMessage("  §bCiv-TP §a插件已重载!    §b作者：§acnlimiter   §b版本：§fv1.0");
                        sender.sendMessage("§d+=================================================+");
                        return false;
                    } else {
                        return true;
                    }
                } else {
                    sender.sendMessage("§4§l此命令仅游戏内玩家可用！");
                    return true;
                }
            } else {
                Player player = (Player) sender;
                if (args.length == 0) {
                    TpUtil.SendChatMsg(player, "§b§l输入：§6/tpa 玩家名 §b§l请求传送到玩家那");
                    TpUtil.SendChatMsg(player, "§b§l输入：§6/tpa here 玩家名 §b§l请求传送玩家到你这");
                    return true;
                }

                Player tptoplayer;
                boolean playerisonline;
                if (args.length == 1) {
                    if (args[0].equalsIgnoreCase("reload") && player.isOp()) {
                        TpConfig.loading();
                        sender.sendMessage("§d+=================================================+");
                        sender.sendMessage("  §bCiv-TP §a插件已重载!    §b作者：§acnlimiter   §b版本：§fv1.0");
                        sender.sendMessage("§d+=================================================+");
                        return false;
                    }

                    tptoplayer = null;
                    playerisonline = false;

                    try {
                        tptoplayer = Bukkit.getServer().getPlayer(args[0]);
                        if (tptoplayer.isOnline()) {
                            playerisonline = true;
                        }
                    } catch (Exception ignored) {
                    }

                    if (player == tptoplayer) {
                        TpUtil.SendChatMsg(player, "§4§l不能TP自己");
                    } else if (playerisonline) {
                        TpHandler.tp(player, tptoplayer);
                    } else {
                        TpUtil.SendChatMsg(player, TpConfig.tpnooltipmsg);
                    }

                    return true;
                }

                if (args.length == 2) {
                    if (args[0].equalsIgnoreCase("here")) {
                        tptoplayer = null;
                        playerisonline = false;

                        try {
                            tptoplayer = Bukkit.getServer().getPlayer(args[1]);
                            if (tptoplayer.isOnline()) {
                                playerisonline = true;
                            }
                        } catch (Exception ignored) {
                        }

                        if (player == tptoplayer) {
                            TpUtil.SendChatMsg(player, "§4§l不能TP自己");
                        } else if (playerisonline) {
                            TpHandler.tpHere(player, tptoplayer);
                        } else {
                            TpUtil.SendChatMsg(player, TpConfig.tpnooltipmsg);
                        }

                        return false;
                    }

                    return true;
                }

                return false;
            }
        } else {
            sender.sendMessage("§4§l此命令仅游戏内玩家可用！");
            return false;
        }
    }
}
