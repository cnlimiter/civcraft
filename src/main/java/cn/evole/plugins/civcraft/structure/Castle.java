package cn.evole.plugins.civcraft.structure;

import cn.evole.plugins.civcraft.exception.CivException;
import cn.evole.plugins.civcraft.object.Town;
import org.bukkit.Location;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Castle extends Structure {
    protected Castle(Location center, String id, Town town) throws CivException {
        super(center, id, town);
    }

    public Castle(ResultSet rs) throws SQLException, CivException {
        super(rs);
    }

    @Override
    public String getMarkerIconName() {
        return "door";
    }
}

