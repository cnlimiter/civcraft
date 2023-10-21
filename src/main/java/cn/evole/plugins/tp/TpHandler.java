package cn.evole.plugins.tp;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;

/**
 * Name: civs-plugin / TpHandler
 * Author: cnlimiter
 * CreateTime: 2023/10/20 14:43
 * Description:
 */

public class TpHandler {
    public static void noAccTp(Player player, Player noacctpplayer) {
        int outtp = Session.Get(player.getName() + "_newtp_" + noacctpplayer.getName());
        if (outtp == 2) {
            Session.del(player.getName() + "_newtp_" + noacctpplayer.getName());
            TpUtil.SendChatMsg(player, "&a已拒绝对方的请求");
            if (!TpConfig.noacctptipmsg.equals("-debug")) {
                TpUtil.SendChatMsg(noacctpplayer, TpConfig.noacctptipmsg);
            }
        } else if (outtp == 1) {
            TpUtil.SendChatMsg(player, "&c对方的TP请求已超时");
        } else {
            TpUtil.SendChatMsg(player, "&c没有这个TP请求，或者请求已处理");
        }

    }

    public static void accTp(Plugin plugin, Player player, Player acctpplayer) {
        int outtp = Session.Get(player.getName() + "_newtp_" + acctpplayer.getName());
        if (outtp == 2) {
            boolean iscan = false;

            try {
                iscan = acctpplayer.isOnline();
            } catch (Exception ignored) {
            }

            if (iscan) {
                TpUtil.SendChatMsg(acctpplayer, TpConfig.okacctptipmsg);
                TpUtil.SendChatMsg(player, "&a已接受：" + acctpplayer.getName() + "的TP请求");
                (new BukkitRunnable() {
                    public void run() {
                        acctpplayer.teleport(player.getLocation());
                        Session.del(player.getName() + "_newtp_" + acctpplayer.getName());
                    }
                }).runTaskLater(plugin, (long)(TpConfig.tpsftime * 20L));
            } else {
                TpUtil.SendChatMsg(player, "&c对方不在线了");
            }
        } else if (outtp == 1) {
            TpUtil.SendChatMsg(player, "&c对方的TP请求已超时");
        } else {
            TpUtil.SendChatMsg(player, "&c没有这个TP请求，或者请求已处理");
        }

    }

    public static void accTpHere(Plugin plugin, Player player, Player acctpplayer) {
        int outtp = Session.Get(player.getName() + "_newtp_" + acctpplayer.getName());
        if (outtp == 2) {
            boolean iscan = false;

            try {
                iscan = acctpplayer.isOnline();
            } catch (Exception var5) {
            }

            if (iscan) {
                TpUtil.SendChatMsg(acctpplayer, TpConfig.okacctptipmsg);
                TpUtil.SendChatMsg(player, "&a已接受：" + acctpplayer.getName() + "的TP请求");
                (new BukkitRunnable() {
                    public void run() {
                        player.teleport(acctpplayer.getLocation());
                        Session.del(player.getName() + "_newtp_" + acctpplayer.getName());
                    }
                }).runTaskLater(plugin, TpConfig.tpsftime * 20L);
            } else {
                TpUtil.SendChatMsg(player, "&c对方不在线了");
            }
        } else if (outtp == 1) {
            TpUtil.SendChatMsg(player, "&c对方的TP请求已超时");
        } else {
            TpUtil.SendChatMsg(player, "&c没有这个TP请求，或者请求已处理");
        }

    }

    public static void tp(Player player, Player toplayer) {
        int cd = Session.Get(player.getName() + "_btp");
        if (cd == 2) {
            TpUtil.SendChatMsg(player, TpConfig.tpcdtipmsg);
        } else {
            Session.Set(player.getName() + "_btp", TpConfig.tpcd * 1000L);
            if (isBlacklist(toplayer, player)) {
                TpUtil.SendChatMsg(player, TpConfig.tpoktipmsg);
            } else {
                Session.Set(toplayer.getName() + "_newtp_" + player.getName(), TpConfig.tpouttime * 1000L);
                String newtptipmsg = TpConfig.newtptipmsg;
                newtptipmsg = newtptipmsg.replace("%p%", player.getName());
                TextComponent messagetp = new TextComponent(newtptipmsg);
                TextComponent spacestr = new TextComponent(" ");
                TextComponent accmsg = new TextComponent("[接受]");
                accmsg.setColor(ChatColor.GREEN);
                accmsg.setBold(true);
                accmsg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/acctp " + player.getName()));
                accmsg.setHoverEvent(new HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT, (new ComponentBuilder("§a接受TP，对方将传送到你身边！")).create()));
                TextComponent noaccmsg = new TextComponent("[拒绝]");
                noaccmsg.setColor(ChatColor.RED);
                noaccmsg.setBold(true);
                noaccmsg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/noacctp " + player.getName()));
                noaccmsg.setHoverEvent(new HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT, (new ComponentBuilder("§c拒绝TP，对方的请求失败！")).create()));
                TextComponent bklmsg = new TextComponent("[黑名单]");
                bklmsg.setColor(ChatColor.YELLOW);
                bklmsg.setBold(true);
                bklmsg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/bklist " + player.getName()));
                bklmsg.setHoverEvent(new HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT, (new ComponentBuilder("§c该玩家以后的传送消息都将不会提示\n§b使用指令 §6/bklistde 玩家名 \n§c可以从你的黑名单中删除这个玩家！")).create()));
                messagetp.addExtra(spacestr);
                messagetp.addExtra(accmsg);
                messagetp.addExtra(spacestr);
                messagetp.addExtra(noaccmsg);
                messagetp.addExtra(spacestr);
                messagetp.addExtra(bklmsg);
                TpUtil.SendChatMsg(toplayer, TpConfig.newtptipmsg_top);
                toplayer.spigot().sendMessage(messagetp);
                TpUtil.SendChatMsg(toplayer, TpConfig.newtptipmsg_bottom);
                TpUtil.SendChatMsg(player, TpConfig.tpoktipmsg);
            }
        }

    }

    public static void tpHere(Player player, Player toplayer) {
        int cd = Session.Get(player.getName() + "_btp");
        if (cd == 2) {
            TpUtil.SendChatMsg(player, TpConfig.tpcdtipmsg);
        } else {
            Session.Set(player.getName() + "_btp", TpConfig.tpcd * 1000L);
            if (isBlacklist(toplayer, player)) {
                TpUtil.SendChatMsg(player, TpConfig.tpoktipmsg);
            } else {
                Session.Set(toplayer.getName() + "_newtp_" + player.getName(), TpConfig.tpouttime * 1000L);
                String newtpheretipmsg = TpConfig.newtpheretipmsg;
                newtpheretipmsg = newtpheretipmsg.replace("%p%", player.getName());
                TextComponent messagetp = new TextComponent(newtpheretipmsg);
                TextComponent spacestr = new TextComponent(" ");
                TextComponent accmsg = new TextComponent("[接受]");
                accmsg.setColor(ChatColor.GREEN);
                accmsg.setBold(true);
                accmsg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/acctp here " + player.getName()));
                accmsg.setHoverEvent(new HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT, (new ComponentBuilder("§a接受TP，你将传送到对方身边！")).create()));
                TextComponent noaccmsg = new TextComponent("[拒绝]");
                noaccmsg.setColor(ChatColor.RED);
                noaccmsg.setBold(true);
                noaccmsg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/noacctp " + player.getName()));
                noaccmsg.setHoverEvent(new HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT, (new ComponentBuilder("§c拒绝TP，对方的请求失败！")).create()));
                TextComponent bklmsg = new TextComponent("[黑名单]");
                bklmsg.setColor(ChatColor.YELLOW);
                bklmsg.setBold(true);
                bklmsg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/bklist " + player.getName()));
                bklmsg.setHoverEvent(new HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT, (new ComponentBuilder("§c该玩家以后的传送消息都将不会提示\n§b使用指令 §6/bklistde 玩家名 \n§c可以从你的黑名单中删除这个玩家！")).create()));
                messagetp.addExtra(spacestr);
                messagetp.addExtra(accmsg);
                messagetp.addExtra(spacestr);
                messagetp.addExtra(noaccmsg);
                messagetp.addExtra(spacestr);
                messagetp.addExtra(bklmsg);
                TpUtil.SendChatMsg(toplayer, TpConfig.newtptipmsg_top);
                toplayer.spigot().sendMessage(messagetp);
                TpUtil.SendChatMsg(toplayer, TpConfig.newtptipmsg_bottom);
                TpUtil.SendChatMsg(player, TpConfig.tpoktipmsg);
            }
        }

    }

    public static boolean isBlacklist(Player player, Player isplayer2) {
        return (new File("./plugins/CivCraft/tp/BlackList/" + player.getName() + "/" + isplayer2.getName() + ".yml")).exists();
    }

    public static void bkList(Player player, Player isplayer2) {
        try {
            if (player == isplayer2) {
                TpUtil.SendChatMsg(player, "&c加入黑名单失败！不能把自己加入黑名单！");
            } else {
                int outtp = Session.Get(player.getName() + "_newtp_" + isplayer2.getName());
                if (outtp == 2) {
                    Session.del(player.getName() + "_newtp_" + isplayer2.getName());
                }

                if (!(new File("./plugins/CivCraft/tp/BlackList/" + player.getName() + "/" + isplayer2.getName() + ".yml")).exists()) {
                    File file = new File("./plugins/CivCraft/tp/BlackList/" + player.getName() + "/" + isplayer2.getName() + ".yml");
                    YamlConfiguration yd = YamlConfiguration.loadConfiguration(file);
                    yd.set("check", true);
                    yd.save(file);
                    TpUtil.SendChatMsg(player, "&a加入黑名单成功！");
                } else {
                    TpUtil.SendChatMsg(player, "&c加入黑名单失败！该玩家已在黑名单列表内！");
                }
            }
        } catch (Exception var5) {
            TpUtil.SendChatMsg(player, "&c加入黑名单失败！错误代码：IO");
        }

    }

    public static void blackListDel(Player player, Player isplayer2) {
        if (!(new File("./plugins/TPAPro/BlackList/" + player.getName() + "/" + isplayer2.getName() + ".yml")).exists()) {
            TpUtil.SendChatMsg(player, "&c删除失败！该玩家不在黑名单内！");
        } else {
            File file = new File("./plugins/TPAPro/BlackList/" + player.getName() + "/" + isplayer2.getName() + ".yml");
            if (file.delete()) {
                TpUtil.SendChatMsg(player, "&a删除成功！");
            } else {
                TpUtil.SendChatMsg(player, "&c删除失败！错误代码：IO");
            }
        }

    }

}
