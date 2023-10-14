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

import cn.evole.plugins.civcraft.exception.InvalidConfiguration;
import cn.evole.plugins.civcraft.main.CivLog;

import java.util.Calendar;


public class EventTimerTask implements Runnable {

    @Override
    public void run() {

        Calendar cal = EventTimer.getCalendarInServerTimeZone();

        for (EventTimer timer : EventTimer.timers.values()) {

            if (cal.after(timer.getNext())) {
                timer.setLast(cal);

                Calendar next;
                try {
                    next = timer.getEventFunction().getNextDate();
                } catch (InvalidConfiguration e) {
                    e.printStackTrace();
                    continue;
                }

                if (next == null) {
                    CivLog.warning("WARNING timer:" + timer.getName() + " did not return a next time.");
                    continue;
                }

                timer.setNext(next);
                timer.save();

                timer.getEventFunction().process();
            }

        }

    }
}
