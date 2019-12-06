
package com.avrgaming.civcraft.structure;

import com.avrgaming.civcraft.exception.CivException;
import com.avrgaming.civcraft.object.Town;
import org.bukkit.Location;

import java.sql.ResultSet;
import java.sql.SQLException;

public class WareHouse extends Structure {
    protected WareHouse(Location center, String id, Town town) throws CivException {
        super(center, id, town);
    }

    public WareHouse(ResultSet rs) throws SQLException, CivException {
        super(rs);
    }

    @Override
    public String getDynmapDescription() {
        return "=)";
    }

    @Override
    public String getMarkerIconName() {
        return "flower";
    }
}

