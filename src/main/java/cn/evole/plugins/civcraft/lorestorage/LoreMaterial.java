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

import cn.evole.plugins.civcraft.loreenhancements.LoreEnhancement;
import cn.evole.plugins.civcraft.object.BuildableDamageBlock;
import cn.evole.plugins.civcraft.util.CivColor;
import cn.evole.plugins.civcraft.util.ItemManager;
import gpl.AttributeUtil;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public abstract class LoreMaterial {

    public static final String MID_TAG = CivColor.Black + "MID";
    public static Map<String, LoreMaterial> materialMap = new HashMap<String, LoreMaterial>();
    private String id;
    private int typeID;
    private short damage;
    private final LinkedList<String> lore = new LinkedList<String>();
    private String name;

    public LoreMaterial(String id, int typeID, short damage) {
        this.id = id;
        this.typeID = typeID;
        this.damage = damage;
        /* Adding quotes around id since NBTString does it =\ */
        materialMap.put(id, this);
    }

    /* Sets MID in NBT Data, use for production. */
    public static String getMID(ItemStack stack) {
        AttributeUtil attrs = new AttributeUtil(stack);

        String mid = attrs.getCivCraftProperty("mid");
        if (mid == null) {
            return "";
        }

        return mid;
    }

    public static void setMIDAndName(AttributeUtil attrs, String mid, String name) {
        attrs.setCivCraftProperty("mid", mid);
        attrs.setName(name);
    }

    /* Sets MID in LORE, use for debugging. */
//	public static String getMID(ItemStack stack) {
//		AttributeUtil attrs = new AttributeUtil(stack);	
//		String[] lore = attrs.getLore();
//		
//		if (lore == null) {
//			return "";
//		}
//		
//		String[] split = lore[0].split(":");
//		if (!split[0].equals(MID_TAG)) {
//			return "";
//		}
//		
//		return split[1];
//	}
//	
//	public static void setMIDAndName(AttributeUtil attrs, String mid, String name) {
//		attrs.setLore(MID_TAG+":"+mid);
//		attrs.setName(name);
//	}

    public static boolean isCustom(ItemStack stack) {
        if (stack == null) {
            return false;
        }

        LoreMaterial material = getMaterial(stack);
        if (material == null) {
            return false;
        }
        return true;
    }

    public static LoreMaterial getMaterial(ItemStack stack) {
        if (stack == null) {
            return null;
        }
        return materialMap.get(getMID(stack));
    }

    public static ItemStack spawn(LoreMaterial material) {
        return spawn(material, 1);
    }

    public static ItemStack spawn(LoreMaterial material, int quantity) {
        ItemStack stack = ItemManager.createItemStack(material.getTypeID(), quantity, material.getDamage());
        AttributeUtil attrs = new AttributeUtil(stack);
        setMIDAndName(attrs, material.getId(), material.getName());
        Boolean isShiny = false;
        if (material instanceof LoreCraftableMaterial) {
            LoreCraftableMaterial craftMat = (LoreCraftableMaterial) material;
            attrs.addLore(CivColor.ITALIC + craftMat.getConfigMaterial().category);
            if (craftMat.getConfigMaterial().tradeable) {
                attrs.setCivCraftProperty("tradeable", "true");
            }
            if (craftMat.getConfigMaterial().tradeValue >= 0) {
                attrs.setCivCraftProperty("tradeValue", "" + craftMat.getConfigMaterial().tradeValue);
            }
            isShiny = craftMat.getConfigMaterial().shiny;
        }

        material.applyAttributes(attrs);
        ItemStack newStack = attrs.getStack();

        if (isShiny) {
            addGlow(newStack);
        }

        return newStack;
    }

    public static void addGlow(ItemStack stack) {
        ItemMeta meta = stack.getItemMeta();
        meta.addEnchant(Enchantment.LURE, 1, false);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        stack.setItemMeta(meta);
    }

    public static ItemStack addEnhancement(ItemStack stack, LoreEnhancement enhancement) {
        AttributeUtil attrs = new AttributeUtil(stack);
        attrs = enhancement.add(attrs);
        return attrs.getStack();
    }

    public static boolean hasEnhancement(ItemStack stack, String enhName) {
        AttributeUtil attrs = new AttributeUtil(stack);
        return attrs.hasEnhancement(enhName);
    }

    public static boolean hasEnhancements(ItemStack stack) {
        AttributeUtil attrs = new AttributeUtil(stack);
        return attrs.hasEnhancements();
    }

    public static LinkedList<LoreEnhancement> getEnhancements(ItemStack stack) {
        AttributeUtil attrs = new AttributeUtil(stack);
        return attrs.getEnhancements();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /*
     * Moves an item stack off of this slot by trying
     * to re-add it to the inventory, if it fails, then
     * we drop it on the ground.
     */
    public void moveDropSet(Player player, Inventory inv, int slot, ItemStack newItem) {

        ItemStack stack = inv.getItem(slot);
        inv.setItem(slot, newItem);

        if (stack != null) {
            if (stack.equals(newItem)) {
                return;
            }

            HashMap<Integer, ItemStack> leftovers = inv.addItem(stack);

            for (ItemStack s : leftovers.values()) {
                player.getWorld().dropItem(player.getLocation(), s);
            }
        }

    }

    public Player getPlayer(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            return (Player) event.getWhoClicked();
        }
        return null;
    }

    public int getTypeID() {
        return typeID;
    }

    public void setTypeID(int typeID) {
        this.typeID = typeID;
    }

    public short getDamage() {
        return damage;
    }

    public void setDamage(short damage) {
        this.damage = damage;
    }

    public void addLore(String lore) {
        this.lore.add(lore);
    }

    public LinkedList<String> getLore() {
        return this.lore;
    }

    public void setLore(String lore) {
        this.lore.clear();
        this.lore.add(lore);
    }

    public void setLore(String[] lore) {
        this.lore.clear();
        Collections.addAll(this.lore, lore);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void applyAttributes(AttributeUtil attrs) {
        /*
         * This is called when the item is created via the LoreMaterial.spawn() command.
         * Can optionally be overriden by classes.
         */
        return;
    }

    /* Events for this Material */
    public abstract void onHit(EntityDamageByEntityEvent event); /* Called when this is the item in-hand */

    public abstract void onInteract(PlayerInteractEvent event);

    public abstract void onInteractEntity(PlayerInteractEntityEvent event);

    public abstract void onBlockPlaced(BlockPlaceEvent event);

    public abstract void onBlockBreak(BlockBreakEvent event);

    public abstract void onBlockDamage(BlockDamageEvent event);

    public abstract void onBlockInteract(PlayerInteractEvent event);

    public abstract void onHold(PlayerItemHeldEvent event);

    public abstract void onDrop(PlayerDropItemEvent event);

    public abstract void onItemDrop(PlayerDropItemEvent event);

    public abstract void onItemCraft(CraftItemEvent event);

    public abstract void onItemSpawn(ItemSpawnEvent event);

    public abstract boolean onAttack(EntityDamageByEntityEvent event, ItemStack stack); /* Called when this item is in inventory. */

    public abstract void onItemPickup(EntityPickupItemEvent event);

    public abstract void onInvItemPickup(InventoryClickEvent event, Inventory fromInv, ItemStack stack);

    public abstract void onInvItemDrop(InventoryClickEvent event, Inventory toInv, ItemStack stack);

    public abstract void onInvShiftClick(InventoryClickEvent event, Inventory fromInv, Inventory toInv, ItemStack stack);

    public abstract void onInvItemSwap(InventoryClickEvent event, Inventory toInv, ItemStack droppedStack, ItemStack pickedStack);

    public abstract void onPlayerDeath(EntityDeathEvent event, ItemStack stack);

    public abstract void onInventoryClose(InventoryCloseEvent event);

    public void onDefense(EntityDamageByEntityEvent event, ItemStack stack) {
    }

    public int onStructureBlockBreak(BuildableDamageBlock dmgBlock, int damage) {
        return damage;
    }

    public void onInventoryOpen(InventoryOpenEvent event, ItemStack stack) {
    }

    public void onBulletAttack(EntityDamageByEntityEvent event, ItemStack inHand) {
    }
}