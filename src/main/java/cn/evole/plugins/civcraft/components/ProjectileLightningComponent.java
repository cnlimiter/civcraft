package cn.evole.plugins.civcraft.components;

import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.exception.InvalidConfiguration;
import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.main.CivMessage;
import cn.evole.plugins.civcraft.object.Resident;
import cn.evole.plugins.civcraft.object.Town;
import cn.evole.plugins.civcraft.structure.Buildable;
import cn.evole.plugins.civcraft.structure.wonders.StatueOfZeus;
import cn.evole.plugins.civcraft.util.BlockCoord;
import cn.evole.plugins.civcraft.util.CivColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class ProjectileLightningComponent extends ProjectileComponent {
    private int fireRate;
    private int halfSecondCount = 0;

    public ProjectileLightningComponent(Buildable buildable, Location turretCenter) {
        super(buildable, turretCenter);
    }

    @Override
    public void fire(Location turretLoc, Entity targetEntity) {
        if (this.halfSecondCount < this.fireRate) {
            ++this.halfSecondCount;
            return;
        }
        this.halfSecondCount = 0;
        World world = turretLoc.getWorld();
        Location location = targetEntity.getLocation();
        world.strikeLightningEffect(location);
        this.applyFire(targetEntity);
    }

    public void applyFire(Entity entity) {
        if (entity == null) {
            return;
        }
        if (!(entity instanceof LivingEntity)) {
            return;
        }
        LivingEntity target = (LivingEntity) entity;
        int dmg = this.getDamage();
        if (!(target instanceof Player)) {
            if (target.getHealth() - (double) dmg > 0.0) {
                target.setHealth(target.getHealth() - (double) dmg);
                target.damage(0.5);
            } else {
                target.setHealth(0.1);
                target.damage(1.0);
            }
            target.setFireTicks(60);
            return;
        }
        Resident resident = CivGlobal.getResident((Player) target);
        if (resident.isPLCImmuned()) {
            return;
        }
        if (resident.getCiv().hasWonder("w_statue_of_zeus")) {
            dmg -= 2;
        }
        if (target.getHealth() - (double) dmg > 0.0) {
            target.setHealth(target.getHealth() - (double) dmg);
            target.damage(0.5);
        } else {
            target.setHealth(0.1);
            target.damage(1.0);
        }
        target.setFireTicks(60);
        resident.addPLCImmune(3);
        CivMessage.send((Object) target, CivColor.LightGray + CivSettings.localize.localizedString("playerListen_combatHeading") + " " + CivSettings.localize.localizedString("var_playerListen_combatDefend", "§d'Enemey Tesla'", CivColor.Red + dmg));
        CivMessage.sendTitle((Object) target, "", CivSettings.localize.localizedString("var_plc_entityMessage", this.getTown().getName()));
    }

    @Override
    public void loadSettings() {
        try {
            this.setDamage(CivSettings.getInteger(CivSettings.warConfig, "tesla_tower.damage"));
            this.range = CivSettings.getDouble(CivSettings.warConfig, "tesla_tower.range");
            if (this.getBuildable() instanceof StatueOfZeus) {
                this.range += 100.0;
            }
            if (this.getTown().getBuffManager().hasBuff("buff_great_lighthouse_tower_range") && this.getBuildable().getConfigId().equals("s_teslatower")) {
                this.range *= this.getTown().getBuffManager().getEffectiveDouble("buff_great_lighthouse_tower_range");
            }
            if (this.getTown().getBuffManager().hasBuff("buff_statue_of_zeus_tower_range")) {
                this.range *= this.getTown().getBuffManager().getEffectiveDouble("buff_statue_of_zeus_tower_range");
            }
            if (this.getTown().getCiv().getCapitol() != null) {
                this.range += this.getTown().getCiv().getCapitol().getBuffManager().getEffectiveDouble("level6_extraRangeTown");
            }
            this.min_range = CivSettings.getDouble(CivSettings.warConfig, "tesla_tower.min_range");
            this.fireRate = CivSettings.getInteger(CivSettings.warConfig, "tesla_tower.fire_rate");
            this.proximityComponent.setBuildable(this.buildable);
            this.proximityComponent.setCenter(new BlockCoord(this.getTurretCenter()));
            this.proximityComponent.setRadius(this.range);
        } catch (InvalidConfiguration e) {
            e.printStackTrace();
        }
    }

    public Town getTown() {
        return this.buildable.getTown();
    }
}

