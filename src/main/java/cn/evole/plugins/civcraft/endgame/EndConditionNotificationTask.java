package cn.evole.plugins.civcraft.endgame;

import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.main.CivMessage;
import cn.evole.plugins.civcraft.object.Civilization;
import cn.evole.plugins.civcraft.sessiondb.SessionEntry;
import cn.evole.plugins.civcraft.util.CivColor;

import java.util.ArrayList;

public class EndConditionNotificationTask implements Runnable {

    @Override
    public void run() {

        for (EndGameCondition endCond : EndGameCondition.endConditions) {
            ArrayList<SessionEntry> entries = CivGlobal.getSessionDB().lookup(endCond.getSessionKey());
            if (entries.size() == 0) {
                continue;
            }

            for (SessionEntry entry : entries) {
                Civilization civ = EndGameCondition.getCivFromSessionData(entry.value);
                if (civ != null) {
                    int daysLeft = endCond.getDaysToHold() - endCond.getDaysHeldFromSessionData(entry.value);
                    if (daysLeft == 0) {
                        CivMessage.global(CivSettings.localize.localizedString("var_cmd_civ_info_victory",
                                CivColor.LightBlue + CivColor.BOLD + civ.getName() + CivColor.White, CivColor.LightPurple + CivColor.BOLD + endCond.getVictoryName() + CivColor.White));
                        break;
                    } else {
                        CivMessage.global(CivSettings.localize.localizedString("var_cmd_civ_info_daysTillVictoryNew",
                                CivColor.LightBlue + CivColor.BOLD + civ.getName() + CivColor.White, CivColor.Yellow + CivColor.BOLD + daysLeft + CivColor.White, CivColor.LightPurple + CivColor.BOLD + endCond.getVictoryName() + CivColor.White));
                    }
                }
            }
        }

    }

}
