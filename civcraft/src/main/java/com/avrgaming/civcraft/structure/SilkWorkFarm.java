
package com.avrgaming.civcraft.structure;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.locks.ReentrantLock;
import org.bukkit.Location;
import com.avrgaming.civcraft.exception.CivException;
import com.avrgaming.civcraft.object.Town;
import com.avrgaming.civcraft.structure.Structure;

public class SilkWorkFarm
extends Structure {
    public int virtualPotatoCount = 0;
    public ReentrantLock lock = new ReentrantLock();

    protected SilkWorkFarm(Location center, String id, Town town) throws CivException {
        super(center, id, town);
    }

    public SilkWorkFarm(ResultSet rs) throws SQLException, CivException {
        super(rs);
    }

    @Override
    public void loadSettings() {
        super.loadSettings();
    }

    @Override
    public String getDynmapDescription() {
        return "";
    }

    @Override
    public String getMarkerIconName() {
        return "bighouse";
    }
}

