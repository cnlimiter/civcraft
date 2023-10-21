package cn.evole.plugins.tp;

import cn.evole.plugins.tp.cmd.*;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

/**
 * Name: civs-plugin / TpMain
 * Author: cnlimiter
 * CreateTime: 2023/10/20 15:10
 * Description:
 */

public class TpMain {
    private final JavaPlugin plugin;
    public TpMain(JavaPlugin plugin){
        this.plugin = plugin;
    }
    public void onLoad() {
        plugin.getServer().getConsoleSender().sendMessage("§d+=================================================+");
        plugin.getServer().getConsoleSender().sendMessage("  §bCiv-TP §a插件已加载!    §b作者：§acnlimiter   §b版本：§fv1.0");
        plugin.getServer().getConsoleSender().sendMessage("§d+=================================================+");
    }

    public void onEnable() {
        if (!(new File("./plugins/CivCraft/tp/tp_config.yml")).exists()) {
            plugin.saveDefaultConfig();
        }

        TpConfig.loading();
        plugin.getCommand("tpa").setExecutor(new TpCommand(plugin));
        plugin.getCommand("acctp").setExecutor(new AcceptCommand(plugin));
        plugin.getCommand("noacctp").setExecutor(new AcceptNoCommand(plugin));
        plugin.getCommand("bklist").setExecutor(new BlkCommand(plugin));
        plugin.getCommand("bklistde").setExecutor(new BlkDelCommand(plugin));
        plugin.getServer().getConsoleSender().sendMessage("§d+=================================================+");
        plugin.getServer().getConsoleSender().sendMessage("  §bCiv-TP §a插件已启用!    §b作者：§acnlimiter   §b版本：§fv1.0");
        plugin.getServer().getConsoleSender().sendMessage("§d+=================================================+");
    }

    public void onDisable() {
        plugin.getServer().getConsoleSender().sendMessage("§d+=================================================+");
        plugin.getServer().getConsoleSender().sendMessage("  §bCiv-TP §a插件已卸载!    §b作者：§acnlimiter   §b版本：§fv1.0");
        plugin.getServer().getConsoleSender().sendMessage("§d+=================================================+");
    }
}
