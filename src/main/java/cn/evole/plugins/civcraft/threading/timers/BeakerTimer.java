/**
 * [2023] Cnlimiter LLC
 * All Rights Reserved.
 * Name: civcraft
 * Author: cnlimiter
 * UpdateTime: 2023/10/14 16:15
 * Description:
 * License: ARR
 */
package cn.evole.plugins.civcraft.threading.timers;

import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.main.CivMessage;
import cn.evole.plugins.civcraft.object.Civilization;
import cn.evole.plugins.civcraft.object.Town;
import cn.evole.plugins.civcraft.structure.TownHall;
import cn.evole.plugins.civcraft.threading.CivAsyncTask;
import cn.evole.plugins.civcraft.threading.TaskMaster;
import cn.evole.plugins.civcraft.threading.tasks.UpdateTechBar;

/**
 * 1分钟执行一次
 * 增加科研进度
 */
public class BeakerTimer extends CivAsyncTask {

    //private double beakersPerRun;

    public static final int BEAKER_PERIOD = 60;

    public BeakerTimer(int periodInSeconds) {

        //	this.beakersPerRun = ((double)periodInSeconds/60);
    }

    @Override
    public void run() {

        for (Civilization civ : CivGlobal.getCivs()) {

            if (civ.getCapitolName() == null) {
                CivMessage.sendCiv(civ, CivSettings.localize.localizedString("beaker_ErrorNoCapitol"));
                continue;
            }

            Town town = CivGlobal.getTown(civ.getCapitolName());
            if (town == null) {
                CivMessage.sendCiv(civ, CivSettings.localize.localizedString("var_beaker_noCapitol", civ.getCapitolName()));
                continue;
            }

            TownHall townhall = town.getTownHall();
            if (townhall == null) {
                CivMessage.sendCiv(civ, CivSettings.localize.localizedString("beaker_noCapitolHall"));
            }

            try {
                /*
                 * The base_beakers defines the number of beakers per hour to give.
                 * This timer runs every min, so dividing my 60 will give us the number
                 * of beakers per min.
                 */
                //研究完成后
                if (civ.getResearchTech() == null && civ.getTechQueued().size() != 0) {
                    civ.setResearchTech(civ.getTechQueued().poll());
                    TaskMaster.asyncTask(new UpdateTechBar(civ), 0);
                }
                if (civ.getResearchTech() != null) {
                    civ.addBeakers(civ.getBeakers() / BEAKER_PERIOD);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


    }

}
