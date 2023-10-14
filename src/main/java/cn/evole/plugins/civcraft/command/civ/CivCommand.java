/**
 * [2023] Cnlimiter LLC
 * All Rights Reserved.
 * Name: civcraft
 * Author: cnlimiter
 * UpdateTime: 2023/10/14 16:15
 * Description:
 * License: ARR
 */
package cn.evole.plugins.civcraft.command.civ;

import cn.evole.plugins.civcraft.command.CommandBase;
import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.endgame.EndConditionDiplomacy;
import cn.evole.plugins.civcraft.endgame.EndGameCondition;
import cn.evole.plugins.civcraft.exception.CivException;
import cn.evole.plugins.civcraft.exception.InvalidConfiguration;
import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.main.CivMessage;
import cn.evole.plugins.civcraft.object.Civilization;
import cn.evole.plugins.civcraft.object.Relation;
import cn.evole.plugins.civcraft.object.Resident;
import cn.evole.plugins.civcraft.object.Town;
import cn.evole.plugins.civcraft.sessiondb.SessionEntry;
import cn.evole.plugins.civcraft.structure.TownHall;
import cn.evole.plugins.civcraft.util.CivColor;
import cn.evole.plugins.civcraft.war.War;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

public class CivCommand extends CommandBase {

    @Override
    public void init() {
        command = "/civ";
        displayName = CivSettings.localize.localizedString("cmd_civ_name");

        commands.put("townlist", CivSettings.localize.localizedString("cmd_civ_townlistDesc"));
        commands.put("deposit", CivSettings.localize.localizedString("cmd_civ_depositDesc"));
        commands.put("withdraw", CivSettings.localize.localizedString("cmd_civ_withdrawDesc"));
        commands.put("info", CivSettings.localize.localizedString("cmd_civ_infoDesc"));
        commands.put("show", CivSettings.localize.localizedString("cmd_civ_showDesc"));
        commands.put("list", CivSettings.localize.localizedString("cmd_civ_listDesc"));
        commands.put("research", CivSettings.localize.localizedString("cmd_civ_researchDesc"));
        commands.put("gov", CivSettings.localize.localizedString("cmd_civ_govDesc"));
        commands.put("time", CivSettings.localize.localizedString("cmd_civ_timeDesc"));
        commands.put("set", CivSettings.localize.localizedString("cmd_civ_setDesc"));
        commands.put("group", CivSettings.localize.localizedString("cmd_civ_groupDesc"));
        commands.put("dip", CivSettings.localize.localizedString("cmd_civ_dipDesc"));
        commands.put("victory", CivSettings.localize.localizedString("cmd_civ_victoryDesc"));
        commands.put("vote", CivSettings.localize.localizedString("cmd_civ_voteDesc"));
        commands.put("votes", CivSettings.localize.localizedString("cmd_civ_votesDesc"));
        commands.put("top5", CivSettings.localize.localizedString("cmd_civ_top5Desc"));
        commands.put("disbandtown", CivSettings.localize.localizedString("cmd_civ_disbandtownDesc"));
        commands.put("revolution", CivSettings.localize.localizedString("cmd_civ_revolutionDesc"));
        commands.put("claimleader", CivSettings.localize.localizedString("cmd_civ_claimleaderDesc"));
        commands.put("motd", CivSettings.localize.localizedString("cmd_civ_motdDesc"));
        commands.put("location", CivSettings.localize.localizedString("cmd_civ_locationDesc"));
        commands.put("members", CivSettings.localize.localizedString("cmd_civ_membersDesc"));
        commands.put("talent", CivSettings.localize.localizedString("cmd_civ_talentDesc"));
        commands.put("space", CivSettings.localize.localizedString("cmd_civ_space_name"));
        commands.put("trade", CivSettings.localize.localizedString("cmd_civ_trade_name"));
        commands.put("culture", CivSettings.localize.localizedString("cmd_civ_culture_name"));
    }

    public void culture_cmd() throws CivException {
        final Resident resident = this.getResident();
        final Civilization civ = this.getSenderCiv();
        if (!civ.getLeaderGroup().hasMember(resident) && !civ.getAdviserGroup().hasMember(resident)) {
            throw new CivException(CivSettings.localize.localizedString("cmd_civ_culture_notLeader"));
        }
        boolean hasBurj = false;
        int cultureSummary = 0;
        for (final Town town : civ.getTowns()) {
            if (town.getMotherCiv() == null) {
                cultureSummary += town.getAccumulatedCulture();
            }
            if (town.hasWonder("w_burj")) {
                hasBurj = true;
            }
        }
        final boolean culturePassed = cultureSummary > 16500000;
        CivMessage.sendHeading(this.sender, CivSettings.localize.localizedString("cmd_civ_culture_heading"));
        if (culturePassed && hasBurj) {
            CivMessage.sendSuccess(this.sender, CivSettings.localize.localizedString("cmd_civ_culture_allConditionsPassed"));
            return;
        }
        final int summary = 16500000 - cultureSummary;
        if (!culturePassed) {
            CivMessage.send(this.sender, "§2" + CivSettings.localize.localizedString("cmd_civ_culture_cultureRequired", CivColor.LightGreenBold + summary + "§2"));
        }
        if (!hasBurj) {
            CivMessage.send(this.sender, "§2" + CivSettings.localize.localizedString("cmd_civ_culture_burjRequired", CivColor.LightGreenBold + "Burj Kalifa"));
        }
    }

    public void trade_cmd() throws CivException {
        final CivTradeCommand cmd = new CivTradeCommand();
        cmd.onCommand(this.sender, null, "trade", this.stripArgs(this.args, 1));
    }

    public void space_cmd() {
        final CivSpaceCommand cmd = new CivSpaceCommand();
        cmd.onCommand(this.sender, null, "space", this.stripArgs(this.args, 1));
    }

    public void talent_cmd() {
        final CivTalentCommand cmd = new CivTalentCommand();
        cmd.onCommand(this.sender, null, "talent", this.stripArgs(this.args, 1));
    }

    public void members_cmd() throws CivException {
        final Civilization civ = this.getSenderCiv();
        String out = "";
        CivMessage.sendHeading(this.sender, CivSettings.localize.localizedString("var_civ_membersHeading", civ.getName()));
        for (final Town t : civ.getTowns()) {
            for (Resident r : t.getResidents()) {
                out += r.getName() + ", ";
            }
        }
        CivMessage.send(this.sender, out);
    }

    public void location_cmd() throws CivException {
        Civilization civ = getSenderCiv();
        Resident resident = getResident();
        if (resident.getCiv() == civ) {
            for (Town town : civ.getTowns()) {
                String name = town.getName();
                TownHall townhall = town.getTownHall();
                if (townhall == null) {
                    CivMessage.send(sender, CivColor.Rose + CivColor.BOLD + name + CivColor.RESET + CivColor.Gray + CivSettings.localize.localizedString("cmd_civ_locationMissingTownHall"));
                } else {
                    CivMessage.send(sender, CivColor.Rose + CivColor.BOLD + name + CivColor.LightPurple + " - " + CivSettings.localize.localizedString("cmd_civ_locationSuccess") + " " + townhall.getCorner());
                }
            }
        }
    }

    public void motd_cmd() throws CivException {
        CivMotdCommand cmd = new CivMotdCommand();
        cmd.onCommand(sender, null, "motd", this.stripArgs(args, 1));
    }

    public void claimleader_cmd() throws CivException {
        Civilization civ = getSenderCiv();
        Resident resident = getResident();

        if (!civ.areLeadersInactive()) {
            throw new CivException(CivSettings.localize.localizedString("cmd_civ_claimleaderStillActive"));
        }

        civ.getLeaderGroup().addMember(resident);
        civ.getLeaderGroup().save();
        CivMessage.sendSuccess(sender, CivSettings.localize.localizedString("var_cmd_civ_claimLeaderSuccess", civ.getName()));
        CivMessage.sendCiv(civ, CivSettings.localize.localizedString("var_cmd_civ_claimLeaderBroadcast", resident.getName()));
    }

    public void votes_cmd() throws CivException {

        CivMessage.sendHeading(sender, CivSettings.localize.localizedString("cmd_civ_votesHeading"));
        for (Civilization civ : CivGlobal.getCivs()) {
            Integer votes = EndConditionDiplomacy.getVotesFor(civ);
            if (votes != 0) {
                CivMessage.send(sender, CivColor.LightBlue +
                        CivColor.BOLD + civ.getName() + CivColor.White + ": " +
                        CivColor.LightPurple + CivColor.BOLD + votes + CivColor.White + " " + CivSettings.localize.localizedString("cmd_civ_votes"));
            }
        }
    }

    public void victory_cmd() {

        CivMessage.sendHeading(sender, CivSettings.localize.localizedString("cmd_civ_victoryHeading"));
        boolean anybody = false;

        for (EndGameCondition endCond : EndGameCondition.endConditions) {
            ArrayList<SessionEntry> entries = CivGlobal.getSessionDB().lookup(endCond.getSessionKey());
            if (entries.size() == 0) {
                continue;
            }

            anybody = true;
            for (SessionEntry entry : entries) {
                Civilization civ = EndGameCondition.getCivFromSessionData(entry.value);
                Integer daysLeft = endCond.getDaysToHold() - endCond.getDaysHeldFromSessionData(entry.value);
                CivMessage.send(sender, CivColor.LightBlue + CivColor.BOLD + civ.getName() + CivColor.White + ": " +
                        CivSettings.localize.localizedString("var_cmd_civ_victoryDays", (CivColor.Yellow + CivColor.BOLD + daysLeft + CivColor.White), (CivColor.LightPurple + CivColor.BOLD + endCond.getVictoryName() + CivColor.White)));
            }
        }

        if (!anybody) {
            CivMessage.send(sender, CivColor.LightGray + CivSettings.localize.localizedString("cmd_civ_victoryNoOne"));
        }

    }

    public void revolution_cmd() throws CivException {
        Town town = getSelectedTown();

        if (War.isWarTime() || War.isWithinWarDeclareDays()) {
            throw new CivException(CivSettings.localize.localizedString("var_cmd_civ_revolutionErrorWar1", War.time_declare_days));
        }

        if (town.getMotherCiv() == null) {
            throw new CivException(CivSettings.localize.localizedString("cmd_civ_revolutionErrorNoMother"));
        }

        Civilization motherCiv = town.getMotherCiv();

        if (!motherCiv.getCapitolName().equals(town.getName())) {
            throw new CivException(CivSettings.localize.localizedString("var_cmd_civ_revolutionErrorNotCapitol", motherCiv.getCapitolName()));
        }

        if (town.getCiv().getCapitol() != null && town.getCiv().getCapitol().getBuffManager().hasBuff("level10_dominatorTown")) {
            throw new CivException(CivSettings.localize.localizedString("var_cmd_civ_revolutionErrorDominator", town.getCiv().getName()));
        }
        try {
            int revolution_cooldown = CivSettings.getInteger(CivSettings.civConfig, "civ.revolution_cooldown");

            Calendar cal = Calendar.getInstance();
            Calendar revCal = Calendar.getInstance();

            Date conquered = town.getMotherCiv().getConqueredDate();
            if (conquered == null) {
                throw new CivException(CivSettings.localize.localizedString("cmd_civ_revolutionErrorNoMother"));
            }

            revCal.setTime(town.getMotherCiv().getConqueredDate());
            revCal.add(Calendar.DAY_OF_MONTH, revolution_cooldown);

            if (!cal.after(revCal)) {
                throw new CivException(CivSettings.localize.localizedString("var_cmd_civ_revolutionErrorTooSoon", revolution_cooldown));
            }

        } catch (InvalidConfiguration e) {
            e.printStackTrace();
            throw new CivException(CivSettings.localize.localizedString("internalException"));
        }


        double revolutionFee = motherCiv.getRevolutionFee();

        if (args.length < 2 || !args[1].equalsIgnoreCase("yes")) {
            CivMessage.send(sender, CivColor.Yellow + ChatColor.BOLD + CivSettings.localize.localizedString("var_cmd_civ_revolutionConfirm1", revolutionFee, CivSettings.CURRENCY_NAME));
            CivMessage.send(sender, CivColor.Yellow + ChatColor.BOLD + CivSettings.localize.localizedString("cmd_civ_revolutionConfirm2"));
            CivMessage.send(sender, CivColor.Yellow + ChatColor.BOLD + CivSettings.localize.localizedString("cmd_civ_revolutionConfirm3"));
            CivMessage.send(sender, CivColor.LightGreen + CivSettings.localize.localizedString("cmd_civ_revolutionConfirm4"));
            return;
        }

        if (!town.getTreasury().hasEnough(revolutionFee)) {
            throw new CivException(CivSettings.localize.localizedString("var_cmd_civ_revolutionErrorTooPoor", revolutionFee, CivSettings.CURRENCY_NAME));
        }

        /* Starting a revolution! Give back all of our towns to us. */
        HashSet<String> warCivs = new HashSet<String>();
        for (Town t : CivGlobal.getTowns()) {
            if (t.getMotherCiv() == motherCiv) {
                warCivs.add(t.getCiv().getName());
                t.changeCiv(motherCiv);
                t.setMotherCiv(null);
                t.save();
            }
        }

        for (String warCivName : warCivs) {
            Civilization civ = CivGlobal.getCiv(warCivName);
            if (civ != null) {
                CivGlobal.setRelation(civ, motherCiv, Relation.Status.WAR);
                /* THEY are the aggressor in a revolution. */
                CivGlobal.setAggressor(civ, motherCiv, civ);
            }
        }

        motherCiv.setConquered(false);
        CivGlobal.removeConqueredCiv(motherCiv);
        CivGlobal.addCiv(motherCiv);
        motherCiv.save();


        town.getTreasury().withdraw(revolutionFee);
        CivMessage.global(CivColor.Yellow + ChatColor.BOLD + CivSettings.localize.localizedString("var_cmd_civ_revolutionSuccess1", motherCiv.getName()));

    }

    public void disbandtown_cmd() throws CivException {
        this.validLeaderAdvisor();
        Town town = this.getNamedTown(1);

        if (town.getMotherCiv() != null) {
            throw new CivException(CivSettings.localize.localizedString("cmd_civ_disbandtownError"));
        }

        if (town.leaderWantsToDisband) {
            town.leaderWantsToDisband = false;
            CivMessage.send(sender, CivSettings.localize.localizedString("cmd_civ_disbandtownErrorLeader"));
            return;
        }

        town.leaderWantsToDisband = true;

        if (town.leaderWantsToDisband && town.mayorWantsToDisband) {
            CivMessage.sendCiv(town.getCiv(), CivSettings.localize.localizedString("var_cmd_civ_disbandtownSuccess", town.getName()));
            town.disband();
        }

        CivMessage.send(sender, CivColor.Yellow + CivSettings.localize.localizedString("cmd_civ_disbandtownPrompt"));
    }

    public void top5_cmd() {
        CivMessage.sendHeading(sender, CivSettings.localize.localizedString("cmd_civ_top5Heading"));
        ArrayList<SessionEntry> entries = CivGlobal.getSessionDB().lookup("endgame:winningCiv");
        int i = 1, v = 0;
        if (entries.size() != 0) {
            CivMessage.sendHeading(sender, CivSettings.localize.localizedString("cmd_civ_VictoriesHeading"));
            for (SessionEntry se : entries) {
                Civilization civ = EndGameCondition.getCivFromSessionData(se.value);
                CivMessage.send(sender, i + ") " + CivColor.Gold + civ.getName() + CivColor.White + " - " + civ.getScore() + " points  --  " + CivColor.BOLD + " VICTORY");
                i++;
                v++;
            }
            return;
        }
//		TreeMap<Integer, Civilization> scores = new TreeMap<Integer, Civilization>();
//		
//		for (Civilization civ : CivGlobal.getCivs()) {
//			if (civ.isAdminCiv()) {
//				continue;
//			}
//			scores.put(civ.getScore(), civ);
//		}

        synchronized (CivGlobal.civilizationScores) {
            for (Integer score : CivGlobal.civilizationScores.descendingKeySet()) {
                CivMessage.send(sender, i + ") " + CivColor.Gold + CivGlobal.civilizationScores.get(score).getName() + CivColor.White + " - " + score);
                i++;
                if (i > 10 + v) break;
            }
        }

    }

    public void dip_cmd() {
        CivDiplomacyCommand cmd = new CivDiplomacyCommand();
        cmd.onCommand(sender, null, "dip", this.stripArgs(args, 1));
    }

    public void group_cmd() {
        CivGroupCommand cmd = new CivGroupCommand();
        cmd.onCommand(sender, null, "group", this.stripArgs(args, 1));
    }

    public void set_cmd() {
        CivSetCommand cmd = new CivSetCommand();
        cmd.onCommand(sender, null, "set", this.stripArgs(args, 1));
    }

    public void time_cmd() throws CivException {
        CivMessage.sendHeading(sender, CivSettings.localize.localizedString("cmd_civ_timeHeading"));
        Resident resident = getResident();
        ArrayList<String> out = new ArrayList<String>();
        SimpleDateFormat sdf = new SimpleDateFormat("M/dd h:mm:ss a z");

        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getTimeZone(resident.getTimezone()));
        sdf.setTimeZone(cal.getTimeZone());


        out.add(CivColor.Green + CivSettings.localize.localizedString("cmd_civ_timeServer") + " " + CivColor.LightGreen + sdf.format(cal.getTime()));

        cal.setTime(CivGlobal.getNextUpkeepDate());
        out.add(CivColor.Green + CivSettings.localize.localizedString("cmd_civ_timeUpkeep") + " " + CivColor.LightGreen + sdf.format(cal.getTime()));

        cal.setTime(CivGlobal.getNextHourlyTickDate());
        out.add(CivColor.Green + CivSettings.localize.localizedString("cmd_civ_timeHourly") + " " + CivColor.LightGreen + sdf.format(cal.getTime()));

        cal.setTime(CivGlobal.getNextRepoTime());
        out.add(CivColor.Green + CivSettings.localize.localizedString("cmd_civ_timeRepo") + " " + CivColor.LightGreen + sdf.format(cal.getTime()));

        cal.setTimeInMillis(CivGlobal.cantDemolishFrom);
        out.add("§2" + CivSettings.localize.localizedString("cmd_civ_timeCantDemolish", "§6" + sdf.format(cal.getTime()) + "§a", "§c" + sdf.format(cal.getTimeInMillis() + 18000000L) + "§a", "§a"));

        if (War.isWarTime()) {
            out.add(CivColor.Yellow + CivSettings.localize.localizedString("cmd_civ_timeWarNow"));
            cal.setTime(War.getStart());
            out.add(CivColor.Yellow + CivSettings.localize.localizedString("cmd_civ_timeWarStarted") + " " + CivColor.LightGreen + sdf.format(cal.getTime()));

            cal.setTime(War.getEnd());
            out.add(CivColor.Yellow + CivSettings.localize.localizedString("cmd_civ_timeWarEnds") + " " + CivColor.LightGreen + sdf.format(cal.getTime()));
        } else {
            cal.setTime(War.getNextWarTime());
            out.add(CivColor.Green + CivSettings.localize.localizedString("cmd_civ_timeWarNext") + " " + CivColor.LightGreen + sdf.format(cal.getTime()));
        }
        out.add("§7" + CivSettings.localize.localizedString("cmd_civ_timeCantDemolishHelp"));

        Player player = null;
        try {
            player = getPlayer();
        } catch (CivException e) {
        }

        if (player == null || player.hasPermission(CivSettings.MINI_ADMIN) || player.isOp()) {
            cal.setTime(CivGlobal.getTodaysSpawnRegenDate());
            out.add(CivColor.LightPurple + CivSettings.localize.localizedString("cmd_civ_timeSpawnRegen") + " " + CivColor.LightGreen + sdf.format(cal.getTime()));

            cal.setTime(CivGlobal.getNextRandomEventTime());
            out.add(CivColor.LightPurple + CivSettings.localize.localizedString("cmd_civ_timeRandomEvent") + " " + CivColor.LightGreen + sdf.format(cal.getTime()));
        }

        CivMessage.send(sender, out);
    }

    public void gov_cmd() {
        CivGovCommand cmd = new CivGovCommand();
        cmd.onCommand(sender, null, "gov", this.stripArgs(args, 1));
    }

    public void research_cmd() {
        CivResearchCommand cmd = new CivResearchCommand();
        cmd.onCommand(sender, null, "research", this.stripArgs(args, 1));
    }

    public void list_cmd() throws CivException {
        if (args.length < 2) {
            String out = "";
            CivMessage.sendHeading(sender, CivSettings.localize.localizedString("cmd_civ_listHeading"));
            for (Civilization civ : CivGlobal.getCivs()) {
                out += civ.getName() + ", ";
            }

            CivMessage.send(sender, out);
            return;
        }

        Civilization civ = getNamedCiv(1);

        String out = "";
        CivMessage.sendHeading(sender, CivSettings.localize.localizedString("var_cmd_civ_listtowns", args[1]));

        for (Town t : civ.getTowns()) {
            out += t.getName() + ", ";
        }

        CivMessage.send(sender, out);
    }

    public void show_cmd() throws CivException {
        if (args.length < 2) {
            throw new CivException(CivSettings.localize.localizedString("cmd_civ_showPrompt"));
        }

        Civilization civ = getNamedCiv(1);
        if (sender instanceof Player) {
            CivInfoCommand.show(sender, getResident(), civ);
        } else {
            CivInfoCommand.show(sender, null, civ);
        }
    }

    public void deposit_cmd() throws CivException {
        if (args.length < 2) {
            throw new CivException(CivSettings.localize.localizedString("cmd_civ_despositPrompt"));
        }

        Resident resident = getResident();
        Civilization civ = getSenderCiv();

        try {
            Double amount = Double.valueOf(args[1]);
            if (amount < 1) {
                throw new CivException(amount + " " + CivSettings.localize.localizedString("cmd_enterNumerError2"));
            }
            amount = Math.floor(amount);

            civ.depositFromResident(resident, Double.valueOf(args[1]));

        } catch (NumberFormatException e) {
            throw new CivException(args[1] + " " + CivSettings.localize.localizedString("cmd_enterNumerError"));
        } catch (SQLException e) {
            e.printStackTrace();
            throw new CivException(CivSettings.localize.localizedString("internalDatabaseException"));
        }

        CivMessage.sendSuccess(sender, CivSettings.localize.localizedString("Deposited") + args[1] + " " + CivSettings.CURRENCY_NAME);
    }

    public void withdraw_cmd() throws CivException {
        if (args.length < 2) {
            throw new CivException(CivSettings.localize.localizedString("cmd_civ_withdrawPrompt"));
        }

        Civilization civ = getSenderCiv();
        Resident resident = getResident();

        if (!civ.getLeaderGroup().hasMember(resident)) {
            throw new CivException(CivSettings.localize.localizedString("cmd_NeedHigherCivRank2"));
        }

        try {
            Double amount = Double.valueOf(args[1]);
            if (amount < 1) {
                throw new CivException(amount + " " + CivSettings.localize.localizedString("cmd_enterNumerError2"));
            }
            amount = Math.floor(amount);

            if (!civ.getTreasury().payTo(resident.getTreasury(), Double.valueOf(args[1]))) {
                throw new CivException(CivSettings.localize.localizedString("cmd_civ_withdrawTooPoor"));
            }
        } catch (NumberFormatException e) {
            throw new CivException(args[1] + " " + CivSettings.localize.localizedString("cmd_enterNumerError"));
        }

        CivMessage.sendSuccess(sender, CivSettings.localize.localizedString("var_cmd_civ_withdrawSuccess", args[1], CivSettings.CURRENCY_NAME));
    }

    public void townlist_cmd() throws CivException {
        Civilization civ = getSenderCiv();

        CivMessage.sendHeading(sender, civ.getName() + " " + CivSettings.localize.localizedString("cmd_civ_townListHeading"));
        String out = "";
        for (Town town : civ.getTowns()) {
            out += town.getName() + ",";
        }
        CivMessage.send(sender, out);
    }

    public void info_cmd() throws CivException {
        CivInfoCommand cmd = new CivInfoCommand();
        cmd.onCommand(sender, null, "info", this.stripArgs(args, 1));
    }

    public void vote_cmd() throws CivException {

        if (args.length < 2) {
            CivMessage.sendError(sender, CivSettings.localize.localizedString("cmd_civ_voteHeading"));
            return;
        }

        if (sender instanceof Player) {
            Player player = (Player) sender;
            Resident resident = CivGlobal.getResident(player);

            if (!resident.hasTown()) {
                CivMessage.sendError(sender, CivSettings.localize.localizedString("cmd_civ_voteNotInTown"));
                return;
            }

            Civilization civ = CivGlobal.getCiv(args[1]);
            if (civ == null) {
                CivMessage.sendError(sender, CivSettings.localize.localizedString("var_cmd_civ_voteInvalidCiv", args[1]));
                return;
            }

            if (!EndConditionDiplomacy.canPeopleVote()) {
                CivMessage.sendError(sender, CivSettings.localize.localizedString("cmd_civ_voteNoCouncil"));
                return;
            }

            EndConditionDiplomacy.addVote(civ, resident);
            return;
        } else {
            return;
        }
    }

    @Override
    public void doDefaultAction() throws CivException {
        showHelp();
    }

    @Override
    public void showHelp() {
        this.showBasicHelp();
    }

    @Override
    public void permissionCheck() throws CivException {

    }

}
