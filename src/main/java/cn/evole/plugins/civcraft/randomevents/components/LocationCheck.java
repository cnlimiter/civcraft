package cn.evole.plugins.civcraft.randomevents.components;

import cn.evole.plugins.civcraft.cache.PlayerLocationCache;
import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.main.CivLog;
import cn.evole.plugins.civcraft.object.Resident;
import cn.evole.plugins.civcraft.randomevents.RandomEventComponent;
import cn.evole.plugins.civcraft.util.BlockCoord;

import java.util.List;

public class LocationCheck extends RandomEventComponent {

    @Override
    public void process() {
    }

    public boolean onCheck() {

        String varname = this.getString("varname");
        String locString = this.getParent().componentVars.get(varname);

        if (locString == null) {
            CivLog.warning("Couldn't get var name: " + varname + " for location check component.");
            return false;
        }

        BlockCoord bcoord = new BlockCoord(locString);
        double radiusSquared = 2500.0; /* 50 block radius */
        List<PlayerLocationCache> cache = PlayerLocationCache.getNearbyPlayers(bcoord, radiusSquared);

        for (PlayerLocationCache pc : cache) {
            Resident resident = CivGlobal.getResident(pc.getName());
            if (resident.getTown() == this.getParentTown()) {
                return true;
            }
        }

        return false;

    }

}
