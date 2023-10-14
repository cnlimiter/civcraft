package cn.evole.plugins.civcraft.structure.wonders;

import cn.evole.plugins.civcraft.exception.CivException;
import cn.evole.plugins.civcraft.main.CivCraft;
import cn.evole.plugins.civcraft.object.Town;
import cn.evole.plugins.civcraft.structure.RespawnLocationHolder;
import cn.evole.plugins.civcraft.util.BlockCoord;
import cn.evole.plugins.civcraft.util.CivColor;
import org.bukkit.Location;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Neuschwanstein extends Wonder implements RespawnLocationHolder {
    private ArrayList<BlockCoord> revivePoints = new ArrayList<BlockCoord>();

    protected Neuschwanstein(Location center, String id, Town town) throws CivException {
        super(center, id, town);
    }

    public Neuschwanstein(ResultSet rs) throws SQLException, CivException {
        super(rs);
    }

    @Override
    protected void removeBuffs() {
        this.removeBuffFromTown(this.getTown(), "buff_neuschwanstein_culture");
    }

    @Override
    protected void addBuffs() {
        this.addBuffToTown(this.getTown(), "buff_neuschwanstein_culture");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.getCiv().updateReviveSigns();
    }

    @Override
    public String getRespawnName() {
        String townInfo = CivColor.LightGray + "Neuschwanstein";
        Town town = this.getTown();
        return townInfo + "\n" + CivColor.Gold + town.getName() + "\nLocation:\n" + CivColor.LightGreen + this.corner.getX() + " " + this.corner.getY() + " " + this.corner.getZ();
    }

    public void setRevivePoint(BlockCoord absCoord) {
        this.revivePoints.add(absCoord);
    }

    @Override
    public List<BlockCoord> getRespawnPoints() {
        return this.revivePoints;
    }

    @Override
    public BlockCoord getRandomRevivePoint() {
        if (this.revivePoints.size() == 0 || !this.isComplete()) {
            return new BlockCoord(this.getCorner());
        }
        Random rand = CivCraft.civRandom;
        int index = rand.nextInt(this.revivePoints.size());
        return this.revivePoints.get(index);
    }

    @Override
    public boolean isTeleportReal() {
        if (this.isDestroyed()) {
            return false;
        }
        return true;
    }
}

