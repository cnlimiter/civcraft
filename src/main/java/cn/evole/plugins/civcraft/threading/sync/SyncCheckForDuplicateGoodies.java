/**
 * [2023] Cnlimiter LLC
 * All Rights Reserved.
 * Name: civcraft
 * Author: cnlimiter
 * UpdateTime: 2023/10/14 16:15
 * Description:
 * License: ARR
 */
package cn.evole.plugins.civcraft.threading.sync;

import cn.evole.plugins.civcraft.main.CivGlobal;

public class SyncCheckForDuplicateGoodies implements Runnable {

    @Override
    public void run() {
        CivGlobal.checkForDuplicateGoodies();
    }

}
