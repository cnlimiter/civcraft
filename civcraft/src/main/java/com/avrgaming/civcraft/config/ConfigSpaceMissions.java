// 
// Decompiled by Procyon v0.5.30
// 

package com.avrgaming.civcraft.config;

import com.avrgaming.civcraft.main.CivLog;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;
import java.util.Map;

public class ConfigSpaceMissions
{
    public int level;
    public String name;
    public String require_hammers;
    public String require_beakers;
    
    public static void loadConfig(final FileConfiguration cfg, final Map<Integer, ConfigSpaceMissions> levels) {
        levels.clear();
        final List<Map<?, ?>> spacemissions_levels = (List<Map<?, ?>>)cfg.getMapList("spacemissions_levels");
        for (final Map<?, ?> level : spacemissions_levels) {
            final ConfigSpaceMissions spaceMission_level = new ConfigSpaceMissions();
            spaceMission_level.level = (Integer)level.get("level");
            spaceMission_level.name = (String)level.get("name");
            spaceMission_level.require_hammers = (String)level.get("require_hammers");
            spaceMission_level.require_beakers = (String)level.get("require_beakers");
            levels.put(spaceMission_level.level, spaceMission_level);
        }
        CivLog.info("Loaded " + levels.size() + " Space Missions.");
    }
}
