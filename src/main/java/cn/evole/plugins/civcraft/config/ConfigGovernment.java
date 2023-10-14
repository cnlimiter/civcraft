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
import cn.evole.plugins.civcraft.object.Civilization;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ConfigGovernment {

    public String id;
    public String displayName;
    public String require_tech;

    public double trade_rate;
    public double upkeep_rate;
    public double cottage_rate;
    public double growth_rate;
    public double culture_rate;
    public double hammer_rate;
    public double beaker_rate;
    public double maximum_tax_rate;

    public static void loadConfig(FileConfiguration cfg, Map<String, ConfigGovernment> government_map) {
        government_map.clear();
        List<Map<?, ?>> techs = cfg.getMapList("governments");
        for (Map<?, ?> level : techs) {
            ConfigGovernment gov = new ConfigGovernment();

            gov.id = (String) level.get("id");
            gov.displayName = (String) level.get("displayName");
            gov.require_tech = (String) level.get("require_tech");

            gov.trade_rate = (Double) level.get("trade_rate");
            gov.upkeep_rate = (Double) level.get("upkeep_rate");
            gov.cottage_rate = (Double) level.get("cottage_rate");
            gov.growth_rate = (Double) level.get("growth_rate");
            gov.culture_rate = (Double) level.get("culture_rate");
            gov.hammer_rate = (Double) level.get("hammer_rate");
            gov.beaker_rate = (Double) level.get("beaker_rate");
            gov.maximum_tax_rate = (Double) level.get("maximum_tax_rate");

            government_map.put(gov.id, gov);
        }
        CivLog.info("Loaded " + government_map.size() + " governments.");
    }

    public static ArrayList<ConfigGovernment> getAvailableGovernments(Civilization civ) {
        ArrayList<ConfigGovernment> govs = new ArrayList<ConfigGovernment>();

        for (ConfigGovernment gov : CivSettings.governments.values()) {
            if (gov.id.equalsIgnoreCase("gov_anarchy")) {
                continue;
            }
            if (gov.isAvailable(civ)) {
                govs.add(gov);
            }
        }

        return govs;
    }

    public static ConfigGovernment getGovernmentFromName(String string) {

        for (ConfigGovernment gov : CivSettings.governments.values()) {
            if (gov.id.equalsIgnoreCase("gov_anarchy")) {
                continue;
            }
            if (gov.displayName.equalsIgnoreCase(string)) {
                return gov;
            }
        }

        return null;
    }

    public boolean isAvailable(Civilization civ) {
        if (civ.hasTechnology(this.require_tech)) {
            return true;
        }
        return false;
    }

}
