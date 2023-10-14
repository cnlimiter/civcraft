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

import cn.evole.plugins.civcraft.loregui.GuiAction;
import cn.evole.plugins.civcraft.util.ItemManager;
import gpl.AttributeUtil;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Constructor;

public class LoreGuiItem {

    public static final int MAX_INV_SIZE = 54;
    public static final int INV_ROW_COUNT = 9;

    public static ItemStack getGUIItem(String title, String[] messages, int type, int data) {
        ItemStack stack = ItemManager.createItemStack(type, 1, (short) data);
        AttributeUtil attrs = new AttributeUtil(stack);
        attrs.setCivCraftProperty("GUI", title);
        attrs.setName(title);
        attrs.setLore(messages);
        return attrs.getStack();
    }

    public static boolean isGUIItem(ItemStack stack) {
        AttributeUtil attrs = new AttributeUtil(stack);
        String title = attrs.getCivCraftProperty("GUI");
        if (title != null) {
            return true;
        }
        return false;
    }

    public static ItemStack setAction(ItemStack stack, String action) {
        AttributeUtil attrs = new AttributeUtil(stack);
        attrs.setCivCraftProperty("GUI_ACTION", action);
        return attrs.getStack();
    }

    public static String getAction(ItemStack stack) {
        AttributeUtil attrs = new AttributeUtil(stack);
        return attrs.getCivCraftProperty("GUI_ACTION");
    }

    public static ItemStack setActionData(ItemStack stack, String key, String value) {
        AttributeUtil attrs = new AttributeUtil(stack);
        attrs.setCivCraftProperty("GUI_ACTION_DATA:" + key, value);
        return attrs.getStack();
    }

    public static String getActionData(ItemStack stack, String key) {
        AttributeUtil attrs = new AttributeUtil(stack);
        return attrs.getCivCraftProperty("GUI_ACTION_DATA:" + key);
    }

    public static ItemStack build(String title, int type, int data, String... messages) {
        return getGUIItem(title, messages, type, data);
    }

    public static ItemStack asGuiItem(ItemStack stack) {
        AttributeUtil attrs = new AttributeUtil(stack);
        attrs.setCivCraftProperty("GUI", "" + ItemManager.getId(stack));
        return attrs.getStack();
    }

    public static void processAction(String action, ItemStack stack, InventoryClickEvent event) {

        /* Get class name from reflection and perform assigned action */
        try {
            Class<?> clazz = Class.forName("cn.evole.plugins.civcraft.loregui." + action);
            Constructor<?> constructor = clazz.getConstructor();
            GuiAction instance = (GuiAction) constructor.newInstance();
            instance.performAction(event, stack);
        } catch (Exception e) {
            try {
                Class<?> clazz = Class.forName("cn.evole.plugins.civcraft.loregui.book." + action);
                Constructor<?> constructor = clazz.getConstructor();
                GuiAction instance = (GuiAction) constructor.newInstance();
                instance.performAction(event, stack);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }

    }


}
