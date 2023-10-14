package cn.evole.plugins.civcraft.structure;

import cn.evole.plugins.civcraft.exception.CivException;
import cn.evole.plugins.civcraft.object.Town;
import org.bukkit.Location;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Theatre extends Structure {
    protected Theatre(Location center, String id, Town town) throws CivException {
        super(center, id, town);
    }

    public Theatre(ResultSet rs) throws SQLException, CivException {
        super(rs);
    }

    @Override
    public String getMarkerIconName() {
        return "theater";
    }

    @Override
    public void loadSettings() {
        super.loadSettings();
    }

    @Override
    public void onLoad() {
        if (this.isActive()) {
            this.addBuffs();
        }
    }

    @Override
    public void onComplete() {
        this.addBuffs();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.removeBuffs();
    }

    protected void removeBuffs() {
    }

    protected void addBuffs() {
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
}

