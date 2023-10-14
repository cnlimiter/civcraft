/**
 * [2023] Cnlimiter LLC
 * All Rights Reserved.
 * Name: civcraft
 * Author: cnlimiter
 * UpdateTime: 2023/10/14 16:15
 * Description:
 * License: ARR
 */
package cn.evole.plugins.civcraft.threading.sync;

public class SyncUpdateChunks implements Runnable {


    //public static HashSet<ChunkCoord> updateChunks = new HashSet<ChunkCoord>();

    /*
     * XXX This is not going to be used anymore. the "refreshChunk" has a bug in it that
     * prevents players from being able to hit each other when it finishes a refresh.
     *
     */

    @Override
    public void run() {

//		int viewDistance = Bukkit.getViewDistance();
//		
//		ChunkCoord playerChunkCoord = new ChunkCoord();
//		
//		for (ChunkCoord c : updateChunks) {
//			CivLog.debug("Updating chunks c:"+c.toString());
//			
//		//	World world = Bukkit.getWorld(c.getWorldname());
//		//	world.refreshChunk(c.getX(), c.getZ());
//			
//		//	for (Player p : Bukkit.getOnlinePlayers()) {
//			///	playerChunkCoord.setFromLocation(p.getLocation());
//				
//			//	if (c.distance(playerChunkCoord) < viewDistance) {
//					//CivLog.debug("\tskipping...");
//					//CivGlobal.nms.queueChunkForUpdate(p, c.getX(), c.getZ());
//				//	p.g
//			//	}
//			//}
//		}
//	
//		updateChunks.clear();
    }
}
