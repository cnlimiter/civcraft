/**
 * [2023] Cnlimiter LLC
 * All Rights Reserved.
 * Name: civcraft
 * Author: cnlimiter
 * UpdateTime: 2023/10/14 16:15
 * Description:
 * License: ARR
 */
package cn.evole.plugins.civcraft.command.debug;

import cn.evole.plugins.civcraft.command.CommandBase;
import cn.evole.plugins.civcraft.exception.CivException;
import cn.evole.plugins.civcraft.main.CivMessage;
import cn.evole.plugins.civcraft.test.TestGetChestThread;
import cn.evole.plugins.civcraft.threading.TaskMaster;

public class DebugTestCommand extends CommandBase {

    /*
     * Here we'll build a collection of integration tests that can be run on server
     * start to verify everything is working.
     */


    @Override
    public void init() {
        command = "/dbg test ";
        displayName = "Test Commands";

        commands.put("getsyncchesttest", "Does a performance test by getting chests. NEVER RUN THIS ON PRODUCTION.");
        commands.put("setlag", "[tps] - tries to set the tps to this amount to simulate lag.");
    }

    public void setlag_cmd() throws CivException {
//        Integer tps = getNamedInteger(1);
//        TaskMaster.syncTimer("lagtimer", new LagSimulationTimer(tps), 0);
//        CivMessage.sendSuccess(sender, "Let the lagging begin.");
    }

    public void getsyncchesttest_cmd() throws CivException {
        Integer count = getNamedInteger(1);

        for (int i = 0; i < count; i++) {
            TaskMaster.asyncTask(new TestGetChestThread(), 0);
        }

        CivMessage.sendSuccess(sender, "Started " + count + " threads, watch logs.");
    }

    @Override
    public void doDefaultAction() throws CivException {
        showHelp();
    }

    @Override
    public void showHelp() {
        showBasicHelp();
    }

    private void isNetizen() throws CivException {
        if (!getPlayer().getName().equalsIgnoreCase("netizen539")) {
            throw new CivException("You must be netizen to run these commands.");
        }
    }


    @Override
    public void permissionCheck() throws CivException {
        isNetizen();
    }


}
