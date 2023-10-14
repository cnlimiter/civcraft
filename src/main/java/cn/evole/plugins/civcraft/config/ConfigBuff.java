/**
 * [2023] Cnlimiter LLC
 * All Rights Reserved.
 * Name: civcraft
 * Author: cnlimiter
 * UpdateTime: 2023/10/14 16:15
 * Description:
 * License: ARR
 */
package cn.evole.plugins.civcraft.config;

import cn.evole.plugins.civcraft.main.CivLog;
import cn.evole.plugins.civcraft.util.CivColor;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;
import java.util.Map;

public class ConfigBuff {
    public String id;
    public String name;
    public String description;
    public String value;
    public boolean stackable;

    public static void loadConfig(FileConfiguration cfg, Map<String, ConfigBuff> buffs) {
        buffs.clear();
        List<Map<?, ?>> configBuffs = cfg.getMapList("buffs");
        for (Map<?, ?> b : configBuffs) {
            ConfigBuff buff = new ConfigBuff();
            buff.id = (String) b.get("id");
            buff.name = (String) b.get("name");

            buff.description = (String) b.get("description");
            buff.description = CivColor.colorize(buff.description);

            buff.value = (String) b.get("value");
            buff.stackable = (Boolean) b.get("stackable");

            buffs.put(buff.id, buff);
        }

        CivLog.info("Loaded " + buffs.size() + " Buffs.");
    }
}
