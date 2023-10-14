/**
 * [2023] Cnlimiter LLC
 * All Rights Reserved.
 * Name: civcraft
 * Author: cnlimiter
 * UpdateTime: 2023/10/14 16:15
 * Description:
 * License: ARR
 */
package cn.evole.plugins.civcraft.util;

import cn.evole.plugins.civcraft.exception.CivException;
import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.main.CivLog;
import cn.evole.plugins.civcraft.object.Town;
import cn.evole.plugins.civcraft.structure.Buildable;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.UUID;

public class ItemFrameStorage {

    // We cannot store the actual frame because it unloads and the resource changes?
    //private ItemFrame frame = null;

    public static HashMap<BlockCoord, ItemFrameStorage> attachedBlockMap = new HashMap<BlockCoord, ItemFrameStorage>();
    // UUID will stay the same, store that instead.
    private UUID frameID;
    // We might need the location of the frame to load it, grab that as well
    private Location location;
    // Might belong to a town and be subject to permissions.
    private Buildable buildable = null;
    private BlockCoord attachedBlock;

    public ItemFrameStorage(ItemFrame frame, Location attachedLoc) throws CivException {
        if (frame != null) {
            this.frameID = frame.getUniqueId();
            this.location = frame.getLocation();
            this.attachedBlock = new BlockCoord(attachedLoc);
            CivGlobal.addProtectedItemFrame(this);
        } else {
            throw new CivException("Passed a null item frame to storage constructor.");
        }

//		this.frame = frame;
//		if (this.frame != null) {
//			CivGlobal.addProtectedItemFrame(this);
//		} else {
//			CivLog.error("Passed a null item frame!!!");
//			throw new CivException("Passed a null item frame.");
//		}
    }

    public ItemFrameStorage(Location location, BlockFace blockface) {
        CivLog.debug("world: " + location.getWorld().toString());
        CivLog.debug("Entity: " + EntityType.ITEM_FRAME.toString());
        CivLog.debug("location: " + location.toString());
        CivLog.debug("Blockface: " + blockface.toString());
        ItemFrame frame = (ItemFrame) location.getWorld().spawnEntity(location, EntityType.ITEM_FRAME);
        CivLog.debug("ID: " + frame.getUniqueId());
        //frame.setItem(new ItemStack(Material.BAKED_POTATO));

        this.frameID = frame.getUniqueId();
        this.location = frame.getLocation();
        this.attachedBlock = new BlockCoord(location);
        CivGlobal.addProtectedItemFrame(this);

    }

    public ItemFrame getItemFrame() {
        // Gets the item frame by loading in the chunk where it is supposed to reside.
        // Then searching for it's UUID.

        if (!this.location.getChunk().isLoaded()) {
            if (!this.location.getChunk().load()) {
                CivLog.error("Could not load chunk to get item frame at:" + this.location);
                return null;
            }
        }

        Entity ent = CivGlobal.getEntityClassFromUUID(this.location.getWorld(), ItemFrame.class, this.frameID);
        if (ent == null) {
            CivLog.error("Could not find frame from frame ID:" + this.frameID.toString());
            return null;
        }

        if (!(ent instanceof ItemFrame)) {
            CivLog.error("Could not get a frame with ID:" + this.frameID + " ... it was not a frame.");
            return null;
        }

        //TODO try and use a cache and isValid()?

        return (ItemFrame) ent;
    }


    public UUID getUUID() {
        return this.getFrameID();
    }

    public void setFacingDirection(BlockFace blockface) {
        //	ItemFrame frame = getItemFrame();
        //	frame.setFacingDirection(blockface);
    }

    public void clearItem() {
        setItem(ItemManager.createItemStack(0, 1));
    }

    public ItemStack getItem() {
        ItemFrame frame = getItemFrame();
        return frame.getItem();
    }

    public void setItem(ItemStack stack) {

        ItemFrame frame = getItemFrame();
        if (frame != null) {
            ItemStack newStack = new ItemStack(stack.getType(), 1, stack.getDurability());
            newStack.setData(stack.getData());
            newStack.setItemMeta(stack.getItemMeta());
            frame.setItem(newStack);
        } else {
            CivLog.warning("Frame:" + this.frameID + " was null when trying to set to " + stack.getType().name());
        }

    }

    public boolean isEmpty() throws CivException {
        ItemFrame frame = getItemFrame();

        if (frame == null) {
            throw new CivException("Bad frame. Could not be found.");
        }

        if (frame.getItem() == null || frame.getItem().getType().equals(Material.AIR)) {
            return true;
        }

        return false;
    }

    public Location getLocation() {
        return this.location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public boolean isOurEntity(Entity entity) {
        return (((ItemFrame) entity).getUniqueId().equals(getUUID()));
    }

    public boolean noFrame() {
        ItemFrame frame = getItemFrame();
        return (frame == null);
    }

    public Object getCoord() {
        return new BlockCoord(this.getLocation());
    }

    public UUID getFrameID() {
        return frameID;
    }

    public void setFrameID(UUID frameID) {
        this.frameID = frameID;
    }

    public Town getTown() {
        if (buildable != null) {
            return buildable.getTown();
        }
        return null;
    }

    public void setBuildable(Buildable buildable) {
        this.buildable = buildable;
    }

    public BlockCoord getAttachedBlock() {
        return attachedBlock;
    }

    public void setAttachedBlock(BlockCoord attachedBlock) {
        this.attachedBlock = attachedBlock;
    }

}
