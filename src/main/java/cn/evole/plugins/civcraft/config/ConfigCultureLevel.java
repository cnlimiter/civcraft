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
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;
import java.util.Map;

public class ConfigCultureLevel {
    public int level;
    public int amount;
    public int chunks;

    public static void loadConfig(FileConfiguration cfg, Map<Integer, ConfigCultureLevel> levels) {
        levels.clear();
        List<Map<?, ?>> culture_levels = cfg.getMapList("culture_levels");
        for (Map<?, ?> level : culture_levels) {
            ConfigCultureLevel culture_level = new ConfigCultureLevel();
            culture_level.level = (Integer) level.get("level");
            culture_level.amount = (Integer) level.get("amount");
            culture_level.chunks = (Integer) level.get("chunks");
            levels.put(culture_level.level, culture_level);
        }
        CivLog.info("Loaded " + levels.size() + " culture levels.");
    }
}
