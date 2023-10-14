package cn.evole.plugins.civcraft.structure.wonders;

import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.exception.CivException;
import cn.evole.plugins.civcraft.object.Town;
import cn.evole.plugins.civcraft.util.BlockCoord;
import cn.evole.plugins.civcraft.util.SimpleBlock;
import org.bukkit.Location;

import java.sql.ResultSet;
import java.sql.SQLException;

public class StockExchange
        extends Wonder {
    private int level = 0;

    public StockExchange(ResultSet rs) throws SQLException, CivException {
        super(rs);
    }

    public StockExchange(Location center, String id, Town town) throws CivException {
        super(center, id, town);
        this.setLevel(town.saved_stock_exchange_level);
    }

    @Override
    protected void removeBuffs() {
    }

    @Override
    protected void addBuffs() {
    }

    @Override
    public String getDynmapDescription() {
        String out = "<u><b>" + CivSettings.localize.localizedString("stockexchange_dynmapName") + "</u></b><br/>";
        out = out + CivSettings.localize.localizedString("Level") + " " + this.level;
        return out;
    }

    public int getLevel() {
        return this.level;
    }

    public final void setLevel(int level) {
        this.level = level;
    }

    @Override
    public void onPostBuild(BlockCoord absCoord, SimpleBlock commandBlock) {
        this.level = this.getTown().saved_stock_exchange_level;
    }
}

