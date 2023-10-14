/**
 * [2023] Cnlimiter LLC
 * All Rights Reserved.
 * Name: civcraft
 * Author: cnlimiter
 * UpdateTime: 2023/10/14 16:15
 * Description:
 * License: ARR
 */
package cn.evole.plugins.civcraft.war;

import cn.evole.plugins.civcraft.main.CivData;
import cn.evole.plugins.civcraft.object.Town;
import cn.evole.plugins.civcraft.util.BukkitObjects;
import cn.evole.plugins.civcraft.util.ItemManager;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileReader;
import cn.hutool.core.io.file.FileWriter;
import gpl.InventorySerializer;
import org.bukkit.block.*;
import org.bukkit.inventory.Inventory;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WarRegen {

    /*
     * Saves an regenerates blocks during a war.
     * 在战争期间保存一个再生方块。
     */
    private static Map<Block, Boolean> blockCache = new HashMap<Block, Boolean>();


    private static String blockAsAir(Block blk) {
        return CivData.AIR + ":0:" + blk.getX() + ":" + blk.getY() + ":" + blk.getZ() + ":" + blk.getWorld().getName();
    }

    private static String blockBasicString(Block blk) {
        return ItemManager.getId(blk) + ":" + ItemManager.getData(blk) + ":"
                + blk.getX() + ":" + blk.getY() + ":" + blk.getZ() + ":" + blk.getWorld().getName();
    }

    public static String blockInventoryString(Inventory inv) {
        String out = ":";

        out += InventorySerializer.InventoryToString(inv);

        return out;
    }

    public static String blockSignString(Sign sign) {
        StringBuilder out = new StringBuilder(":");

        for (String str : sign.getLines()) {
            out.append(str).append(",");
        }

        return out.toString();
    }

    private static String blockToString(Block blk, boolean save_as_air) {
        if (save_as_air) {
            return blockAsAir(blk);
        } else {
            String str = blockBasicString(blk);

            Inventory inv = null;
            switch (blk.getType()) {
                case TRAPPED_CHEST:
                case CHEST:
                    inv = ((Chest) blk.getState()).getBlockInventory();
                    str += blockInventoryString(inv);
                    break;
                case DISPENSER:
                    inv = ((Dispenser) blk.getState()).getInventory();
                    str += blockInventoryString(inv);
                    break;
                case BURNING_FURNACE:
                case FURNACE:
                    inv = ((Furnace) blk.getState()).getInventory();
                    str += blockInventoryString(inv);
                    break;
                case DROPPER:
                    inv = ((Dropper) blk.getState()).getInventory();
                    str += blockInventoryString(inv);
                    break;
                case HOPPER:
                    inv = ((Hopper) blk.getState()).getInventory();
                    str += blockInventoryString(inv);
                    break;
                case SIGN:
                case SIGN_POST:
                case WALL_SIGN:
                    Sign sign = (Sign) blk.getState();
                    str += blockSignString(sign);
                    break;
                default:
                    break;
            }

            return str;
        }
    }

    /**
     * 解析文件的每一行 还原为方块
     */
    private static void restoreBlockFromString(String line) {
        String[] split = line.split(":");

        int type = Integer.parseInt(split[0]);
        byte data = Byte.parseByte(split[1]);
        int x = Integer.parseInt(split[2]);
        int y = Integer.parseInt(split[3]);
        int z = Integer.parseInt(split[4]);
        String world = split[5];

        Block block = BukkitObjects.getWorld(world).getBlockAt(x, y, z);

        ItemManager.setTypeId(block, type);
        ItemManager.setData(block, data, false);

        // End of basic block info, try to get more now.
        Inventory inv = null;
        switch (block.getType()) {
            case TRAPPED_CHEST:
                inv = ((Chest) block.getState()).getBlockInventory();
                InventorySerializer.StringToInventory(inv, split[6]);
                break;
            case CHEST:
                inv = ((Chest) block.getState()).getBlockInventory();
                InventorySerializer.StringToInventory(inv, split[6]);
                break;
            case DISPENSER:
                inv = ((Dispenser) block.getState()).getInventory();
                InventorySerializer.StringToInventory(inv, split[6]);
                break;
            case BURNING_FURNACE:
            case FURNACE:
                inv = ((Furnace) block.getState()).getInventory();
                InventorySerializer.StringToInventory(inv, split[6]);
                break;
            case DROPPER:
                inv = ((Dropper) block.getState()).getInventory();
                InventorySerializer.StringToInventory(inv, split[6]);
                break;
            case HOPPER:
                inv = ((Hopper) block.getState()).getInventory();
                InventorySerializer.StringToInventory(inv, split[6]);
                break;
            case SIGN:
            case SIGN_POST:
            case WALL_SIGN:
                Sign sign = (Sign) block.getState();
                String[] messages = split[6].split(",");
                for (int i = 0; i < 4; i++) {
                    if (messages[i] != null) {
                        sign.setLine(i, messages[i]);
                    }
                }
                sign.update();
                break;
            default:
                break;
        }


    }

    public static void explodeThisBlock(Block blk, String file) {
        // TNT和大炮的爆炸块
        switch (blk.getType()) {
            case SIGN_POST:
                return;
            case WALL_SIGN:
                return;
            case TNT:
                return;
            default:
                break;
        }

        WarRegen.saveBlock(blk, file, false);

        switch (blk.getType()) {
            case TRAPPED_CHEST:
                ((Chest) blk.getState()).getBlockInventory().clear();
                break;
            case CHEST:
                ((Chest) blk.getState()).getBlockInventory().clear();
                break;
            case DISPENSER:
                ((Dispenser) blk.getState()).getInventory().clear();
                break;
            case BURNING_FURNACE:
            case FURNACE:
                ((Furnace) blk.getState()).getInventory().clear();
                break;
            case DROPPER:
                ((Dropper) blk.getState()).getInventory().clear();
                break;
            case HOPPER:
                ((Hopper) blk.getState()).getInventory().clear();
                break;
            default:
                break;
        }

        ItemManager.setTypeId(blk, CivData.AIR);
        ItemManager.setData(blk, 0x0, true);

    }

    public static void destroyThisBlock(Block blk, Town town) {

        WarRegen.saveBlock(blk, town.getName(), false);

        switch (blk.getType()) {
            case TRAPPED_CHEST:
                ((Chest) blk.getState()).getBlockInventory().clear();
                break;
            case CHEST:
                ((Chest) blk.getState()).getBlockInventory().clear();
                break;
            case DISPENSER:
                ((Dispenser) blk.getState()).getInventory().clear();
                break;
            case BURNING_FURNACE:
            case FURNACE:
                ((Furnace) blk.getState()).getInventory().clear();
                break;
            case DROPPER:
                ((Dropper) blk.getState()).getInventory().clear();
                break;
            case HOPPER:
                ((Hopper) blk.getState()).getInventory().clear();
                break;
            default:
                break;
        }

        ItemManager.setTypeId(blk, CivData.AIR);
        ItemManager.setData(blk, 0x0, true);

    }

    public static boolean canPlaceThisBlock(Block blk) {
        switch (blk.getType()) {
            case LAVA:
            case WATER:
                return false;
            default:
                break;
        }
        return true;
    }

    public static void saveBlock(Block blk, String name, boolean save_as_air) {
        // Open this town's war log file.
        // append this block to the war log file.
        // 打开该镇的战争日志文件。 将此块追加到战争日志文件。
        Boolean saved = blockCache.get(blk);
        if (saved == Boolean.TRUE) {
            //Block has already been saved, dont save it again.
            //This should prevent enemies from being able to overwrite
            //legit blocks.
            ///块已保存，请勿再次保存。
            //             //这应该防止敌人覆盖
            //             //合法块。
            return;
        }

        //会报IO错误
        String filepath = "templates/war/" + name;
        File file = FileUtil.touch(FileUtil.newFile(filepath));
        FileWriter writer = new FileWriter(file);
        writer.append(blockToString(blk, save_as_air) + "\n");
//            FileWriter fstream = new FileWriter(filepath, true);
//            BufferedWriter out = new BufferedWriter(fstream);
        //out.append(blockToString(blk, save_as_air) + "\n");
        blockCache.put(blk, Boolean.TRUE);

    }

    /**
     * 读取缓存文件
     */
    public static void restoreBlocksFor(String name) {
        // Open the town's war log file and restore all the blocks
        // Call this function when we start up.
        // Delete the war log file to save space.
        File warLog = null;

        int count = 0;
        String filepath = "templates/war/" + name;
        warLog = new File(filepath);

        if (!warLog.exists())
            return;

        FileReader reader = new FileReader(warLog);
        List<String> lines = reader.readLines();
        for (String line : lines) {
            try {
                restoreBlockFromString(line);
                count++;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        FileUtil.del(warLog);
        System.out.println("[CivCraft] Restored " + count + " blocks for town " + name);
    }


}
