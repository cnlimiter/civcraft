/**
 * [2023] Cnlimiter LLC
 * All Rights Reserved.
 * Name: civcraft
 * Author: cnlimiter
 * UpdateTime: 2023/10/14 16:15
 * Description:
 * License: ARR
 */
package cn.evole.plugins.civcraft.event;

import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.exception.InvalidConfiguration;
import cn.evole.plugins.civcraft.items.BonusGoodie;
import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.main.CivLog;
import cn.evole.plugins.civcraft.main.CivMessage;
import cn.evole.plugins.civcraft.object.Town;
import cn.evole.plugins.civcraft.threading.TaskMaster;

import java.util.Calendar;

public class GoodieRepoEvent implements EventInterface {

    public static void repoProcess() {
        class SyncTask implements Runnable {
            @Override
            public void run() {

                for (Town town : CivGlobal.getTowns()) {
                    for (BonusGoodie goodie : town.getBonusGoodies()) {
                        town.removeGoodie(goodie);
                    }
                }

                for (BonusGoodie goodie : CivGlobal.getBonusGoodies()) {
                    try {
                        goodie.replenish();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

        }

        TaskMaster.syncTask(new SyncTask());
    }

    @Override
    public void process() {
        CivLog.info("TimerEvent: GoodieRepo -------------------------------------");
        repoProcess();
        CivMessage.globalTitle(CivSettings.localize.localizedString("goodieRepoBroadcastTitle"), "");
        CivMessage.global(CivSettings.localize.localizedString("goodieRepoBroadcast"));
    }

    @Override
    public Calendar getNextDate() throws InvalidConfiguration {
        Calendar cal = EventTimer.getCalendarInServerTimeZone();
        int repo_day = CivSettings.getInteger(CivSettings.goodsConfig, "trade_goodie_repo_day");
        int repo_hour = CivSettings.getInteger(CivSettings.goodsConfig, "trade_goodie_repo_hour");
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.HOUR_OF_DAY, repo_hour);
        cal.add(Calendar.DATE, repo_day);
        return cal;
    }

}
