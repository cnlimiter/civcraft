package cn.evole.plugins.civcraft.structure;

import cn.evole.plugins.civcraft.exception.CivException;
import cn.evole.plugins.civcraft.object.Town;
import org.bukkit.Location;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ShopingCenter
        extends Structure {
    protected ShopingCenter(Location center, String id, Town town) throws CivException {
        super(center, id, town);
    }

    public ShopingCenter(ResultSet rs) throws SQLException, CivException {
        super(rs);
    }

    @Override
    public String getDynmapDescription() {
        return null;
    }

    protected void removeBuffs() {
        this.removeBuffFromTown(this.getTown(), "buff_shopingcenter");
    }

    protected void addBuffs() {
        this.addBuffToTown(this.getTown(), "buff_shopingcenter");
    }

    protected void addBuffToTown(Town town, String id) {
        try {
            town.getBuffManager().addBuff(id, id, this.getDisplayName() + " in " + this.getTown().getName());
        } catch (CivException e) {
            e.printStackTrace();
        }
    }

    protected void removeBuffFromTown(Town town, String id) {
        town.getBuffManager().removeBuff(id);
    }

    @Override
    public String getMarkerIconName() {
        return "coins";
    }
}

