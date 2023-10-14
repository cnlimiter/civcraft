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

public class ConfigHemisphere {
    public String id;
    public int x_min;
    public int x_max;
    public int z_min;
    public int z_max;


    public static void loadConfig(FileConfiguration cfg, Map<String, ConfigHemisphere> hemis) {
        hemis.clear();
        List<Map<?, ?>> configHemis = cfg.getMapList("hemispheres");
        for (Map<?, ?> b : configHemis) {
            ConfigHemisphere buff = new ConfigHemisphere();
            buff.id = (String) b.get("id");
            buff.x_min = (Integer) b.get("x_min");
            buff.x_max = (Integer) b.get("x_max");
            buff.z_min = (Integer) b.get("z_min");
            buff.z_max = (Integer) b.get("z_max");
            hemis.put(buff.id, buff);
        }

        CivLog.info("Loaded " + hemis.size() + " Hemispheres.");
    }

}
