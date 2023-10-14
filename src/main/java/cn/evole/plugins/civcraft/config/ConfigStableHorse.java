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

public class ConfigStableHorse {
    public int id;
    public double speed;
    public double jump;
    public double health;
    public boolean mule;
    public String variant;
    public String name;

    public static void loadConfig(FileConfiguration cfg, Map<Integer, ConfigStableHorse> horses) {
        horses.clear();
        List<Map<?, ?>> config_horses = cfg.getMapList("stable_horses");
        for (Map<?, ?> level : config_horses) {
            ConfigStableHorse horse = new ConfigStableHorse();
            horse.id = (Integer) level.get("id");
            horse.speed = (Double) level.get("speed");
            horse.jump = (Double) level.get("jump");
            horse.health = (Double) level.get("health");
            horse.variant = (String) level.get("variant");
            horse.name = (String) level.get("name");

            Boolean mule = (Boolean) level.get("mule");
            if (mule == null || !mule) {
                horse.mule = false;
            } else {
                horse.mule = true;
            }

            horses.put(horse.id, horse);
        }
        CivLog.info("Loaded " + horses.size() + " Horses.");
    }

}
