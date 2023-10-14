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

import cn.evole.plugins.civcraft.structure.Barracks;

public class UnitSaveAsyncTask implements Runnable {

    Barracks barracks;

    public UnitSaveAsyncTask(Barracks barracks) {
        this.barracks = barracks;
    }

    @Override
    public void run() {
        barracks.saveProgress();
    }


}
