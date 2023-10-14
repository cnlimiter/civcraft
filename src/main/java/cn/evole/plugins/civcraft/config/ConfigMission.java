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

public class ConfigMission {

    public String id;
    public String name;
    public List<String> description;
    public Double cost;
    public Double range;
    public Double cooldown;
    public Integer intel;
    public Integer slot;
    public Double fail_chance;
    public Integer length;
    public Double compromise_chance;

    public ConfigMission() {
    }

    public ConfigMission(ConfigMission mission) {
        this.id = mission.id;
        this.name = mission.name;
        this.description = mission.description;
        this.cost = mission.cost;
        this.range = mission.range;
        this.cooldown = mission.cooldown;
        this.intel = mission.intel;
    }

    @SuppressWarnings("unchecked")
    public static void loadConfig(FileConfiguration cfg, Map<String, ConfigMission> missions) {
        missions.clear();
        List<Map<?, ?>> configMissions = cfg.getMapList("missions");
        for (Map<?, ?> b : configMissions) {
            ConfigMission mission = new ConfigMission();
            mission.id = (String) b.get("id");
            mission.name = (String) b.get("name");
            mission.cost = (Double) b.get("cost");
            mission.range = (Double) b.get("range");
            mission.cooldown = (Double) b.get("cooldown");
            mission.intel = (Integer) b.get("intel");
            mission.length = (Integer) b.get("length");
            mission.fail_chance = (Double) b.get("fail_chance");
            mission.compromise_chance = (Double) b.get("compromise_chance");
            mission.slot = (Integer) b.get("slot");
            mission.description = (List<String>) b.get("description");

            missions.put(mission.id.toLowerCase(), mission);
        }

        CivLog.info("Loaded " + missions.size() + " Espionage Missions.");
    }

}
