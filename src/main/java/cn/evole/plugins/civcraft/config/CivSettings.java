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

import cn.evole.plugins.civcraft.camp.Camp;
import cn.evole.plugins.civcraft.exception.CivException;
import cn.evole.plugins.civcraft.exception.InvalidConfiguration;
import cn.evole.plugins.civcraft.items.units.Unit;
import cn.evole.plugins.civcraft.loreenhancements.LoreEnhancement;
import cn.evole.plugins.civcraft.lorestorage.LoreCraftableMaterial;
import cn.evole.plugins.civcraft.main.CivCraft;
import cn.evole.plugins.civcraft.main.CivData;
import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.main.CivLog;
import cn.evole.plugins.civcraft.object.Town;
import cn.evole.plugins.civcraft.randomevents.ConfigRandomEvent;
import cn.evole.plugins.civcraft.structure.FortifiedWall;
import cn.evole.plugins.civcraft.structure.Wall;
import cn.evole.plugins.civcraft.template.Template;
import cn.evole.plugins.civcraft.util.CivColor;
import cn.evole.plugins.global.perks.Perk;
import cn.evole.plugins.localize.Localize;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class CivSettings {

    public static final long MOB_REMOVE_INTERVAL = 5000;
    //TODO make this configurable.
    public static final int GRACE_DAYS = 3;
    /* Number of days that you can remain in debt before an action occurs. */
    public static final int CIV_DEBT_GRACE_DAYS = 7;
    public static final int CIV_DEBT_SELL_DAYS = 14;
    public static final int CIV_DEBT_TOWN_SELL_DAYS = 21;
    public static final int TOWN_DEBT_GRACE_DAYS = 7;
    public static final int TOWN_DEBT_SELL_DAYS = 14;
    public static final String MINI_ADMIN = "civ.admin";
    public static final String HACKER = "civ.hacker";
    public static final String MODERATOR = "civ.moderator";
    public static final String FREE_PERKS = "civ.freeperks";
    public static final String ECON = "civ.econ";
    public static final String TPALLY = "civ.tp.ally";
    public static final String TPNEUTRAL = "civ.tp.neutral";
    public static final String TPHOSTILE = "civ.tp.hostile";
    public static final String TPWAR = "civ.tp.war";
    public static final String TPPEACE = "civ.tp.peace";
    public static final String TPCAMP = "civ.tp.camp";
    public static final String TPALL = "civ.tp.*";
    public static final int MARKET_COIN_STEP = 5;
    public static final int MARKET_BUYSELL_COIN_DIFF = 30;
    public static final int MARKET_STEP_THRESHOLD = 2;
    public static CivCraft plugin;
    /* cached for faster access. */
    //public static float leather_speed;
    //public static float metal_speed;
    public static float T1_leather_speed;
    public static float T2_leather_speed;
    public static float T3_leather_speed;
    public static float T4_leather_speed;
    public static float T1_metal_speed;
    public static float T2_metal_speed;
    public static float T3_metal_speed;
    public static float T4_metal_speed;
    public static float normal_speed;
    public static double highjump;
    public static FileConfiguration townConfig; /* town.yml */
    public static Map<Integer, ConfigTownLevel> townLevels = new HashMap<Integer, ConfigTownLevel>();
    public static Map<String, ConfigTownUpgrade> townUpgrades = new TreeMap<String, ConfigTownUpgrade>();
    public static FileConfiguration civConfig; /* civ.yml */
    public static Map<String, ConfigEndCondition> endConditions = new HashMap<String, ConfigEndCondition>();
    public static FileConfiguration cultureConfig; /* culture.yml */
    public static Map<Integer, ConfigCultureLevel> cultureLevels = new HashMap<Integer, ConfigCultureLevel>();
    public static FileConfiguration structureConfig; /* structures.yml */
    public static Map<String, ConfigBuildableInfo> structures = new HashMap<String, ConfigBuildableInfo>();
    public static Map<Integer, ConfigGrocerLevel> grocerLevels = new HashMap<Integer, ConfigGrocerLevel>();
    public static Map<Integer, ConfigAlchLevel> alchLevels = new HashMap<Integer, ConfigAlchLevel>();
    public static Map<Integer, ConfigBankLevel> bankLevels = new HashMap<>();
    public static Map<Integer, ConfigCottageLevel> cottageLevels = new HashMap<Integer, ConfigCottageLevel>();
    public static Map<Integer, ConfigMineLevel> mineLevels = new HashMap<Integer, ConfigMineLevel>();
    public static Map<Integer, ConfigTempleLevel> templeLevels = new HashMap<Integer, ConfigTempleLevel>();
    public static Map<Integer, ConfigTradeShipLevel> tradeShipLevels = new HashMap<Integer, ConfigTradeShipLevel>();
    public static FileConfiguration wonderConfig; /* wonders.yml */
    public static Map<String, ConfigBuildableInfo> wonders = new HashMap<String, ConfigBuildableInfo>();
    public static Map<String, ConfigWonderBuff> wonderBuffs = new HashMap<String, ConfigWonderBuff>();
    public static FileConfiguration techsConfig; /* techs.yml */
    public static Map<String, ConfigTech> techs = new HashMap<String, ConfigTech>();
    public static Map<Integer, ConfigTechItem> techItems = new HashMap<Integer, ConfigTechItem>();
    public static Map<String, ConfigTechPotion> techPotions = new HashMap<String, ConfigTechPotion>();
    public static FileConfiguration goodsConfig; /* goods.yml */
    public static Map<String, ConfigTradeGood> goods = new HashMap<String, ConfigTradeGood>();
    public static Map<String, ConfigTradeGood> landGoods = new HashMap<String, ConfigTradeGood>();
    public static Map<String, ConfigTradeGood> waterGoods = new HashMap<String, ConfigTradeGood>();
    public static Map<String, ConfigHemisphere> hemispheres = new HashMap<String, ConfigHemisphere>();
    public static FileConfiguration buffConfig;
    public static Map<String, ConfigBuff> buffs = new HashMap<String, ConfigBuff>();
    public static FileConfiguration unitConfig;
    public static Map<String, ConfigUnit> units = new HashMap<String, ConfigUnit>();
    public static FileConfiguration espionageConfig;
    public static Map<String, ConfigMission> missions = new HashMap<String, ConfigMission>();
    public static FileConfiguration governmentConfig; /* governments.yml */
    public static Map<String, ConfigGovernment> governments = new HashMap<String, ConfigGovernment>();
    public static HashSet<Material> switchItems = new HashSet<Material>();
    // 限制物品
    public static Map<Material, Integer> restrictedItems = new HashMap<Material, Integer>();
    public static Map<Material, Integer> blockPlaceExceptions = new HashMap<Material, Integer>();
    // 禁止生成的生物
    public static List<EntityType> restrictedSpawns = new ArrayList<>();
    // 限制生成的敌对生物
    public static List<EntityType> vanillaHostileMobs = new ArrayList<>();
    public static HashSet<EntityType> playerEntityWeapons = new HashSet<EntityType>();
    public static HashSet<Integer> alwaysCrumble = new HashSet<Integer>();
    public static FileConfiguration warConfig; /* war.yml */
    public static FileConfiguration scoreConfig; /* score.yml */
    public static FileConfiguration perkConfig; /* perks.yml */
    public static Map<String, ConfigPerk> perks = new HashMap<String, ConfigPerk>();
    public static Map<String, ConfigPerk> templates = new HashMap<String, ConfigPerk>();
    public static FileConfiguration enchantConfig; /* enchantments.yml */
    public static Map<String, ConfigEnchant> enchants = new HashMap<String, ConfigEnchant>();
    public static FileConfiguration campConfig; /* camp.yml */
    public static Map<Integer, ConfigCampLonghouseLevel> longhouseLevels = new HashMap<Integer, ConfigCampLonghouseLevel>();
    public static Map<String, ConfigCampUpgrade> campUpgrades = new HashMap<String, ConfigCampUpgrade>();
    public static FileConfiguration marketConfig; /* market.yml */
    public static Map<Integer, ConfigMarketItem> marketItems = new HashMap<Integer, ConfigMarketItem>();
    public static Set<ConfigStableItem> stableItems = new HashSet<ConfigStableItem>();
    public static HashMap<Integer, ConfigStableHorse> horses = new HashMap<Integer, ConfigStableHorse>();
    public static FileConfiguration happinessConfig; /* happiness.yml */
    public static HashMap<Integer, ConfigTownHappinessLevel> townHappinessLevels = new HashMap<Integer, ConfigTownHappinessLevel>();
    public static HashMap<Integer, ConfigHappinessState> happinessStates = new HashMap<Integer, ConfigHappinessState>();
    public static FileConfiguration materialsConfig; /* materials.yml */
    public static HashMap<String, ConfigMaterial> materials = new HashMap<String, ConfigMaterial>();
    public static FileConfiguration randomEventsConfig; /* randomevents.yml */
    public static HashMap<String, ConfigRandomEvent> randomEvents = new HashMap<String, ConfigRandomEvent>();
    public static ArrayList<String> randomEventIDs = new ArrayList<String>();
    public static FileConfiguration fishingConfig; /* fishing.yml */
    public static ArrayList<ConfigFishing> fishingDrops = new ArrayList<ConfigFishing>();
    // mobs.yml
    public static FileConfiguration mobsConfig;
    public static Map<String, ConfigMobs> customMobs = new HashMap<>();
    public static double iron_rate;
    public static double gold_rate;
    public static double diamond_rate;
    public static double emerald_rate;
    public static double startingCoins;
    public static ArrayList<String> kitItems = new ArrayList<String>();
    public static HashMap<Integer, ConfigRemovedRecipes> removedRecipies = new HashMap<Integer, ConfigRemovedRecipes>();
    public static HashSet<Material> restrictedUndoBlocks = new HashSet<Material>();
    public static boolean hasVanishNoPacket = false;
    public static String CURRENCY_NAME;
    public static Localize localize;
    public static boolean hasTitleAPI = false;
    public static boolean hasITag = false;
    public static boolean hasCustomMobs = false;
    public static Material previewMaterial = Material.GLASS;
    public static Boolean showPreview = true;
    public static Map<Integer, ConfigLevelTalent> talentLevels = new HashMap<Integer, ConfigLevelTalent>();
    public static Map<String, ConfigNewspaper> newspapers = new HashMap<String, ConfigNewspaper>();
    public static FileConfiguration missionsConfig;
    public static Map<Integer, ConfigSpaceMissions> spacemissions_levels = new HashMap<Integer, ConfigSpaceMissions>();
    public static Map<Integer, ConfigSpaceRocket> spaceRocket_name = new HashMap<Integer, ConfigSpaceRocket>();
    public static Map<String, ConfigSpaceCraftMat> space_crafts = new HashMap<String, ConfigSpaceCraftMat>();
    public static Map<Integer, ConfigLabLevel> labLevels = new HashMap<Integer, ConfigLabLevel>();
    private static Map<String, ConfigCultureBiomeInfo> cultureBiomes = new HashMap<String, ConfigCultureBiomeInfo>();

    public static void init(JavaPlugin plugin) throws FileNotFoundException, IOException, InvalidConfigurationException, InvalidConfiguration {
        CivSettings.plugin = (CivCraft) plugin;

        String languageFile = CivSettings.getStringBase("localization_file");
        localize = new Localize(plugin, languageFile);

        CivLog.debug(localize.localizedString("welcome_string", "test", 1337, 100.50));
        CURRENCY_NAME = localize.localizedString("civ_currencyName");
        CivGlobal.fullMessage = CivSettings.localize.localizedString("civGlobal_serverFullMsg");

        // Check for required data folder, if it's not there export it.
        CivSettings.validateFiles();

        initRestrictedItems();
        initRestrictedUndoBlocks();
        initSwitchItems();
        initRestrictedSpawns();
        initVanillaHostileMobs();
        initBlockPlaceExceptions();
        initPlayerEntityWeapons();

        loadConfigFiles();
        loadConfigObjects();

        Perk.init();
        Unit.init();

        //CivSettings.leather_speed = (float)CivSettings.getDouble(CivSettings.unitConfig, "base.leather_speed");
        //CivSettings.metal_speed = (float)CivSettings.getDouble(CivSettings.unitConfig, "base.metal_speed");
        CivSettings.T1_leather_speed = (float) CivSettings.getDouble(CivSettings.unitConfig, "base.T1_leather_speed");
        CivSettings.T2_leather_speed = (float) CivSettings.getDouble(CivSettings.unitConfig, "base.T2_leather_speed");
        CivSettings.T3_leather_speed = (float) CivSettings.getDouble(CivSettings.unitConfig, "base.T3_leather_speed");
        CivSettings.T4_leather_speed = (float) CivSettings.getDouble(CivSettings.unitConfig, "base.T4_leather_speed");
        CivSettings.T1_metal_speed = (float) CivSettings.getDouble(CivSettings.unitConfig, "base.T1_metal_speed");
        CivSettings.T2_metal_speed = (float) CivSettings.getDouble(CivSettings.unitConfig, "base.T2_metal_speed");
        CivSettings.T3_metal_speed = (float) CivSettings.getDouble(CivSettings.unitConfig, "base.T3_metal_speed");
        CivSettings.T4_metal_speed = (float) CivSettings.getDouble(CivSettings.unitConfig, "base.T4_metal_speed");
        CivSettings.normal_speed = 0.2f;

        for (Object obj : civConfig.getList("global.start_kit")) {
            if (obj instanceof String) {
                kitItems.add((String) obj);
            }
        }


        CivGlobal.banWords.add("fuck");
        CivGlobal.banWords.add("shit");
        CivGlobal.banWords.add("nigger");
        CivGlobal.banWords.add("faggot");
        CivGlobal.banWords.add("gay");
        CivGlobal.banWords.add("rape");
        CivGlobal.banWords.add("http");
        CivGlobal.banWords.add("cunt");

        iron_rate = CivSettings.getDouble(civConfig, "ore_rates.iron");
        gold_rate = CivSettings.getDouble(civConfig, "ore_rates.gold");
        diamond_rate = CivSettings.getDouble(civConfig, "ore_rates.diamond");
        emerald_rate = CivSettings.getDouble(civConfig, "ore_rates.emerald");
        startingCoins = CivSettings.getDouble(civConfig, "global.starting_coins");

        alwaysCrumble.add(CivData.BEDROCK);
        alwaysCrumble.add(CivData.COAL_BLOCK);
        alwaysCrumble.add(CivData.EMERALD_BLOCK);
        alwaysCrumble.add(CivData.LAPIS_BLOCK);
        alwaysCrumble.add(CivData.SPONGE);
        alwaysCrumble.add(CivData.HAY_BALE);
        alwaysCrumble.add(CivData.GOLD_BLOCK);
        alwaysCrumble.add(CivData.DIAMOND_BLOCK);
        alwaysCrumble.add(CivData.IRON_BLOCK);
        alwaysCrumble.add(CivData.REDSTONE_BLOCK);
        alwaysCrumble.add(CivData.ENDER_CHEST);
        alwaysCrumble.add(CivData.BEACON);

        LoreEnhancement.init();
        LoreCraftableMaterial.buildStaticMaterials();
        LoreCraftableMaterial.buildRecipes();
        Template.initAttachableTypes();

        if (CivSettings.plugin.hasPlugin("TitleAPI")) {
            hasTitleAPI = true;
            CivLog.info("TitleAPI hooks enabled");
        } else {
            CivLog.warning("TitleAPI not found, not registering TitleAPI hooks. This is fine if you're not using TitleAPI.");
        }

        try {
            String materialName = CivSettings.getString(structureConfig, "previewBlock");
            previewMaterial = Material.getMaterial(materialName);
        } catch (InvalidConfiguration e) {
            CivLog.warning("Unable to change Preview Block. Defaulting to Glass.");
        }

        try {
            showPreview = CivSettings.getBoolean(structureConfig, "shouldShowPreview");
        } catch (InvalidConfiguration e) {
            CivLog.warning("Unable to change Structure Preview Settings. Defaulting to True.");
        }

    }


    public static void reloadNewspaperConfigFiles() throws IOException, InvalidConfigurationException {
        CivSettings.newspapers.clear();
        ConfigNewspaper.loadConfig(CivSettings.civConfig = loadCivConfig("civ.yml"), CivSettings.newspapers);
    }


    private static void initRestrictedUndoBlocks() {
        restrictedUndoBlocks.add(Material.CROPS);
        restrictedUndoBlocks.add(Material.CARROT);
        restrictedUndoBlocks.add(Material.POTATO);
        restrictedUndoBlocks.add(Material.REDSTONE);
        restrictedUndoBlocks.add(Material.REDSTONE_WIRE);
        restrictedUndoBlocks.add(Material.REDSTONE_TORCH_OFF);
        restrictedUndoBlocks.add(Material.REDSTONE_TORCH_ON);
        restrictedUndoBlocks.add(Material.DIODE_BLOCK_OFF);
        restrictedUndoBlocks.add(Material.DIODE_BLOCK_ON);
        restrictedUndoBlocks.add(Material.REDSTONE_COMPARATOR_OFF);
        restrictedUndoBlocks.add(Material.REDSTONE_COMPARATOR_ON);
        restrictedUndoBlocks.add(Material.REDSTONE_COMPARATOR);
        restrictedUndoBlocks.add(Material.STRING);
        restrictedUndoBlocks.add(Material.TRIPWIRE);
        restrictedUndoBlocks.add(Material.SUGAR_CANE_BLOCK);
        restrictedUndoBlocks.add(Material.BEETROOT_SEEDS);
        restrictedUndoBlocks.add(Material.LONG_GRASS);
        restrictedUndoBlocks.add(Material.RED_ROSE);
        restrictedUndoBlocks.add(Material.RED_MUSHROOM);
        restrictedUndoBlocks.add(Material.DOUBLE_PLANT);
        restrictedUndoBlocks.add(Material.CAKE_BLOCK);
        restrictedUndoBlocks.add(Material.CACTUS);
        restrictedUndoBlocks.add(Material.PISTON_BASE);
        restrictedUndoBlocks.add(Material.PISTON_EXTENSION);
        restrictedUndoBlocks.add(Material.PISTON_MOVING_PIECE);
        restrictedUndoBlocks.add(Material.PISTON_STICKY_BASE);
        restrictedUndoBlocks.add(Material.TRIPWIRE_HOOK);
        restrictedUndoBlocks.add(Material.SAPLING);
        restrictedUndoBlocks.add(Material.PUMPKIN_STEM);
        restrictedUndoBlocks.add(Material.MELON_STEM);

    }

    private static void initPlayerEntityWeapons() {
        playerEntityWeapons.add(EntityType.PLAYER);
        playerEntityWeapons.add(EntityType.ARROW);
        playerEntityWeapons.add(EntityType.SPECTRAL_ARROW);
        playerEntityWeapons.add(EntityType.TIPPED_ARROW);
        playerEntityWeapons.add(EntityType.EGG);
        playerEntityWeapons.add(EntityType.SNOWBALL);
        playerEntityWeapons.add(EntityType.SPLASH_POTION);
        playerEntityWeapons.add(EntityType.LINGERING_POTION);
        playerEntityWeapons.add(EntityType.FISHING_HOOK);
    }

    public static void validateFiles() {
//		if (plugin == null) {
//			CivLog.debug("null plugin");
//		}
//		
//		if (plugin.getDataFolder() == null) {
//			CivLog.debug("null data folder");
//		}
//		
//		if (plugin.getDataFolder().getPath() == null) {
//			CivLog.debug("path null");
//		}
        File data = new File(plugin.getDataFolder().getPath() + "/data");
        if (!data.exists()) {
            data.mkdirs();
        }
//		
    }

    public static void streamResourceToDisk(String filepath) throws IOException {
        URL inputUrl = plugin.getClass().getResource(filepath);
        File dest = new File(plugin.getDataFolder().getPath() + filepath);
        if (inputUrl == null) {
            CivLog.error("目标文件夹是空的: " + filepath);
        } else {
            FileUtils.copyURLToFile(inputUrl, dest);
        }
    }

    public static FileConfiguration loadCivConfig(String filepath) throws FileNotFoundException, IOException, InvalidConfigurationException {

        File file = new File(plugin.getDataFolder().getPath() + "/data/" + filepath);
        if (!file.exists()) {
            CivLog.warning("配置文件:" + filepath + " 不见了. Streaming to disk from Jar.");
            streamResourceToDisk("/data/" + filepath);
        }

        CivLog.info("加载配置文件中:" + filepath);
        // read the config.yml into memory
        YamlConfiguration cfg = new YamlConfiguration();
        cfg.load(file);
        return cfg;
    }

    public static void reloadGovConfigFiles() throws FileNotFoundException, IOException, InvalidConfigurationException, InvalidConfiguration {
        CivSettings.governments.clear();
        governmentConfig = loadCivConfig("governments.yml");
        ConfigGovernment.loadConfig(governmentConfig, governments);
    }

    private static void loadConfigFiles() throws FileNotFoundException, IOException, InvalidConfigurationException {
        townConfig = loadCivConfig("town.yml");
        civConfig = loadCivConfig("civ.yml");
        cultureConfig = loadCivConfig("culture.yml");
        structureConfig = loadCivConfig("structures.yml");
        techsConfig = loadCivConfig("techs.yml");
        goodsConfig = loadCivConfig("goods.yml");
        buffConfig = loadCivConfig("buffs.yml");
        governmentConfig = loadCivConfig("governments.yml");
        warConfig = loadCivConfig("war.yml");
        wonderConfig = loadCivConfig("wonders.yml");
        unitConfig = loadCivConfig("units.yml");
        espionageConfig = loadCivConfig("espionage.yml");
        scoreConfig = loadCivConfig("score.yml");
        perkConfig = loadCivConfig("perks.yml");
        enchantConfig = loadCivConfig("enchantments.yml");
        campConfig = loadCivConfig("camp.yml");
        marketConfig = loadCivConfig("market.yml");
        happinessConfig = loadCivConfig("happiness.yml");
        materialsConfig = loadCivConfig("materials.yml");
        randomEventsConfig = loadCivConfig("randomevents.yml");
        fishingConfig = loadCivConfig("fishing.yml");
        missionsConfig = loadCivConfig("missions.yml");
        mobsConfig = loadCivConfig("mobs.yml");
    }

    public static void reloadPerks() throws FileNotFoundException, IOException, InvalidConfigurationException, InvalidConfiguration {
        perkConfig = loadCivConfig("perks.yml");
        ConfigPerk.loadConfig(perkConfig, perks);
        ConfigPerk.loadTemplates(perkConfig, templates);
    }

    private static void loadConfigObjects() throws InvalidConfiguration {
        ConfigMobs.loadConfig(mobsConfig, customMobs);
        ConfigTownLevel.loadConfig(townConfig, townLevels);
        ConfigTownUpgrade.loadConfig(townConfig, townUpgrades);
        ConfigCultureLevel.loadConfig(cultureConfig, cultureLevels);
        ConfigBuildableInfo.loadConfig(structureConfig, "structures", structures, false);
        ConfigBuildableInfo.loadConfig(wonderConfig, "wonders", wonders, true);
        ConfigTech.loadConfig(techsConfig, techs);
        ConfigTechItem.loadConfig(techsConfig, techItems);
        ConfigTechPotion.loadConfig(techsConfig, techPotions);
        ConfigHemisphere.loadConfig(goodsConfig, hemispheres);
        ConfigBuff.loadConfig(buffConfig, buffs);
        ConfigWonderBuff.loadConfig(wonderConfig, wonderBuffs);
        ConfigTradeGood.loadConfig(goodsConfig, goods, landGoods, waterGoods);
        ConfigGrocerLevel.loadConfig(structureConfig, grocerLevels);
        ConfigAlchLevel.loadConfig(structureConfig, alchLevels);
        ConfigBankLevel.loadConfig(structureConfig, bankLevels);
        ConfigCottageLevel.loadConfig(structureConfig, cottageLevels);
        ConfigTempleLevel.loadConfig(structureConfig, templeLevels);
        ConfigMineLevel.loadConfig(structureConfig, mineLevels);
        ConfigLabLevel.loadConfig(structureConfig, labLevels);
        ConfigGovernment.loadConfig(governmentConfig, governments);
        ConfigEnchant.loadConfig(enchantConfig, enchants);
        ConfigUnit.loadConfig(unitConfig, units);
        ConfigMission.loadConfig(espionageConfig, missions);
        ConfigPerk.loadConfig(perkConfig, perks);
        ConfigPerk.loadTemplates(perkConfig, templates);
        ConfigCampLonghouseLevel.loadConfig(campConfig, longhouseLevels);
        ConfigCampUpgrade.loadConfig(campConfig, campUpgrades);
        ConfigMarketItem.loadConfig(marketConfig, marketItems);
        ConfigStableItem.loadConfig(structureConfig, stableItems);
        ConfigStableHorse.loadConfig(structureConfig, horses);
        ConfigTownHappinessLevel.loadConfig(happinessConfig, townHappinessLevels);
        ConfigHappinessState.loadConfig(happinessConfig, happinessStates);
        ConfigCultureBiomeInfo.loadConfig(cultureConfig, cultureBiomes);
        ConfigMaterial.loadConfig(materialsConfig, materials);
        ConfigRandomEvent.loadConfig(randomEventsConfig, randomEvents, randomEventIDs);
        ConfigEndCondition.loadConfig(civConfig, endConditions);
        ConfigFishing.loadConfig(fishingConfig, fishingDrops);
        ConfigTradeShipLevel.loadConfig(structureConfig, tradeShipLevels);
        ConfigLevelTalent.loadConfig(cultureConfig, talentLevels);
        ConfigNewspaper.loadConfig(civConfig, newspapers);
        ConfigSpaceMissions.loadConfig(missionsConfig, spacemissions_levels);
        ConfigSpaceRocket.loadConfig(missionsConfig, spaceRocket_name);
        ConfigSpaceCraftMat.loadConfig(missionsConfig, space_crafts);
        ConfigRemovedRecipes.removeRecipes(materialsConfig, removedRecipies);
        CivGlobal.tradeGoodPreGenerator.preGenerate();
        Wall.init_settings();
        FortifiedWall.init_settings();
    }

    private static void initRestrictedSpawns() {
        restrictedSpawns.add(EntityType.BAT);
        restrictedSpawns.add(EntityType.BLAZE);
        restrictedSpawns.add(EntityType.CAVE_SPIDER);
        // restrictedSpawns.add(EntityType.CREEPER);
        restrictedSpawns.add(EntityType.ELDER_GUARDIAN); // 渊古守护者
        restrictedSpawns.add(EntityType.ENDER_DRAGON);
        restrictedSpawns.add(EntityType.ENDERMAN);
        restrictedSpawns.add(EntityType.ENDERMITE);
        restrictedSpawns.add(EntityType.EVOKER); // 幻魔这
        restrictedSpawns.add(EntityType.EVOKER_FANGS);
        restrictedSpawns.add(EntityType.GHAST);
        // restrictedSpawns.add(EntityType.GIANT); //巨人
        restrictedSpawns.add(EntityType.ILLUSIONER); // 幻术师
        restrictedSpawns.add(EntityType.MAGMA_CUBE);
        restrictedSpawns.add(EntityType.PIG_ZOMBIE);
        restrictedSpawns.add(EntityType.SHULKER);
        restrictedSpawns.add(EntityType.SILVERFISH);
        // restrictedSpawns.add(EntityType.SLIME);
        restrictedSpawns.add(EntityType.VEX);
        restrictedSpawns.add(EntityType.WITHER);
        restrictedSpawns.add(EntityType.VINDICATOR);
        restrictedSpawns.add(EntityType.WITHER_SKELETON);
    }

    private static void initVanillaHostileMobs() {
        vanillaHostileMobs.add(EntityType.CREEPER);  //JJ
        vanillaHostileMobs.add(EntityType.GUARDIAN); //守卫者
        vanillaHostileMobs.add(EntityType.HUSK);  // 尸壳
        vanillaHostileMobs.add(EntityType.SKELETON); // 骷髅
        vanillaHostileMobs.add(EntityType.SPIDER); //蜘蛛
        vanillaHostileMobs.add(EntityType.STRAY); // 流浪者
        vanillaHostileMobs.add(EntityType.WITCH); // 女巫
        vanillaHostileMobs.add(EntityType.ZOMBIE);
        vanillaHostileMobs.add(EntityType.ZOMBIE_VILLAGER);
    }

    private static void initRestrictedItems() {
        // TODO make this configurable?
        restrictedItems.put(Material.FLINT_AND_STEEL, 0);
        restrictedItems.put(Material.BUCKET, 0);
        restrictedItems.put(Material.WATER_BUCKET, 0);
        restrictedItems.put(Material.LAVA_BUCKET, 0);
        restrictedItems.put(Material.CAKE_BLOCK, 0);
        restrictedItems.put(Material.CAULDRON, 0);
        restrictedItems.put(Material.DIODE, 0);
        restrictedItems.put(Material.INK_SACK, 0);
        restrictedItems.put(Material.ITEM_FRAME, 0);
        restrictedItems.put(Material.PAINTING, 0);
        restrictedItems.put(Material.SHEARS, 0);
        restrictedItems.put(Material.STATIONARY_LAVA, 0);
        restrictedItems.put(Material.STATIONARY_WATER, 0);
        restrictedItems.put(Material.TNT, 0);
    }

    private static void initSwitchItems() {
        //TODO make this configurable?
        switchItems.add(Material.ANVIL);
        switchItems.add(Material.BEACON);
        switchItems.add(Material.BREWING_STAND);
        switchItems.add(Material.BURNING_FURNACE);
        switchItems.add(Material.CAKE_BLOCK);
        switchItems.add(Material.CAULDRON);
        switchItems.add(Material.CHEST);
        switchItems.add(Material.TRAPPED_CHEST);
        switchItems.add(Material.COMMAND);
        switchItems.add(Material.DIODE);
        switchItems.add(Material.DIODE_BLOCK_OFF);
        switchItems.add(Material.DIODE_BLOCK_ON);
        switchItems.add(Material.DISPENSER);
        switchItems.add(Material.FENCE_GATE);
        switchItems.add(Material.FURNACE);
        switchItems.add(Material.JUKEBOX);
        switchItems.add(Material.LEVER);
        //	switchItems.add(Material.LOCKED_CHEST);
        switchItems.add(Material.STONE_BUTTON);
        switchItems.add(Material.STONE_PLATE);
        switchItems.add(Material.IRON_DOOR);
        switchItems.add(Material.TNT);
        switchItems.add(Material.TRAP_DOOR);
        switchItems.add(Material.WOOD_DOOR);
        switchItems.add(Material.WOODEN_DOOR);
        switchItems.add(Material.WOOD_PLATE);
        //switchItems.put(Material.WOOD_BUTTON, 0); //intentionally left out

        // 1.5 additions.
        switchItems.add(Material.HOPPER);
        switchItems.add(Material.HOPPER_MINECART);
        switchItems.add(Material.DROPPER);
        switchItems.add(Material.REDSTONE_COMPARATOR);
        switchItems.add(Material.REDSTONE_COMPARATOR_ON);
        switchItems.add(Material.REDSTONE_COMPARATOR_OFF);
        switchItems.add(Material.TRAPPED_CHEST);
        switchItems.add(Material.GOLD_PLATE);
        switchItems.add(Material.IRON_PLATE);
        switchItems.add(Material.IRON_TRAPDOOR);

        // 1.6 additions.
        switchItems.add(Material.SPRUCE_DOOR);
        switchItems.add(Material.BIRCH_DOOR);
        switchItems.add(Material.JUNGLE_DOOR);
        switchItems.add(Material.ACACIA_DOOR);
        switchItems.add(Material.DARK_OAK_DOOR);

        // 1.7 additions
        switchItems.add(Material.ACACIA_FENCE_GATE);
        switchItems.add(Material.BIRCH_FENCE_GATE);
        switchItems.add(Material.DARK_OAK_FENCE_GATE);
        switchItems.add(Material.SPRUCE_FENCE_GATE);
        switchItems.add(Material.JUNGLE_FENCE_GATE);
    }

    private static void initBlockPlaceExceptions() {
        /* These blocks can be placed regardless of permissions.
         * this is currently used only for blocks that are generated
         * by specific events such as portal or fire creation.
         */
        blockPlaceExceptions.put(Material.FIRE, 0);
        blockPlaceExceptions.put(Material.PORTAL, 0);
    }

    public static boolean getBooleanBase(String path) throws InvalidConfiguration {
        return getBoolean(plugin.getConfig(), path);
    }

    public static String getStringBase(String path) throws InvalidConfiguration {
        return getString(plugin.getConfig(), path);
    }

    public static double getDoubleTown(String path) throws InvalidConfiguration {
        return getDouble(townConfig, path);
    }

    public static double getDoubleCiv(String path) throws InvalidConfiguration {
        return getDouble(civConfig, path);
    }

    public static void saveGenID(String gen_id) {
        try {
            Writer writer = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(Paths.get("plugins/CivCraft/genid.data"))));
            writer.write(gen_id);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getGenID() {
        String genid = null;
        try {
            BufferedReader br = new BufferedReader(new FileReader("plugins/CivCraft/genid.data"));
            genid = br.readLine();
            br.close();
        } catch (IOException e) {
        }
        return genid;
    }

    public static Double getDoubleStructure(String path) {
        double ret;
        try {
            ret = getDouble(structureConfig, path);
        } catch (InvalidConfiguration e) {
            ret = 0.0;
            e.printStackTrace();
        }
        return ret;
    }

    public static Boolean getBooleanStructure(String path) {
        boolean ret;
        try {
            ret = getBoolean(structureConfig, path);
        } catch (InvalidConfiguration e) {
            ret = false;
            e.printStackTrace();
        }
        return ret;
    }

    public static int getIntegerStructure(String path) {
        int ret;
        try {
            ret = getInteger(structureConfig, path);
        } catch (InvalidConfiguration e) {
            ret = 0;
            e.printStackTrace();
        }
        return ret;
    }

    public static Integer getIntegerGovernment(String path) {
        int ret;
        try {
            ret = getInteger(governmentConfig, path);
        } catch (InvalidConfiguration e) {
            ret = 0;
            e.printStackTrace();
        }
        return ret;
    }

    public static Integer getInteger(FileConfiguration cfg, String path) throws InvalidConfiguration {
        if (!cfg.contains(path)) {
            throw new InvalidConfiguration("不能获取到配置的整数 " + path);
        }

        int data = cfg.getInt(path);
        return data;
    }

    public static String getString(FileConfiguration cfg, String path) throws InvalidConfiguration {
        String data = cfg.getString(path);
        if (data == null) {
            throw new InvalidConfiguration("不能获取到配置的字符 " + path);
        }
        return data;
    }

    public static double getDouble(FileConfiguration cfg, String path) throws InvalidConfiguration {
        if (!cfg.contains(path)) {
            throw new InvalidConfiguration("不能获取到配置的浮点 " + path);
        }

        double data = cfg.getDouble(path);
        return data;
    }

    public static boolean getBoolean(FileConfiguration cfg, String path) throws InvalidConfiguration {
        if (!cfg.contains(path)) {
            throw new InvalidConfiguration("不能获取到配置的布尔型 " + path);
        }

        boolean data = cfg.getBoolean(path);
        return data;
    }

    public static int getMaxNameLength() {
        // TODO make this configurable?
        return 32;
    }

    public static String getNameCheckRegex() throws InvalidConfiguration {
        return getStringBase("regex.name_check_regex");
    }

    public static String getNameFilterRegex() throws InvalidConfiguration {
        return getStringBase("regex.name_filter_regex");
    }

    public static String getNameRemoveRegex() throws InvalidConfiguration {
        return getStringBase("regex.name_remove_regex");
    }

    public static ConfigTownUpgrade getUpgradeByName(String name) {
        for (ConfigTownUpgrade upgrade : townUpgrades.values()) {
            if (upgrade.name.equalsIgnoreCase(name)) {
                return upgrade;
            }
        }
        return null;
    }

    public static ConfigTownUpgrade getUpgradeById(String id) {
        for (ConfigTownUpgrade upgrade : townUpgrades.values()) {
            if (upgrade.id.equalsIgnoreCase(id)) {
                return upgrade;
            }
        }
        return null;
    }

    public static ConfigTech getTechById(final String id) {
        for (final ConfigTech tech : CivSettings.techs.values()) {
            if (tech.id.equalsIgnoreCase(id)) {
                return tech;
            }
        }
        return null;
    }

    public static ConfigHappinessState getHappinessState(double amount) {
        ConfigHappinessState closestState = happinessStates.get(0);

        for (int i = 0; i < happinessStates.size(); i++) {
            ConfigHappinessState state = happinessStates.get(i);
            amount = (double) Math.round(amount * 100) / 100;
            if (amount >= state.amount) {
                closestState = state;
            }
        }

        return closestState;
    }

    public static ConfigTownUpgrade getUpgradeByNameRegex(Town town, String name) throws CivException {
        ConfigTownUpgrade returnUpgrade = null;
        for (ConfigTownUpgrade upgrade : townUpgrades.values()) {
            if (!upgrade.isAvailable(town)) {
                continue;
            }

            if (name.equalsIgnoreCase(upgrade.name)) {
                return upgrade;
            }

            String loweredUpgradeName = upgrade.name.toLowerCase();
            String loweredName = name.toLowerCase();

            if (loweredUpgradeName.contains(loweredName)) {
                if (returnUpgrade == null) {
                    returnUpgrade = upgrade;
                } else {
                    throw new CivException(CivSettings.localize.localizedString("var_cmd_notSpecificUpgrade", name));
                }
            }
        }
        return returnUpgrade;
    }

    public static ConfigCampUpgrade getCampUpgradeByNameRegex(Camp camp, String name) throws CivException {
        ConfigCampUpgrade returnUpgrade = null;
        for (ConfigCampUpgrade upgrade : campUpgrades.values()) {
            if (!upgrade.isAvailable(camp)) {
                continue;
            }

            if (name.equalsIgnoreCase(upgrade.name)) {
                return upgrade;
            }

            String loweredUpgradeName = upgrade.name.toLowerCase();
            String loweredName = name.toLowerCase();

            if (loweredUpgradeName.contains(loweredName)) {
                if (returnUpgrade == null) {
                    returnUpgrade = upgrade;
                } else {
                    throw new CivException(CivSettings.localize.localizedString("var_cmd_notSpecificUpgrade", name));
                }
            }
        }
        return returnUpgrade;
    }

    public static ConfigBuildableInfo getBuildableInfoByName(String fullArgs) {
        for (ConfigBuildableInfo sinfo : structures.values()) {
            if (sinfo.displayName.equalsIgnoreCase(fullArgs)) {
                return sinfo;
            }
        }

        for (ConfigBuildableInfo sinfo : wonders.values()) {
            if (sinfo.displayName.equalsIgnoreCase(fullArgs)) {
                return sinfo;
            }
        }

        return null;
    }

    public static ConfigTech getTechByName(String techname) {
        for (ConfigTech tech : techs.values()) {
            if (tech.name.equalsIgnoreCase(techname)) {
                return tech;
            }
        }
        return null;
    }

    public static int getCottageMaxLevel() {
        int returnLevel = 0;
        for (Integer level : cottageLevels.keySet()) {
            if (returnLevel < level) {
                returnLevel = level;
            }
        }

        return returnLevel;
    }

    public static int getTempleMaxLevel() {
        int returnLevel = 0;
        for (Integer level : templeLevels.keySet()) {
            if (returnLevel < level) {
                returnLevel = level;
            }
        }
        return returnLevel;
    }

    public static int getMineMaxLevel() {
        int returnLevel = 0;
        for (Integer level : mineLevels.keySet()) {
            if (returnLevel < level) {
                returnLevel = level;
            }
        }

        return returnLevel;
    }

    public static int getMaxCultureLevel() {
        int returnLevel = 0;
        for (Integer level : cultureLevels.keySet()) {
            if (returnLevel < level) {
                returnLevel = level;
            }
        }

        return returnLevel;

    }


    public static ConfigCultureBiomeInfo getCultureBiome(String name) {
        ConfigCultureBiomeInfo biomeInfo = cultureBiomes.get(name);
        if (biomeInfo == null) {
            biomeInfo = cultureBiomes.get("UNKNOWN");
        }

        return biomeInfo;
    }

    public static String getBonusDisplayString(ConfigTradeGood configTradeGood, String addText) {
        StringBuilder out = new StringBuilder();
        out.append(CivColor.PurpleItalic + CivSettings.localize.localizedString("var_tradeGood_heading"));
        out.append(";");
        for (ConfigBuff cBuff : configTradeGood.buffs.values()) {
            out.append((Object) ChatColor.UNDERLINE).append(cBuff.name);
            out.append(";");
            out.append("§f" + (Object) ChatColor.ITALIC).append(cBuff.description);
            out.append(";");
        }
        if (configTradeGood.water) {
            out.append("§b" + CivSettings.localize.localizedString("var_tradegood_water"));
        } else {
            out.append("§a" + CivSettings.localize.localizedString("var_tradegood_earth"));
        }
        out.append(";");
        if (!StringUtils.isBlank((String) addText)) {
            out.append(addText);
            out.append(";");
        }
        return out.toString();
    }


}
