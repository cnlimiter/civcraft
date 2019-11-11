
package com.avrgaming.civcraft.items.units;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import gpl.AttributeUtil;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.config.ConfigUnit;
import com.avrgaming.civcraft.items.units.Unit;
import com.avrgaming.civcraft.items.units.UnitMaterial;
import com.avrgaming.civcraft.lorestorage.LoreMaterial;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.exception.CivException;
import com.avrgaming.civcraft.object.Resident;
import com.avrgaming.civcraft.object.Town;
import com.avrgaming.civcraft.util.CivColor;

public class Archer2
extends UnitMaterial {
    public Archer2(String id, ConfigUnit configUnit) {
        super(id, configUnit);
    }

    public static void spawn(Inventory inv, Town town) throws CivException {
        ItemStack is = LoreMaterial.spawn(Unit.ARCHER2_ARTIFACT);
        Archer2.setOwningTown(town, is);
        AttributeUtil attrs = new AttributeUtil(is);
        attrs.addEnhancement("LoreEnhancementSoulBound", null, null);
        attrs.addLore(CivColor.Gold+CivSettings.localize.localizedString("itemLore_Souldbound"));
        attrs.addLore(CivColor.Yellow +"Single Use");
        attrs.addLore(CivColor.LightGray + "Effect:");
        attrs.addLore(CivColor.LightGray + "Active");
        attrs.addLore(CivColor.LightGray + "Prevents Hand-to-hand combat");
        attrs.addLore(CivColor.LightGray + "Arrows ignite for ~3 seconds");
        attrs.addLore(CivColor.LightGray + "Slows players for ~3 seconds");
        attrs.addLore(CivColor.LightGray + "1 minute");
        attrs.addLore(CivColor.LightGray + "Cooldown: 1 minute");
        is = attrs.getStack();
        if (!Unit.addItemNoStack(inv, is)) {
            throw new CivException(CivSettings.localize.localizedString("var_arrtifacts_errorBarracksFull", Unit.ARCHER2_ARTIFACT.getUnit().name));
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
            player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 1200, 100));
            CivGlobal.setUnitCooldown(this.getClass(), 1, event.getPlayer());
            CivMessage.sendSuccess(interacter, CivSettings.localize.localizedString("var_artifact_useSuccusess", sdf.format(timeNow + 60000L), Unit.ARCHER2_ARTIFACT.getUnit().name));
        } else {
            CivMessage.sendError(interacter, CivSettings.localize.localizedString("var_artifact_useFailure", sdf.format(nextUse), Unit.ARCHER2_ARTIFACT.getUnit().name));
        }
        event.setCancelled(true);
        player.updateInventory();
    }
}

