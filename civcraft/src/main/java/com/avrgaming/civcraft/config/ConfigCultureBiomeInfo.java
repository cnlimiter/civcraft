package com.avrgaming.civcraft.config;

import com.avrgaming.civcraft.main.CivLog;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;
import java.util.Map;

public class ConfigCultureBiomeInfo {
    public String name;
    public String description;
    public double hammers;
    public double growth;
    public double happiness;
    public double beakers;

    public static void loadConfig(FileConfiguration cfg, Map<String, ConfigCultureBiomeInfo> culture_biomes) {
        culture_biomes.clear();
        List<Map<?, ?>> list = cfg.getMapList("culture_biomes");
        for (Map<?, ?> cl : list) {

            ConfigCultureBiomeInfo biome = new ConfigCultureBiomeInfo();
            biome.name = (String) cl.get("name");
            biome.description = (String) cl.get("description");
            biome.hammers = (Double) cl.get("hammers");
            biome.growth = (Double) cl.get("growth");
            biome.happiness = (Double) cl.get("happiness");
            biome.beakers = (Double) cl.get("beakers");

            culture_biomes.put(biome.name, biome);
        }
        CivLog.info("Loaded " + culture_biomes.size() + " Culture Biomes.");
    }
}
