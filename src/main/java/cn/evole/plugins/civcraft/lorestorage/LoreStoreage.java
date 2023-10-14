/**
 * [2023] Cnlimiter LLC
 * All Rights Reserved.
 * Name: civcraft
 * Author: cnlimiter
 * UpdateTime: 2023/10/14 16:15
 * Description:
 * License: ARR
 */
package cn.evole.plugins.civcraft.lorestorage;

import cn.evole.plugins.civcraft.util.CivColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoreStoreage {

    public static void setMatID(int id, ItemStack stack) {
        ItemMeta meta = stack.getItemMeta();

        List<String> lore;
        if (meta.hasLore()) {
            lore = meta.getLore();
        } else {
            lore = new ArrayList<String>();
        }

        lore.set(0, CivColor.Black + "MID:" + id);
        meta.setLore(lore);
        stack.setItemMeta(meta);
    }

//	public static void addLore(String)

    public static void setItemName(String name, ItemStack stack) {
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(name);
        stack.setItemMeta(meta);
    }

    public static void saveLoreMap(String type, Map<String, String> map, ItemStack stack) {

        ItemMeta meta = stack.getItemMeta();
        ArrayList<String> lore = new ArrayList<String>();

        lore.add(CivColor.Blue + type);

        for (String key : map.keySet()) {
            String value = map.get(key);
            lore.add(CivColor.Gray + key + ":" + value);
        }

        meta.setLore(lore);
        stack.setItemMeta(meta);
    }

    public static String getType(ItemStack stack) {

        ItemMeta meta = stack.getItemMeta();

        if (meta.hasLore()) {
            List<String> lore = meta.getLore();
            return lore.get(0);
        } else {
            return "none";
        }
    }

    public static Map<String, String> getLoreMap(ItemStack stack) {
        HashMap<String, String> map = new HashMap<String, String>();

        ItemMeta meta = stack.getItemMeta();
        if (meta.hasLore()) {
            List<String> lore = meta.getLore();
            for (String str : lore) {
                String[] split = str.split(":");
                if (split.length > 2) {
                    map.put(split[0], split[1]);
                }
            }
        }
        return map;
    }


}
