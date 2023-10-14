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

import cn.evole.plugins.civcraft.threading.CivAsyncTask;

public class CampUpdateTick extends CivAsyncTask {

    private Camp camp;

    public CampUpdateTick(Camp camp) {
        this.camp = camp;
    }

    @Override
    public void run() {
        if (camp.sifterLock.tryLock()) {
            try {
                if (camp.isSifterEnabled()) {
                    camp.sifter.run(this);
                }
            } finally {
                camp.sifterLock.unlock();
            }
        }

    }

}
