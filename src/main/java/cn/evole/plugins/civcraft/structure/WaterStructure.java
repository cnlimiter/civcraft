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

import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.exception.CivException;
import cn.evole.plugins.civcraft.object.Town;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.sql.ResultSet;
import java.sql.SQLException;

public class WaterStructure extends Structure {

    public static int WATER_LEVEL = 62;
    public static int TOLERANCE = 20;

    public WaterStructure(ResultSet rs) throws SQLException, CivException {
        super(rs);
    }

    protected WaterStructure(Location center, String id, Town town)
            throws CivException {
        super(center, id, town);
    }

    @Override
    protected Location repositionCenter(Location center, String dir, double x_size, double z_size) {
        Location loc = new Location(center.getWorld(),
                center.getX(), center.getY(), center.getZ(),
                center.getYaw(), center.getPitch());

        // Reposition tile improvements
        if (this.isTileImprovement()) {
            // just put the center at 0,0 of this chunk?
            loc = center.getChunk().getBlock(0, center.getBlockY(), 0).getLocation();
            //loc = center.getChunk().getBlock(arg0, arg1, arg2)
        } else {
            if (dir.equalsIgnoreCase("east")) {
                loc.setZ(loc.getZ() - (z_size / 2));
                loc.setX(loc.getX() + SHIFT_OUT);
            } else if (dir.equalsIgnoreCase("west")) {
                loc.setZ(loc.getZ() - (z_size / 2));
                loc.setX(loc.getX() - (SHIFT_OUT + x_size));

            } else if (dir.equalsIgnoreCase("north")) {
                loc.setX(loc.getX() - (x_size / 2));
                loc.setZ(loc.getZ() - (SHIFT_OUT + z_size));
            } else if (dir.equalsIgnoreCase("south")) {
                loc.setX(loc.getX() - (x_size / 2));
                loc.setZ(loc.getZ() + SHIFT_OUT);

            }
        }

        if (this.getTemplateYShift() != 0) {
            // Y-Shift based on the config, this allows templates to be built underground.
            loc.setY(WATER_LEVEL + this.getTemplateYShift());
        }

        return loc;
    }

    @Override
    protected void checkBlockPermissionsAndRestrictions(Player player, Block centerBlock, int regionX, int regionY, int regionZ, Location savedLocation) throws CivException {
        super.checkBlockPermissionsAndRestrictions(player, centerBlock, regionX, regionY, regionZ, savedLocation);

        if ((player.getLocation().getBlockY() - WATER_LEVEL) > TOLERANCE) {
            throw new CivException(CivSettings.localize.localizedString("buildable_Water_notValidWaterSpot"));
        }

    }

    @Override
    public String getMarkerIconName() {
        return "anchor";
    }
}
