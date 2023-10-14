package cn.evole.plugins.civcraft.structure.wonders;

import cn.evole.plugins.civcraft.exception.CivException;
import cn.evole.plugins.civcraft.object.Town;
import org.bukkit.Location;

import java.sql.ResultSet;
import java.sql.SQLException;

public class GlobeTheatre extends Wonder {

    public GlobeTheatre(ResultSet rs) throws SQLException, CivException {
        super(rs);
    }

    public GlobeTheatre(Location center, String id, Town town) throws CivException {
        super(center, id, town);
    }

    @Override
    protected void removeBuffs() {
        removeBuffFromCiv(this.getCiv(), "buff_globe_theatre_happiness_to_towns");
        removeBuffFromTown(this.getTown(), "buff_globe_theatre_culture_from_towns");
    }

    @Override
    protected void addBuffs() {
        addBuffToCiv(this.getCiv(), "buff_globe_theatre_happiness_to_towns");
        addBuffToTown(this.getTown(), "buff_globe_theatre_culture_from_towns");
    }

    @Override
    public void onLoad() {
        if (this.isActive()) {
            addBuffs();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        removeBuffs();
    }

    @Override
    public void onComplete() {
        addBuffs();
    }

}
