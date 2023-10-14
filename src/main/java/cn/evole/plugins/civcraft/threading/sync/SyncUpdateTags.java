///**
// * [2023] Cnlimiter LLC
// * All Rights Reserved.
// * Name: civcraft
// * Author: cnlimiter
// * UpdateTime: 2023/10/14 16:15
// * Description:
// * License: ARR
// */
//package cn.evole.plugins.civcraft.threading.sync;
//
//import cn.evole.plugins.civcraft.config.CivSettings;
//import cn.evole.plugins.civcraft.exception.CivException;
//import cn.evole.plugins.civcraft.main.CivGlobal;
//import cn.evole.plugins.civcraft.object.Resident;
//import net.md_5.itag.iTag;
//import org.bukkit.entity.Player;
//
//import java.util.Collection;
//
//public class SyncUpdateTags implements Runnable {
//
//    Collection<Resident> residentsToSendUpdate;
//    String playerToUpdate;
//
//    public SyncUpdateTags(String playerToUpdate, Collection<Resident> residentsToSendUpdate) {
//        this.residentsToSendUpdate = residentsToSendUpdate;
//        this.playerToUpdate = playerToUpdate;
//    }
//
//    @Override
//    public void run() {
//        if (CivSettings.hasITag) {
//            try {
//                Player player = CivGlobal.getPlayer(playerToUpdate);
//                for (Resident resident : residentsToSendUpdate) {
//                    try {
//                        Player resPlayer = CivGlobal.getPlayer(resident);
//                        if (player == resPlayer) {
//                            continue;
//                        }
//                        iTag.getInstance().refreshPlayer(player, resPlayer);
//                        iTag.getInstance().refreshPlayer(resPlayer, player);
//                    } catch (CivException e) {
//                        // one of these players is not online.
//                    }
//                }
//
//
//            } catch (CivException e1) {
//                return;
//            }
//        }
//    }
//
//}
