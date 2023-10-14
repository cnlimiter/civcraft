package cn.evole.plugins.civcraft.items.units;

import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.config.ConfigUnit;
import cn.evole.plugins.civcraft.exception.CivException;
import cn.evole.plugins.civcraft.lorestorage.LoreMaterial;
import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.main.CivMessage;
import cn.evole.plugins.civcraft.object.Resident;
import cn.evole.plugins.civcraft.object.Town;
import cn.evole.plugins.civcraft.util.CivColor;
import cn.evole.plugins.civcraft.war.War;
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

public class MinersAmulet extends UnitMaterial {
    public MinersAmulet(String id, ConfigUnit configUnit) {
        super(id, configUnit);
    }

    public static void spawn(Inventory inv, Town town) throws CivException {
        ItemStack is = LoreMaterial.spawn(Unit.MINERSAMULET_ARTIFACT);
        setOwningTown(town, is);
        AttributeUtil attrs = new AttributeUtil(is);
        attrs.addEnhancement("LoreEnhancementSoulBound", null, null);
        attrs.addLore(CivColor.Gold + CivSettings.localize.localizedString("itemLore_Souldbound"));
        ConfigUnit u = CivSettings.units.get(Unit.MINERSAMULET_ARTIFACT.getUnit().id);
        for (String d : u.description) {
            attrs.addLore(CivColor.colorize(d));
        }
        is = attrs.getStack();
        if (!Unit.addItemNoStack(inv, is)) {
            throw new CivException(CivSettings.localize.localizedString("var_arrtifacts_errorBarracksFull", Unit.MINERSAMULET_ARTIFACT.getUnit().name));
        }
    }

    public static void spawnOnce(Location location) {
        ItemStack is = LoreMaterial.spawn(Unit.MINERSAMULET_ARTIFACT);
        AttributeUtil attrs = new AttributeUtil(is);
        attrs.addEnhancement("LoreEnhancementSoulBound", null, null);
        attrs.addLore(CivColor.Gold + CivSettings.localize.localizedString("itemLore_Souldbound"));
        attrs.addLore(CivColor.Yellow + "Single Use");
        attrs.addLore(CivColor.LightGray + "Effect:");
        attrs.addLore(CivColor.LightGray + "Active");
        attrs.addLore(CivColor.LightGray + "Increases your dig speed");
        attrs.addLore(CivColor.LightGray + "(10 minutes)");
        attrs.addLore(CivColor.LightGray + "Cooldown: 20 minutes");
        attrs.addLore(CivColor.Red + "Cannot be used during War Time");
        is = attrs.getStack();
        location.getWorld().dropItemNaturally(location, is);
    }

    @Override
    public void onInteract(PlayerInteractEvent event) {
        if (War.isWarTime()) {
            CivMessage.sendError((Object) event.getPlayer(), CivSettings.localize.localizedString("var_artifact_useFailureSinceWar", Unit.MINERSAMULET_ARTIFACT.getUnit().name));
            return;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("M/dd h:mm:ss a z");
        Player player = event.getPlayer();
        Resident interacter = CivGlobal.getResident(player);
        long nextUse = CivGlobal.getUnitCooldown(this.getClass(), event.getPlayer());
        long timeNow = Calendar.getInstance().getTimeInMillis();
        if (nextUse < timeNow) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 13500, 1));
            CivGlobal.setUnitCooldown(this.getClass(), 20, event.getPlayer());
            CivMessage.sendSuccess(interacter, CivSettings.localize.localizedString("var_artifact_useSuccusess", sdf.format(timeNow + 1200000L), Unit.MINERSAMULET_ARTIFACT.getUnit().name));
            if (CivGlobal.isOneUseArtifact(event.getItem())) {
                this.removeChildren(player.getInventory());
                CivMessage.sendError(interacter, CivSettings.localize.localizedString("var_artifact_useSuccusessButNot"));
            }
        } else {
            CivMessage.sendError(interacter, CivSettings.localize.localizedString("var_artifact_useFailure", sdf.format(nextUse), Unit.MINERSAMULET_ARTIFACT.getUnit().name));
        }
        event.setCancelled(true);
    }
}

