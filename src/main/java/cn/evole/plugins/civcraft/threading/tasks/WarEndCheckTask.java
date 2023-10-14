/**
 * [2023] Cnlimiter LLC
 * All Rights Reserved.
 * Name: civcraft
 * Author: cnlimiter
 * UpdateTime: 2023/10/14 16:15
 * Description:
 * License: ARR
 */
package cn.evole.plugins.civcraft.threading.tasks;

import cn.evole.plugins.civcraft.threading.TaskMaster;
import cn.evole.plugins.civcraft.util.TimeTools;
import cn.evole.plugins.civcraft.war.War;

import java.util.Date;

public class WarEndCheckTask implements Runnable {
    @Override
    public void run() {
        Date now = new Date();
        if (War.isWarTime()) {
            if (War.getEnd() == null || now.after(War.getEnd())) {
                War.setWarTime(false);
            } else {
                TaskMaster.syncTask(this, TimeTools.toTicks(1));
            }
        }
    }

}
