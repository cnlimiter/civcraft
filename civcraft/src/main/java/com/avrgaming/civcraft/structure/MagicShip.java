
package com.avrgaming.civcraft.structure;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.Location;
import com.avrgaming.civcraft.components.ProjectileMagicComponent;
import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.exception.CivException;
import com.avrgaming.civcraft.exception.InvalidConfiguration;
import com.avrgaming.civcraft.object.Town;
import com.avrgaming.civcraft.structure.MagicTower;
import com.avrgaming.civcraft.structure.Structure;
import com.avrgaming.civcraft.structure.WaterStructure;
import com.avrgaming.civcraft.util.BlockCoord;

public class MagicShip
        extends WaterStructure {
    ProjectileMagicComponent magicComponent;

    protected MagicShip(Location center, String id, Town town) throws CivException {
        super(center, id, town);
    }

    protected MagicShip(ResultSet rs) throws SQLException, CivException {
        super(rs);
    }

    @Override
    public void loadSettings() {
        super.loadSettings();
        this.magicComponent = new ProjectileMagicComponent(this, this.getCenterLocation().getLocation());
        this.magicComponent.createComponent(this);
    }

    public int getDamage() {
        double rate = 1.0;
        return (int) ((double) this.magicComponent.getDamage() * (rate += this.getTown().getBuffManager().getEffectiveDouble("buff_fire_bomb")));
    }

    @Override
    public String getMarkerIconName() {
        return "shield";
    }

    public void setTurretLocation(BlockCoord absCoord) {
        this.magicComponent.setTurretLocation(absCoord);
    }

    @Override
    public void onCheck() throws CivException {
        try {
            double build_distance = CivSettings.getDouble(CivSettings.warConfig, "tesla_tower.build_distance");
            for (Town town : this.getTown().getCiv().getTowns()) {
                for (Structure structure : town.getStructures()) {
                    BlockCoord center = structure.getCenterLocation();
                    double distance = center.distance(this.getCenterLocation());
                    if (structure instanceof MagicTower && distance <= build_distance) {
                        throw new CivException(CivSettings.localize.localizedString("var_buildable_tooCloseToMagicTower", "" + center.getX() + "," + center.getY() + "," + center.getZ()));
                    }
                    if (structure instanceof MagicShip && distance <= build_distance) {
                        throw new CivException(CivSettings.localize.localizedString("var_buildable_tooCloseToMagicShip", "" + center.getX() + "," + center.getY() + "," + center.getZ()));
                    }
                }
            }
        } catch (InvalidConfiguration e) {
            e.printStackTrace();
            throw new CivException(e.getMessage());
        }
    }

    @Override
    public int getMaxHitPoints() {
        double rate = 1.0;
        if (this.getTown().getBuffManager().hasBuff("buff_chichen_itza_tower_hp")) {
            rate += this.getTown().getBuffManager().getEffectiveDouble("buff_chichen_itza_tower_hp");
        }
        if (this.getTown().getBuffManager().hasBuff("buff_barricade")) {
            rate += this.getTown().getBuffManager().getEffectiveDouble("buff_barricade");
        }
        if (this.getCiv().getCapitol() != null && this.getCiv().getCapitol().getBuffManager().hasBuff("level5_extraTowerHPTown")) {
            rate *= this.getCiv().getCapitol().getBuffManager().getEffectiveDouble("level5_extraTowerHPTown");
        }
        return (int) ((double) this.info.max_hitpoints * rate);
    }
}

