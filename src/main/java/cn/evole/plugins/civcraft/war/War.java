/**
 * [2023] Cnlimiter LLC
 * All Rights Reserved.
 * Name: civcraft
 * Author: cnlimiter
 * UpdateTime: 2023/10/14 16:15
 * Description:
 * License: ARR
 */
package cn.evole.plugins.civcraft.war;

import cn.evole.plugins.civcraft.camp.WarCamp;
import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.endgame.EndGameCondition;
import cn.evole.plugins.civcraft.event.DisableTeleportEvent;
import cn.evole.plugins.civcraft.event.EventTimer;
import cn.evole.plugins.civcraft.exception.InvalidConfiguration;
import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.main.CivLog;
import cn.evole.plugins.civcraft.main.CivMessage;
import cn.evole.plugins.civcraft.object.Civilization;
import cn.evole.plugins.civcraft.object.Relation;
import cn.evole.plugins.civcraft.object.Town;
import cn.evole.plugins.civcraft.sessiondb.SessionEntry;
import cn.evole.plugins.civcraft.siege.Cannon;
import cn.evole.plugins.civcraft.util.CivColor;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class War {

    public static int time_declare_days = 3;
    /* If true, WarTime is on. */
    private static boolean warTime;
    private static Date start = null;
    private static Date end = null;

    private static boolean onlyWarriors = false;

    private static HashMap<String, Civilization> defeatedTowns = new HashMap<String, Civilization>();
    private static HashMap<String, Civilization> defeatedCivs = new HashMap<String, Civilization>();

    public static void saveDefeatedTown(String townName, Civilization master) {
        defeatedTowns.put(townName, master);
        /* Save in the SessionDB just in case the server goes down. */
        String key = "capturedTown";
        String value = townName + ":" + master.getId();

        CivGlobal.getSessionDB().add(key, value, master.getId(), 0, 0);
    }

    public static void saveDefeatedCiv(Civilization defeated, Civilization master) {
        defeatedCivs.put(defeated.getName(), master);
        /* Save in the SessionDB just in case the server goes down. */
        String key = "capturedCiv";
        String value = defeated.getName() + ":" + master.getId();

        EndGameCondition.onCivilizationWarDefeat(defeated);
        CivGlobal.getSessionDB().add(key, value, master.getId(), 0, 0);
    }

    /*
     * Rebuild the defeated civs session db vars when a transfer of defeated towns occurs.
     */
    public static void resaveAllDefeatedCivs() {
        CivGlobal.getSessionDB().delete_all("capturedCiv");

        for (String civName : defeatedCivs.keySet()) {
            Civilization master = defeatedCivs.get(civName);
            saveDefeatedCiv(CivGlobal.getCiv(civName), master);
        }
    }

    public static void resaveAllDefeatedTowns() {
        CivGlobal.getSessionDB().delete_all("capturedTown");

        for (String townName : defeatedTowns.keySet()) {
            Civilization master = defeatedTowns.get(townName);
            saveDefeatedTown(townName, master);
        }
    }

    public static void loadDefeatedTowns() {
        ArrayList<SessionEntry> entries = CivGlobal.getSessionDB().lookup("capturedTown");

        for (SessionEntry entry : entries) {
            String[] split = entry.value.split(":");
            defeatedTowns.put(split[0], CivGlobal.getCivFromId(Integer.parseInt(split[1])));
        }
    }

    public static void loadDefeatedCivs() {
        ArrayList<SessionEntry> entries = CivGlobal.getSessionDB().lookup("capturedCiv");

        for (SessionEntry entry : entries) {
            String[] split = entry.value.split(":");
            defeatedCivs.put(split[0], CivGlobal.getCivFromId(Integer.parseInt(split[1])));
        }
    }

    public static void clearSavedDefeats() {
        CivGlobal.getSessionDB().delete_all("capturedTown");
        CivGlobal.getSessionDB().delete_all("capturedCiv");
    }

    public static void init() {
        try {
            time_declare_days = CivSettings.getInteger(CivSettings.warConfig, "war.time_declare_days");
        } catch (InvalidConfiguration e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        loadDefeatedTowns();
        loadDefeatedCivs();
        processDefeated();
    }

    /**
     * @return the warTime
     */
    public static boolean isWarTime() {
        return warTime;
    }

    /**
     * @param warTime the warTime to set
     */
    public static void setWarTime(boolean warTime) {

        if (warTime && !War.hasWars()) {

            CivMessage.globalHeading(CivColor.BOLD + CivSettings.localize.localizedString("war_wartimeSkippedHeading"));
            try {
                DisableTeleportEvent.enableTeleport();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
        if (!warTime) {
            try {
                DisableTeleportEvent.enableTeleport();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            /* War time has ended. */
            War.setStart(null);
            War.setEnd(null);
            War.restoreAllTowns();
            War.repositionPlayers(CivSettings.localize.localizedString("war_repositionMessage"));
            War.processDefeated();

            CivGlobal.growthEnabled = true;
            CivGlobal.trommelsEnabled = true;
            CivGlobal.quarriesEnabled = true;
            CivGlobal.tradeEnabled = true;
            CivGlobal.fisheryEnabled = true;

            /* Delete any wartime file used to prevent reboots. */
            File file = new File("wartime");
            file.delete();

            CivMessage.globalTitle(CivColor.Yellow + CivColor.BOLD + CivSettings.localize.localizedString("war_wartimeEndedHeading"), CivSettings.localize.localizedString("var_war_mostLethal", WarStats.getTopKiller()));
            /* display some stats. */
            List<String> civs = WarStats.getCapturedCivs();
            if (civs.size() > 0) {
                for (String str : civs) {
                    CivMessage.global(str);
                }
            }
            WarStats.clearStats();

            for (Civilization civ : CivGlobal.getCivs()) {
                civ.onWarEnd();
            }

        } else {
            try {
                DisableTeleportEvent.disableTeleport();
            } catch (IOException e2) {
                // TODO Auto-generated catch block
                e2.printStackTrace();
            }

            int mins = 0;
            try {
                mins = CivSettings.getInteger(CivSettings.warConfig, "war.time_length");
            } catch (InvalidConfiguration e2) {
                // TODO Auto-generated catch block
                e2.printStackTrace();
            }

            /* War time has started. */
            CivMessage.globalTitle(CivColor.Red + CivColor.BOLD + CivSettings.localize.localizedString("war_wartimeBeginHeading"), CivSettings.localize.localizedString("war_wartimeBegin_title_length", mins / 60.0));
            War.setStart(new Date());
            War.repositionPlayers(CivSettings.localize.localizedString("war_wartimeBeginOutOfPosition"));
            //War.vassalTownsWithNoTownHalls();
            War.resetTownClaimFlags();

            /* Put a flag on the filesystem to prevent cron reboots. */
            File file = new File("wartime");
            try {
                file.createNewFile();
            } catch (IOException e1) {
                e1.printStackTrace();
            }

            CivGlobal.growthEnabled = false;
            CivGlobal.trommelsEnabled = false;
            CivGlobal.quarriesEnabled = false;
            CivGlobal.tradeEnabled = false;
            CivGlobal.fisheryEnabled = false;

            Calendar endCal = Calendar.getInstance();
            endCal.add(Calendar.MINUTE, mins);

            War.setEnd(endCal.getTime());

        }

        War.warTime = warTime;
    }

    public static boolean hasWars() {

        for (Civilization civ : CivGlobal.getCivs()) {
            for (Relation relation : civ.getDiplomacyManager().getRelations()) {
                if (relation.getStatus().equals(Relation.Status.WAR)) {
                    return true;
                }
            }
        }
        return false;
    }

    /*
     * When a civ conqueres a town, then has its capitol conquered,
     * the town that it just conquered needs to go to the new owner.
     * If the new owner was the town's old owner, then that town is no longer
     * defeated.
     * 当公民征服城镇时，便征服了国会大厦，刚刚征服的城镇需要交给新主人。 如果新所有者是该镇的旧所有者，则该镇不再被击败。
     */
    public static void transferDefeated(Civilization loser, Civilization winner) {

        /* Transfer any defeated towns */
        ArrayList<String> removeUs = new ArrayList<>();

        for (String townName : defeatedTowns.keySet()) {
            Civilization civ = defeatedTowns.get(townName);
            if (civ == loser) {
                Town town = CivGlobal.getTown(townName);
                if (town.getCiv() == winner) {
                    removeUs.add(townName);
                } else {
                    defeatedTowns.put(townName, winner);
                }
            }
        }

        for (String townName : removeUs) {
            defeatedTowns.remove(townName);
        }
        resaveAllDefeatedTowns();

        /* Transfer any defeated civs */
        for (String civName : defeatedCivs.keySet()) {
            Civilization civ = defeatedCivs.get(civName);

            /* Defeated civs should never be our own, always transfer. */
            if (civ == loser) {
                defeatedCivs.put(civName, winner);
            }
        }
        resaveAllDefeatedCivs();

    }

    private static void processDefeated() {

        if (!CivGlobal.isCasualMode()) {
            for (String townName : defeatedTowns.keySet()) {
                try {
                    Town town = CivGlobal.getTown(townName);
                    if (town == null) {
                        continue;
                    }

                    Civilization winner = defeatedTowns.get(townName);

                    town.onDefeat(winner);
                    CivMessage.sendTown(town, CivColor.LightBlue + CivSettings.localize.localizedString("var_war_overlordAnnounce", winner.getName()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            for (String civName : defeatedCivs.keySet()) {
                try {
                    Civilization civ = CivGlobal.getCiv(civName);
                    if (civ == null) {
                        /*
                         * Civ is no longer on the list of active civs. Which means it's
                         * already been conquered?
                         */
                        CivLog.error("Couldn't find civilization named " + civName + " in civ hash table while trying to process it's defeat.");
                        continue;
                    }

                    Civilization winner = defeatedCivs.get(civName);
                    CivMessage.sendCiv(civ, CivColor.LightBlue + CivColor.LightBlue + CivSettings.localize.localizedString("var_war_overlordAnnounce", winner.getName()));
                    civ.onDefeat(winner);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            /* Casual Mode */

            /* Nothing happens for defeated towns. */

            /* Defeated Civs cause the war to 'reset' */
            for (String civName : defeatedCivs.keySet()) {
                try {
                    Civilization civ = CivGlobal.getCiv(civName);
                    if (civ == null) {
                        /*
                         * Civ is no longer on the list of active civs. Which means it's
                         * already been conquered?
                         */
                        CivLog.error("Couldn't find civilization named " + civName + " in civ hash table while trying to process it's defeat.");
                        continue;
                    }

                    Civilization winner = defeatedCivs.get(civName);
                    CivMessage.global(CivSettings.localize.localizedString("var_war_defeatedMsg1", winner, civ));
                    CivGlobal.setRelation(winner, civ, Relation.Status.NEUTRAL);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        defeatedTowns.clear();
        defeatedCivs.clear();
        clearSavedDefeats();
    }

    /*
     * The 'claimed' flag is used to tag towns that had illegal town hall placements, and have already been claimed.
     * When war time starts, we should reset the claim flag so it can be claimed by someone else.
     * “claimed”标志用于标记拥有非法市政厅位置且已经被声明的城镇。 战时开始时，我们应该重置索赔标志，以便其他人可以索赔。
     */
    private static void resetTownClaimFlags() {
        for (Town t : CivGlobal.getTowns()) {
            t.claimed = false;
            t.defeated = false;
        }
    }

    private static void repositionPlayers(String reason) {
        for (Civilization civ : CivGlobal.getCivs()) {
            try {
                civ.repositionPlayers(reason);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void restoreAllTowns() {
        for (Town town : CivGlobal.getTowns()) {
            try {
                WarRegen.restoreBlocksFor(town.getName());
                if (town.getTownHall() != null && town.getTownHall().isActive()) {
                    town.getTownHall().regenControlBlocks();
                }
            } catch (Exception e) {
                CivLog.error("Error encountered while restoring town:" + town.getName());
                e.printStackTrace();
            }
        }

        WarRegen.restoreBlocksFor(WarCamp.RESTORE_NAME);
        WarRegen.restoreBlocksFor(Cannon.RESTORE_NAME);
        WarRegen.restoreBlocksFor(WarListener.RESTORE_NAME);
        Cannon.cleanupAll();
    }

    /**
     * @return the start
     */
    public static Date getStart() {
        return start;
    }


    /**
     * @param start the start to set
     */
    public static void setStart(Date start) {
        War.start = start;
    }

    /**
     * @return the end
     */
    public static Date getEnd() {
        return end;
    }

    /**
     * @param end the end to set
     */
    public static void setEnd(Date end) {
        War.end = end;
    }

    public static Date getNextWarTime() {
        EventTimer warTimer = EventTimer.timers.get("war");
        return warTimer.getNext().getTime();
    }

    public static boolean isOnlyWarriors() {
        return onlyWarriors;
    }

    public static void setOnlyWarriors(boolean onlyWarriors) {
        War.onlyWarriors = onlyWarriors;
    }

    public static boolean isWithinWarDeclareDays() {
        Date nextWar = War.getNextWarTime();
        Date now = new Date();

        if ((now.getTime() + time_declare_days * (1000 * 60 * 60 * 24)) >= nextWar.getTime()) {
            return true;
        }

        return false;
    }

    public static boolean isWithinAllyDeclareHours() {
        Date nextWar = War.getNextWarTime();
        int ally_declare_hours = getAllyDeclareHours();

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR_OF_DAY, ally_declare_hours);

        if (cal.getTime().after(nextWar)) {
            return true;
        }

        return false;
    }

    public static int getAllyDeclareHours() {
        try {
            int ally_decare_hours = CivSettings.getInteger(CivSettings.warConfig, "war.ally_declare_hours");
            return ally_decare_hours;
        } catch (InvalidConfiguration e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static boolean isCivAggressor(Civilization civ) {
        for (Relation relation : civ.getDiplomacyManager().getRelations()) {
            if (relation.getStatus() == Relation.Status.WAR) {
                if (relation.getAggressor() == civ) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isCivAggressorToAlly(Civilization enemy, Civilization ourCiv) {
        for (Relation relation : ourCiv.getDiplomacyManager().getRelations()) {
            if (relation.getStatus() == Relation.Status.ALLY) {
                Civilization ally = relation.getOtherCiv();
                Relation allyRelation = ally.getDiplomacyManager().getRelation(enemy);
                if (allyRelation == null) {
                    continue;
                }

                if (allyRelation.getStatus() == Relation.Status.WAR && (allyRelation.getAggressor() == enemy)) {
                    return true;
                }
            }
        }
        return false;
    }

}
