/**
 * [2023] Cnlimiter LLC
 * All Rights Reserved.
 * Name: civcraft
 * Author: cnlimiter
 * UpdateTime: 2023/10/14 16:15
 * Description:
 * License: ARR
 */
package cn.evole.plugins.civcraft.camp;

import cn.evole.plugins.civcraft.main.CivGlobal;

public class CampHourlyTick implements Runnable {

    @Override
    public void run() {
        for (Camp camp : CivGlobal.getCamps()) {
            try {
                camp.processFirepoints();
                if (camp.isLonghouseEnabled()) {
                    camp.processLonghouse();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
