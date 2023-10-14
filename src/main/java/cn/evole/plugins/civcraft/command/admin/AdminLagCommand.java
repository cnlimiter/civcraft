/**
 * [2023] Cnlimiter LLC
 * All Rights Reserved.
 * Name: civcraft
 * Author: cnlimiter
 * UpdateTime: 2023/10/14 16:15
 * Description:
 * License: ARR
 */
package cn.evole.plugins.civcraft.command.admin;

import cn.evole.plugins.civcraft.command.CommandBase;
import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.exception.CivException;
import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.main.CivMessage;
import cn.evole.plugins.civcraft.threading.sync.SyncBuildUpdateTask;

public class AdminLagCommand extends CommandBase {

    @Override
    public void init() {
        command = "/ad lag";
        displayName = CivSettings.localize.localizedString("adcmd_lag_cmdDesc");

        commands.put("trommels", CivSettings.localize.localizedString("adcmd_lag_trommelsDesc"));
        commands.put("quarries", CivSettings.localize.localizedString("adcmd_lag_quarryDesc"));
        commands.put("fishery", CivSettings.localize.localizedString("adcmd_lag_fishHatcherDesc"));
        commands.put("grinders", CivSettings.localize.localizedString("adcmd_lag_grinderDesc"));
        commands.put("towers", CivSettings.localize.localizedString("adcmd_lag_towersDesc"));
        commands.put("growth", CivSettings.localize.localizedString("adcmd_lag_growthDesc"));
        commands.put("trade", CivSettings.localize.localizedString("adcmd_lag_tradeDesc"));
        commands.put("score", CivSettings.localize.localizedString("adcmd_lag_scoreDesc"));
        commands.put("warning", CivSettings.localize.localizedString("adcmd_lag_warningDesc"));
        commands.put("blockupdate", CivSettings.localize.localizedString("adcmd_lag_blockUpdateDesc"));
        commands.put("speedchunks", CivSettings.localize.localizedString("adcmd_lag_speedCheckOnChunk"));

    }

    public void blockupdate_cmd() throws CivException {
        Integer blocks = this.getNamedInteger(1);

        SyncBuildUpdateTask.UPDATE_LIMIT = blocks;
        CivMessage.sendSuccess(sender, CivSettings.localize.localizedString("var_adcmd_lag_blockupdateSet", blocks));
    }

    public void speedchunks_cmd() {
        CivGlobal.speedChunks = !CivGlobal.speedChunks;
        CivMessage.sendSuccess(sender, CivSettings.localize.localizedString("adcmd_lag_SpeedCheck") + " " + CivGlobal.speedChunks);
    }

    public void score_cmd() {
        CivGlobal.scoringEnabled = !CivGlobal.scoringEnabled;
        CivMessage.sendSuccess(sender, CivSettings.localize.localizedString("adcmd_lag_scoring") + " " + CivGlobal.scoringEnabled);
    }

    public void grinders_cmd() {
        CivGlobal.mobGrinderEnabled = !CivGlobal.mobGrinderEnabled;
        CivMessage.sendSuccess(sender, CivSettings.localize.localizedString("adcmd_lag_mobGrinders") + " " + CivGlobal.mobGrinderEnabled);
    }

    public void trommels_cmd() {
        CivGlobal.trommelsEnabled = !CivGlobal.trommelsEnabled;
        CivMessage.sendSuccess(sender, CivSettings.localize.localizedString("adcmd_lag_trommel") + " " + CivGlobal.trommelsEnabled);
    }

    public void quarries_cmd() {
        CivGlobal.quarriesEnabled = !CivGlobal.quarriesEnabled;
        CivMessage.sendSuccess(sender, CivSettings.localize.localizedString("adcmd_lag_quarry") + " " + CivGlobal.quarriesEnabled);
    }

    public void fishery_cmd() {
        CivGlobal.fisheryEnabled = !CivGlobal.fisheryEnabled;
        CivMessage.sendSuccess(sender, CivSettings.localize.localizedString("adcmd_lag_fishHatchery") + " " + CivGlobal.fisheryEnabled);
    }

    public void towers_cmd() {
        CivGlobal.towersEnabled = !CivGlobal.towersEnabled;
        CivMessage.sendSuccess(sender, CivSettings.localize.localizedString("adcmd_lag_towers") + " " + CivGlobal.towersEnabled);
    }

    public void growth_cmd() {
        CivGlobal.growthEnabled = !CivGlobal.growthEnabled;
        CivMessage.sendSuccess(sender, CivSettings.localize.localizedString("adcmd_lag_growth") + " " + CivGlobal.growthEnabled);
    }

    public void trade_cmd() {
        CivGlobal.tradeEnabled = !CivGlobal.tradeEnabled;
        CivMessage.sendSuccess(sender, CivSettings.localize.localizedString("adcmd_lag_trade") + " " + CivGlobal.tradeEnabled);
    }

    public void warning_cmd() {
        CivGlobal.growthEnabled = !CivGlobal.growthEnabled;
        CivMessage.sendSuccess(sender, CivSettings.localize.localizedString("adcmd_lag_warnings") + " " + CivGlobal.warningsEnabled);
    }

    @Override
    public void doDefaultAction() throws CivException {
        showHelp();
    }

    @Override
    public void showHelp() {
        showBasicHelp();
    }

    @Override
    public void permissionCheck() throws CivException {
        //Admin is checked in parent command.
    }

}
