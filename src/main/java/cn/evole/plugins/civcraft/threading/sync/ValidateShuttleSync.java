package cn.evole.plugins.civcraft.threading.sync;

import cn.evole.plugins.civcraft.object.Town;
import cn.evole.plugins.civcraft.structure.wonders.SpaceShuttle;
import cn.evole.plugins.civcraft.threading.CivAsyncTask;
import org.bukkit.entity.Player;

import java.util.concurrent.locks.ReentrantLock;

public class ValidateShuttleSync extends CivAsyncTask {
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

