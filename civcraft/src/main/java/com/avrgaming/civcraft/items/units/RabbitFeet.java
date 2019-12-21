
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
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class RabbitFeet
        extends UnitMaterial {
    public RabbitFeet(String id, ConfigUnit configUnit) {
        super(id, configUnit);
    }

    public static void spawn(Inventory inv, Town town) throws CivException {
        ItemStack is = LoreMaterial.spawn(Unit.RABBITFEET_ARTIFACT);
        RabbitFeet.setOwningTown(town, is);
        AttributeUtil attrs = new AttributeUtil(is);
        attrs.addEnhancement("LoreEnhancementSoulBound", null, null);
        attrs.addLore(CivColor.Gold + CivSettings.localize.localizedString("itemLore_Souldbound"));
        ConfigUnit u = CivSettings.units.get(Unit.RABBITFEET_ARTIFACT.getUnit().id);
        for (String d : u.description) {
            attrs.addLore(CivColor.colorize(d));
        }
        is = attrs.getStack();
        if (!Unit.addItemNoStack(inv, is)) {
            throw new CivException(CivSettings.localize.localizedString("var_arrtifacts_errorBarracksFull", Unit.RABBITFEET_ARTIFACT.getUnit().name));
        }
    }

    public static void spawnOnce(Location location) {
        ItemStack is = LoreMaterial.spawn(Unit.RABBITFEET_ARTIFACT);
        AttributeUtil attrs = new AttributeUtil(is);
        attrs.addEnhancement("LoreEnhancementSoulBound", null, null);
        attrs.addLore(CivColor.Gold + CivSettings.localize.localizedString("itemLore_Souldbound"));
        attrs.addLore(CivColor.Yellow + "Single Use");
        attrs.addLore(CivColor.LightGray + "Effect:");
        attrs.addLore(CivColor.LightGray + "Active");
        attrs.addLore(CivColor.LightGray + "Gives increased jump height");
        attrs.addLore(CivColor.LightGray + "(5 minutes)");
        attrs.addLore(CivColor.LightGray + "Cooldown: 15 minutes");
        is = attrs.getStack();
        location.getWorld().dropItemNaturally(location, is);
    }

    @Override
    public void onInteract(PlayerInteractEvent event) {
        SimpleDateFormat sdf = new SimpleDateFormat("M/dd h:mm:ss a z");

        Player player = event.getPlayer();
        Resident interacter = CivGlobal.getResident(player);
        long nextUse = CivGlobal.getUnitCooldown(this.getClass(), event.getPlayer());
        long timeNow = Calendar.getInstance().getTimeInMillis();
        if (nextUse < timeNow) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 6000, 1));
            CivGlobal.setUnitCooldown(this.getClass(), 15, event.getPlayer());
            CivMessage.sendSuccess(interacter, CivSettings.localize.localizedString("var_artifact_useSuccusess", sdf.format(timeNow + 900000L), Unit.RABBITFEET_ARTIFACT.getUnit().name));
            if (CivGlobal.isOneUseArtifact(event.getItem())) {
                this.removeChildren(player.getInventory());
                CivMessage.sendError(interacter, CivSettings.localize.localizedString("var_artifact_useSuccusessButNot"));
            }
        } else {
            CivMessage.sendError(interacter, CivSettings.localize.localizedString("var_artifact_useFailure", sdf.format(nextUse), Unit.RABBITFEET_ARTIFACT.getUnit().name));
        }
        event.setCancelled(true);
    }
}

