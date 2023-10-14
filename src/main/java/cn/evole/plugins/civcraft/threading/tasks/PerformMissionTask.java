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


import cn.evole.plugins.civcraft.config.ConfigMission;
import cn.evole.plugins.civcraft.items.units.MissionBook;

public class PerformMissionTask implements Runnable {
    ConfigMission mission;
    String playerName;

    public PerformMissionTask(ConfigMission mission, String playerName) {
        this.mission = mission;
        this.playerName = playerName;
    }


    @Override
    public void run() {
        MissionBook.performMission(mission, playerName);
    }

}
