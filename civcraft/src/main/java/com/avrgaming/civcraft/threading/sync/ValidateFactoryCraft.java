
package com.avrgaming.civcraft.threading.sync;

import java.util.concurrent.locks.ReentrantLock;
import org.bukkit.entity.Player;
import com.avrgaming.civcraft.object.Town;
import com.avrgaming.civcraft.structure.Factory;
import com.avrgaming.civcraft.threading.CivAsyncTask;

public class ValidateFactoryCraft
extends CivAsyncTask {
    public static ReentrantLock runningLock = new ReentrantLock();
    public static Town town;
    public static Player player;

    public ValidateFactoryCraft(Town town, Player player) {
        ValidateFactoryCraft.town = town;
        ValidateFactoryCraft.player = player;
    }

    private void processTick() {
        Factory factory = (Factory)town.getStructureByType("ti_factory");
        try {
            factory.trainCraftMat(this, player);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        if (runningLock.tryLock()) {
            try {
                this.processTick();
            }
            finally {
                runningLock.unlock();
            }
        }
    }
}

