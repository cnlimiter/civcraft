package cn.evole.plugins.civcraft.endgame;

import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.main.CivMessage;
import cn.evole.plugins.civcraft.object.Civilization;
import cn.evole.plugins.civcraft.sessiondb.SessionEntry;

import java.util.ArrayList;


public class EndGameCheckTask implements Runnable {

    @Override
    public void run() {

        /*
         * Run through every Civilization in the game, if one the checks pass,
         * then declare that Civilization the winner.
         * 运行游戏中的每个文明，如果一项检查通过， 然后宣布文明为获胜者。
         */

        /* TODO automate as much of this as possible. */

        /* TODO Record winner.
         *  - Record Scores for all civs. 记录所有文明的分数。
         *  - Mark game as over and disallow score changes. 将游戏标记为结束，并禁止分数变化。
         *  - Add top5 civs to global 'hall of fame' table. 将top5 civs添加到全球“名人堂”表中。
         *  - check that game is noArrayList<E>fore doing end game checks. 检查游戏是否不是用于进行残局检查的ArrayList <E>。
         */
        if (CivGlobal.isCasualMode()) {
            return;
        }

        ArrayList<SessionEntry> entries = CivGlobal.getSessionDB().lookup("endgame:winningCiv");
        if (entries.size() != 0) {
            CivMessage.global(entries.get(0).value);
            return;
        }

        for (Civilization civ : CivGlobal.getCivs()) {
            if (civ.isAdminCiv()) {
                continue;
            }

            /* Check for every condition. */
            for (EndGameCondition cond : EndGameCondition.endConditions) {
                if (cond.check(civ)) {
                    cond.onSuccess(civ);
                } else {
                    cond.onFailure(civ);
                }
            }
            if (false) {
                civ.declareAsWinner();
                break;
            }
        }

    }

}
