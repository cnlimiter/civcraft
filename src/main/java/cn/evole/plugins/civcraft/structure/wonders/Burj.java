package cn.evole.plugins.civcraft.structure.wonders;

import cn.evole.plugins.civcraft.exception.CivException;
import cn.evole.plugins.civcraft.object.Town;
import org.bukkit.Location;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Burj extends Wonder {
    public Burj(ResultSet rs) throws SQLException, CivException {
        super(rs);
    }

    public Burj(Location center, String id, Town town) throws CivException {
        super(center, id, town);
    }

    @Override
    protected void removeBuffs() {
        this.removeBuffFromCiv(this.getCiv(), "buff_burj_growth");
        this.removeBuffFromCiv(this.getCiv(), "buff_burj_happy");
    }

    @Override
    protected void addBuffs() {
        this.addBuffToCiv(this.getCiv(), "buff_burj_growth");
        this.addBuffToCiv(this.getCiv(), "buff_burj_happy");
    }
}

