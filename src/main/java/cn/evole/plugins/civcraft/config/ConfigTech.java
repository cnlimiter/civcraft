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

import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.main.CivLog;
import cn.evole.plugins.civcraft.main.CivMessage;
import cn.evole.plugins.civcraft.object.Civilization;
import cn.evole.plugins.civcraft.object.Town;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ConfigTech {
    public String id;
    public String name;
    public double beaker_cost;
    public double cost;
    public String require_techs;
    public int era;
    public Integer points;
    public List<String> info;

    public static void loadConfig(FileConfiguration cfg, Map<String, ConfigTech> tech_maps) {
        tech_maps.clear();
        List<Map<?, ?>> techs = cfg.getMapList("techs");
        for (Map<?, ?> confTech : techs) {
            ConfigTech tech = new ConfigTech();

            tech.id = (String) confTech.get("id");
            tech.name = (String) confTech.get("name");
            tech.beaker_cost = (Double) confTech.get("beaker_cost");
            tech.cost = (Double) confTech.get("cost");
            tech.era = (Integer) confTech.get("era");
            tech.require_techs = (String) confTech.get("require_techs");
            tech.points = (Integer) confTech.get("points");
            List<String> info = (List<String>) confTech.get("info");

            if (info != null) {
                tech.info = new ArrayList<>(info);
            }

            tech_maps.put(tech.id, tech);
        }
        CivLog.info("Loaded " + tech_maps.size() + " technologies.");
    }

    public static double eraRate(Civilization civ) {
        double rate = 0.0;
        double era = (CivGlobal.highestCivEra - 1) - civ.getCurrentEra();
        if (era > 0) {
            rate = (era / 10);
        }
        return rate;
    }

    public static ArrayList<ConfigTech> getAvailableTechs(Civilization civ) {
        ArrayList<ConfigTech> returnTechs = new ArrayList<ConfigTech>();

        for (ConfigTech tech : CivSettings.techs.values()) {
            if (!civ.hasTechnology(tech.id)) {
                if (tech.isAvailable(civ)) {
                    returnTechs.add(tech);
                }


				/*if (tech.require_techs == null || tech.require_techs.equals("")) {
					returnTechs.add(tech);
				} else {
					String[] requireTechs = tech.require_techs.split(":");
					// Search for the prereq techs.
					boolean hasRequirements = true;
					for (String reqTech : requireTechs) {
						if (!civ.hasTech(reqTech)) {
							hasRequirements = false;
							break;
						}
					}
					if (hasRequirements) {
						// If we're here, then we have all the required techs.
						returnTechs.add(tech);
					}
				}*/
            }
        }
        return returnTechs;
    }

    public double getAdjustedBeakerCost(Civilization civ) {
        double rate = 1.0;
        if (civ.getCapitol() != null && civ.getCapitol().getBuffManager().hasBuff("level7_cheaperCostCap")) {
            rate += 0.05;
        }
        rate -= eraRate(civ);
        return Math.floor(this.beaker_cost * Math.max(rate, .01));
    }

    public double getAdjustedTechCost(Civilization civ) {
        double rate = 1.0;

        for (Town town : civ.getTowns()) {
            if (town.getBuffManager().hasBuff("buff_profit_sharing")) {
                rate -= town.getBuffManager().getEffectiveDouble("buff_profit_sharing");
            }
            if (town.getBuffManager().hasBuff("buff_moscowstateuni_profit_sharing")) {
                rate -= town.getBuffManager().getEffectiveDouble("buff_moscowstateuni_profit_sharing");
            }
        }
        if (civ.getCapitol() != null && civ.getCapitol().getBuffManager().hasBuff("level7_cheaperCostCap")) {
            rate -= 0.05;
        }
        rate = Math.max(rate, 0.75);
        rate -= eraRate(civ);

        return Math.floor(this.cost * Math.max(rate, .01));
    }

    public boolean isAvailable(Civilization civ) {
        if (CivGlobal.testFileFlag("debug-norequire")) {
            CivMessage.global("Ignoring requirements! debug-norequire found.");
            return true;
        }

        if (require_techs == null || require_techs.equals("")) {
            return true;
        }

        String[] requireTechs = require_techs.split(":");

        for (String reqTech : requireTechs) {
            if (!civ.hasTechnology(reqTech)) {
                return false;
            }
        }
        return true;
    }

}
