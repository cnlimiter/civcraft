package cn.evole.plugins.civcraft.structure.wonders;

import cn.evole.plugins.civcraft.exception.CivException;
import cn.evole.plugins.civcraft.object.Town;
import org.bukkit.Location;

import java.sql.ResultSet;
import java.sql.SQLException;

public class GrandCanyon
        extends Wonder {
    public GrandCanyon(ResultSet rs) throws SQLException, CivException {
        super(rs);
    }

    public GrandCanyon(Location center, String id, Town town) throws CivException {
        super(center, id, town);
    }

    @Override
    protected void removeBuffs() {
        this.removeBuffFromTown(this.getTown(), "buff_grandcanyon_rush");
        this.removeBuffFromTown(this.getTown(), "rush");
        this.removeBuffFromTown(this.getTown(), "buff_grandcanyon_hammers");
        this.removeBuffFromTown(this.getTown(), "buff_grandcanyon_quarry_and_trommel");
    }

    @Override
    protected void addBuffs() {
        this.addBuffToTown(this.getTown(), "buff_grandcanyon_rush");
        this.addBuffToTown(this.getTown(), "rush");
        this.addBuffToTown(this.getTown(), "buff_grandcanyon_hammers");
        this.addBuffToTown(this.getTown(), "buff_grandcanyon_quarry_and_trommel");
    }
}

