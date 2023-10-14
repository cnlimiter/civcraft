// 
// Decompiled by Procyon v0.5.30
// 

package cn.evole.plugins.civcraft.config;

import cn.evole.plugins.civcraft.main.CivLog;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;
import java.util.Map;

public class ConfigSpaceRocket {
    public int level;
    public String name;
    public String components;

    public static void loadConfig(final FileConfiguration cfg, final Map<Integer, ConfigSpaceRocket> names) {
        names.clear();
        final List<Map<?, ?>> spaceRocket_names = (List<Map<?, ?>>) cfg.getMapList("spacerocket_names");
        for (final Map<?, ?> level : spaceRocket_names) {
            final ConfigSpaceRocket spaceRocket_name = new ConfigSpaceRocket();
            spaceRocket_name.level = (Integer) level.get("level");
            spaceRocket_name.name = (String) level.get("name");
            spaceRocket_name.components = (String) level.get("components");
            names.put(spaceRocket_name.level, spaceRocket_name);
        }
        CivLog.info("Loaded" + names.size() + " Space Rockets.");
    }
}
