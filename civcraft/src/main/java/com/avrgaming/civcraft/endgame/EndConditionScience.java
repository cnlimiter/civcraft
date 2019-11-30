
package com.avrgaming.civcraft.endgame;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.bukkit.Bukkit;
import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.endgame.EndGameCondition;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.sessiondb.SessionEntry;
import com.avrgaming.civcraft.object.Civilization;
import com.avrgaming.civcraft.object.Town;
import com.avrgaming.civcraft.structure.wonders.Wonder;
import com.avrgaming.civcraft.war.War;

public class EndConditionScience
        extends EndGameCondition {
    public static boolean check = false;
    String wonderId;
    int daysAfterStart;
    Date startDate = null;

    public static String getBeakerSessionKey(Civilization civ) {
        return "endgame:sciencebeakers:" + civ.getId();
    }

    public static Double getBeakersFor(Civilization civ) {
        ArrayList<SessionEntry> entries = CivGlobal.getSessionDB().lookup(EndConditionScience.getBeakerSessionKey(civ));
        if (entries.size() == 0) {
            return 0.0;
        }
        return Double.valueOf(entries.get((int) 0).value);
    }

    @Override
    public void onLoad() {
        this.wonderId = this.getString("wonder");
        this.daysAfterStart = Integer.valueOf(this.getString("days_after_start"));
        this.getStartDate();
    }

    private void getStartDate() {
        String key = "endcondition:science:startdate";
        ArrayList<SessionEntry> entries = CivGlobal.getSessionDB().lookup(key);
        if (entries.size() == 0) {
            this.startDate = new Date();
            CivGlobal.getSessionDB().add(key, "" + this.startDate.getTime(), 0, 0, 0);
        } else {
            long time = Long.valueOf(entries.get((int) 0).value);
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
    public boolean check(Civilization civ) {
        if (!this.isAfterStartupTime()) {
            return false;
        }
        if (civ.isAdminCiv()) {
            return false;
        }
        boolean hasSpaceShuttle = false;
        for (Town town : civ.getTowns()) {
            if (town.getMotherCiv() != null) continue;
            for (Wonder wonder : town.getWonders()) {
                if (!wonder.isActive() || !wonder.getConfigId().equals(this.wonderId)) continue;
                hasSpaceShuttle = true;
                break;
            }
            if (!hasSpaceShuttle) continue;
            break;
        }
        if (!hasSpaceShuttle) {
            return false;
        }
        if (civ.getCurrentMission() < 8) {
            return false;
        }
        check = true;
        War.time_declare_days = 1;
        return true;
    }

    @Override
    public boolean finalWinCheck(Civilization civ) {
        if (civ.getCurrentMission() < 8) {
            return false;
        }
        boolean hasSpaceShuttle = false;
        for (Town town : civ.getTowns()) {
            if (town.getMotherCiv() != null) continue;
            for (Wonder wonder : town.getWonders()) {
                if (!wonder.isActive() || !wonder.getConfigId().equals(this.wonderId)) continue;
                hasSpaceShuttle = true;
                break;
            }
            if (!hasSpaceShuttle) continue;
            break;
        }
        return hasSpaceShuttle;
    }

    @Override
    public String getSessionKey() {
        return "endgame:science";
    }

    @Override
    protected void onWarDefeat(Civilization civ) {
        CivGlobal.getSessionDB().delete_all(EndConditionScience.getBeakerSessionKey(civ));
        CivMessage.sendCiv(civ, CivSettings.localize.localizedString("end_scienceWarDefeat"));
        civ.save();
        this.onFailure(civ);
    }

    public void addExtraBeakersToCiv(Civilization civ, double beakers) {
        ArrayList<SessionEntry> entries = CivGlobal.getSessionDB().lookup(EndConditionScience.getBeakerSessionKey(civ));
        double current = 0.0;
        if (entries.size() == 0) {
            CivGlobal.getSessionDB().add(EndConditionScience.getBeakerSessionKey(civ), "" + beakers, civ.getId(), 0, 0);
        } else {
            current = Double.valueOf(entries.get((int) 0).value);
            CivGlobal.getSessionDB().update(entries.get((int) 0).request_id, entries.get((int) 0).key, "" + (current += beakers));
        }
    }
}

