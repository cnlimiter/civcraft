
package com.avrgaming.civcraft.components;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import com.avrgaming.civcraft.components.ProjectileComponent;
import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.exception.InvalidConfiguration;
import com.avrgaming.civcraft.object.Town;
import com.avrgaming.civcraft.structure.Buildable;
import com.avrgaming.civcraft.util.BlockCoord;

public class ProjectileMagicComponent
        extends ProjectileComponent {
    private int fireRate;
    private int halfSecondCount = 0;

    public ProjectileMagicComponent(Buildable buildable, Location turretCenter) {
        super(buildable, turretCenter);
    }

    @Override
    public void fire(Location turretLoc, Entity targetEntity) {
        Player target;
        if (this.halfSecondCount < this.fireRate) {
            ++this.halfSecondCount;
            return;
        }
        this.halfSecondCount = 0;
        try {
            target = (Player) targetEntity;
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 1200, 0));
        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 600, 0));
        CivMessage.sendTitle((Object) targetEntity, "", CivSettings.localize.localizedString("var_pmc_entityMessage", this.getTown().getName()));
    }

    @Override
    public void loadSettings() {
        try {
            this.setDamage(2);
            this.range = CivSettings.getDouble(CivSettings.warConfig, "tesla_tower.range");
            if (this.getTown().getBuffManager().hasBuff("buff_great_lighthouse_tower_range") && this.getBuildable().getConfigId().equals("s_magictower")) {
                this.range *= this.getTown().getBuffManager().getEffectiveDouble("buff_great_lighthouse_tower_range");
            }
            if (this.getTown().getCiv().getCapitol() != null) {
                this.range += this.getTown().getCiv().getCapitol().getBuffManager().getEffectiveDouble("level6_extraRangeTown");
            }
            this.min_range = CivSettings.getDouble(CivSettings.warConfig, "tesla_tower.min_range");
            this.fireRate = 20;
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

