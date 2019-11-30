
package com.avrgaming.civcraft.structure;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.Location;
import com.avrgaming.civcraft.components.ProjectileLightningComponent;
import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.exception.CivException;
import com.avrgaming.civcraft.exception.InvalidConfiguration;
import com.avrgaming.civcraft.object.Town;
import com.avrgaming.civcraft.structure.Structure;
import com.avrgaming.civcraft.structure.TeslaTower;
import com.avrgaming.civcraft.structure.WaterStructure;
import com.avrgaming.civcraft.util.BlockCoord;

public class TeslaShip
        extends WaterStructure {
    ProjectileLightningComponent teslaComponent;

    protected TeslaShip(Location center, String id, Town town) throws CivException {
        super(center, id, town);
    }

    protected TeslaShip(ResultSet rs) throws SQLException, CivException {
        super(rs);
    }

    @Override
    public void loadSettings() {
        super.loadSettings();
        this.teslaComponent = new ProjectileLightningComponent(this, this.getCenterLocation().getLocation());
        this.teslaComponent.createComponent(this);
        this.teslaComponent.setDamage(this.getDamage());
    }

    public int getDamage() {
        double rate = 1.0;
        return (int) ((double) this.teslaComponent.getDamage() * (rate += this.getTown().getBuffManager().getEffectiveDouble("buff_fire_bomb")));
    }

    @Override
    public String getMarkerIconName() {
        return "shield";
    }

    public void setTurretLocation(BlockCoord absCoord) {
        this.teslaComponent.setTurretLocation(absCoord);
    }

    @Override
    public void onCheck() throws CivException {
        try {
            double build_distance = CivSettings.getDouble(CivSettings.warConfig, "tesla_tower.build_distance");
            for (Town town : this.getTown().getCiv().getTowns()) {
                for (Structure struct : town.getStructures()) {
                    BlockCoord center = struct.getCenterLocation();
                    double distance = center.distance(this.getCenterLocation());
                    if (struct instanceof TeslaTower && distance <= build_distance) {
                        throw new CivException(CivSettings.localize.localizedString("var_buildable_tooCloseToTeslaTower", "" + center.getX() + "," + center.getY() + "," + center.getZ()));
                    }
                    if (struct instanceof TeslaShip && distance <= build_distance) {
                        throw new CivException(CivSettings.localize.localizedString("var_buildable_tooCloseToTeslaShip", "" + center.getX() + "," + center.getY() + "," + center.getZ()));
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
        if (this.getCiv().getCapitol() != null && this.getCiv().getCapitol().getBuffManager().hasBuff("level5_extraTowerHPTown")) {
            rate *= this.getCiv().getCapitol().getBuffManager().getEffectiveDouble("level5_extraTowerHPTown");
        }
        return (int) ((double) this.info.max_hitpoints * rate);
    }
}

