package cn.evole.plugins.tp;

import org.bukkit.entity.Player;

/**
 * Name: civs-plugin / TpUtil
 * Author: cnlimiter
 * CreateTime: 2023/10/20 14:45
 * Description:
 */

public class TpUtil {
    public static void SendChatMsg(Player player, String message) {
        message = (String.valueOf(TpConfig.prefix) + message).replace("&", "");
                player.sendMessage(message);
    }

    public static String FormatText(String message) {
        return message.replace("&", "");
    }
}
