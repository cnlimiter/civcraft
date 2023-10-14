// 
// Decompiled by Procyon v0.5.30
// 

package cn.evole.plugins.civcraft.config;

import cn.evole.plugins.civcraft.main.CivLog;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;
import java.util.Map;

public class ConfigLevelTalent {
    public int level;
    public String levelName;
    public String levelBuff1;
    public String levelBuff2;
    public String levelBuff3;
    public String levelBuffDesc1;
    public String levelBuffDesc2;
    public String levelBuffDesc3;

    public static void loadConfig(final FileConfiguration cfg, final Map<Integer, ConfigLevelTalent> levels) {
        levels.clear();
        final List<Map<?, ?>> talent_levels = (List<Map<?, ?>>) cfg.getMapList("talent_levels");
        for (final Map<?, ?> level : talent_levels) {
            final ConfigLevelTalent talent_level = new ConfigLevelTalent();
            talent_level.level = (Integer) level.get("level");
            talent_level.levelName = (String) level.get("levelName");
            talent_level.levelBuff1 = (String) level.get("levelBuff1");
            talent_level.levelBuff2 = (String) level.get("levelBuff2");
            talent_level.levelBuff3 = (String) level.get("levelBuff3");
            talent_level.levelBuffDesc1 = (String) level.get("levelBuffDesc1");
            talent_level.levelBuffDesc2 = (String) level.get("levelBuffDesc2");
            talent_level.levelBuffDesc3 = (String) level.get("levelBuffDesc3");
            levels.put(talent_level.level, talent_level);
        }
        CivLog.info("Loaded " + levels.size() + " Talents.");
    }
}
