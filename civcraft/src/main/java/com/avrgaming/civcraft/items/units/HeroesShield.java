
package com.avrgaming.civcraft.items.units;

import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.config.ConfigUnit;
import com.avrgaming.civcraft.exception.CivException;
import com.avrgaming.civcraft.lorestorage.LoreMaterial;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.object.Resident;
import com.avrgaming.civcraft.object.Town;
import com.avrgaming.civcraft.util.CivColor;
import gpl.AttributeUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class HeroesShield extends UnitMaterial {
    public HeroesShield(String id, ConfigUnit configUnit) {
        super(id, configUnit);
    }

    public static void spawn(Inventory inv, Town town) throws CivException {
        ItemStack is = LoreMaterial.spawn(Unit.HEROESSHIELD_ARTIFACT);
        HeroesShield.setOwningTown(town, is);
        AttributeUtil attrs = new AttributeUtil(is);
        attrs.addEnhancement("LoreEnhancementSoulBound", null, null);
        attrs.addLore(CivColor.Gold + CivSettings.localize.localizedString("itemLore_Souldbound"));
        ConfigUnit u = CivSettings.units.get(Unit.HEROESSHIELD_ARTIFACT.getUnit().id);
        for (String d : u.description) {
            attrs.addLore(CivColor.colorize(d));
        }
        is = attrs.getStack();
        if (!Unit.addItemNoStack(inv, is)) {
            throw new CivException(CivSettings.localize.localizedString("var_arrtifacts_errorBarracksFull", Unit.HEROESSHIELD_ARTIFACT.getUnit().name));
        }
    }

    @Override
    public void onInteract(PlayerInteractEvent event) {
        SimpleDateFormat sdf = new SimpleDateFormat("M/dd h:mm:ss a z");
        Player player = event.getPlayer();
        Resident interacter = CivGlobal.getResident(player);
        long nextUse = CivGlobal.getUnitCooldown(this.getClass(), event.getPlayer());
        long timeNow = Calendar.getInstance().getTimeInMillis();
        if (nextUse < timeNow) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 620, 0));
            CivGlobal.setUnitCooldown(this.getClass(), 30, event.getPlayer());
            CivMessage.sendSuccess(interacter, CivSettings.localize.localizedString("var_artifact_useSuccusess", sdf.format(timeNow + 1800000L), Unit.HEROESSHIELD_ARTIFACT.getUnit().name));
            this.removeChildren(player.getInventory());
            CivMessage.sendError(interacter, CivSettings.localize.localizedString("var_artifact_useSuccusessButNot"));
        } else {
            CivMessage.sendError(interacter, CivSettings.localize.localizedString("var_artifact_useFailure", sdf.format(nextUse), Unit.HEROESSHIELD_ARTIFACT.getUnit().name));
        }
        event.setCancelled(true);
    }
}

