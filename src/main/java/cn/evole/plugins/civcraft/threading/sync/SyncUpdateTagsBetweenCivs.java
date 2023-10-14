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
//import net.md_5.itag.iTag;
//import org.bukkit.entity.Player;
//
//import java.util.HashSet;
//import java.util.Set;
//
//
//public class SyncUpdateTagsBetweenCivs implements Runnable {
//    Set<Player> civList = new HashSet<Player>();
//    Set<Player> otherCivList = new HashSet<Player>();
//
//    public SyncUpdateTagsBetweenCivs(Set<Player> civList, Set<Player> otherCivList) {
//        this.civList = civList;
//        this.otherCivList = otherCivList;
//    }
//
//    @Override
//    public void run() {
//        if (CivSettings.hasITag) {
//            for (Player player : civList) {
//                if (!otherCivList.isEmpty()) {
//                    iTag.getInstance().refreshPlayer(player, otherCivList);
//                }
//            }
//
//            for (Player player : otherCivList) {
//                if (!civList.isEmpty()) {
//                    iTag.getInstance().refreshPlayer(player, civList);
//                }
//            }
//        }
//    }
//
//}
