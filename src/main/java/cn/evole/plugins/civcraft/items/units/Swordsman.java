package cn.evole.plugins.civcraft.items.units;

import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.config.ConfigUnit;
import cn.evole.plugins.civcraft.exception.CivException;
import cn.evole.plugins.civcraft.lorestorage.LoreMaterial;
import cn.evole.plugins.civcraft.main.CivCraft;
import cn.evole.plugins.civcraft.main.CivMessage;
import cn.evole.plugins.civcraft.object.Town;
import cn.evole.plugins.civcraft.util.CivColor;
import cn.evole.plugins.civcraft.war.War;
import gpl.AttributeUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class Swordsman extends UnitMaterial {
    public Swordsman(String id, ConfigUnit configUnit) {
        super(id, configUnit);
    }

    public static void spawn(Inventory inv, Town town) throws CivException {
        ItemStack is = LoreMaterial.spawn(Unit.SWORDSMAN_ARTIFACT);
        setOwningTown(town, is);
        AttributeUtil attrs = new AttributeUtil(is);
        attrs.addEnhancement("LoreEnhancementSoulBound", null, null);
        attrs.addLore(CivColor.Gold + CivSettings.localize.localizedString("itemLore_Souldbound"));
        ConfigUnit u = CivSettings.units.get(Unit.SWORDSMAN_ARTIFACT.getUnit().id);
        for (String d : u.description) {
            attrs.addLore(CivColor.colorize(d));
        }
        is = attrs.getStack();
        if (!Unit.addItemNoStack(inv, is)) {
            throw new CivException(CivSettings.localize.localizedString("var_arrtifacts_errorBarracksFull", Unit.SWORDSMAN_ARTIFACT.getUnit().name));
        }
    }

    @Override
    public void onPlayerDeath(EntityDeathEvent event, ItemStack stack) {
        Player player = (Player) event.getEntity();
        Random random = CivCraft.civRandom;
        int destroyChance = random.nextInt(100);
        if (War.isWarTime()) {
            CivMessage.send((Object) player, CivColor.YellowBold + CivSettings.localize.localizedString("var_arrtifacts_Keept_Since_WarTime_No_Rolled"));
        } else {
            CivMessage.send((Object) player, CivColor.LightBlueBold + CivSettings.localize.localizedString("var_arrtifacts_Keept_Since_No_WarTime"));
        }
        if (5 < destroyChance) {
            this.removeChildren(player.getInventory());
            CivMessage.send((Object) player, CivColor.RoseBold + CivSettings.localize.localizedString("var_arrtifacts_Break", destroyChance));
        }
    }
}

