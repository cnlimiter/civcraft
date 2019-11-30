
package com.avrgaming.civcraft.threading.sync;

import java.util.concurrent.locks.ReentrantLock;

import org.bukkit.entity.Player;
import com.avrgaming.civcraft.object.Town;
import com.avrgaming.civcraft.structure.wonders.SpaceShuttle;
import com.avrgaming.civcraft.threading.CivAsyncTask;

public class ValidateShuttleSync
        extends CivAsyncTask {
    public static ReentrantLock runningLock = new ReentrantLock();
    public static Town town;
    public static Player player;

    public ValidateShuttleSync(Town town, Player player) {
        ValidateShuttleSync.town = town;
        ValidateShuttleSync.player = player;
    }

    private void processTick() {
        SpaceShuttle spaceShuttle = (SpaceShuttle) town.getWonderByType("w_space_shuttle");
        try {
            spaceShuttle.startMission(this, player);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        if (runningLock.tryLock()) {
            try {
                this.processTick();
            } finally {
                runningLock.unlock();
            }
        }
    }
}

