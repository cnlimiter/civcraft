/*************************************************************************
 * 
 * AVRGAMING LLC
 * __________________
 * 
 *  [2013] AVRGAMING LLC
 *  All Rights Reserved.
 * 
 * NOTICE:  All information contained herein is, and remains
 * the property of AVRGAMING LLC and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to AVRGAMING LLC
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from AVRGAMING LLC.
 */
package com.avrgaming.civcraft.event;

import com.avrgaming.civcraft.camp.CampHourlyTick;
import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.exception.InvalidConfiguration;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivLog;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.object.Civilization;
import com.avrgaming.civcraft.threading.TaskMaster;
import com.avrgaming.civcraft.threading.tasks.CultureProcessAsyncTask;
import com.avrgaming.civcraft.threading.timers.EffectEventTimer;
import com.avrgaming.civcraft.threading.timers.SyncTradeTimer;
import com.avrgaming.civcraft.util.CivColor;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class HourlyTickEvent implements EventInterface {

	@Override
	public void process() {
		CivLog.info("TimerEvent: Hourly -------------------------------------");
		TaskMaster.asyncTask("cultureProcess", new CultureProcessAsyncTask(), 0);
		TaskMaster.asyncTask("EffectEventTimer", new EffectEventTimer(), 0);
		TaskMaster.syncTask(new SyncTradeTimer(), 0);
		TaskMaster.syncTask(new CampHourlyTick(), 0);
		
		for (Civilization civ : CivGlobal.getCivs()) {
            if (civ.getCapitol().highestTalentLevel() >= civ.getCapitol().getCultureLevel()) continue;
            CivMessage.sendCiv(civ, CivSettings.localize.localizedString("PlayerLoginAsync_civTalentNotUsed"));
        }
        for (Civilization civ : CivGlobal.getCivs()) {
            if (!civ.getMissionActive()) continue;
            Integer currentMission = civ.getCurrentMission();
            String missionName = CivSettings.spacemissions_levels.get((Object)currentMission).name;
            String[] split = civ.getMissionProgress().split(":");
            double completedBeakers = Math.round(Double.valueOf(split[0]));
            double completedHammers = Math.round(Double.valueOf(split[1]));
            int percentageCompleteBeakers = (int)((double)Math.round(Double.parseDouble(split[0])) / Double.parseDouble(CivSettings.spacemissions_levels.get((Object)Integer.valueOf((int)civ.getCurrentMission())).require_beakers) * 100.0);
            int percentageCompleteHammers = (int)((double)Math.round(Double.parseDouble(split[1])) / Double.parseDouble(CivSettings.spacemissions_levels.get((Object)Integer.valueOf((int)civ.getCurrentMission())).require_hammers) * 100.0);
            CivMessage.sendCiv(civ, CivSettings.localize.localizedString("var_spaceshuttle_progress", CivColor.Red + missionName + CivColor.RESET, "§b" + completedBeakers + CivColor.Red + "(" + percentageCompleteBeakers + "%)" + CivColor.RESET, CivColor.LightGray + completedHammers + CivColor.Red + "(" + percentageCompleteHammers + "%)" + CivColor.RESET));
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
