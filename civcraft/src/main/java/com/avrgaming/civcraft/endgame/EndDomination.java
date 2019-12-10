
package com.avrgaming.civcraft.endgame;

import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.object.Civilization;
import com.avrgaming.civcraft.object.Town;
import com.avrgaming.civcraft.sessiondb.SessionEntry;
import com.avrgaming.civcraft.structure.wonders.Wonder;
import com.avrgaming.civcraft.war.War;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TreeMap;

/**
 * 统战胜利
 */
public class EndDomination extends EndGameCondition {
    public static boolean check = false;
    private int daysAfterStart;
    private int timesMore;
    private Date startDate = null;

    @Override
    public void onLoad() {
        this.daysAfterStart = Integer.parseInt(this.getString("days_after_start"));
        this.timesMore = Integer.parseInt(this.getString("timesMore"));
        this.getStartDate();
    }

    private void getStartDate() {
        String key = "endcondition:domination:startdate";
        ArrayList<SessionEntry> entries = CivGlobal.getSessionDB().lookup(key);
        if (entries.isEmpty()) {
            this.startDate = new Date();
            CivGlobal.getSessionDB().add(key, "" + this.startDate.getTime(), 0, 0, 0);
        } else {
            long time = Long.parseLong(entries.get((int) 0).value);
            this.startDate = new Date(time);
        }
    }

    private boolean isAfterStartupTime() {
        Calendar startCal = Calendar.getInstance();
        startCal.setTime(this.startDate);
        Calendar now = Calendar.getInstance();
        startCal.add(5, this.daysAfterStart);
        return now.after(startCal);
    }

    @Override
    public String getSessionKey() {
        return "endgame:domination";
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * 删除尝试自行捕获-可能的行为更改。
     */
    @Override
    public boolean check(Civilization civ) {
        Civilization top1;
        Civilization top2;
        if (!this.isAfterStartupTime()) {
            return false;
        }
        TreeMap<Integer, Civilization> treeMap = CivGlobal.civilizationScores;
        synchronized (treeMap) {
            top1 = (Civilization) CivGlobal.civilizationScores.values().toArray()[0];
            top2 = (Civilization) CivGlobal.civilizationScores.values().toArray()[1];
        }
        int diff = top1.getScore() / top2.getScore();
        if (diff < 9) {
            return false;
        }
        int wonderCount = 0;
        for (Town town : top1.getTowns()) {
            for (Wonder wonder : town.getWonders()) {
                if (wonder.getConfigId().equalsIgnoreCase("w_space_shuttle")
                        || wonder.getConfigId().equalsIgnoreCase("w_colosseum")
                        || wonder.getConfigId().equalsIgnoreCase(""))
                    continue;
                ++wonderCount;
            }
        }
        if (wonderCount < 6) {
            return false;
        }
        check = true;
        War.time_declare_days = 1;
        return true;
    }

    @Override
    protected void onWarDefeat(Civilization civ) {
        this.onFailure(civ);
    }
}

