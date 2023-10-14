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

public class ConfigTownLevel {
    public int level;
    public String title;
    public double upkeep;
    public int plots;
    public double plot_cost;
    public int tile_improvements;


    public static void loadConfig(FileConfiguration cfg, Map<Integer, ConfigTownLevel> levels) {
        levels.clear();
        List<Map<?, ?>> culture_levels = cfg.getMapList("town_levels");
        for (Map<?, ?> level : culture_levels) {
            ConfigTownLevel town_level = new ConfigTownLevel();
            town_level.level = (Integer) level.get("level");
            town_level.title = (String) level.get("title");
            town_level.upkeep = (Double) level.get("upkeep");
            town_level.plots = (Integer) level.get("plots");
            town_level.plot_cost = (Double) level.get("plot_cost");
            town_level.tile_improvements = (Integer) level.get("tile_improvements");

            levels.put(town_level.level, town_level);
        }
        CivLog.info("Loaded " + levels.size() + " town levels.");
    }

}
