package cn.evole.plugins.civcraft.structure;

import cn.evole.plugins.civcraft.exception.CivException;
import cn.evole.plugins.civcraft.object.Town;
import org.bukkit.Location;

import java.sql.ResultSet;
import java.sql.SQLException;

public class BeMine extends Structure {
    protected BeMine(Location center, String id, Town town) throws CivException {
        super(center, id, town);
    }

    public BeMine(ResultSet rs) throws SQLException, CivException {
        super(rs);
    }

    @Override
    public String getMarkerIconName() {
        return "offlineuser";
    }

    @Override
    public void loadSettings() {
        super.loadSettings();
    }

    @Override
    public void onLoad() {
    }

    @Override
    public void onComplete() {
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}

