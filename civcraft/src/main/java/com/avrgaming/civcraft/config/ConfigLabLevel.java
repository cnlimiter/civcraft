// 
// Decompiled by Procyon v0.5.30
// 

package com.avrgaming.civcraft.config;

import com.avrgaming.civcraft.main.CivLog;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;
import java.util.Map;

public class ConfigLabLevel {
    public int level;
    public int amount;
    public int count;
    public double beakers;

    public static void loadConfig(final FileConfiguration cfg, final Map<Integer, ConfigLabLevel> levels) {
        levels.clear();
        final List<Map<?, ?>> mine_levels = (List<Map<?, ?>>) cfg.getMapList("lab_levels");
        for (final Map<?, ?> level : mine_levels) {
            final ConfigLabLevel mine_level = new ConfigLabLevel();
            mine_level.level = (Integer) level.get("level");
            mine_level.amount = (Integer) level.get("amount");
            mine_level.beakers = (Double) level.get("beakers");
            mine_level.count = (Integer) level.get("count");
            levels.put(mine_level.level, mine_level);
        }
        CivLog.info("Loaded " + levels.size() + " lab levels.");
    }
}
