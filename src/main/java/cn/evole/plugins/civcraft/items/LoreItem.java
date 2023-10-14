/**
 * [2023] Cnlimiter LLC
 * All Rights Reserved.
 * Name: civcraft
 * Author: cnlimiter
 * UpdateTime: 2023/10/14 16:15
 * Description:
 * License: ARR
 */
package cn.evole.plugins.civcraft.items;

import cn.evole.plugins.civcraft.object.SQLObject;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public abstract class LoreItem extends SQLObject {
    private Type type;


    public LoreItem() {
    }

    public abstract void load();

    public void setLore(ItemStack stack, List<String> lore) {
        ItemMeta meta = stack.getItemMeta();
        meta.setLore(lore);
        stack.setItemMeta(meta);
    }

    public List<String> getLore(ItemStack stack) {
        return stack.getItemMeta().getLore();
    }

    public void setDisplayName(ItemStack stack, String name) {
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(name);
        stack.setItemMeta(meta);
    }

    public String getDisplayName(ItemStack stack) {
        return stack.getItemMeta().getDisplayName();
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    /*
     * A lore item represents a custom item inside of civcraft which overloads the lore data.
     */
    public enum Type {
        NORMAL,
        BONUSGOODIE,
    }

}
