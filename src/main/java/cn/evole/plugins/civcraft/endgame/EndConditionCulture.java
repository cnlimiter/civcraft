package cn.evole.plugins.civcraft.endgame;

import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.object.Civilization;
import cn.evole.plugins.civcraft.object.Town;
import cn.evole.plugins.civcraft.sessiondb.SessionEntry;
import cn.evole.plugins.civcraft.structure.wonders.Wonder;
import cn.evole.plugins.civcraft.war.War;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * 文化胜利
 */
public class EndConditionCulture extends EndGameCondition {
    public static boolean check = false;
    int daysAfterStart;
    Date startDate = null;

    @Override
    public void onLoad() {
        this.daysAfterStart = Integer.parseInt(this.getString("days_after_start"));
        this.getStartDate();
    }

    private void getStartDate() {
        String key = "endcondition:culture:startdate";
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
        if (Bukkit.getServerName().equalsIgnoreCase("Test")) {
            return true;
        }
        Calendar startCal = Calendar.getInstance();
        startCal.setTime(this.startDate);
        Calendar now = Calendar.getInstance();
        startCal.add(Calendar.DATE, this.daysAfterStart);
        return now.after(startCal);
    }

    @Override
    public String getSessionKey() {
        return "endgame:cultural";
    }

    @Override
    public boolean check(Civilization civ) {
        if (!this.isAfterStartupTime()) {
            return false;
        }
        if (civ.isConquered()) {
            return false;
        }
        boolean hasBurj = false;
        for (Town town : civ.getTowns()) {
            if (town.getMotherCiv() != null) continue;
            for (Wonder wonder : town.getWonders()) {
                if (!wonder.isActive() || !wonder.getConfigId().equals("w_burj")) continue;
                hasBurj = true;
                break;
            }
            if (!hasBurj) continue;
            break;
        }
        if (!hasBurj) {
            return false;
        }
        int cultureCount = 0;
        for (Town town : civ.getTowns()) {
            if (town.getMotherCiv() != null) continue;
            cultureCount += town.getAccumulatedCulture();
        }
        if (cultureCount < 16500000) {
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

