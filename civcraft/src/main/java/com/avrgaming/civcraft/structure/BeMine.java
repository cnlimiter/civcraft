
package com.avrgaming.civcraft.structure;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.Location;
import com.avrgaming.civcraft.exception.CivException;
import com.avrgaming.civcraft.object.Town;
import com.avrgaming.civcraft.structure.Structure;

public class BeMine
        extends Structure {
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

