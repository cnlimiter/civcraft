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

import cn.evole.plugins.civcraft.camp.CampHourlyTick;
import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.exception.InvalidConfiguration;
import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.main.CivLog;
import cn.evole.plugins.civcraft.main.CivMessage;
import cn.evole.plugins.civcraft.object.Civilization;
import cn.evole.plugins.civcraft.threading.TaskMaster;
import cn.evole.plugins.civcraft.threading.tasks.CultureProcessAsyncTask;
import cn.evole.plugins.civcraft.threading.timers.HourlyEventTimer;
import cn.evole.plugins.civcraft.threading.timers.SyncTradeTimer;
import cn.evole.plugins.civcraft.util.CivColor;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class HourlyTickEvent implements EventInterface {

    @Override
    public void process() {
        CivLog.info("TimerEvent: Hourly -------------------------------------");
        TaskMaster.asyncTask("cultureProcess", new CultureProcessAsyncTask(), 0);
        TaskMaster.asyncTask("EffectEventTimer", new HourlyEventTimer(), 0);
        TaskMaster.syncTask(new SyncTradeTimer(), 0);
        TaskMaster.syncTask(new CampHourlyTick(), 0);

        for (Civilization civ : CivGlobal.getCivs()) {
            if (civ.getCapitol().highestTalentLevel() >= civ.getCapitol().getCultureLevel()) continue;
            CivMessage.sendCiv(civ, CivSettings.localize.localizedString("PlayerLoginAsync_civTalentNotUsed"));
        }
        for (Civilization civ : CivGlobal.getCivs()) {
            if (!civ.getMissionActive()) continue;
            int currentMission = civ.getCurrentMission();
            String missionName = CivSettings.spacemissions_levels.get(currentMission).name;
            String[] split = civ.getMissionProgress().split(":");
            double completedBeakers = Math.round(Double.parseDouble(split[0]));
            double completedHammers = Math.round(Double.parseDouble(split[1]));
            int percentageCompleteBeakers = (int) ((double) Math.round(Double.parseDouble(split[0])) /
                    Double.parseDouble(CivSettings.spacemissions_levels.get(currentMission).require_beakers)
                    * 100.0);
            int percentageCompleteHammers = (int) ((double) Math.round(Double.parseDouble(split[1])) /
                    Double.parseDouble(CivSettings.spacemissions_levels.get(currentMission).require_hammers) * 100.0);
            CivMessage.sendCiv(civ, CivSettings.localize.localizedString("var_spaceshuttle_progress",
                    CivColor.Red + missionName + CivColor.RESET,
                    "§b" + completedBeakers + CivColor.Red + "(" + percentageCompleteBeakers + "%)" + CivColor.RESET,
                    CivColor.LightGray + completedHammers + CivColor.Red + "(" + percentageCompleteHammers + "%)" + CivColor.RESET));
        }

        CivLog.info("TimerEvent: Hourly Finished -----------------------------");
    }

    @Override
    public Calendar getNextDate() throws InvalidConfiguration {
        SimpleDateFormat sdf = new SimpleDateFormat("M/dd h:mm:ss a z");
        Calendar cal = EventTimer.getCalendarInServerTimeZone();

        int hourly_peroid = CivSettings.getInteger(CivSettings.civConfig, "global.hourly_tick");
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.add(Calendar.SECOND, hourly_peroid);
        sdf.setTimeZone(cal.getTimeZone());
        return cal;
    }

}
