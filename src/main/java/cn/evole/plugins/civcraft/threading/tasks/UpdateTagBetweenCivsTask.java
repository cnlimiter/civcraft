///**
// * [2023] Cnlimiter LLC
// * All Rights Reserved.
// * Name: civcraft
// * Author: cnlimiter
// * UpdateTime: 2023/10/14 16:15
// * Description:
// * License: ARR
// */
//package cn.evole.plugins.civcraft.threading.tasks;
//
//import cn.evole.plugins.civcraft.main.CivGlobal;
//import cn.evole.plugins.civcraft.object.Civilization;
//import cn.evole.plugins.civcraft.object.Resident;
//import cn.evole.plugins.civcraft.threading.TaskMaster;
//import cn.evole.plugins.civcraft.threading.sync.SyncUpdateTagsBetweenCivs;
//import org.bukkit.Bukkit;
//import org.bukkit.entity.Player;
//
//import java.util.HashSet;
//import java.util.Set;
//
//public class UpdateTagBetweenCivsTask implements Runnable {
//
//    Civilization civ;
//    Civilization otherCiv;
//
//    public UpdateTagBetweenCivsTask(Civilization civ, Civilization otherCiv) {
//        this.civ = civ;
//        this.otherCiv = otherCiv;
//    }
//
//    @Override
//    public void run() {
//        Set<Player> civList = new HashSet<Player>();
//        Set<Player> otherCivList = new HashSet<Player>();
//
//        for (Player player : Bukkit.getOnlinePlayers()) {
//            Resident resident = CivGlobal.getResident(player);
//            if (resident == null || !resident.hasTown()) {
//                continue;
//            }
//
//            if (resident.getTown().getCiv() == civ) {
//                civList.add(player);
//            } else if (resident.getTown().getCiv() == otherCiv) {
//                otherCivList.add(player);
//            }
//        }
//
//        TaskMaster.syncTask(new SyncUpdateTagsBetweenCivs(civList, otherCivList));
//    }
//
//}
