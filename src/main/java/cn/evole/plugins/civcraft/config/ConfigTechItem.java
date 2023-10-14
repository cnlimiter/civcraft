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

public class ConfigTechItem {

    public int id;
    public String name;
    public String require_tech;

    public static void loadConfig(FileConfiguration cfg, Map<Integer, ConfigTechItem> tech_maps) {
        tech_maps.clear();
        List<Map<?, ?>> techs = cfg.getMapList("items");
        for (Map<?, ?> confTech : techs) {
            ConfigTechItem tech = new ConfigTechItem();

            tech.id = (Integer) confTech.get("id");
            tech.name = (String) confTech.get("name");
            tech.require_tech = (String) confTech.get("require_tech");
            tech_maps.put(tech.id, tech);
        }
        CivLog.info("Loaded " + tech_maps.size() + " technologies.");
    }

}
