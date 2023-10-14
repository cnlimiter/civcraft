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

import cn.evole.plugins.civcraft.cache.PlayerLocationCache;
import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.config.ConfigMission;
import cn.evole.plugins.civcraft.exception.CivException;
import cn.evole.plugins.civcraft.exception.InvalidConfiguration;
import cn.evole.plugins.civcraft.items.units.Unit;
import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.main.CivMessage;
import cn.evole.plugins.civcraft.object.CultureChunk;
import cn.evole.plugins.civcraft.object.Resident;
import cn.evole.plugins.civcraft.object.Town;
import cn.evole.plugins.civcraft.structure.ScoutShip;
import cn.evole.plugins.civcraft.structure.ScoutTower;
import cn.evole.plugins.civcraft.structure.Structure;
import cn.evole.plugins.civcraft.threading.TaskMaster;
import cn.evole.plugins.civcraft.util.BlockCoord;
import cn.evole.plugins.civcraft.util.ChunkCoord;
import cn.evole.plugins.civcraft.util.CivColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class EspionageMissionTask implements Runnable {

    ConfigMission mission;
    String playerName;
    Town target;
    int secondsLeft;
    Location startLocation;


    public EspionageMissionTask(ConfigMission mission, String playerName, Location startLocation, Town target, int seconds) {
        this.mission = mission;
        this.playerName = playerName;
        this.target = target;
        this.startLocation = startLocation;
        this.secondsLeft = seconds;
    }

    @Override
    public void run() {
        int exposePerSecond;
        int exposePerPlayer;
        int exposePerScout;
        try {
            exposePerSecond = CivSettings.getInteger(CivSettings.espionageConfig, "espionage.exposure_per_second");
            exposePerPlayer = CivSettings.getInteger(CivSettings.espionageConfig, "espionage.exposure_per_player");
            exposePerScout = CivSettings.getInteger(CivSettings.espionageConfig, "espionage.exposure_per_scout");
        } catch (InvalidConfiguration e) {
            e.printStackTrace();
            return;
        }

        Player player;
        try {
            player = CivGlobal.getPlayer(playerName);
        } catch (CivException e) {
            return;
        }
        Resident resident = CivGlobal.getResident(player);
        CivMessage.send(player, CivColor.LightGreen + CivColor.BOLD + CivSettings.localize.localizedString("espionage_missionStarted"));

        while (secondsLeft > 0) {
            // TODO :时间计算还得改
            if (secondsLeft > 0) {
                secondsLeft--;

                /* Add base exposure. */
                resident.setPerformingMission(true);
                resident.setSpyExposure(resident.getSpyExposure() + exposePerSecond);

                /* Add players nearby exposure */
                //PlayerLocationCache.lock.lock();
                double searchRadius;
                try {
                    searchRadius = CivSettings.getDouble(CivSettings.espionageConfig, "espionage.player_search_radius");
                } catch (InvalidConfiguration e) {
                    e.printStackTrace();
                    searchRadius = 600;
//					resident.setPerformingMission(false);
                }
                try {
                    int playerCount = PlayerLocationCache.getNearbyPlayers(new BlockCoord(player.getLocation()), searchRadius).size();
                    playerCount--;
                    resident.setSpyExposure(resident.getSpyExposure() + (playerCount * exposePerPlayer));
                } finally {
                    //	PlayerLocationCache.lock.unlock();
                }

                /* Add scout tower exposure */
                int amount = 0;
                double range;
                try {
                    range = CivSettings.getDouble(CivSettings.warConfig, "scout_tower.range");
                } catch (InvalidConfiguration e) {
                    e.printStackTrace();
                    range = 400;
//                    resident.setPerformingMission(false);
//                    return;
                }

                BlockCoord bcoord = new BlockCoord(player.getLocation());

                for (Structure struct : target.getStructures()) {
                    if (!struct.isActive()) {
                        continue;
                    }

                    if (struct instanceof ScoutTower || struct instanceof ScoutShip) {
                        if (bcoord.distance(struct.getCenterLocation()) < range) {
                            amount += exposePerScout;
                        }
                    }
                }
                resident.setSpyExposure(resident.getSpyExposure() + amount);

                /* Process exposure penalities */
                if (target.processSpyExposure(resident)) {
                    // 任务失败
                    CivMessage.global(CivColor.Yellow + CivSettings.localize.localizedString("var_espionage_missionFailedAlert", (CivColor.White + player.getName()), mission.name, target.getName()));
                    CivMessage.send(player, CivColor.Rose + CivSettings.localize.localizedString("espionage_missionFailed"));
                    Unit.removeUnit(player);
                    resident.setPerformingMission(false);
                    return;
                }

                if ((secondsLeft % 15) == 0) {
                    CivMessage.send(player, CivColor.Yellow + CivColor.BOLD + CivSettings.localize.localizedString("var_espionage_secondsRemain", secondsLeft));
                } else if (secondsLeft < 10) {
                    CivMessage.send(player, CivColor.Yellow + CivColor.BOLD + CivSettings.localize.localizedString("var_espionage_secondsRemain", secondsLeft));
                }

            }

            ChunkCoord coord = new ChunkCoord(player.getLocation());
            CultureChunk cc = CivGlobal.getCultureChunk(coord);

            if (cc == null || cc.getCiv() != target.getCiv()) {
                CivMessage.sendError(player, CivSettings.localize.localizedString("espionage_missionAborted"));
                return;
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                return;
            }
        }

        resident.setPerformingMission(false);
        TaskMaster.syncTask(new PerformMissionTask(mission, playerName));
    }

}
