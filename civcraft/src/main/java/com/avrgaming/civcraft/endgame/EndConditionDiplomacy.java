
package com.avrgaming.civcraft.endgame;

import java.sql.SQLException;
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
import com.avrgaming.civcraft.object.Resident;
import com.avrgaming.civcraft.object.Town;
import com.avrgaming.civcraft.structure.wonders.Wonder;
import com.avrgaming.civcraft.war.War;

public class EndConditionDiplomacy
extends EndGameCondition {
    public static boolean check = false;
    public static int vote_cooldown_hours;
    int daysAfterStart;
    Date startDate = null;

    @Override
    public void onLoad() {
        vote_cooldown_hours = Integer.valueOf(this.getString("vote_cooldown_hours"));
        this.daysAfterStart = Integer.valueOf(this.getString("days_after_start"));
        this.getStartDate();
    }

    private void getStartDate() {
        String key = "endcondition:diplomacy:startdate";
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
    public boolean check(Civilization civ) {
        if (!this.isAfterStartupTime()) {
            return false;
        }
        boolean hasCouncil = false;
        for (Town town : civ.getTowns()) {
            if (town.getMotherCiv() != null) continue;
            for (Wonder wonder : town.getWonders()) {
                if (!wonder.isActive() || !wonder.getConfigId().equals("w_council_of_eight")) continue;
                hasCouncil = true;
                break;
            }
            if (!hasCouncil) continue;
            break;
        }
        if (!hasCouncil) {
            return false;
        }
        if (civ.isAdminCiv()) {
            return false;
        }
        if (civ.isConquered()) {
            return false;
        }
        check = true;
        War.time_declare_days = 1;
        return true;
    }

    @Override
    public String getSessionKey() {
        return "endgame:diplomacy";
    }

    @Override
    protected void onWarDefeat(Civilization civ) {
        block2 : for (Town town : civ.getTowns()) {
            if (town.getMotherCiv() != null) continue;
            for (Wonder wonder : town.getWonders()) {
                if (!wonder.getConfigId().equals("w_council_of_eight")) continue;
                if (!wonder.isActive()) continue block2;
                wonder.fancyDestroyStructureBlocks();
                wonder.getTown().removeWonder(wonder);
                try {
                    wonder.delete();
                }
                catch (SQLException e) {
                    e.printStackTrace();
                }
                continue block2;
            }
        }
        EndConditionDiplomacy.deleteAllVotes(civ);
        this.onFailure(civ);
    }

    @Override
    public void onVictoryReset(Civilization civ) {
        EndConditionDiplomacy.deleteAllVotes(civ);
    }

    public static boolean canPeopleVote() {
        for (Wonder wonder : CivGlobal.getWonders()) {
            if (!wonder.isActive() || !wonder.getConfigId().equals("w_council_of_eight")) continue;
            return true;
        }
        return false;
    }

    @Override
    public boolean finalWinCheck(Civilization civ) {
        Integer votes = EndConditionDiplomacy.getVotesFor(civ);
        for (Civilization otherCiv : CivGlobal.getCivs()) {
            Integer otherVotes = EndConditionDiplomacy.getVotesFor(otherCiv);
            if (otherCiv == civ || otherVotes <= votes) continue;
            CivMessage.global(CivSettings.localize.localizedString("var_end_diplomacyError", civ.getName(), otherCiv.getName()));
            return false;
        }
        return true;
    }

    public static String getVoteSessionKey(Civilization civ) {
        return "endgame:diplomacyvote:" + civ.getId();
    }

    public static void deleteAllVotes(Civilization civ) {
        CivGlobal.getSessionDB().delete_all(EndConditionDiplomacy.getVoteSessionKey(civ));
    }

    public static void addVote(Civilization civ, Resident resident) {
        if (!EndConditionDiplomacy.canVoteNow(resident)) {
            return;
        }
        ArrayList<SessionEntry> entries = CivGlobal.getSessionDB().lookup(getVoteSessionKey(civ));
		if (entries.size() == 0) {
			CivGlobal.getSessionDB().add(getVoteSessionKey(civ), ""+1, civ.getId(), 0, 0);
		} else {
			Integer votes = Integer.valueOf(entries.get(0).value);
			votes++;
			CivGlobal.getSessionDB().update(entries.get(0).request_id, entries.get(0).key, ""+votes);			
		}
        CivMessage.sendSuccess(resident, CivSettings.localize.localizedString("var_end_diplomacyAddVote", civ.getName()));
    }

    public static void setVotes(Civilization civ, Integer votes) {
        ArrayList<SessionEntry> entries = CivGlobal.getSessionDB().lookup(EndConditionDiplomacy.getVoteSessionKey(civ));
        if (entries.size() == 0) {
            CivGlobal.getSessionDB().add(EndConditionDiplomacy.getVoteSessionKey(civ), "" + votes, civ.getId(), 0, 0);
        } else {
            CivGlobal.getSessionDB().update(entries.get((int)0).request_id, entries.get((int)0).key, "" + votes);
        }
    }

    public static Integer getVotesFor(Civilization civ) {
        ArrayList<SessionEntry> entries = CivGlobal.getSessionDB().lookup(EndConditionDiplomacy.getVoteSessionKey(civ));
        if (entries.size() == 0) {
            return 0;
        }
        return Integer.valueOf(entries.get((int)0).value);
    }

    private static boolean canVoteNow(Resident resident) {
        String key = "endgame:residentvote:" + resident.getName();
        ArrayList<SessionEntry> entries = CivGlobal.getSessionDB().lookup(key);
        if (entries.size() == 0) {
            CivGlobal.getSessionDB().add(key, "" + new Date().getTime() + "", 0, 0, 0);
            return true;
        }
        Date then = new Date(Long.valueOf(entries.get((int)0).value));
        Date now = new Date();
        if (now.getTime() > then.getTime() + (long)(vote_cooldown_hours * 60 * 60 * 1000)) {
            CivGlobal.getSessionDB().update(entries.get((int)0).request_id, entries.get((int)0).key, "" + now.getTime() + "");
            return true;
        }
        CivMessage.sendError(resident, CivSettings.localize.localizedString("end_diplomacy24hours"));
        return false;
    }
}

