package cn.evole.plugins.civcraft.threading.tasks;

import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.object.Resident;
import org.bukkit.entity.Player;

public class PlayerArmorSpeedAsyncTask implements Runnable {

    Player player;

    public PlayerArmorSpeedAsyncTask(Player player) {
        this.player = player;
    }

    @Override
    public void run() {
        doArmorSpeedCheck();
    }

    public void doArmorSpeedCheck() {
        Resident resident = CivGlobal.getResident(this.player);
        resident.calculateWalkingModifier(this.player);
    }

}
