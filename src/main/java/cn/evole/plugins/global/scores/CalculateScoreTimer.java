/************************************************************************
 <p>
 AVRGAMING LLC
 __________________
 <p>
 [2013] AVRGAMING LLC
 All Rights Reserved.
 <p>
 NOTICE:  All information contained herein is, and remains
 the property of AVRGAMING LLC and its suppliers,
 if any.  The intellectual and technical concepts contained
 herein are proprietary to AVRGAMING LLC
 and its suppliers and may be covered by U.S. and Foreign Patents,
 patents in process, and are protected by trade secret or copyright law.
 Dissemination of this information or reproduction of this material
 is strictly forbidden unless prior written permission is obtained
 from AVRGAMING LLC.
 */
package cn.evole.plugins.global.scores;

import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.object.Civilization;
import cn.evole.plugins.civcraft.object.Town;
import cn.evole.plugins.civcraft.sessiondb.SessionEntry;
import cn.evole.plugins.civcraft.threading.CivAsyncTask;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.TreeMap;

public class CalculateScoreTimer extends CivAsyncTask {

    @Override
    public void run() {

        if (!CivGlobal.scoringEnabled) {
            return;
        }

        ArrayList<SessionEntry> entries = CivGlobal.getSessionDB().lookup("endgame:winningCiv");
        if (!entries.isEmpty()) {
            /* we have a winner, do not accumulate scores anymore. */
            return;
        }

        TreeMap<Integer, Civilization> civScores = new TreeMap<>();
        for (Civilization civ : CivGlobal.getCivs()) {
            if (civ.isAdminCiv()) {
                continue;
            }
            civScores.put(civ.getScore(), civ);

            try {
                ScoreManager.UpdateScore(civ, civ.getScore());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        TreeMap<Integer, Town> townScores = new TreeMap<Integer, Town>();
        for (Town town : CivGlobal.getTowns()) {
            if (town.getCiv().isAdminCiv()) {
                continue;
            }
            try {
                townScores.put(town.getScore(), town);
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                ScoreManager.UpdateScore(town, town.getScore());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        synchronized (CivGlobal.civilizationScores) {
            CivGlobal.civilizationScores = civScores;
        }

        synchronized (CivGlobal.townScores) {
            CivGlobal.townScores = townScores;
        }


//		//Save out to file.
//		try {
//			writeCivScores(civScores);
//			writeTownScores(townScores);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
    }

}
