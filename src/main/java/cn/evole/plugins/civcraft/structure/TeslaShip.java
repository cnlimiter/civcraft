package cn.evole.plugins.civcraft.structure;

import cn.evole.plugins.civcraft.components.ProjectileLightningComponent;
import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.exception.CivException;
import cn.evole.plugins.civcraft.exception.InvalidConfiguration;
import cn.evole.plugins.civcraft.object.Town;
import cn.evole.plugins.civcraft.util.BlockCoord;
import org.bukkit.Location;

import java.sql.ResultSet;
import java.sql.SQLException;

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
        return (int) ((double) this.teslaComponent.getDamage() * (rate + this.getTown().getBuffManager().getEffectiveDouble("buff_fire_bomb")));
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
        if (this.getCiv().getCapitol() != null && this.getCiv().getCapitol().getBuffManager().hasBuff("level6_extraTowerHPTown")) {
            rate *= this.getCiv().getCapitol().getBuffManager().getEffectiveDouble("level6_extraTowerHPTown");
        }
        return (int) ((double) this.info.max_hitpoints * rate);
    }
}

