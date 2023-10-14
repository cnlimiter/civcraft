package cn.evole.plugins.civcraft.structure;

import cn.evole.plugins.civcraft.components.ProjectileArrowComponent;
import cn.evole.plugins.civcraft.exception.CivException;
import cn.evole.plugins.civcraft.object.Buff;
import cn.evole.plugins.civcraft.object.Town;
import cn.evole.plugins.civcraft.util.BlockCoord;
import cn.evole.plugins.civcraft.util.SimpleBlock;
import org.bukkit.Location;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

public class ArrowShip extends WaterStructure {

    ProjectileArrowComponent arrowComponent;
    private HashMap<Integer, ProjectileArrowComponent> arrowTowers = new HashMap<Integer, ProjectileArrowComponent>();


    protected ArrowShip(Location center, String id, Town town)
            throws CivException {
        super(center, id, town);
    }

    protected ArrowShip(ResultSet rs) throws SQLException, CivException {
        super(rs);
    }

    @Override
    public void loadSettings() {
        super.loadSettings();
        arrowComponent = new ProjectileArrowComponent(this, this.getCenterLocation().getLocation());
        arrowComponent.createComponent(this);
    }

    public void onPostBuild(BlockCoord absCoord, SimpleBlock commandBlock) {
        if (commandBlock.command.equals("/towerfire")) {
            String id = commandBlock.keyvalues.get("id");
            Integer towerID = Integer.valueOf(id);

            if (!arrowTowers.containsKey(towerID)) {

                ProjectileArrowComponent arrowTower = new ProjectileArrowComponent(this, absCoord.getLocation());
                arrowTower.createComponent(this);
                arrowTower.setTurretLocation(absCoord);

                arrowTowers.put(towerID, arrowTower);
            }
        }
    }

    /**
     * @return the damage
     */
    public int getDamage() {
        double rate = 1;
        rate += this.getTown().getBuffManager().getEffectiveDouble(Buff.FIRE_BOMB);
        return (int) (arrowComponent.getDamage() * rate);
    }

//	/**
//	 * @param damage the damage to set
//	 */
//	public void setDamage(int damage) {
//		arrowComponent.setDamage(damage);
//	}

//	/**
//	 * @return the power
//	 */
//	public double getPower() {
//		return arrowComponent.getPower();
//	}

    /**
     * @param power the power to set
     */
    public void setPower(double power) {
        arrowComponent.setPower(power);
    }

    public void setTurretLocation(BlockCoord absCoord) {
        arrowComponent.setTurretLocation(absCoord);
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
