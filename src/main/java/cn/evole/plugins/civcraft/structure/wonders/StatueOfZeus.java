package cn.evole.plugins.civcraft.structure.wonders;

import cn.evole.plugins.civcraft.components.ProjectileLightningComponent;
import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.exception.CivException;
import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.main.CivMessage;
import cn.evole.plugins.civcraft.object.Town;
import cn.evole.plugins.civcraft.util.BlockCoord;
import cn.evole.plugins.civcraft.util.CivColor;
import org.bukkit.Location;

import java.sql.ResultSet;
import java.sql.SQLException;

public class StatueOfZeus
        extends Wonder {
    ProjectileLightningComponent teslaComponent;

    public StatueOfZeus(ResultSet rs) throws SQLException, CivException {
        super(rs);
    }

    public StatueOfZeus(Location center, String id, Town town) throws CivException {
        super(center, id, town);
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
        if (this.getTown().getBuffManager().hasBuff("buff_powerstation")) {
            rate = 1.4;
        }
        return (int) ((double) this.teslaComponent.getDamage() * rate);
    }

    public void setTurretLocation(BlockCoord absCoord) {
        this.teslaComponent.setTurretLocation(absCoord);
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
        if (this.getCiv().getCapitol() != null && this.getCiv().getCapitol().getBuffManager().hasBuff("level6_extraTowerHPTown")) {
            rate *= this.getCiv().getCapitol().getBuffManager().getEffectiveDouble("level6_extraTowerHPTown");
        }
        return (int) ((double) this.info.max_hitpoints * rate);
    }

    @Override
    public void onComplete() {
        this.addBuffs();
    }

    @Override
    protected void removeBuffs() {
        this.removeBuffFromTown(this.getTown(), "buff_statue_of_zeus_tower_range");
        this.removeBuffFromTown(this.getTown(), "buff_statue_of_zeus_struct_regen");
    }

    @Override
    protected void addBuffs() {
        this.addBuffToTown(this.getTown(), "buff_statue_of_zeus_tower_range");
        this.addBuffToTown(this.getTown(), "buff_statue_of_zeus_struct_regen");
    }

    public void processBonuses() {
        int culture = 500;
        int coins = 10000;
        int totalCulture = 0;
        int totalCoins = 0;
        for (Town town : CivGlobal.getTowns()) {
            if (town.getMotherCiv() == null) continue;
            totalCulture += culture;
            totalCoins += coins;
        }
        if (totalCoins != 0) {
            this.getTown().getTreasury().deposit(totalCoins);
            this.getTown().addAccumulatedCulture(totalCulture);
            int captured = totalCulture / culture;
            CivMessage.sendCiv(this.getCiv(), CivSettings.localize.localizedString("var_statue_of_zeus_addedCoinsAndCulture",
                    CivColor.LightGreen + totalCulture + CivColor.RESET, CivColor.Gold + totalCoins + " " + CivSettings.CURRENCY_NAME + CivColor.RESET, CivColor.Rose + captured + CivColor.RESET,
                    CivColor.Yellow + this.getTown().getName() + CivColor.RESET));
        }
    }
}

