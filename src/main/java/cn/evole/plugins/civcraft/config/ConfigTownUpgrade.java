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

import cn.evole.plugins.civcraft.exception.CivException;
import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.main.CivLog;
import cn.evole.plugins.civcraft.main.CivMessage;
import cn.evole.plugins.civcraft.object.LibraryEnchantment;
import cn.evole.plugins.civcraft.object.StoreMaterial;
import cn.evole.plugins.civcraft.object.Town;
import cn.evole.plugins.civcraft.structure.*;
import cn.evole.plugins.civcraft.structure.wonders.StockExchange;
import cn.evole.plugins.civcraft.structure.wonders.Wonder;
import org.bukkit.configuration.file.FileConfiguration;

import java.text.DecimalFormat;
import java.util.*;

public class ConfigTownUpgrade {
    public static HashMap<String, Integer> categories = new HashMap<String, Integer>();
    public String id;
    public String name;
    public double cost;
    public String action;
    public String require_upgrade = null;
    public String require_tech = null;
    public String require_structure = null;
    public String require_wonder = null;
    public String category = null;

    public static void loadConfig(FileConfiguration cfg, Map<String, ConfigTownUpgrade> upgrades) {
        upgrades.clear();
        List<Map<?, ?>> culture_levels = cfg.getMapList("upgrades");
        for (Map<?, ?> level : culture_levels) {
            ConfigTownUpgrade town_upgrade = new ConfigTownUpgrade();

            town_upgrade.id = (String) level.get("id");
            town_upgrade.name = (String) level.get("name");
            town_upgrade.cost = (Double) level.get("cost");
            town_upgrade.action = (String) level.get("action");
            town_upgrade.require_upgrade = (String) level.get("require_upgrade");
            town_upgrade.require_tech = (String) level.get("require_tech");
            town_upgrade.require_wonder = (String) level.get("require_wonder");
            town_upgrade.require_structure = (String) level.get("require_structure");
            town_upgrade.category = (String) level.get("category");

            Integer categoryCount = categories.get(town_upgrade.category);
            if (categoryCount == null) {
                categories.put(town_upgrade.category.toLowerCase(), 1);
            } else {
                categories.put(town_upgrade.category.toLowerCase(), categoryCount + 1);
            }

            upgrades.put(town_upgrade.id, town_upgrade);
        }
        CivLog.info("Loaded " + upgrades.size() + " town upgrades.");
    }

    public static int getAvailableCategoryCount(String category, Town town) {
        int count = 0;

        for (ConfigTownUpgrade upgrade : CivSettings.townUpgrades.values()) {
            if (upgrade.category.equalsIgnoreCase(category) || category.equalsIgnoreCase("all")) {
                if (upgrade.isAvailable(town)) {
                    count++;
                }
            }
        }

        return count;
    }

    public static Set<ConfigTownUpgrade> getAllUpgrades(final Town town) {
        final Set<ConfigTownUpgrade> rightSequence = new LinkedHashSet<ConfigTownUpgrade>();
        for (final ConfigTownUpgrade upgrade : CivSettings.townUpgrades.values()) {
            if (upgrade.isAvailable(town)) {
                rightSequence.add(upgrade);
            }
        }
        return rightSequence;
    }

    public void processAction(Town town) throws CivException {
        if (this.action == null)
            return;
        String[] args = this.action.split(",");

        Structure struct;

        switch (args[0]) {
            case "set_town_level":
                if (town.getLevel() < Integer.valueOf(args[1].trim())) {
                    town.setLevel(Integer.valueOf(args[1].trim()));
                    CivMessage.global(CivSettings.localize.localizedString("var_townUpgrade_town", town.getName(), town.getLevelTitle()));
                }
                break;

            case "set_bank_level":
                if (town.saved_bank_level < Integer.parseInt(args[1].trim())) {
                    town.saved_bank_level = Integer.parseInt(args[1].trim());
                }
                struct = town.getStructureByType("s_bank");
                if ((struct instanceof Bank)) {
                    Bank bank = (Bank) struct;
                    if (bank.getLevel() < town.saved_bank_level) {
                        bank.setLevel(town.saved_bank_level);
                        bank.updateSignText();
                        CivMessage.sendTown(town, CivSettings.localize.localizedString("var_townUpgrade_bank", bank.getLevel()));
                    }
                }
                break;
            case "set_bank_interest":
                if (town.saved_bank_interest_amount < Double.parseDouble(args[1].trim())) {
                    town.saved_bank_interest_amount = Double.parseDouble(args[1].trim());
                }
                struct = town.getStructureByType("s_bank");
                if ((struct instanceof Bank)) {
                    Bank bank = (Bank) struct;
                    if (bank.getInterestRate() < town.saved_bank_interest_amount) {
                        bank.setInterestRate(town.saved_bank_interest_amount);
                        DecimalFormat df = new DecimalFormat();
                        CivMessage.sendTown(town, CivSettings.localize.localizedString("var_townupgrade_interest", df.format(bank.getInterestRate() * 100)));
                    }
                }
                break;
            case "set_stock_exchange_level":
                if (town.saved_stock_exchange_level < Integer.valueOf(args[1].trim())) {
                    town.saved_stock_exchange_level = Integer.valueOf(args[1].trim());
                }
                Wonder wonder = town.getWonderByType("w_stock_exchange");
                struct = town.getStructureByType("s_bank");
                if (wonder != null && wonder instanceof StockExchange) {
                    final StockExchange stock = (StockExchange) wonder;
                    if (stock.getLevel() < town.saved_stock_exchange_level) {
                        stock.setLevel(town.saved_stock_exchange_level);
                        if (stock.getLevel() != 6) {
                            CivMessage.global(CivSettings.localize.localizedString("var_townUpgrade_stockexchange", stock.getLevel(), town.getName()));
                        } else {
                            CivMessage.global(CivSettings.localize.localizedString("var_townUpgrade_stockexchangeWinStart", town.getName(), town.getCiv().getName()));
                        }
                    }
                }
                if (struct != null && (struct instanceof Bank)) {
                    struct.updateSignText();
                    break;
                }
                break;
            case "set_store_level":
                if (town.saved_store_level < Integer.valueOf(args[1].trim())) {
                    town.saved_store_level = Integer.valueOf(args[1].trim());
                }
                struct = town.getStructureByType("s_store");
                if (struct != null && (struct instanceof Store)) {
                    Store store = (Store) struct;
                    if (store.getLevel() < town.saved_store_level) {
                        store.setLevel(town.saved_store_level);
                        store.updateSignText();
                        CivMessage.sendTown(town, CivSettings.localize.localizedString("var_townupgrade_store", store.getLevel()));
                    }
                }
                break;
            case "set_store_material":
                struct = town.getStructureByType("s_store");
                if (struct != null && (struct instanceof Store)) {
                    Store store = (Store) struct;
                    StoreMaterial mat = new StoreMaterial(args[1].trim(), args[2].trim(), args[3].trim(), args[4].trim());
                    store.addStoreMaterial(mat);
                    store.updateSignText();
                }
                break;
            case "set_library_level":
                if (town.saved_library_level < Integer.valueOf(args[1].trim())) {
                    town.saved_library_level = Integer.valueOf(args[1].trim());
                }
                struct = town.getStructureByType("s_library");
                if (struct != null && (struct instanceof Library)) {
                    Library library = (Library) struct;
                    if (library.getLevel() < town.saved_library_level) {
                        library.setLevel(town.saved_library_level);
                        library.updateSignText();
                        CivMessage.sendTown(town, CivSettings.localize.localizedString("var_townupgrade_library", library.getLevel()));
                    }
                }
                break;
            case "enable_library_enchantment":
                struct = town.getStructureByType("s_library");
                if (struct != null && (struct instanceof Library)) {
                    Library library = (Library) struct;
                    LibraryEnchantment enchant = new LibraryEnchantment(args[1].trim(), Integer.valueOf(args[2].trim()), Double.valueOf(args[3].trim()));
                    library.addEnchant(enchant);
                    library.updateSignText();
                    CivMessage.sendTown(town, CivSettings.localize.localizedString("var_townupgrade_enchantment", args[1].trim() + " " + args[2]));
                }
                break;
            case "set_fish_hatchery_level":
                boolean didUpgradeFishery = false;
                if (town.saved_fish_hatchery_level < Integer.valueOf(args[1].trim())) {
                    town.saved_fish_hatchery_level = Integer.valueOf(args[1].trim());
                }
                for (Structure structure : town.getStructures()) {
                    if (structure.getConfigId().equalsIgnoreCase("ti_fish_hatchery")) {

                        if (structure != null && (structure instanceof FishHatchery)) {
                            FishHatchery fishery = (FishHatchery) structure;
                            if (fishery.getLevel() < town.saved_fish_hatchery_level) {
                                didUpgradeFishery = true;
                                fishery.setLevel(town.saved_fish_hatchery_level);
                                fishery.updateSignText();
                            }
                        }
                    }
                }
                if (didUpgradeFishery) {
                    CivMessage.sendTown(town, CivSettings.localize.localizedString("var_townupgrade_fish_hatchery", town.saved_fish_hatchery_level));
                }
                break;
            case "set_tradeship_upgrade_level":
                if (town.saved_tradeship_upgrade_levels < Integer.valueOf(args[1].trim())) {
                    town.saved_tradeship_upgrade_levels = Integer.valueOf(args[1].trim());
                }
                boolean didUpgradeTradeShip = false;
                for (Structure structure : town.getStructures()) {
                    if (structure.getConfigId().equalsIgnoreCase("ti_trade_ship")) {

                        if (structure != null && (structure instanceof TradeShip)) {
                            TradeShip tradeShip = (TradeShip) structure;
                            if (tradeShip.getUpgradeLvl() < town.saved_tradeship_upgrade_levels) {
                                didUpgradeTradeShip = true;
                                tradeShip.setUpgradeLvl(town.saved_tradeship_upgrade_levels);
                                tradeShip.reprocessCommandSigns();
                            }
                        }
                    }
                }
                if (didUpgradeTradeShip) {
                    CivMessage.sendTown(town, CivSettings.localize.localizedString("var_townupgrade_tradeship", town.saved_tradeship_upgrade_levels));
                }
                break;
            case "set_grocer_level":
                if (town.saved_grocer_levels < Integer.valueOf(args[1].trim())) {
                    town.saved_grocer_levels = Integer.valueOf(args[1].trim());
                }
                struct = town.getStructureByType("s_grocer");
                if (struct != null && (struct instanceof Grocer)) {
                    Grocer grocer = (Grocer) struct;
                    if (grocer.getLevel() < town.saved_grocer_levels) {
                        grocer.setLevel(town.saved_grocer_levels);
                        grocer.updateSignText();
                        CivMessage.sendTown(town, CivSettings.localize.localizedString("var_townupgrade_grocer", grocer.getLevel()));
                    }
                }
                break;
            case "set_alch_level":
                if (town.saved_alch_levels < Integer.valueOf(args[1].trim())) {
                    town.saved_alch_levels = Integer.valueOf(args[1].trim());
                }
                for (Structure structure : town.getStructures()) {
                    if (structure != null && structure instanceof Alch) {
                        final Alch alch = (Alch) structure;
                        if (alch.getLevel() < town.saved_alch_levels) {
                            alch.setLevel(town.saved_alch_levels);
                            alch.updateSignText();
                            CivMessage.sendTown(town, CivSettings.localize.localizedString("var_townupgrade_alch", alch.getLevel()));
                        }
                        break;
                    }
                }
                break;
            case "set_trommel_level":
                boolean didUpgrade = false;
                if (town.saved_trommel_level < Integer.parseInt(args[1].trim())) {
                    town.saved_trommel_level = Integer.parseInt(args[1].trim());
                }
                for (Structure structure : town.getStructures()) {
                    if (structure.getConfigId().equalsIgnoreCase("s_trommel")) {

                        if ((structure instanceof Trommel)) {
                            Trommel trommel = (Trommel) structure;
                            if (trommel.getLevel() < town.saved_trommel_level) {
                                didUpgrade = true;
                                trommel.setLevel(town.saved_trommel_level);
                            }
                        }
                    }
                }
                if (didUpgrade) {
                    CivMessage.sendTown(town, CivSettings.localize.localizedString("var_townupgrade_trommels", town.saved_trommel_level));
                }
                break;
            case "set_quarry_level":
                if (Integer.valueOf(args[1].trim()) > town.saved_quarry_level) {
                    town.saved_quarry_level = Integer.valueOf(args[1].trim());
                }
                boolean didUpgradeQuarry = false;
                for (Structure structure : town.getStructures()) {
                    if (structure.getConfigId().equalsIgnoreCase("ti_quarry")) {

                        if (structure != null && (structure instanceof Quarry)) {
                            Quarry quarry = (Quarry) structure;
                            if (quarry.getLevel() < town.saved_quarry_level) {
                                didUpgradeQuarry = true;
                                quarry.setLevel(town.saved_quarry_level);
                            }
                        }
                    }
                }
                if (didUpgradeQuarry) {
                    CivMessage.sendTown(town, CivSettings.localize.localizedString("var_townupgrade_quarries", town.saved_quarry_level));
                }
                break;
        }
    }

    public boolean isAvailable(Town town) {
        if (CivGlobal.testFileFlag("debug-norequire")) {
            CivMessage.global("Ignoring requirements! debug-norequire found.");
            return true;
        }

        if (town.hasUpgrade(this.require_upgrade)) {
            if (town.getCiv().hasTechnology(this.require_tech)) {
                if (town.hasStructure(require_structure)) {
                    if (town.hasWonder(require_wonder)) {
                        if (!town.hasUpgrade(this.id)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

}
