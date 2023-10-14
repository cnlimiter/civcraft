/**
 * [2023] Cnlimiter LLC
 * All Rights Reserved.
 * Name: civcraft
 * Author: cnlimiter
 * UpdateTime: 2023/10/14 16:15
 * Description:
 * License: ARR
 */
package cn.evole.plugins.civcraft.items.components;

import cn.evole.plugins.civcraft.lorestorage.ItemChangeResult;
import gpl.AttributeUtil;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

public class DurabilityOnDeath extends ItemComponent {

    @Override
    public void onPrepareCreate(AttributeUtil attrs) {
//		attrs.addLore(CivColor.Blue+""+this.getDouble("value")+" Durability");
    }

    @Override
    public ItemChangeResult onDurabilityDeath(PlayerDeathEvent event, ItemChangeResult result, ItemStack sourceStack) {
        if (result == null) {
            result = new ItemChangeResult();
            result.stack = sourceStack;
            result.destroyItem = false;
        }

        if (result.destroyItem) {
            return result;
        }

        double percent = this.getDouble("value");

        int reduction = (int) (result.stack.getType().getMaxDurability() * percent);
        int durabilityLeft = result.stack.getType().getMaxDurability() - result.stack.getDurability();

        if (durabilityLeft > reduction) {
            result.stack.setDurability((short) (result.stack.getDurability() + reduction));
        } else {
            result.destroyItem = true;
        }

        return result;
    }

}
