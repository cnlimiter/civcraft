// 
// Decompiled by Procyon v0.5.30
// 

package com.avrgaming.civcraft.config;

import com.avrgaming.civcraft.main.CivLog;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;
import java.util.Map;

public class ConfigAlchLevel {
    public int level;
    public String itemName;
    public int itemId;
    public int itemData;
    public int amount;
    public double price;

    public static void loadConfig(final FileConfiguration cfg, final Map<Integer, ConfigAlchLevel> levels) {
        levels.clear();
        List<Map<?, ?>> culture_levels = cfg.getMapList("alch_levels");
        for (final Map<?, ?> level : culture_levels) {
            ConfigAlchLevel alch_level = new ConfigAlchLevel();
            alch_level.level = (Integer) level.get("level");
            alch_level.itemName = (String) level.get("itemName");
            alch_level.itemId = (Integer) level.get("itemId");
            alch_level.itemData = (Integer) level.get("itemData");
            alch_level.amount = (Integer) level.get("amount");
            alch_level.price = (Double) level.get("price");
            levels.put(alch_level.level, alch_level);
        }
        CivLog.info("Loaded " + levels.size() + " alch levels.");
    }
}
