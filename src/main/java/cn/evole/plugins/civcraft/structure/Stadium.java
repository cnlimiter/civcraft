package cn.evole.plugins.civcraft.structure;

import cn.evole.plugins.civcraft.exception.CivException;
import cn.evole.plugins.civcraft.object.Town;
import org.bukkit.Location;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Stadium extends Structure {

    protected Stadium(Location center, String id, Town town)
            throws CivException {
        super(center, id, town);
    }

    public Stadium(ResultSet rs) throws SQLException, CivException {
        super(rs);
    }

    @Override
    public String getDynmapDescription() {
        return null;
    }

    @Override
    public String getMarkerIconName() {
        return "flower";
    }
}
