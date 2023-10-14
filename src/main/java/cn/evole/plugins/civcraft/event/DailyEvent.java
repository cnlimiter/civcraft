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
import cn.evole.plugins.civcraft.exception.CivException;
import cn.evole.plugins.civcraft.exception.InvalidConfiguration;
import cn.evole.plugins.civcraft.main.CivLog;
import cn.evole.plugins.civcraft.threading.TaskMaster;
import cn.evole.plugins.civcraft.threading.tasks.CultureProcessAsyncTask;
import cn.evole.plugins.civcraft.threading.timers.DailyTimer;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class DailyEvent implements EventInterface {

    public static Boolean dailyTimerFinished = true;

    public static int dayExecuted = 0;

    @Override
    public void process() {

        CivLog.info("TimerEvent: Daily -------------------------------------");

        while (!CultureProcessAsyncTask.cultureProcessedSinceStartup) {
            CivLog.info("DailyTimer: Waiting for culture to finish processing.");
            try {
                Thread.sleep(10 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }
        }

        // TODO I don't think this timer needs to be synchronous.. we can find a way.
        CivLog.info("Daily timer was finished, starting a new timer.");
        Calendar cal = Calendar.getInstance();
        if (dayExecuted != cal.get(Calendar.DAY_OF_MONTH)) {
            dayExecuted = cal.get(Calendar.DAY_OF_MONTH);
            TaskMaster.syncTask(new DailyTimer(), 0);
        } else {
            try {
                throw new CivException("TRIED TO EXECUTE DAILY EVENT TWICE: " + dayExecuted);
            } catch (CivException e) {
                e.printStackTrace();
            }
        }


    }

    @Override
    public Calendar getNextDate() throws InvalidConfiguration {
        Calendar cal = EventTimer.getCalendarInServerTimeZone();
        int daily_upkeep_hour;
        daily_upkeep_hour = CivSettings.getInteger(CivSettings.civConfig, "global.daily_upkeep_hour");
        cal.add(Calendar.DATE, 1);
        cal.set(Calendar.HOUR_OF_DAY, daily_upkeep_hour);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        SimpleDateFormat sdf = new SimpleDateFormat("M/dd h:mm:ss a z");
        CivLog.info("Setting Next Daily Event:" + sdf.format(cal.getTime()));

        return cal;
    }

}
