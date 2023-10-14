/**
 * [2023] Cnlimiter LLC
 * All Rights Reserved.
 * Name: civcraft
 * Author: cnlimiter
 * UpdateTime: 2023/10/14 16:15
 * Description:
 * License: ARR
 */
package cn.evole.plugins.civcraft.structure;

import cn.evole.plugins.civcraft.exception.CivException;
import cn.evole.plugins.civcraft.object.Town;
import org.bukkit.Location;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Monument extends Structure {

    protected Monument(Location center, String id, Town town) throws CivException {
        super(center, id, town);
    }

    public Monument(ResultSet rs) throws SQLException, CivException {
        super(rs);
    }

    @Override
    public String getDynmapDescription() {
        return null;
    }

    @Override
    public String getMarkerIconName() {
        return "building";
    }
}
