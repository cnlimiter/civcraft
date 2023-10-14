package cn.evole.plugins.civcraft.config;
/**
 * [2023] Cnlimiter LLC
 * All Rights Reserved.
 * Name: civcraft
 * Author: cnlimiter
 * UpdateTime: 2023/10/14 16:15
 * Description:
 * License: ARR
 */


import cn.evole.plugins.civcraft.main.CivLog;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ConfigFishing {
    public String craftMatId;
    public int type_id;
    public double drop_chance;

    public static void loadConfig(FileConfiguration cfg, ArrayList<ConfigFishing> configList) {
        configList.clear();
        List<Map<?, ?>> drops = cfg.getMapList("fishing_drops");
        for (Map<?, ?> item : drops) {
            ConfigFishing g = new ConfigFishing();

            g.craftMatId = (String) item.get("craftMatId");
            g.type_id = (Integer) item.get("type_id");
            g.drop_chance = (Double) item.get("drop_chance");

            configList.add(g);

        }
        CivLog.info("Loaded " + configList.size() + " fishing drops.");

    }

}


