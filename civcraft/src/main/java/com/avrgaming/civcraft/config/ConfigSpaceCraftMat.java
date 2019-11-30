// 
// Decompiled by Procyon v0.5.30
// 

package com.avrgaming.civcraft.config;

import com.avrgaming.civcraft.main.CivLog;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigSpaceCraftMat {
    public String originalCraftMat;
    public String civcraftComponents;
    public HashMap<Integer, ConfigSpaceCraftMat2> minecraftComponents;

    @SuppressWarnings("unchecked")
    public static void loadConfig(final FileConfiguration cfg, final Map<String, ConfigSpaceCraftMat> crafts) {
        crafts.clear();
        final List<Map<?, ?>> spaceCrafts = (List<Map<?, ?>>) cfg.getMapList("space_crafts");
        for (final Map<?, ?> level : spaceCrafts) {
            final ConfigSpaceCraftMat configSpaceCraftMat = new ConfigSpaceCraftMat();
            configSpaceCraftMat.originalCraftMat = (String) level.get("originalCraftMat");
            configSpaceCraftMat.civcraftComponents = (String) level.get("civcraftComponents");
            final List<Map<?, ?>> configSpaceCraftMat2 = (List<Map<?, ?>>) level.get("minecraftComponents");
            if (configSpaceCraftMat2 != null) {
                configSpaceCraftMat.minecraftComponents = new HashMap<Integer, ConfigSpaceCraftMat2>();
                for (final Map<?, ?> ingred : configSpaceCraftMat2) {
                    final ConfigSpaceCraftMat2 ingredient = new ConfigSpaceCraftMat2();
                    ingredient.typeID = (Integer) ingred.get("type_id");
                    ingredient.data = (Integer) ingred.get("data");
                    ingredient.name = (String) ingred.get("name");
                    ingredient.count = (Integer) ingred.get("count");
                    configSpaceCraftMat.minecraftComponents.put(ingredient.typeID, ingredient);
                }
            }
            crafts.put(configSpaceCraftMat.originalCraftMat, configSpaceCraftMat);
        }
        CivLog.info("Loaded " + crafts.size() + "Spacecraft Materials");
    }
}
