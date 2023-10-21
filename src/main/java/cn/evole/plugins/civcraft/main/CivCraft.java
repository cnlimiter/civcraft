/**
 * [2023] Cnlimiter LLC
 * All Rights Reserved.
 * Name: civcraft
 * Author: cnlimiter
 * UpdateTime: 2023/10/14 16:15
 * Description:
 * License: ARR
 */
package cn.evole.plugins.civcraft.main;

import cn.evole.plugins.civ_dynmap.CivDynmap;
import cn.evole.plugins.civcraft.command.*;
import cn.evole.plugins.civcraft.command.admin.AdminCommand;
import cn.evole.plugins.civcraft.command.camp.CampCommand;
import cn.evole.plugins.civcraft.command.civ.CivChatCommand;
import cn.evole.plugins.civcraft.command.civ.CivCommand;
import cn.evole.plugins.civcraft.command.debug.DebugCommand;
import cn.evole.plugins.civcraft.command.market.MarketCommand;
import cn.evole.plugins.civcraft.command.plot.PlotCommand;
import cn.evole.plugins.civcraft.command.resident.ResidentCommand;
import cn.evole.plugins.civcraft.command.town.TownChatCommand;
import cn.evole.plugins.civcraft.command.town.TownCommand;
import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.database.SQL;
import cn.evole.plugins.civcraft.database.SQLUpdate;
import cn.evole.plugins.civcraft.endgame.EndConditionNotificationTask;
import cn.evole.plugins.civcraft.event.EventTimerTask;
import cn.evole.plugins.civcraft.exception.CivException;
import cn.evole.plugins.civcraft.exception.InvalidConfiguration;
import cn.evole.plugins.civcraft.fishing.FishingListener;
import cn.evole.plugins.civcraft.listener.*;
import cn.evole.plugins.civcraft.listener.armor.ArmorListener;
import cn.evole.plugins.civcraft.lorestorage.LoreCraftableMaterialListener;
import cn.evole.plugins.civcraft.lorestorage.LoreGuiItemListener;
import cn.evole.plugins.civcraft.mobs.MobListener;
import cn.evole.plugins.civcraft.mobs.MobSpawnerTimer;
import cn.evole.plugins.civcraft.populators.TradeGoodPopulator;
import cn.evole.plugins.civcraft.randomevents.RandomEventSweeper;
import cn.evole.plugins.civcraft.sessiondb.SessionDBAsyncTimer;
import cn.evole.plugins.civcraft.siege.CannonListener;
import cn.evole.plugins.civcraft.structure.Farm;
import cn.evole.plugins.civcraft.structure.farm.FarmGrowthSyncTask;
import cn.evole.plugins.civcraft.structure.farm.FarmPreCachePopulateTimer;
import cn.evole.plugins.civcraft.structurevalidation.StructureValidationChecker;
import cn.evole.plugins.civcraft.structurevalidation.StructureValidationPunisher;
import cn.evole.plugins.civcraft.threading.TaskMaster;
import cn.evole.plugins.civcraft.threading.sync.*;
import cn.evole.plugins.civcraft.threading.tasks.ArrowProjectileTask;
import cn.evole.plugins.civcraft.threading.tasks.ProjectileComponentTimer;
import cn.evole.plugins.civcraft.threading.tasks.ScoutTowerTask;
import cn.evole.plugins.civcraft.threading.timers.*;
import cn.evole.plugins.civcraft.trade.TradeInventoryListener;
import cn.evole.plugins.civcraft.util.BukkitObjects;
import cn.evole.plugins.civcraft.util.ChunkCoord;
import cn.evole.plugins.civcraft.util.TimeTools;
import cn.evole.plugins.civcraft.war.WarListener;
import cn.evole.plugins.global.scores.CalculateScoreTimer;
import cn.evole.plugins.pvptimer.PvPListener;
import cn.evole.plugins.pvptimer.PvPTimer;
import cn.evole.plugins.sls.SLSManager;
import cn.evole.plugins.tp.TpMain;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Random;

public final class CivCraft extends JavaPlugin {

    public static boolean isDisable = false;
    public static Random civRandom = new Random();
    private static JavaPlugin plugin;
    private boolean isError = false;

    public static void setPlugin(JavaPlugin plugin) {
        CivCraft.plugin = plugin;
    }

    public static Plugin getPlugin() {
        return plugin;
    }

    private void startTimers() {

        TaskMaster.asyncTask("SQLUpdate", new SQLUpdate(), 0);

        // Sync Timers
        TaskMaster.syncTimer(SyncBuildUpdateTask.class.getName(),
                new SyncBuildUpdateTask(), 0, 1);

        TaskMaster.syncTimer(SyncUpdateChunks.class.getName(),
                new SyncUpdateChunks(), 0, TimeTools.toTicks(1));

        TaskMaster.syncTimer(SyncLoadChunk.class.getName(),
                new SyncLoadChunk(), 0, 1);

        TaskMaster.syncTimer(SyncGetChestInventory.class.getName(),
                new SyncGetChestInventory(), 0, 1);

        TaskMaster.syncTimer(SyncUpdateInventory.class.getName(),
                new SyncUpdateInventory(), 0, 1);

        TaskMaster.syncTimer(SyncGrowTask.class.getName(),
                new SyncGrowTask(), 0, 1);

        TaskMaster.syncTimer(PlayerLocationCacheUpdate.class.getName(),
                new PlayerLocationCacheUpdate(), 0, 10);

        TaskMaster.asyncTimer("RandomEventSweeper", new RandomEventSweeper(), 0, TimeTools.toTicks(10));

        // Structure event timers
        TaskMaster.asyncTimer("UpdateEventTimer", new UpdateEventTimer(), TimeTools.toTicks(1));
        TaskMaster.asyncTimer("UpdateMinuteEventTimer", new UpdateMinuteEventTimer(), TimeTools.toTicks(20));
        // 应该是1的 貌似谁砍了
        TaskMaster.asyncTimer("RegenTimer", new RegenTimer(), TimeTools.toTicks(5));

        TaskMaster.asyncTimer("BeakerTimer", new BeakerTimer(60), TimeTools.toTicks(60));
        TaskMaster.syncTimer("UnitTrainTimer", new UnitTrainTimer(), TimeTools.toTicks(1));
        TaskMaster.asyncTimer("ReduceExposureTimer", new ReduceExposureTimer(), 0, TimeTools.toTicks(5));

        try {
            double arrow_firerate = CivSettings.getDouble(CivSettings.warConfig, "arrow_tower.fire_rate");
            TaskMaster.syncTimer("arrowTower", new ProjectileComponentTimer(), (int) (arrow_firerate * 20));
            TaskMaster.asyncTimer("ScoutTowerTask", new ScoutTowerTask(), TimeTools.toTicks(1));

        } catch (InvalidConfiguration e) {
            e.printStackTrace();
            return;
        }
        TaskMaster.syncTimer("arrowhomingtask", new ArrowProjectileTask(), 5);

        // Global Event timers
        TaskMaster.syncTimer("FarmCropCache", new FarmPreCachePopulateTimer(), TimeTools.toTicks(30));

        TaskMaster.asyncTimer("FarmGrowthTimer",
                new FarmGrowthSyncTask(), TimeTools.toTicks(Farm.GROW_RATE));

        TaskMaster.asyncTimer("AnnouncerTips", new AnnouncementTimer("tips.txt", 5), 0, TimeTools.toTicks(60 * 60));
        TaskMaster.asyncTimer("AnnouncerWar", new AnnouncementTimer("war.txt", 60), 0, TimeTools.toTicks(60 * 60));

        TaskMaster.asyncTimer("ChangeGovernmentTimer", new ChangeGovernmentTimer(), TimeTools.toTicks(60));
        TaskMaster.asyncTimer("CalculateScoreTimer", new CalculateScoreTimer(), 0, TimeTools.toTicks(60));

        TaskMaster.asyncTimer(PlayerProximityComponentTimer.class.getName(),
                new PlayerProximityComponentTimer(), TimeTools.toTicks(1));

        TaskMaster.asyncTimer(EventTimerTask.class.getName(), new EventTimerTask(), TimeTools.toTicks(5));

        TaskMaster.syncTimer("WindmillTimer", new WindmillTimer(), TimeTools.toTicks(60));
        TaskMaster.asyncTimer("EndGameNotification", new EndConditionNotificationTask(), TimeTools.toTicks(3600));

        TaskMaster.asyncTask(new StructureValidationChecker(), TimeTools.toTicks(120));
        TaskMaster.asyncTimer("StructureValidationPunisher", new StructureValidationPunisher(), TimeTools.toTicks(3600));
        TaskMaster.asyncTimer("SessionDBAsyncTimer", new SessionDBAsyncTimer(), 10);
        TaskMaster.asyncTimer("MobSpawner", new MobSpawnerTimer(), TimeTools.toTicks(30));
        TaskMaster.asyncTimer("cn/evole/plugins/pvptimer", new PvPTimer(), TimeTools.toTicks(30));
    }

    private void registerEvents() {
        final PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(new BlockListener(), this);
        pluginManager.registerEvents(new ChatListener(), this);
        pluginManager.registerEvents(new BonusGoodieManager(), this);
        pluginManager.registerEvents(new MarkerPlacementManager(), this);
        pluginManager.registerEvents(new CustomItemManager(), this);
        pluginManager.registerEvents(new PlayerListener(), this);
        pluginManager.registerEvents(new DebugListener(), this);
        pluginManager.registerEvents(new LoreCraftableMaterialListener(), this);
        pluginManager.registerEvents(new LoreGuiItemListener(), this);
        pluginManager.registerEvents(new MobListener(), this);

        boolean useEXPAsCurrency;
        try {
            useEXPAsCurrency = CivSettings.getBoolean(CivSettings.civConfig, "global.use_exp_as_currency");

        } catch (InvalidConfiguration e) {
            useEXPAsCurrency = true;
            CivLog.error("Unable to check if EXP should be enabled. Disabling.");
            e.printStackTrace();
        }

        if (useEXPAsCurrency) {
            pluginManager.registerEvents(new DisableXPListener(), this);
        }
        pluginManager.registerEvents(new TradeInventoryListener(), this);
        pluginManager.registerEvents(new CannonListener(), this);
        pluginManager.registerEvents(new WarListener(), this);
        pluginManager.registerEvents(new FishingListener(), this);
        pluginManager.registerEvents(new PvPListener(), this);


        pluginManager.registerEvents(new ArmorListener(getConfig().getStringList("blocked")), this);
    }

    @Override
    public void onEnable() {
        setPlugin(this);
        civRandom.setSeed(System.currentTimeMillis());

        this.saveDefaultConfig();


        CivLog.init(this);
        BukkitObjects.initialize(this);

        //Load World Populators
        BukkitObjects.getWorlds().get(0).getPopulators().add(new TradeGoodPopulator());

        try {
            CivSettings.init(this);

            SQL.initialize(this);
            SQL.initCivObjectTables();
            ChunkCoord.buildWorldList();
            CivGlobal.loadGlobals();

            try {
                SLSManager.init();
            } catch (CivException e1) {
                e1.printStackTrace();
            }

        } catch (InvalidConfiguration | SQLException | IOException | InvalidConfigurationException | CivException |
                 ClassNotFoundException e) {
            e.printStackTrace();
            setError(true);
            return;
        }

        //初始化指令
        getCommand("town").setExecutor(new TownCommand());
        getCommand("resident").setExecutor(new ResidentCommand());
        getCommand("dbg").setExecutor(new DebugCommand());
        getCommand("plot").setExecutor(new PlotCommand());
        getCommand("accept").setExecutor(new AcceptCommand());
        getCommand("deny").setExecutor(new DenyCommand());
        getCommand("civ").setExecutor(new CivCommand());
        getCommand("tc").setExecutor(new TownChatCommand());
        getCommand("cc").setExecutor(new CivChatCommand());
        getCommand("gc").setExecutor(new GlobalChatCommand());
        getCommand("ad").setExecutor(new AdminCommand());
        getCommand("econ").setExecutor(new EconCommand());
        getCommand("pay").setExecutor(new PayCommand());
        getCommand("build").setExecutor(new BuildCommand());
        getCommand("market").setExecutor(new MarketCommand());
        getCommand("select").setExecutor(new SelectCommand());
        getCommand("here").setExecutor(new HereCommand());
        getCommand("camp").setExecutor(new CampCommand());
        getCommand("report").setExecutor(new ReportCommand());
        getCommand("trade").setExecutor(new TradeCommand());
        getCommand("kill").setExecutor(new KillCommand());

        registerEvents();
        startTimers();

        if (hasPlugin("dynmap")) CivDynmap.INSTANCE.init(this);
        new TpMain(this).onEnable();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        new TpMain(this).onLoad();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        isDisable = true;
        SQLUpdate.save();
        new TpMain(this).onDisable();
    }

    public boolean hasPlugin(String name) {
        Plugin p;
        p = getServer().getPluginManager().getPlugin(name);
        return (p != null);
    }

    public boolean isError() {
        return isError;
    }

    public void setError(boolean isError) {
        this.isError = isError;
    }


}
