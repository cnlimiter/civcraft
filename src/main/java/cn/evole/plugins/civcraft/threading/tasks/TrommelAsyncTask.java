/**
 * [2023] Cnlimiter LLC
 * All Rights Reserved.
 * Name: civcraft
 * Author: cnlimiter
 * UpdateTime: 2023/10/14 16:15
 * Description:
 * License: ARR
 */
package cn.evole.plugins.civcraft.threading.tasks;

import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.exception.CivTaskAbortException;
import cn.evole.plugins.civcraft.lorestorage.LoreMaterial;
import cn.evole.plugins.civcraft.main.CivData;
import cn.evole.plugins.civcraft.main.CivLog;
import cn.evole.plugins.civcraft.object.StructureChest;
import cn.evole.plugins.civcraft.structure.Structure;
import cn.evole.plugins.civcraft.structure.Trommel;
import cn.evole.plugins.civcraft.threading.CivAsyncTask;
import cn.evole.plugins.civcraft.threading.sync.request.UpdateInventoryRequest;
import cn.evole.plugins.civcraft.util.ItemManager;
import cn.evole.plugins.civcraft.util.MultiInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

import static cn.evole.plugins.civcraft.structure.Trommel.*;

public class TrommelAsyncTask extends CivAsyncTask {

    private static final int GRAVEL_RATE = 1; //0.10%
    public static HashSet<String> debugTowns = new HashSet<String>();
    private Trommel trommel;

    public TrommelAsyncTask(Structure trommel) {
        this.trommel = (Trommel) trommel;
    }

    public static void debug(Trommel trommel, String msg) {
        if (debugTowns.contains(trommel.getTown().getName())) {
            CivLog.warning("TrommelDebug:" + trommel.getTown().getName() + ":" + msg);
        }
    }

    private void processTrommelUpdate() {
        if (!trommel.isActive()) {
            debug(trommel, "trommel inactive...");
            return;
        }

        debug(trommel, "Processing trommel...");
        // Grab each CivChest object we'll require.
        ArrayList<StructureChest> sources = trommel.getAllChestsById(1);
        ArrayList<StructureChest> destinations = trommel.getAllChestsById(2);

        if (sources.size() != 2 || destinations.size() != 2) {
            CivLog.error("Bad chests for trommel in town:" + trommel.getTown().getName() + " sources:" + sources.size() + " dests:" + destinations.size());
            return;
        }

        // Make sure the chunk is loaded before continuing. Also, add get chest and add it to inventory.
        MultiInventory source_inv = new MultiInventory();
        MultiInventory dest_inv = new MultiInventory();

        try {
            for (StructureChest src : sources) {
                //this.syncLoadChunk(src.getCoord().getWorldname(), src.getCoord().getX(), src.getCoord().getZ());
                Inventory tmp;
                try {
                    tmp = this.getChestInventory(src.getCoord().getWorldname(), src.getCoord().getX(), src.getCoord().getY(), src.getCoord().getZ(), false);
                } catch (CivTaskAbortException e) {
                    //e.printStackTrace();
                    CivLog.warning("Trommel:" + e.getMessage());
                    return;
                }
                if (tmp == null) {
                    trommel.skippedCounter++;
                    return;
                }
                source_inv.addInventory(tmp);
            }

            boolean full = true;
            for (StructureChest dst : destinations) {
                //this.syncLoadChunk(dst.getCoord().getWorldname(), dst.getCoord().getX(), dst.getCoord().getZ());
                Inventory tmp;
                try {
                    tmp = this.getChestInventory(dst.getCoord().getWorldname(), dst.getCoord().getX(), dst.getCoord().getY(), dst.getCoord().getZ(), false);
                } catch (CivTaskAbortException e) {
                    //e.printStackTrace();
                    CivLog.warning("Trommel:" + e.getMessage());
                    return;
                }
                if (tmp == null) {
                    trommel.skippedCounter++;
                    return;
                }
                dest_inv.addInventory(tmp);

                for (ItemStack stack : tmp.getContents()) {
                    if (stack == null) {
                        full = false;
                        break;
                    }
                }
            }

            if (full) {
                /* Trommel destination chest is full, stop processing. */
                return;
            }

        } catch (InterruptedException e) {
            return;
        }

        debug(trommel, "Processing trommel:" + trommel.skippedCounter + 1);
        ItemStack[] contents = source_inv.getContents();
        for (int i = 0; i < trommel.skippedCounter + 1; i++) {

            for (ItemStack stack : contents) {
                if (stack == null) {
                    continue;
                }

                if (ItemManager.getId(stack) == CivData.COBBLESTONE) {
                    try {
                        this.updateInventory(UpdateInventoryRequest.Action.REMOVE, source_inv, ItemManager.createItemStack(CivData.COBBLESTONE, 1));
                    } catch (InterruptedException e) {
                        return;
                    }

                    // Attempt to get special resources
                    // 尝试获得特殊资源
                    Random rand = new Random();
                    int randMax = GRAVEL_MAX_CHANCE;
                    int rand1 = rand.nextInt(randMax);
                    ItemStack newItem;

                    if (rand1 < ((int) ((trommel.getGravelChance(Mineral.CHROMIUM)) * randMax))) {
                        newItem = LoreMaterial.spawn(LoreMaterial.materialMap.get("mat_chromium_ore"));
                    } else if (rand1 < ((int) ((trommel.getGravelChance(Mineral.EMERALD)) * randMax))) {
                        newItem = ItemManager.createItemStack(CivData.EMERALD, 1);
                    } else if (rand1 < ((int) ((trommel.getGravelChance(Mineral.DIAMOND)) * randMax))) {
                        newItem = ItemManager.createItemStack(CivData.DIAMOND, 1);
                    } else if (rand1 < ((int) ((trommel.getGravelChance(Mineral.GOLD)) * randMax))) {
                        newItem = ItemManager.createItemStack(CivData.GOLD_INGOT, 1);
                    } else if (rand1 < ((int) ((trommel.getGravelChance(Mineral.REDSTONE)) * randMax))) {
                        int itemRand = rand.nextInt(5) + 1;
                        newItem = ItemManager.createItemStack(CivData.REDSTONE_DUST, itemRand);
                    } else if (rand1 < ((int) ((trommel.getGravelChance(Mineral.IRON)) * randMax))) {
                        newItem = ItemManager.createItemStack(CivData.IRON_INGOT, 1);
                    } else {
                        newItem = ItemManager.createItemStack(CivData.GRAVEL, GRAVEL_RATE);
                    }

                    //Try to add the new item to the dest chest, if we cant, oh well.
                    // 如果不能的话，请尝试将新商品添加到最好的箱子中，好吧。
                    try {
                        debug(trommel, "Updating inventory:" + newItem);
                        this.updateInventory(UpdateInventoryRequest.Action.ADD, dest_inv, newItem);
                    } catch (InterruptedException e) {
                        return;
                    }
                    break;
                }
                if (ItemManager.getId(stack) == CivData.STONE) {

                    if (this.trommel.getLevel() >= 2
                            && ItemManager.getData(stack) ==
                            ItemManager.getData(ItemManager.getMaterialData(CivData.STONE, CivData.GRANITE))) {
                        try {
                            this.updateInventory(UpdateInventoryRequest.Action.REMOVE, source_inv, ItemManager.createItemStack(CivData.STONE, 1, (short) CivData.GRANITE));
                        } catch (InterruptedException e) {
                            return;
                        }

                        // Attempt to get special resources
                        Random rand = new Random();
                        int randMax = GRANITE_MAX_CHANCE;
                        int rand1 = rand.nextInt(randMax);
                        ItemStack newItem;

                        if (rand1 < ((int) ((trommel.getGraniteChance(Mineral.CRYSTAL)) * randMax))) {
                            int rand2 = rand.nextInt(randMax);
                            if (rand2 < (randMax / 10)) {
                                int rand3 = rand.nextInt(randMax);
                                if (rand3 < (randMax / 2)) {
                                    newItem = LoreMaterial.spawn(LoreMaterial.materialMap.get("mat_ionic_crystal_2"));
                                } else {
                                    newItem = LoreMaterial.spawn(LoreMaterial.materialMap.get("mat_metallic_crystal_2"));
                                }
                            } else if (rand2 < (randMax / 2)) {
                                newItem = LoreMaterial.spawn(LoreMaterial.materialMap.get("mat_ionic_crystal_1"));
                            } else {
                                newItem = LoreMaterial.spawn(LoreMaterial.materialMap.get("mat_metallic_crystal_1"));
                            }
                        } else if (rand1 < ((int) ((trommel.getGraniteChance(Mineral.REFINED_CHROMIUM)) * randMax))) {
                            newItem = LoreMaterial.spawn(LoreMaterial.materialMap.get("mat_forged_chromium"));
                        } else if (rand1 < ((int) ((trommel.getGraniteChance(Mineral.REFINED_TUNGSTEN)) * randMax))) {
                            newItem = LoreMaterial.spawn(LoreMaterial.materialMap.get("mat_forged_tungsten"));
                        } else if (rand1 < ((int) ((trommel.getGraniteChance(Mineral.CRYSTAL_FRAGMENT)) * randMax))) {
                            int rand2 = rand.nextInt(randMax);
                            if (rand2 < (randMax / 10)) {
                                int rand3 = rand.nextInt(randMax);
                                if (rand3 < (randMax / 2)) {
                                    newItem = LoreMaterial.spawn(LoreMaterial.materialMap.get("mat_ionic_crystal_fragment_2"));
                                } else {
                                    newItem = LoreMaterial.spawn(LoreMaterial.materialMap.get("mat_metallic_crystal_fragment_2"));
                                }
                            } else if (rand2 < (randMax / 2)) {
                                newItem = LoreMaterial.spawn(LoreMaterial.materialMap.get("mat_ionic_crystal_fragment_1"));
                            } else {
                                newItem = LoreMaterial.spawn(LoreMaterial.materialMap.get("mat_metallic_crystal_fragment_1"));
                            }
                        } else if (rand1 < ((int) ((trommel.getGraniteChance(Mineral.CHROMIUM)) * randMax))) {
                            newItem = LoreMaterial.spawn(LoreMaterial.materialMap.get("mat_chromium_ore"));
                        } else if (rand1 < ((int) ((trommel.getGraniteChance(Mineral.EMERALD)) * randMax))) {
                            newItem = ItemManager.createItemStack(CivData.EMERALD, 1);
                        } else if (rand1 < ((int) ((trommel.getGraniteChance(Mineral.DIAMOND)) * randMax))) {
                            newItem = ItemManager.createItemStack(CivData.DIAMOND, 1);
                        } else if (rand1 < ((int) ((trommel.getGraniteChance(Mineral.TUNGSTEN)) * randMax))) {
                            newItem = LoreMaterial.spawn(LoreMaterial.materialMap.get("mat_tungsten_ore"));
                        } else if (rand1 < ((int) ((trommel.getGraniteChance(Mineral.GOLD)) * randMax))) {
                            newItem = ItemManager.createItemStack(CivData.GOLD_INGOT, 1);
                        } else if (rand1 < ((int) ((trommel.getGraniteChance(Mineral.REDSTONE)) * randMax))) {
                            int itemRand = rand.nextInt(5) + 1;
                            newItem = ItemManager.createItemStack(CivData.REDSTONE_DUST, itemRand);
                        } else if (rand1 < ((int) ((trommel.getGraniteChance(Mineral.IRON)) * randMax))) {
                            newItem = ItemManager.createItemStack(CivData.IRON_INGOT, 1);
                        } else if (rand1 < ((int) ((trommel.getGraniteChance(Mineral.POLISHED)) * randMax))) {
                            newItem = ItemManager.createItemStack(CivData.STONE, 1, (short) CivData.POLISHED_GRANITE);
                        } else if (rand1 < ((int) ((trommel.getGraniteChance(Mineral.DIRT)) * randMax))) {
                            newItem = ItemManager.createItemStack(CivData.DIRT, 1);
                        } else {
                            newItem = ItemManager.createItemStack(CivData.GRAVEL, GRAVEL_RATE);
                        }

                        //Try to add the new item to the dest chest, if we cant, oh well.
                        try {
                            debug(trommel, "Updating inventory:" + newItem);
                            this.updateInventory(UpdateInventoryRequest.Action.ADD, dest_inv, newItem);
                        } catch (InterruptedException e) {
                            return;
                        }
                        break;
                    }
                    if (this.trommel.getLevel() >= 3 && ItemManager.getData(stack) ==
                            ItemManager.getData(ItemManager.getMaterialData(CivData.STONE, CivData.DIORITE))) {
                        try {
                            this.updateInventory(UpdateInventoryRequest.Action.REMOVE, source_inv, ItemManager.createItemStack(CivData.STONE, 1, (short) CivData.DIORITE));
                        } catch (InterruptedException e) {
                            return;
                        }
                        // Attempt to get special resources
                        Random rand = new Random();
                        int randMax = DIORITE_MAX_CHANCE;
                        int rand1 = rand.nextInt(randMax);
                        ItemStack newItem;

                        if (rand1 < ((int) ((trommel.getDioriteChance(Mineral.CRYSTAL)) * randMax))) {
                            int rand2 = rand.nextInt(randMax);
                            if (rand2 < (randMax / 10)) {
                                int rand3 = rand.nextInt(randMax);
                                if (rand3 < (randMax / 2)) {
                                    newItem = LoreMaterial.spawn(LoreMaterial.materialMap.get("mat_ionic_crystal_2"));
                                } else {
                                    newItem = LoreMaterial.spawn(LoreMaterial.materialMap.get("mat_metallic_crystal_2"));
                                }
                            } else if (rand2 < (randMax / 2)) {
                                newItem = LoreMaterial.spawn(LoreMaterial.materialMap.get("mat_ionic_crystal_1"));
                            } else {
                                newItem = LoreMaterial.spawn(LoreMaterial.materialMap.get("mat_metallic_crystal_1"));
                            }
                        } else if (rand1 < ((int) ((trommel.getDioriteChance(Mineral.REFINED_CHROMIUM)) * randMax))) {
                            newItem = LoreMaterial.spawn(LoreMaterial.materialMap.get("mat_forged_chromium"));
                        } else if (rand1 < ((int) ((trommel.getDioriteChance(Mineral.REFINED_TUNGSTEN)) * randMax))) {
                            newItem = LoreMaterial.spawn(LoreMaterial.materialMap.get("mat_forged_tungsten"));
                        } else if (rand1 < ((int) ((trommel.getDioriteChance(Mineral.CRYSTAL_FRAGMENT)) * randMax))) {
                            int rand2 = rand.nextInt(randMax);
                            if (rand2 < (randMax / 10)) {
                                int rand3 = rand.nextInt(randMax);
                                if (rand3 < (randMax / 2)) {
                                    newItem = LoreMaterial.spawn(LoreMaterial.materialMap.get("mat_ionic_crystal_fragment_2"));
                                } else {
                                    newItem = LoreMaterial.spawn(LoreMaterial.materialMap.get("mat_metallic_crystal_fragment_2"));
                                }
                            } else if (rand2 < (randMax / 2)) {
                                newItem = LoreMaterial.spawn(LoreMaterial.materialMap.get("mat_ionic_crystal_fragment_1"));
                            } else {
                                newItem = LoreMaterial.spawn(LoreMaterial.materialMap.get("mat_metallic_crystal_fragment_1"));
                            }
                        } else if (rand1 < ((int) ((trommel.getDioriteChance(Mineral.CHROMIUM)) * randMax))) {
                            newItem = LoreMaterial.spawn(LoreMaterial.materialMap.get("mat_chromium_ore"));
                        } else if (rand1 < ((int) ((trommel.getDioriteChance(Mineral.EMERALD)) * randMax))) {
                            newItem = ItemManager.createItemStack(CivData.EMERALD, 1);
                        } else if (rand1 < ((int) ((trommel.getDioriteChance(Mineral.DIAMOND)) * randMax))) {
                            newItem = ItemManager.createItemStack(CivData.DIAMOND, 1);
                        } else if (rand1 < ((int) ((trommel.getDioriteChance(Mineral.TUNGSTEN)) * randMax))) {
                            newItem = LoreMaterial.spawn(LoreMaterial.materialMap.get("mat_tungsten_ore"));
                        } else if (rand1 < ((int) ((trommel.getDioriteChance(Mineral.GOLD)) * randMax))) {
                            newItem = ItemManager.createItemStack(CivData.GOLD_INGOT, 1);
                        } else if (rand1 < ((int) ((trommel.getDioriteChance(Mineral.REDSTONE)) * randMax))) {
                            int itemRand = rand.nextInt(5) + 1;
                            newItem = ItemManager.createItemStack(CivData.REDSTONE_DUST, itemRand);
                        } else if (rand1 < ((int) ((trommel.getDioriteChance(Mineral.IRON)) * randMax))) {
                            newItem = ItemManager.createItemStack(CivData.IRON_INGOT, 1);
                        } else if (rand1 < ((int) ((trommel.getDioriteChance(Mineral.POLISHED)) * randMax))) {
                            newItem = ItemManager.createItemStack(CivData.STONE, 1, (short) CivData.POLISHED_DIORITE);
                        } else if (rand1 < ((int) ((trommel.getDioriteChance(Mineral.DIRT)) * randMax))) {
                            newItem = ItemManager.createItemStack(CivData.DIRT, 1);
                        } else {
                            newItem = ItemManager.createItemStack(CivData.GRAVEL, (Integer) GRAVEL_RATE);
                        }

                        //Try to add the new item to the dest chest, if we cant, oh well.
                        try {
                            debug(trommel, "Updating inventory:" + newItem);
                            this.updateInventory(UpdateInventoryRequest.Action.ADD, dest_inv, newItem);
                        } catch (InterruptedException e) {
                            return;
                        }
                        break;
                    }
                    if (this.trommel.getLevel() >= 4 && ItemManager.getData(stack) ==
                            ItemManager.getData(ItemManager.getMaterialData(CivData.STONE, CivData.ANDESITE))) {
                        try {
                            this.updateInventory(UpdateInventoryRequest.Action.REMOVE, source_inv, ItemManager.createItemStack(CivData.STONE, 1, (short) CivData.ANDESITE));
                        } catch (InterruptedException e) {
                            return;
                        }
                        // Attempt to get special resources
                        Random rand = new Random();
                        int randMax = ANDESITE_MAX_CHANCE;
                        int rand1 = rand.nextInt(randMax);
                        ItemStack newItem;

                        if (rand1 < ((int) ((trommel.getAndesiteChance(Mineral.CRYSTAL)) * randMax))) {
                            int rand2 = rand.nextInt(randMax);
                            if (rand2 < (randMax / 10)) {
                                int rand3 = rand.nextInt(randMax);
                                if (rand3 < (randMax / 2)) {
                                    newItem = LoreMaterial.spawn(LoreMaterial.materialMap.get("mat_ionic_crystal_2"));
                                } else {
                                    newItem = LoreMaterial.spawn(LoreMaterial.materialMap.get("mat_metallic_crystal_2"));
                                }
                            } else if (rand2 < (randMax / 2)) {
                                newItem = LoreMaterial.spawn(LoreMaterial.materialMap.get("mat_ionic_crystal_1"));
                            } else {
                                newItem = LoreMaterial.spawn(LoreMaterial.materialMap.get("mat_metallic_crystal_1"));
                            }
                        } else if (rand1 < ((int) ((trommel.getAndesiteChance(Mineral.REFINED_CHROMIUM)) * randMax))) {
                            newItem = LoreMaterial.spawn(LoreMaterial.materialMap.get("mat_forged_chromium"));
                        } else if (rand1 < ((int) ((trommel.getAndesiteChance(Mineral.REFINED_TUNGSTEN)) * randMax))) {
                            newItem = LoreMaterial.spawn(LoreMaterial.materialMap.get("mat_forged_tungsten"));
                        } else if (rand1 < ((int) ((trommel.getAndesiteChance(Mineral.CRYSTAL_FRAGMENT)) * randMax))) {
                            int rand2 = rand.nextInt(randMax);
                            if (rand2 < (randMax / 10)) {
                                int rand3 = rand.nextInt(randMax);
                                if (rand3 < (randMax / 2)) {
                                    newItem = LoreMaterial.spawn(LoreMaterial.materialMap.get("mat_ionic_crystal_fragment_2"));
                                } else {
                                    newItem = LoreMaterial.spawn(LoreMaterial.materialMap.get("mat_metallic_crystal_fragment_2"));
                                }
                            } else if (rand2 < (randMax / 2)) {
                                newItem = LoreMaterial.spawn(LoreMaterial.materialMap.get("mat_ionic_crystal_fragment_1"));
                            } else {
                                newItem = LoreMaterial.spawn(LoreMaterial.materialMap.get("mat_metallic_crystal_fragment_1"));
                            }
                        } else if (rand1 < ((int) ((trommel.getAndesiteChance(Mineral.CHROMIUM)) * randMax))) {
                            newItem = LoreMaterial.spawn(LoreMaterial.materialMap.get("mat_chromium_ore"));
                        } else if (rand1 < ((int) ((trommel.getAndesiteChance(Mineral.EMERALD)) * randMax))) {
                            newItem = ItemManager.createItemStack(CivData.EMERALD, 1);
                        } else if (rand1 < ((int) ((trommel.getAndesiteChance(Mineral.DIAMOND)) * randMax))) {
                            newItem = ItemManager.createItemStack(CivData.DIAMOND, 1);
                        } else if (rand1 < ((int) ((trommel.getAndesiteChance(Mineral.TUNGSTEN)) * randMax))) {
                            newItem = LoreMaterial.spawn(LoreMaterial.materialMap.get("mat_tungsten_ore"));
                        } else if (rand1 < ((int) ((trommel.getAndesiteChance(Mineral.GOLD)) * randMax))) {
                            newItem = ItemManager.createItemStack(CivData.GOLD_INGOT, 1);
                        } else if (rand1 < ((int) ((trommel.getAndesiteChance(Mineral.REDSTONE)) * randMax))) {
                            int itemRand = rand.nextInt(5) + 1;
                            newItem = ItemManager.createItemStack(CivData.REDSTONE_DUST, itemRand);
                        } else if (rand1 < ((int) ((trommel.getAndesiteChance(Mineral.IRON)) * randMax))) {
                            newItem = ItemManager.createItemStack(CivData.IRON_INGOT, 1);
                        } else if (rand1 < ((int) ((trommel.getAndesiteChance(Mineral.POLISHED)) * randMax))) {
                            newItem = ItemManager.createItemStack(CivData.STONE, 1, (short) CivData.POLISHED_ANDESITE);
                        } else if (rand1 < ((int) ((trommel.getAndesiteChance(Mineral.DIRT)) * randMax))) {
                            newItem = ItemManager.createItemStack(CivData.DIRT, 1);
                        } else {
                            newItem = ItemManager.createItemStack(CivData.GRAVEL, (Integer) GRAVEL_RATE);
                        }

                        //Try to add the new item to the dest chest, if we cant, oh well.
                        try {
                            debug(trommel, "Updating inventory:" + newItem);
                            this.updateInventory(UpdateInventoryRequest.Action.ADD, dest_inv, newItem);
                        } catch (InterruptedException e) {
                            return;
                        }
                        break;
                    }
                }
            }
        }
        trommel.skippedCounter = 0;
    }


    @Override
    public void run() {
        if (this.trommel.lock.tryLock()) {
            try {
                try {
                    if (this.trommel.getTown().getGovernment().id.equals("gov_theocracy")
                            || this.trommel.getTown().getGovernment().id.equals("gov_monarchy")) {
                        Random rand = new Random();
                        int randMax = 100;
                        int rand1 = rand.nextInt(randMax);
                        Double chance = CivSettings.getDouble(CivSettings.structureConfig, "trommel.penalty_rate") * 100;
                        if (rand1 < chance) {
                            processTrommelUpdate();
                            debug(this.trommel, "Not penalized");
                        } else {
                            debug(this.trommel, "Skip Due to Penalty");
                        }
                    } else {
                        processTrommelUpdate();
                        if (this.trommel.getTown().getGovernment().id.equals("gov_despotism")) {
                            debug(this.trommel, "Doing Bonus");
                            processTrommelUpdate();
                        }
                    }
                    if (CivData.randChance(this.trommel.getTown().getReturnChance())) {
                        processTrommelUpdate();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } finally {
                this.trommel.lock.unlock();
            }
        } else {
            debug(this.trommel, "Failed to get lock while trying to start task, aborting.");
        }
    }

}
