package cn.evole.plugins.civcraft.threading.sync;

import cn.evole.plugins.civcraft.object.Town;
import cn.evole.plugins.civcraft.structure.Factory;
import cn.evole.plugins.civcraft.threading.CivAsyncTask;
import org.bukkit.entity.Player;

import java.util.concurrent.locks.ReentrantLock;

public class ValidateFactoryCraft extends CivAsyncTask {
    public static ReentrantLock runningLock = new ReentrantLock();
    public static Town town;
    public static Player player;

    public ValidateFactoryCraft(Town town, Player player) {
        ValidateFactoryCraft.town = town;
        ValidateFactoryCraft.player = player;
    }

    private void processTick() {
        Factory factory = (Factory) town.getStructureByType("ti_factory");
        try {
            factory.trainCraftMat(this, player);
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

