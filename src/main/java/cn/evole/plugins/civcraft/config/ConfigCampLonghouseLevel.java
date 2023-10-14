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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class ConfigCampLonghouseLevel {
    public int level;            /* Current level number */
    public Map<Integer, Integer> consumes; /* A map of block ID's and amounts required for this level to progress */
    public int count; /* Number of times that consumes must be met to level up */
    public double coins; /* Coins generated each time for the cottage */

    public ConfigCampLonghouseLevel() {

    }

    public ConfigCampLonghouseLevel(ConfigCampLonghouseLevel currentlvl) {
        this.level = currentlvl.level;
        this.count = currentlvl.count;
        this.coins = currentlvl.coins;

        this.consumes = new HashMap<Integer, Integer>();
        for (Entry<Integer, Integer> entry : currentlvl.consumes.entrySet()) {
            this.consumes.put(entry.getKey(), entry.getValue());
        }

    }


    public static void loadConfig(FileConfiguration cfg, Map<Integer, ConfigCampLonghouseLevel> longhouse_levels) {
        longhouse_levels.clear();
        List<Map<?, ?>> list = cfg.getMapList("longhouse_levels");
        Map<Integer, Integer> consumes_list = null;
        for (Map<?, ?> cl : list) {
            List<?> consumes = (List<?>) cl.get("consumes");
            if (consumes != null) {
                consumes_list = new HashMap<Integer, Integer>();
                for (int i = 0; i < consumes.size(); i++) {
                    String line = (String) consumes.get(i);
                    String split[];
                    split = line.split(",");
                    consumes_list.put(Integer.valueOf(split[0]), Integer.valueOf(split[1]));
                }
            }


            ConfigCampLonghouseLevel level = new ConfigCampLonghouseLevel();
            level.level = (Integer) cl.get("level");
            level.consumes = consumes_list;
            level.count = (Integer) cl.get("count");
            level.coins = (Double) cl.get("coins");

            longhouse_levels.put(level.level, level);

        }
        CivLog.info("Loaded " + longhouse_levels.size() + " longhouse levels.");
    }
}
