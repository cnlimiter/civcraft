/**
 * [2023] Cnlimiter LLC
 * All Rights Reserved.
 * Name: civcraft
 * Author: cnlimiter
 * UpdateTime: 2023/10/14 16:15
 * Description:
 * License: ARR
 */
package cn.evole.plugins.civcraft.items.units;

import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.config.ConfigUnit;
import cn.evole.plugins.civcraft.exception.CivException;
import cn.evole.plugins.civcraft.lorestorage.LoreMaterial;
import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.main.CivMessage;
import cn.evole.plugins.civcraft.object.Resident;
import cn.evole.plugins.civcraft.object.Town;
import cn.evole.plugins.civcraft.threading.TaskMaster;
import cn.evole.plugins.civcraft.threading.tasks.DelayMoveInventoryItem;
import cn.evole.plugins.civcraft.util.CivColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
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
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashSet;
import java.util.List;

public class UnitMaterial extends LoreMaterial {

    private static final int LAST_SLOT = 8;
    public HashSet<Integer> allowedSubslots = new HashSet<Integer>();
    private ConfigUnit unit = null;

    public UnitMaterial(String id, int itemId, short damage) {
        super(id, itemId, damage);
    }

    public UnitMaterial(String id, ConfigUnit configUnit) {
        //	Material mat = Material.getMaterial(configUnit.item_id);
        super(id, configUnit.item_id, (short) 0);
        setUnit(configUnit);

        this.setLore("Unit Item");
        this.setName(configUnit.name);
    }

    private static List<String> stripTownLore(List<String> lore) {
        for (String str : lore) {
            if (str.startsWith("Town:")) {
                lore.remove(str);
                break;
            }
        }
        return lore;
    }

    public static void setOwningTown(Town town, ItemStack stack) {
        if (town == null) {
            return;
        }

        ItemMeta meta = stack.getItemMeta();
        if (meta != null && meta.hasLore()) {
            List<String> lore = meta.getLore();
            //看起来像是更变所有权的亚子
            lore = stripTownLore(lore);

            if (lore != null) {
                lore.add("Town:" + town.getName() + " " + CivColor.Black + town.getId());
            }

            meta.setLore(lore);
            stack.setItemMeta(meta);
        }
    }

    public static Town getOwningTown(ItemStack stack) {
        ItemMeta meta = stack.getItemMeta();
        if (meta == null || !meta.hasLore()) {
            return null;
        }

        String loreLine = null;
        List<String> lore = meta.getLore();
        for (String str : lore) {
            if (str.startsWith("Town:")) {
                loreLine = str;
                break;
            }
        }

        if (loreLine == null) {
            return null;
        }

        try {
            String[] split = loreLine.split(CivColor.Black);
            int townId = Integer.parseInt(split[1]);

            return CivGlobal.getTownFromId(townId);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    public static boolean validateUnitUse(Player player, ItemStack stack) {
        if (stack == null) {
            return true;
        }

        Resident resident = CivGlobal.getResident(player);
        Town town = getOwningTown(stack);


        if (town == null) {
            return true;
        }

        if (town.getCiv() != resident.getCiv()) {
            return false;
        }

        return true;
    }

    public ConfigUnit getUnit() {
        return unit;
    }

    public void setUnit(ConfigUnit unit) {
        this.unit = unit;
    }

    @Override
    public void onBlockBreak(BlockBreakEvent event) {
    }

    @Override
    public void onBlockDamage(BlockDamageEvent event) {
    }

    @Override
    public void onBlockInteract(PlayerInteractEvent event) {

    }

    @Override
    public void onBlockPlaced(BlockPlaceEvent event) {
        event.setCancelled(true);
        CivMessage.sendError(event.getPlayer(), CivSettings.localize.localizedString("unitMaterial_cannotPlace"));
        event.getPlayer().updateInventory();
    }

    @Override
    public void onHit(EntityDamageByEntityEvent event) {
    }

    @Override
    public void onHold(PlayerItemHeldEvent event) {
    }

    @Override
    public void onInteract(PlayerInteractEvent event) {
        event.setUseItemInHand(Result.DENY);
        //if (event.getClickedBlock() == null) {
        event.setCancelled(true);
        CivMessage.sendError(event.getPlayer(), CivSettings.localize.localizedString("unitMaterial_cannotUse"));
        //}
    }

    @Override
    public void onInteractEntity(PlayerInteractEntityEvent event) {
    }

    @Override
    public void onItemDrop(PlayerDropItemEvent event) {
        this.onItemFromPlayer(event.getPlayer(), event.getItemDrop().getItemStack());
    }

    protected void removeChildren(Inventory inv) {
        for (ItemStack stack : inv.getContents()) {
            if (stack != null) {
                //	CustomItemStack is = new CustomItemStack(stack);
                LoreMaterial material = LoreMaterial.getMaterial(stack);
                if (material != null && (material instanceof UnitItemMaterial)) {
                    UnitItemMaterial umat = (UnitItemMaterial) material;
                    if (umat.getParent() == this) {
                        inv.remove(stack);
                    }
                }
            }
        }
    }

    @Override
    public void onItemCraft(CraftItemEvent event) {
        try {
            CivMessage.sendError(CivGlobal.getPlayer(event.getWhoClicked().getName()), CivSettings.localize.localizedString("unitItem_cannotCraft"));
        } catch (CivException e) {
            //player offline?
        }
        event.setCancelled(true);
    }

    @Override
    public void onItemPickup(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (!validateUnitUse(player, event.getItem().getItemStack())) {
                CivMessage.sendErrorNoRepeat(player, CivSettings.localize.localizedString("unitMaterial_errorWrongCiv"));
                event.setCancelled(true);
                return;
            }

            ConfigUnit unit = Unit.getPlayerUnit(player);
            if (unit != null) {
                CivMessage.sendErrorNoRepeat(player, CivSettings.localize.localizedString("var_unitMaterial_errorHave1", unit.name));
                event.setCancelled(true);
            } else {
                // Reposition item to the last quickbar slot

                // Check that the inventory is not full, clear out the
                // the required slot, and then re-add what was in there.
                Inventory inv = player.getInventory();

                ItemStack lastSlot = inv.getItem(LAST_SLOT);
                if (lastSlot != null) {
                    inv.setItem(LAST_SLOT, event.getItem().getItemStack());
                    inv.addItem(lastSlot);
                    player.updateInventory();
                } else {
                    inv.setItem(LAST_SLOT, event.getItem().getItemStack());
                }


                this.onItemToPlayer(player, event.getItem().getItemStack());
                event.getItem().remove();
                event.setCancelled(true);
            }
        } else {
            event.setCancelled(true);
        }
    }

    public int getFreeSlotCount(Inventory inv) {
        int count = 0;
        for (ItemStack stack : inv.getContents()) {
            if (stack == null) {
                count++;
            }
        }
        return count;
    }

    public boolean hasFreeSlot(Inventory inv) {
        for (ItemStack stack : inv.getContents()) {
            if (stack == null) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onInvItemPickup(InventoryClickEvent event,
                                Inventory fromInv, ItemStack stack) {

        if (fromInv.getHolder() instanceof Player) {
            Player player = (Player) fromInv.getHolder();
            onItemFromPlayer(player, stack);
        }
    }

    @Override
    public void onInvItemDrop(InventoryClickEvent event, Inventory toInv, ItemStack stack) {

        if (toInv.getHolder() instanceof Player) {
            //A hack to make sure we are always moving the item to the player's inv.
            //A player inv is always on the bottom, toInv could be the 'crafting' inv
            toInv = event.getView().getBottomInventory();
            Player player = (Player) toInv.getHolder();

            if (!validateUnitUse(player, stack)) {
                CivMessage.sendError(player, CivSettings.localize.localizedString("unitMaterial_errorWrongCiv"));
                event.setCancelled(true);
                return;
            }

            //Prevent dropping in two unit materials.
            ConfigUnit unit = Unit.getPlayerUnit(player);
            if (unit != null) {
                //player already has a unit item, cancel this event.
                CivMessage.sendError(player, CivSettings.localize.localizedString("var_unitMaterial_errorHave1", unit.name));
                event.setCancelled(true);
                event.setResult(Result.DENY);
                event.getView().close();
                player.updateInventory();
                return;
            }

            // Reposition item to the last quickbar slot
            if (event.getSlot() != LAST_SLOT) {

                DelayMoveInventoryItem task = new DelayMoveInventoryItem();
                task.fromSlot = event.getSlot();
                task.toSlot = LAST_SLOT;
                task.inv = toInv;
                task.playerName = player.getName();
                TaskMaster.syncTask(task);
            }

            onItemToPlayer(player, stack);
        }

    }

    @Override
    public void onInvShiftClick(InventoryClickEvent event,
                                Inventory fromInv, Inventory toInv,
                                ItemStack stack) {

        if (fromInv.equals(toInv)) {
            event.setCancelled(true);
            event.setResult(Result.DENY);
            return;
        }

        if (toInv.getHolder() instanceof Player) {
            Player player = (Player) toInv.getHolder();

            if (!validateUnitUse(player, stack)) {
                CivMessage.sendError(player, CivSettings.localize.localizedString("unitMaterial_errorWrongCiv"));
                event.setCancelled(true);
                return;
            }

            //Prevent dropping in two unit materials.
            ConfigUnit unit = Unit.getPlayerUnit(player);
            if (unit != null) {
                //player already has a unit item, cancel this event.
                CivMessage.sendError(player, CivSettings.localize.localizedString("var_unitMaterial_errorHave1", unit.name));
                event.setCancelled(true);
                event.setResult(Result.DENY);
                event.getView().close();
                player.updateInventory();
                return;
            }


            onItemToPlayer(player, stack);
        } else if (fromInv.getHolder() instanceof Player) {
            onItemFromPlayer((Player) fromInv.getHolder(), stack);
        }

    }

    @SuppressWarnings("deprecation")
    @Override
    public void onInvItemSwap(InventoryClickEvent event, Inventory toInv,
                              ItemStack droppedStack, ItemStack pickedStack) {

        // Prevent stacking items
        if (droppedStack.getTypeId() == pickedStack.getTypeId()) {
            event.setCancelled(true);
            event.setResult(Result.DENY);
            Player player = getPlayer(event);
            player.updateInventory();

        }

        if (toInv.getHolder() instanceof Player) {
            //CustomItemStack is = new CustomItemStack(droppedStack);
            LoreMaterial material = LoreMaterial.getMaterial(droppedStack);

            if (material != null && (material instanceof UnitMaterial)) {
                Player player = (Player) toInv.getHolder();

                if (!validateUnitUse(player, droppedStack)) {
                    CivMessage.sendError(player, CivSettings.localize.localizedString("unitMaterial_errorWrongCiv"));
                    event.setCancelled(true);
                    return;
                }

                DelayMoveInventoryItem task = new DelayMoveInventoryItem();
                task.fromSlot = event.getSlot();
                task.toSlot = LAST_SLOT;
                task.inv = toInv;
                task.playerName = player.getName();
                TaskMaster.syncTask(task);

                onItemToPlayer(player, droppedStack);
                onItemFromPlayer(player, pickedStack);
            }
        }
    }

    /*
     * Called when a unit material is added to a player.
     */
    public void onItemToPlayer(Player player, ItemStack stack) {
    }

    /*
     * Called when a unit material is removed from a player.
     */
    public void onItemFromPlayer(Player player, ItemStack stack) {

    }

    @Override
    public void onItemSpawn(ItemSpawnEvent event) {

    }

    @Override
    public boolean onAttack(EntityDamageByEntityEvent event, ItemStack stack) {
        return false;
    }

    @Override
    public void onPlayerDeath(EntityDeathEvent event, ItemStack stack) {
    }

    @Override
    public void onDrop(PlayerDropItemEvent event) {

    }

    @Override
    public void onInventoryClose(InventoryCloseEvent event) {
        ItemStack foundStack = null;
        for (ItemStack stack : event.getPlayer().getInventory().getContents()) {
            if (stack == null) {
                continue;
            }

            if (LoreMaterial.isCustom(stack)) {
                if (LoreMaterial.getMaterial(stack) instanceof UnitMaterial) {
                    if (foundStack == null) {
                        foundStack = stack;
                    } else {
                        event.getPlayer().getWorld().dropItem(event.getPlayer().getLocation(), stack);
                        event.getPlayer().getInventory().remove(stack);
                    }
                }
            }

        }
    }

}

