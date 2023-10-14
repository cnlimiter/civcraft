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
import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.main.CivMessage;
import cn.evole.plugins.civcraft.object.Buff;
import cn.evole.plugins.civcraft.object.Civilization;
import cn.evole.plugins.civcraft.object.Resident;
import cn.evole.plugins.civcraft.object.Town;
import cn.evole.plugins.civcraft.sessiondb.SessionEntry;
import cn.evole.plugins.civcraft.util.CivColor;
import cn.evole.plugins.civcraft.util.DecimalHelper;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class CivInfoCommand extends CommandBase {

    public static void show(CommandSender sender, Resident resident, Civilization civ) {

        boolean isOP = false;
        if (sender instanceof Player) {
            Player player;
            try {
                player = CivGlobal.getPlayer(resident);
                if (player.isOp()) {
                    isOP = true;
                }
            } catch (CivException e) {
                /* Allow console to display. */
            }
        } else {
            /* We're the console. */
            isOP = true;
        }


        CivMessage.sendHeading(sender, CivSettings.localize.localizedString("var_cmd_civ_info_showHeading", civ.getName()));

        CivMessage.send(sender, CivColor.Green + CivSettings.localize.localizedString("Score") + " " + CivColor.LightGreen + civ.getScore() +
                CivColor.Green + " " + CivSettings.localize.localizedString("Towns") + " " + CivColor.LightGreen + civ.getTownCount());
        if (civ.hasResident(resident)) {
            CivMessage.send((Object) sender, "§2" + CivSettings.localize.localizedString("Goverment", new StringBuilder().append("§a").append(civ.getGovernment().displayName).toString()));
        }
        if (civ.getLeaderGroup() == null) {
            CivMessage.send(sender, CivColor.Green + CivSettings.localize.localizedString("Leaders") + " " + CivColor.Rose + "NONE");
        } else {
            CivMessage.send(sender, CivColor.Green + CivSettings.localize.localizedString("Leaders") + " " + CivColor.LightGreen + civ.getLeaderGroup().getMembersString());
        }

        if (civ.getAdviserGroup() == null) {
            CivMessage.send(sender, CivColor.Green + CivSettings.localize.localizedString("Advisors") + " " + CivColor.Rose + "NONE");
        } else {
            CivMessage.send(sender, CivColor.Green + CivSettings.localize.localizedString("Advisors") + " " + CivColor.LightGreen + civ.getAdviserGroup().getMembersString());
        }

        if (resident == null || civ.hasResident(resident)) {
            CivMessage.send(sender, CivColor.Green + CivSettings.localize.localizedString("cmd_civ_info_showTax") + " " + CivColor.LightGreen + civ.getIncomeTaxRateString() +
                    CivColor.Green + " " + CivSettings.localize.localizedString("cmd_civ_info_showScience") + " " + CivColor.LightGreen + DecimalHelper.formatPercentage(civ.getSciencePercentage()));
            CivMessage.send(sender, CivColor.Green + CivSettings.localize.localizedString("Beakers") + " " + CivColor.LightGreen + civ.getBeakers() +
                    CivColor.Green + " " + CivSettings.localize.localizedString("Online") + " " + CivColor.LightGreen + civ.getOnlineResidents().size());
        }

        if (resident == null || civ.getLeaderGroup().hasMember(resident) || civ.getAdviserGroup().hasMember(resident) || isOP) {
            CivMessage.send(sender, CivColor.Green + CivSettings.localize.localizedString("Treasury") + " " + CivColor.LightGreen + civ.getTreasury().getBalance() + CivColor.Green + " " + CivSettings.CURRENCY_NAME);
        }

        if (civ.getTreasury().inDebt()) {
            CivMessage.send(sender, CivColor.Yellow + CivSettings.localize.localizedString("InDebt") + " " + civ.getTreasury().getDebt() + " Coins.");
            CivMessage.send(sender, CivColor.Yellow + civ.getDaysLeftWarning());
        }

        for (EndGameCondition endCond : EndGameCondition.endConditions) {
            ArrayList<SessionEntry> entries = CivGlobal.getSessionDB().lookup(endCond.getSessionKey());
            if (entries.size() == 0) {
                continue;
            }

            for (SessionEntry entry : entries) {
                if (civ == EndGameCondition.getCivFromSessionData(entry.value)) {
                    Integer daysLeft = endCond.getDaysToHold() - endCond.getDaysHeldFromSessionData(entry.value);

                    CivMessage.send(sender, CivSettings.localize.localizedString("var_cmd_civ_info_daysTillVictoryNew", CivColor.LightBlue + CivColor.BOLD + civ.getName() + CivColor.White,
                            CivColor.Yellow + CivColor.BOLD + daysLeft + CivColor.White, CivColor.LightPurple + CivColor.BOLD + endCond.getVictoryName() + CivColor.White));
                    break;
                }
            }
        }

        Integer votes = EndConditionDiplomacy.getVotesFor(civ);
        if (votes > 0) {
            CivMessage.send(sender, CivSettings.localize.localizedString("var_cmd_civ_votesHeading", CivColor.LightBlue + CivColor.BOLD + civ.getName() + CivColor.White,
                    CivColor.LightPurple + CivColor.BOLD + votes + CivColor.White));
        }

        String out = CivColor.Green + CivSettings.localize.localizedString("Towns") + " ";
        for (Town town : civ.getTowns()) {
            if (town.isCapitol()) {
                out += CivColor.Gold + town.getName();
            } else if (town.getMotherCiv() != null) {
                out += CivColor.Yellow + town.getName();
            } else {
                out += CivColor.White + town.getName();
            }
            out += ", ";
        }

        CivMessage.send(sender, out);
    }

    @Override
    public void init() {
        command = "/civ info";
        displayName = CivSettings.localize.localizedString("cmd_civ_info_name");

        commands.put("upkeep", CivSettings.localize.localizedString("cmd_civ_info_upkeepDesc"));
        commands.put("taxes", CivSettings.localize.localizedString("cmd_civ_info_taxesDesc"));
        commands.put("beakers", CivSettings.localize.localizedString("cmd_civ_info_beakersDesc"));
        commands.put("online", CivSettings.localize.localizedString("cmd_civ_info_onlineDesc"));
    }

    public void online_cmd() throws CivException {
        Civilization civ = getSenderCiv();

        CivMessage.sendHeading(sender, CivSettings.localize.localizedString("var_cmd_civ_info_onlineHeading", civ.getName()));
        String out = "";
        for (Resident resident : civ.getOnlineResidents()) {
            out += resident.getName() + " ";
        }
        CivMessage.send(sender, out);
    }

    public void beakers_cmd() throws CivException {
        Civilization civ = getSenderCiv();

        CivMessage.sendHeading(sender, CivSettings.localize.localizedString("cmd_civ_info_beakersHeading"));
        ArrayList<String> out = new ArrayList<String>();

        for (Town t : civ.getTowns()) {
            for (Buff b : t.getBuffManager().getEffectiveBuffs(Buff.SCIENCE_RATE)) {
                out.add(CivColor.Green + CivSettings.localize.localizedString("From") + " " + b.getSource() + ": " + CivColor.LightGreen + b.getDisplayDouble());
            }
        }

        out.add(CivColor.LightBlue + "------------------------------------");
        out.add(CivColor.Green + CivSettings.localize.localizedString("Total") + " " + CivColor.LightGreen + df.format(civ.getBeakers()));
        CivMessage.send(sender, out);
    }

    public void taxes_cmd() throws CivException {
        Civilization civ = getSenderCiv();

        CivMessage.sendHeading(sender, CivSettings.localize.localizedString("cmd_civ_info_taxesHeading"));
        for (Town t : civ.getTowns()) {
            CivMessage.send(sender, CivColor.Green + CivSettings.localize.localizedString("Town") + " " + CivColor.LightGreen + t.getName() + CivColor.Green +
                    CivSettings.localize.localizedString("Total") + " " + CivColor.LightGreen + civ.lastTaxesPaidMap.get(t.getName()));
        }

    }

    private double getTownTotalLastTick(Town town, Civilization civ) {
        double total = 0;
        for (String key : civ.lastUpkeepPaidMap.keySet()) {
            String townName = key.split(",")[0];

            if (townName.equalsIgnoreCase(town.getName())) {
                total += civ.lastUpkeepPaidMap.get(key);
            }
        }
        return total;
    }

    public void upkeep_cmd() throws CivException {
        Civilization civ = getSenderCiv();

        if (args.length < 2) {
            CivMessage.sendHeading(sender, civ.getName() + CivSettings.localize.localizedString("cmd_civ_info_upkeepHeading"));

            for (Town town : civ.getTowns()) {
                CivMessage.send(sender, CivColor.Green + CivSettings.localize.localizedString("Town") + " " + CivColor.LightGreen + town.getName() + CivColor.Green +
                        CivSettings.localize.localizedString("Total") + " " + CivColor.LightGreen + getTownTotalLastTick(town, civ));
            }
            CivMessage.send(sender, CivColor.Green + CivSettings.localize.localizedString("WarColon") + " " + CivColor.LightGreen + df.format(civ.getWarUpkeep()));

            Object[] arrobject = new Object[2];
            arrobject[0] = "\u00a7a" + (civ.getCapitol().getBonusUpkeep() == 1.0 ? "No" : "Yes");
            arrobject[1] = "\u00a76" + (civ.getCapitol().getBonusUpkeep() == 1.0 ? "" : (civ.getCapitol().getBonusUpkeep() == 2.5 ? "2.5x (Enemy doesn't have Notre Dame)" : "5x (Enemy has Notre Dame"));
            CivMessage.send(sender, CivColor.LightGray + CivSettings.localize.localizedString("var_cmd_civ_info_upkeepTownWar", arrobject));
            CivMessage.send(sender, CivColor.LightGray + CivSettings.localize.localizedString("cmd_civ_info_upkeepHeading2"));
            CivMessage.send(sender, CivColor.LightGray + CivSettings.localize.localizedString("cmd_civ_info_upkeepHeading3"));

            return;
        } else {

            Town town = civ.getTown(args[1]);
            if (town == null) {
                throw new CivException(CivSettings.localize.localizedString("var_cmd_civ_info_upkeepTownInvalid", args[1]));
            }

            CivMessage.sendHeading(sender, CivSettings.localize.localizedString("var_cmd_civ_info_upkeepTownHeading1", town.getName()));
            CivMessage.send(sender, CivColor.Green + CivSettings.localize.localizedString("Base") + " " + CivColor.LightGreen + civ.getUpkeepPaid(town, "base"));
            CivMessage.send(sender, CivColor.Green + CivSettings.localize.localizedString("Distance") + " " + CivColor.LightGreen + civ.getUpkeepPaid(town, "distance"));
            CivMessage.send(sender, CivColor.Green + CivSettings.localize.localizedString("DistanceUpkeep") + " " + CivColor.LightGreen + civ.getUpkeepPaid(town, "distanceUpkeep"));
            CivMessage.send(sender, CivColor.Green + CivSettings.localize.localizedString("Debt") + " " + CivColor.LightGreen + civ.getUpkeepPaid(town, "debt"));
            Object[] arrobject = new Object[2];
            arrobject[0] = "\u00a7a" + (civ.getCapitol().getBonusUpkeep() == 1.0 ? "No" : "Yes");
            arrobject[1] = "\u00a76" + (civ.getCapitol().getBonusUpkeep() == 1.0 ? "" : (civ.getCapitol().getBonusUpkeep() == 2.5 ? "2.5x (Enemy doesn't have Notre Dame)" : "5x (Enemy has Notre Dame"));
            CivMessage.send(sender, CivColor.Green + CivSettings.localize.localizedString("var_cmd_civ_info_upkeepTownWar", arrobject));
            CivMessage.send(sender, CivColor.Green + CivSettings.localize.localizedString("Total") + " " + CivColor.LightGreen + getTownTotalLastTick(town, civ));

            CivMessage.send(sender, CivColor.LightGray + CivSettings.localize.localizedString("cmd_civ_info_upkeepHeading2"));
        }


    }

    @Override
    public void doDefaultAction() throws CivException {
        show_info();
        CivMessage.send(sender, CivColor.LightGray + CivSettings.localize.localizedString("cmd_civ_info_help"));
    }

    public void show_info() throws CivException {
        Civilization civ = getSenderCiv();
        Resident resident = getResident();
        show(sender, resident, civ);
    }

    @Override
    public void showHelp() {
        showBasicHelp();
    }

    @Override
    public void permissionCheck() throws CivException {

    }


}
