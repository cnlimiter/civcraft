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
import cn.evole.plugins.civcraft.main.CivLog;
import cn.evole.plugins.civcraft.threading.TaskMaster;
import cn.evole.plugins.civcraft.threading.tasks.WarEndCheckTask;
import cn.evole.plugins.civcraft.util.TimeTools;
import cn.evole.plugins.civcraft.war.War;

import java.util.Calendar;

public class WarEvent implements EventInterface {

    @Override
    public void process() {
        CivLog.info("TimerEvent: WarEvent -------------------------------------");

        try {
            War.setWarTime(true);
        } catch (Exception e) {
            CivLog.error("WarStartException:" + e.getMessage());
            e.printStackTrace();
        }

        // Start repeating task waiting for war time to end.
        TaskMaster.syncTask(new WarEndCheckTask(), TimeTools.toTicks(1));
    }

    @Override
    public Calendar getNextDate() throws InvalidConfiguration {
        Calendar cal = EventTimer.getCalendarInServerTimeZone();

        int dayOfWeek = CivSettings.getInteger(CivSettings.warConfig, "war.time_day");
        int hourOfWar = CivSettings.getInteger(CivSettings.warConfig, "war.time_hour");

        cal.set(Calendar.DAY_OF_WEEK, dayOfWeek);
        cal.set(Calendar.HOUR_OF_DAY, hourOfWar);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);

        Calendar now = Calendar.getInstance();
        if (now.after(cal)) {
            cal.add(Calendar.WEEK_OF_MONTH, 1);
            cal.set(Calendar.DAY_OF_WEEK, dayOfWeek);
            cal.set(Calendar.HOUR_OF_DAY, hourOfWar);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
        }

        return cal;
    }

}
