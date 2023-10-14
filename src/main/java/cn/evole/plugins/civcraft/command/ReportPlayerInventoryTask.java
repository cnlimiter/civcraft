/**
 * [2023] Cnlimiter LLC
 * All Rights Reserved.
 * Name: civcraft
 * Author: cnlimiter
 * UpdateTime: 2023/10/14 16:15
 * Description:
 * License: ARR
 */
package cn.evole.plugins.civcraft.command;


import cn.evole.plugins.civcraft.main.CivMessage;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.util.Queue;

public class ReportPlayerInventoryTask implements Runnable {

    Queue<OfflinePlayer> offplayers;
    CommandSender sender;

    public ReportPlayerInventoryTask(CommandSender sender, Queue<OfflinePlayer> offplayers) {
        this.sender = sender;
        this.offplayers = offplayers;
    }

//	private int countItem(ItemStack[] stacks, int id) {
//		int total = 0;
//		for (ItemStack stack : stacks) {
//			if (stack == null) {
//				continue;
//			}
//			
//			if (ItemManager.getId(stack) == id) {
//				total += stack.getAmount();
//			}
//		}
//		
//		return total;
//	}

    @Override
    public void run() {
        CivMessage.sendError(sender, "Deprecated do not use anymore.. or fix it..");
//		for (int i = 0; i < 20; i++) {
//			OfflinePlayer off = offplayers.poll();
//			if (off == null) {
//				sender.sendMessage("Done.");
//				return;
//			}
//			
//			try {
//				PlayerFile pFile = new PlayerFile(off);
//				
//				int diamondBlocks = countItem(pFile.getEnderchestContents(), ItemManager.getId(Material.DIAMOND_BLOCK));
//				int diamonds = countItem(pFile.getEnderchestContents(), ItemManager.getId(Material.DIAMOND));
//				int goldBlocks = countItem(pFile.getEnderchestContents(), ItemManager.getId(Material.GOLD_BLOCK));
//				int gold = countItem(pFile.getEnderchestContents(), ItemManager.getId(Material.GOLD_INGOT));
//				int emeraldBlocks = countItem(pFile.getEnderchestContents(), ItemManager.getId(Material.EMERALD_BLOCK));
//				int emeralds = countItem(pFile.getEnderchestContents(), ItemManager.getId(Material.EMERALD));
//				int diamondOre = countItem(pFile.getEnderchestContents(), ItemManager.getId(Material.DIAMOND_ORE));
//				int goldOre = countItem(pFile.getEnderchestContents(), ItemManager.getId(Material.GOLD_ORE));
//				int emeraldOre = countItem(pFile.getEnderchestContents(), ItemManager.getId(Material.EMERALD_ORE));
//				
//				String out =  off.getName()+": DB:"+diamondBlocks+" EB:"+emeraldBlocks+" GB:"+goldBlocks+" D:"+diamonds+" E:"+emeralds+" G:"+
//						gold+" DO:"+diamondOre+" EO:"+emeraldOre+" GO:"+goldOre;
//				if (diamondBlocks != 0 || diamonds != 0 || goldBlocks != 0 || gold != 0 || emeraldBlocks != 0 
//						|| emeralds != 0 || diamondOre != 0 || goldOre != 0 || emeraldOre != 0) {
//					CivMessage.send(sender, out);
//					CivLog.info("REPORT:"+out);
//				}
//				
//				
//				diamondBlocks = countItem(pFile.getInventoryContents(), ItemManager.getId(Material.DIAMOND_BLOCK));
//				diamonds = countItem(pFile.getInventoryContents(), ItemManager.getId(Material.DIAMOND));
//				goldBlocks = countItem(pFile.getInventoryContents(), ItemManager.getId(Material.GOLD_BLOCK));
//				gold = countItem(pFile.getInventoryContents(), ItemManager.getId(Material.GOLD_INGOT));
//				emeraldBlocks = countItem(pFile.getInventoryContents(), ItemManager.getId(Material.EMERALD_BLOCK));
//				emeralds = countItem(pFile.getInventoryContents(),ItemManager.getId( Material.EMERALD));
//				diamondOre = countItem(pFile.getInventoryContents(), ItemManager.getId(Material.DIAMOND_ORE));
//				goldOre = countItem(pFile.getInventoryContents(), ItemManager.getId(Material.GOLD_ORE));
//				emeraldOre = countItem(pFile.getInventoryContents(), ItemManager.getId(Material.EMERALD_ORE));
//				
//				String out2 =  off.getName()+": DB:"+diamondBlocks+" EB:"+emeraldBlocks+" GB:"+goldBlocks+" D:"+diamonds+" E:"+emeralds+" G:"+
//						gold+" DO:"+diamondOre+" EO:"+emeraldOre+" GO:"+goldOre;
//				if (diamondBlocks != 0 || diamonds != 0 || goldBlocks != 0 || gold != 0 || emeraldBlocks != 0 
//						|| emeralds != 0 || diamondOre != 0 || goldOre != 0 || emeraldOre != 0) {
//					CivMessage.send(sender, out2);
//					CivLog.info("REPORT:"+out2);
//				}
//			} catch (Exception e) {
//				CivLog.info("REPORT: "+off.getName()+" EXCEPTION:"+e.getMessage());
//			}
//		}
//		
//		TaskMaster.syncTask(new ReportPlayerInventoryTask(sender, offplayers));

    }

}
