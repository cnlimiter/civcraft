/**
 * [2023] Cnlimiter LLC
 * All Rights Reserved.
 * Name: civcraft
 * Author: cnlimiter
 * UpdateTime: 2023/10/14 16:15
 * Description:
 * License: ARR
 */
package cn.evole.plugins.civcraft.event;

import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.exception.InvalidConfiguration;
import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.main.CivLog;
import cn.evole.plugins.civcraft.object.Civilization;
import cn.evole.plugins.civcraft.object.CultureChunk;
import cn.evole.plugins.civcraft.object.Town;
import cn.evole.plugins.civcraft.object.TownChunk;
import cn.evole.plugins.civcraft.threading.TaskMaster;
import org.bukkit.Bukkit;

import java.util.Calendar;

public class SpawnRegenEvent implements EventInterface {

    @Override
    public void process() {
        CivLog.info("TimerEvent: SpawnRegenEvent -------------------------------------");

        class RegenSyncTask implements Runnable {

            CultureChunk cc;

            public RegenSyncTask(CultureChunk cc) {
                this.cc = cc;
            }

            @Override
            public void run() {
                Bukkit.getWorld("world").regenerateChunk(cc.getChunkCoord().getX(), cc.getChunkCoord().getZ());
            }
        }

        int tickDelay = 0;
        for (Civilization civ : CivGlobal.getCivs()) {
            if (civ.isAdminCiv()) {
                for (Town town : civ.getTowns()) {
                    for (CultureChunk cc : town.getCultureChunks()) {
                        TownChunk tc = CivGlobal.getTownChunk(cc.getChunkCoord());
                        if (tc == null) {
                            TaskMaster.syncTask(new RegenSyncTask(cc), tickDelay);
                            tickDelay += 1;
                        }
                    }
                }
            }
        }


    }

    @Override
    public Calendar getNextDate() throws InvalidConfiguration {
        Calendar cal = EventTimer.getCalendarInServerTimeZone();
        int regen_hour = CivSettings.getInteger(CivSettings.civConfig, "global.regen_spawn_hour");
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.add(Calendar.HOUR_OF_DAY, regen_hour);

        Calendar now = Calendar.getInstance();
        if (now.after(cal)) {
            cal.add(Calendar.DATE, 1);
            cal.set(Calendar.HOUR_OF_DAY, regen_hour);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
        }

        return cal;
    }

}
