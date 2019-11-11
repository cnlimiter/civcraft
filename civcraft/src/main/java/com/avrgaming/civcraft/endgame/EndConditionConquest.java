
package com.avrgaming.civcraft.endgame;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import org.bukkit.Bukkit;
import com.avrgaming.civcraft.endgame.EndGameCondition;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivLog;
import com.avrgaming.civcraft.sessiondb.SessionEntry;
import com.avrgaming.civcraft.object.Civilization;
import com.avrgaming.civcraft.object.Town;
import com.avrgaming.civcraft.structure.wonders.Wonder;
import com.avrgaming.civcraft.war.War;

public class EndConditionConquest
extends EndGameCondition {
    public static boolean check = false;
    int daysAfterStart;
    double percentCaptured;
    double percentCapturedWithWonder;
    Date startDate = null;

    @Override
    public void onLoad() {
        this.daysAfterStart = Integer.valueOf(this.getString("days_after_start"));
        this.percentCaptured = Double.valueOf(this.getString("percent_captured"));
        this.percentCapturedWithWonder = Double.valueOf(this.getString("percent_captured_with_wonder"));
        this.getStartDate();
    }

    private void getStartDate() {
        String key = "endcondition:conquest:startdate";
        ArrayList<SessionEntry> entries = CivGlobal.getSessionDB().lookup(key);
        if (entries.isEmpty()) {
            this.startDate = new Date();
            CivGlobal.getSessionDB().add(key, "" + this.startDate.getTime(), 0, 0, 0);
        } else {
            long time = Long.valueOf(entries.get((int)0).value);
            this.startDate = new Date(time);
        }
    }

    private boolean isAfterStartupTime() {
        if (Bukkit.getServerName().equalsIgnoreCase("Test")) {
            return true;
        }
        Calendar startCal = Calendar.getInstance();
        startCal.setTime(this.startDate);
        Calendar now = Calendar.getInstance();
        startCal.add(5, this.daysAfterStart);
        return now.after(startCal);
    }

    @Override
    public String getSessionKey() {
        return "endgame:conquer";
    }

    @Override
    public boolean check(Civilization civ) {
        if (!this.isAfterStartupTime()) {
            return false;
        }
        boolean hasChichenItza = false;
        for (Town town : civ.getTowns()) {
            if (town.getMotherCiv() != null) continue;
            for (Wonder wonder : town.getWonders()) {
                if (!wonder.isActive() || !wonder.getConfigId().equals("w_chichen_itza")) continue;
                hasChichenItza = true;
                break;
            }
            if (!hasChichenItza) continue;
            break;
        }
        int towncountglobal = 0;
        int towncountciv = 0;
        for (Town town : CivGlobal.getTowns()) {
            if (town.getName() != null) {
                ++towncountglobal;
                continue;
            }
            CivLog.debug("Town with number " + towncountglobal + " = null. Sure ?\nEndCondition Class");
        }
        for (Town town : civ.getTowns()) {
            if (town.getName() != null) {
                ++towncountciv;
                continue;
            }
            CivLog.debug("Town with number " + towncountciv + " = null. Sure ?\nEndCondition Class");
        }
        double captured = (double)towncountciv / (double)towncountglobal;
        if (!hasChichenItza) {
            if (captured < this.percentCaptured) {
                CivLog.debug("No towns for condition win " + captured + " needed:" + this.percentCaptured);
                return false;
            }
        } else if (captured < this.percentCapturedWithWonder) {
            CivLog.debug("No towns for condition win " + captured + " needed:" + this.percentCapturedWithWonder);
        }
        if (civ.isConquered()) {
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

