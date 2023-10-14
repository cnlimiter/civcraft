// 
// Decompiled by Procyon v0.5.30
// 

package cn.evole.plugins.civcraft.config;

import cn.evole.plugins.civcraft.main.CivLog;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;
import java.util.Map;

public class ConfigAlchemistHouseLevel {
    public int level;
    public String itemName;
    public int itemId;
    public int itemData;
    public int amount;
    public double price;

    public static void loadConfig(final FileConfiguration cfg, final Map<Integer, ConfigAlchemistHouseLevel> levels) {
        levels.clear();
        final List<Map<?, ?>> culture_levels = cfg.getMapList("alchemisthouse_levels");
        for (final Map<?, ?> level : culture_levels) {
            final ConfigAlchemistHouseLevel alchemisthouse_level = new ConfigAlchemistHouseLevel();
            alchemisthouse_level.level = (Integer) level.get("level");
            alchemisthouse_level.itemName = (String) level.get("itemName");
            alchemisthouse_level.itemId = (Integer) level.get("itemId");
            alchemisthouse_level.itemData = (Integer) level.get("itemData");
            alchemisthouse_level.amount = (Integer) level.get("amount");
            alchemisthouse_level.price = (Double) level.get("price");
            levels.put(alchemisthouse_level.level, alchemisthouse_level);
        }
        CivLog.info("Loaded " + levels.size() + " alchemisthouse levels.");
    }
}
