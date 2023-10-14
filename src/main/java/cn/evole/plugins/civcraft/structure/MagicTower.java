package cn.evole.plugins.civcraft.structure;

import cn.evole.plugins.civcraft.components.ProjectileMagicComponent;
import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.exception.CivException;
import cn.evole.plugins.civcraft.exception.InvalidConfiguration;
import cn.evole.plugins.civcraft.object.Town;
import cn.evole.plugins.civcraft.util.BlockCoord;
import org.bukkit.Location;

import java.sql.ResultSet;
import java.sql.SQLException;

public class MagicTower extends Structure {
    ProjectileMagicComponent magicComponent;

    protected MagicTower(Location center, String id, Town town) throws CivException {
        super(center, id, town);
        this.hitpoints = this.getMaxHitPoints();
    }

    protected MagicTower(ResultSet rs) throws SQLException, CivException {
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

    @Override
    public int getMaxHitPoints() {
        double rate = 1.0;
        if (this.getTown().getBuffManager().hasBuff("buff_chichen_itza_tower_hp")) {
            rate += this.getTown().getBuffManager().getEffectiveDouble("buff_chichen_itza_tower_hp");
        }
        if (this.getTown().getBuffManager().hasBuff("buff_barricade")) {
            rate += this.getTown().getBuffManager().getEffectiveDouble("buff_barricade");
        }
        if (this.getCiv().getCapitol() != null && this.getCiv().getCapitol().getBuffManager().hasBuff("level6_extraTowerHPTown") && this.getCiv().getCapitol() != null) {
            rate *= this.getCiv().getCapitol().getBuffManager().getEffectiveDouble("level6_extraTowerHPTown");
        }
        return (int) ((double) this.info.max_hitpoints * rate);
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
}

