package cn.evole.plugins.civcraft.structure;

import cn.evole.plugins.civcraft.exception.CivException;
import cn.evole.plugins.civcraft.object.Town;
import org.bukkit.Location;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.locks.ReentrantLock;

public class SilkWorkFarm extends Structure {
    public int virtualPotatoCount = 0;
    public ReentrantLock lock = new ReentrantLock();

    protected SilkWorkFarm(Location center, String id, Town town) throws CivException {
        super(center, id, town);
    }

    public SilkWorkFarm(ResultSet rs) throws SQLException, CivException {
        super(rs);
    }

    @Override
    public void loadSettings() {
        super.loadSettings();
    }

    @Override
    public String getDynmapDescription() {
        return "";
    }

    @Override
    public String getMarkerIconName() {
        return "bighouse";
    }
}

