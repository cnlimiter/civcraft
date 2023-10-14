/**
 * [2023] Cnlimiter LLC
 * All Rights Reserved.
 * Name: civcraft
 * Author: cnlimiter
 * UpdateTime: 2023/10/14 16:15
 * Description:
 * License: ARR
 */
package cn.evole.plugins.civcraft.structure.wonders;

import cn.evole.plugins.civcraft.exception.CivException;
import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.object.Civilization;
import cn.evole.plugins.civcraft.object.Town;
import org.bukkit.Location;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.TreeMap;

public class TheGreatPyramid extends Wonder {


    public TheGreatPyramid(Location center, String id, Town town)
            throws CivException {
        super(center, id, town);
    }

    public TheGreatPyramid(ResultSet rs) throws SQLException, CivException {
        super(rs);
    }

    private Civilization calculateNearestCivilization() {
        TreeMap<Double, Civilization> civMaps = CivGlobal.findNearestCivilizations(this.getTown());
        Civilization nearestCiv = null;
        if (civMaps.size() > 0) {
            nearestCiv = civMaps.firstEntry().getValue();
        }
        return nearestCiv;
    }

    @Override
    protected void addBuffs() {
        addBuffToTown(this.getTown(), "buff_pyramid_cottage_consume");
        addBuffToTown(this.getTown(), "buff_pyramid_cottage_bonus");
        addBuffToCiv(this.getCiv(), "buff_pyramid_culture");
        addBuffToTown(this.getTown(), "buff_pyramid_leech");
        Civilization nearest = calculateNearestCivilization();
        if (nearest != null) {
            addBuffToCiv(nearest, "debuff_pyramid_leech");
        }
    }

    @Override
    protected void removeBuffs() {
        removeBuffFromTown(this.getTown(), "buff_pyramid_cottage_consume");
        removeBuffFromTown(this.getTown(), "buff_pyramid_cottage_bonus");
        removeBuffFromCiv(this.getCiv(), "buff_pyramid_culture");
        removeBuffFromTown(this.getTown(), "buff_pyramid_leech");
        Civilization nearest = calculateNearestCivilization();
        if (nearest != null) {
            removeBuffFromCiv(nearest, "debuff_pyramid_leech");
        }
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
