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

import cn.evole.plugins.civcraft.object.Resident;


public class PlayerDelayedDebtWarning implements Runnable {
    Resident resident;

    public PlayerDelayedDebtWarning(Resident resident) {
        this.resident = resident;
    }

    @Override
    public void run() {
        resident.warnDebt();
    }


}
