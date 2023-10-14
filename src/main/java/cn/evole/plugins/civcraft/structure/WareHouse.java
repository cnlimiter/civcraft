package cn.evole.plugins.civcraft.structure;

import cn.evole.plugins.civcraft.exception.CivException;
import cn.evole.plugins.civcraft.object.Town;
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

