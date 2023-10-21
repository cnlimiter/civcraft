package cn.evole.plugins.tp;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

/**
 * Name: civs-plugin / TpConfig
 * Author: cnlimiter
 * CreateTime: 2023/10/20 14:46
 * Description:
 */

public class TpConfig {
    public static int tpcd;

    public static int tpsftime;

    public static int tpouttime;

    public static String prefix;

    public static String tpoktipmsg;

    public static String tpnooltipmsg;

    public static String noacctptipmsg;

    public static String okacctptipmsg;

    public static String tpcdtipmsg;

    public static String newtptipmsg;

    public static String newtpheretipmsg;

    public static String newtptipmsg_top;

    public static String newtptipmsg_bottom;

    public static void loading() {
        File file = new File("./plugins/CivCraft/tp/tp_config.yml");
        YamlConfiguration fd = YamlConfiguration.loadConfiguration(file);
        tpcd = fd.getInt("tpcd");
        tpsftime = fd.getInt("tpsftime");
        tpouttime = fd.getInt("tpouttime");
        tpoktipmsg = fd.getString("tpoktipmsg");
        tpnooltipmsg = fd.getString("tpnooltipmsg");
        prefix = fd.getString("prefix");
        noacctptipmsg = fd.getString("noacctptipmsg");
        okacctptipmsg = fd.getString("okacctptipmsg");
        tpcdtipmsg = fd.getString("tpcdtipmsg");
        newtptipmsg = fd.getString("newtptipmsg");
        newtpheretipmsg = fd.getString("newtpheretipmsg");
        newtptipmsg_top = fd.getString("newtptipmsg_top");
        newtptipmsg_bottom = fd.getString("newtptipmsg_bottom");
    }
}
