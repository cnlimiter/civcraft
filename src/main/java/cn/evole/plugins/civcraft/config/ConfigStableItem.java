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
import java.util.Set;

public class ConfigStableItem {
    public String name;
    public double cost;
    public int store_id;
    public int item_id;
    public int horse_id;

    public static void loadConfig(FileConfiguration cfg, Set<ConfigStableItem> items) {
        items.clear();
        List<Map<?, ?>> cfg_items = cfg.getMapList("stable_items");
        for (Map<?, ?> level : cfg_items) {
            ConfigStableItem itm = new ConfigStableItem();

            itm.name = (String) level.get("name");
            itm.cost = (Double) level.get("cost");
            itm.store_id = (Integer) level.get("store_id");
            itm.item_id = (Integer) level.get("item_id");
            itm.horse_id = (Integer) level.get("horse_id");

            items.add(itm);
        }
        CivLog.info("Loaded " + items.size() + " stable items.");
    }

}
