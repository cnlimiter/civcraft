/**
 * [2023] Cnlimiter LLC
 * All Rights Reserved.
 * Name: civcraft
 * Author: cnlimiter
 * UpdateTime: 2023/10/14 16:15
 * Description:
 * License: ARR
 */
package cn.evole.plugins.civcraft.command.resident;

import cn.evole.plugins.civcraft.command.CommandBase;
import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.exception.CivException;
import cn.evole.plugins.civcraft.exception.InvalidConfiguration;
import cn.evole.plugins.civcraft.lorestorage.LoreCraftableMaterial;
import cn.evole.plugins.civcraft.lorestorage.LoreMaterial;
import cn.evole.plugins.civcraft.main.CivData;
import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.main.CivMessage;
import cn.evole.plugins.civcraft.object.Report;
import cn.evole.plugins.civcraft.object.Resident;
import cn.evole.plugins.civcraft.object.Town;
import cn.evole.plugins.civcraft.util.CivColor;
import cn.evole.plugins.civcraft.util.ItemManager;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

public class ResidentCommand extends CommandBase {

    public static void show(CommandSender sender, Resident resident) {
        CivMessage.sendHeading(sender, CivSettings.localize.localizedString("var_Resident", resident.getName()));
        Date lastOnline = new Date(resident.getLastOnline());
        SimpleDateFormat sdf = new SimpleDateFormat("M/dd/yy h:mm:ss a z");
        CivMessage.send(sender, CivColor.Green + CivSettings.localize.localizedString("cmd_res_showLastOnline") + " " + CivColor.LightGreen + sdf.format(lastOnline));
        CivMessage.send(sender, CivColor.Green + CivSettings.localize.localizedString("Town") + " " + CivColor.LightGreen + resident.getTownString());
        CivMessage.send(sender, CivColor.Green + CivSettings.localize.localizedString("Camp") + " " + CivColor.LightGreen + resident.getCampString());

        if (sender.getName().equalsIgnoreCase(resident.getName()) || sender.isOp()) {
            CivMessage.send(sender, CivColor.Green + CivSettings.localize.localizedString("cmd_res_showTreasure") + " " + CivColor.LightGreen + resident.getTreasury().getBalance() + " " +
                    CivColor.Green + CivSettings.localize.localizedString("cmd_res_showTaxes") + " " + CivColor.LightGreen + (resident.getPropertyTaxOwed() + resident.getFlatTaxOwed()));
            if (resident.hasTown()) {
                if (resident.getSelectedTown() != null) {
                    CivMessage.send(sender, CivColor.Green + CivSettings.localize.localizedString("cmd_res_showSelected") + " " + CivColor.LightGreen + resident.getSelectedTown().getName());
                } else {
                    CivMessage.send(sender, CivColor.Green + CivSettings.localize.localizedString("cmd_res_showSelected") + " " + CivColor.LightGreen + resident.getTown().getName());
                }
            }
        }

        if (resident.getTreasury().inDebt()) {
            CivMessage.send(resident, CivColor.Yellow + CivSettings.localize.localizedString("cmd_res_showDebt") + " " + resident.getTreasury().getDebt() + " " + CivSettings.CURRENCY_NAME);
        }

        if (resident.getDaysTilEvict() > 0) {
            CivMessage.send(resident, CivColor.Yellow + CivSettings.localize.localizedString("cmd_res_showEviction") + " " + resident.getDaysTilEvict());
        }

        CivMessage.send(sender, CivColor.Green + CivSettings.localize.localizedString("Groups") + " " + resident.getGroupsString());
    }

    @Override
    public void init() {
        command = "/resident";
        displayName = CivSettings.localize.localizedString("cmd_res_Name");

        commands.put("info", CivSettings.localize.localizedString("cmd_res_infoDesc"));
        commands.put("paydebt", CivSettings.localize.localizedString("cmd_res_paydebtDesc"));
        commands.put("friend", CivSettings.localize.localizedString("cmd_res_friendDesc"));
        commands.put("toggle", CivSettings.localize.localizedString("cmd_res_toggleDesc"));
        commands.put("show", CivSettings.localize.localizedString("cmd_res_showDesc"));
        commands.put("resetspawn", CivSettings.localize.localizedString("cmd_res_resetspawnDesc"));
        commands.put("exchange", CivSettings.localize.localizedString("cmd_res_exchangeDesc"));
        commands.put("book", CivSettings.localize.localizedString("cmd_res_bookDesc"));
        commands.put("perks", CivSettings.localize.localizedString("cmd_res_perksDesc"));
        commands.put("refresh", CivSettings.localize.localizedString("cmd_res_refreshDesc"));
        commands.put("timezone", CivSettings.localize.localizedString("cmd_res_timezoneDesc"));
        commands.put("cn/evole/plugins/pvptimer", CivSettings.localize.localizedString("cmd_res_pvptimerDesc"));
        commands.put("outlawed", CivSettings.localize.localizedString("cmd_res_outlawedDesc"));
        commands.put("structure", CivSettings.localize.localizedString("cmd_res_strucutreDesc"));
        commands.put("tech", CivSettings.localize.localizedString("cmd_res_techDesc"));
        commands.put("upgrade", CivSettings.localize.localizedString("cmd_res_upgradeDesc"));
        commands.put("dip", CivSettings.localize.localizedString("cmd_res_dipDesc"));
        commands.put("report", CivSettings.localize.localizedString("cmd_res_reportDesc"));
        //commands.put("switchtown", "[town] - Allows you to instantly change your town to this town, if this town belongs to your civ.");
    }

    public void report_cmd() throws CivException {
        Report report;
        Resident resident = this.getResident();
        if (!resident.getReportChecked()) {
            throw new CivException(CivSettings.localize.localizedString("cmd_res_report_notNow"));
        }
        String[] split = resident.getReportResult().split("///");
        String admin = split[0];
        String result = split[1];
        try {
            report = CivGlobal.getReportByCloseTime(Long.valueOf(split[2]));
        } catch (NumberFormatException badVerdict) {
            resident.setReportChecked(false);
            throw new CivException(CivSettings.localize.localizedString("cmd_res_report_badMessage", CivColor.GoldBold + split[2] + " " + split[3]));
        }
        SimpleDateFormat sdf = new SimpleDateFormat("M/dd/yy h:mm:ss a z");
        CivMessage.send((Object) this.sender, CivColor.Red + admin + ": Complaint #" + report.getId() + ": " + "§a" + result + "§b" + " (" + sdf.format(report.getCloseTime()) + ")");
        resident.setReportChecked(false);
    }

    public void outlawed_cmd() throws CivException {
        Resident resident = this.getResident();
        StringBuilder outlaws = new StringBuilder();
        for (Town town : CivGlobal.getTowns()) {
            if (!town.isOutlaw(resident)) continue;
            outlaws.append(CivColor.Red).append(town.getName()).append(" [").append(town.getCiv().getName()).append("] ").append("\n");
        }
        if (outlaws.toString().equals("")) {
            throw new CivException(CivSettings.localize.localizedString("cmd_res_outlawed_noOne"));
        }
        CivMessage.send((Object) this.sender, CivSettings.localize.localizedString("cmd_res_outlawed_list", outlaws.toString()));
    }

    public void pvptimer_cmd() throws CivException {
        Resident resident = getResident();

        if (!resident.isProtected()) {
            CivMessage.sendError(sender, CivSettings.localize.localizedString("cmd_res_pvptimerNotActive"));
        }

        resident.setisProtected(false);
        CivMessage.sendSuccess(sender, CivSettings.localize.localizedString("cmd_res_pvptimerSuccess"));
    }

    public void timezone_cmd() throws CivException {
        Resident resident = getResident();

        if (args.length < 2) {
            ;
            CivMessage.sendSuccess(sender, CivSettings.localize.localizedString("cmd_res_timezoneCurrent") + " " + resident.getTimezone());
            return;
        }

        if (args[1].equalsIgnoreCase("list")) {
            CivMessage.sendHeading(sender, CivSettings.localize.localizedString("cmd_res_timezoneHeading"));
            String out = "";
            for (String zone : TimeZone.getAvailableIDs()) {
                out += zone + ", ";
            }
            CivMessage.send(sender, out);
            return;
        }

        TimeZone timezone = TimeZone.getTimeZone(args[1]);

        if (timezone.getID().equals("GMT") && !args[1].equalsIgnoreCase("GMT")) {
            CivMessage.send(sender, CivColor.LightGray + CivSettings.localize.localizedString("var_cmd_res_timezonenotFound1", args[1]));
            CivMessage.send(sender, CivColor.LightGray + CivSettings.localize.localizedString("cmd_res_timezoneNotFound3"));
        }

        resident.setTimezone(timezone.getID());
        resident.save();
        CivMessage.sendSuccess(sender, CivSettings.localize.localizedString("var_cmd_res_timezoneSuccess", timezone.getID()));
    }

    public void refresh_cmd() throws CivException {
        Resident resident = getResident();
        resident.perks.clear();
        resident.loadPerks(getPlayer());
        CivMessage.sendSuccess(sender, CivSettings.localize.localizedString("cmd_res_refreshSuccess"));
    }

    public void perks_cmd() throws CivException {
        Resident resident = getResident();
        resident.showPerkPage(0);
    }

    public void structure_cmd() throws CivException {
        Resident resident = this.getResident();
        if (resident.getCiv() == null) {
            throw new CivException(CivSettings.localize.localizedString("belongTownGUI"));
        }
        resident.showStructPage();
    }

    public void upgrade_cmd() throws CivException {
        Resident resident = this.getResident();
        if (resident.getCiv() == null) {
            throw new CivException(CivSettings.localize.localizedString("belongTownGUI"));
        }
        resident.showUpgradePage();
    }

    public void tech_cmd() throws CivException {
        Resident resident = this.getResident();
        if (resident.getCiv() == null) {
            throw new CivException(CivSettings.localize.localizedString("belongTownGUI"));
        }
        resident.showTechPage();
    }

    public void dip_cmd() throws CivException {
        Resident resident = this.getResident();
        if (resident.getCiv() == null) {
            throw new CivException(CivSettings.localize.localizedString("belongTownGUI"));
        }
        resident.showRelationPage();
    }

    /*
     * We need to figure out how to handle debt for the resident when he switches towns.
     * Should we even allow this? idk. Maybe war respawn points is enough?
     */
//	public void switchtown_cmd() throws CivException {
//		Town town = getNamedTown(1);
//		Resident resident = getResident();
//		
//		if (resident.getTown() == town) {
//			throw new CivException("You cannot switch to your own town.");
//		}
//		
//		if (resident.getTown().getMotherCiv() != town.getMotherCiv()) {
//			throw new CivException("You cannot place yourself into a conquered civ's town.");
//		}
//		
//		if (town.getCiv() != resident.getCiv()) {
//			throw new CivException("You cannot switch to a town not in your civ.");
//		}
//		
//		if (town.getMayorGroup().hasMember(resident) && town.getMayorGroup().getMemberCount() <= 1) {
//			throw new CivException("You are the last mayor of the town and cannot leave it.");
//		}
//		
//		resident.getTown().removeResident(resident);
//		try {
//			town.addResident(resident);
//		} catch (AlreadyRegisteredException e) {
//			e.printStackTrace();
//			throw new CivException("You already belong to this town.");
//		}
//		
//	}

    public void book_cmd() throws CivException {
        Player player = getPlayer();

        /* Determine if he already has the book. */
        for (ItemStack stack : player.getInventory().getContents()) {
            if (stack == null) {
                continue;
            }

            LoreCraftableMaterial craftMat = LoreCraftableMaterial.getCraftMaterial(stack);
            if (craftMat == null) {
                continue;
            }

            if (craftMat.getConfigId().equals("mat_tutorial_book")) {
                throw new CivException(CivSettings.localize.localizedString("cmd_res_bookHaveOne"));
            }
        }

        LoreCraftableMaterial craftMat = LoreCraftableMaterial.getCraftMaterialFromId("mat_tutorial_book");
        ItemStack helpBook = LoreCraftableMaterial.spawn(craftMat);

        HashMap<Integer, ItemStack> leftovers = player.getInventory().addItem(helpBook);
        if (leftovers != null && leftovers.size() >= 1) {
            throw new CivException(CivSettings.localize.localizedString("cmd_res_bookInvenFull"));
        }

        CivMessage.sendSuccess(player, CivSettings.localize.localizedString("cmd_res_bookSuccess"));
    }

    public void exchange_cmd() throws CivException {
        Player player = getPlayer();
        Resident resident = getResident();
        String type = getNamedString(1, CivSettings.localize.localizedString("cmd_res_exchangePrompt"));
        Integer amount = getNamedInteger(2);

        if (amount <= 0) {
            throw new CivException(CivSettings.localize.localizedString("cmd_res_exchangeLessThan0"));
        }

        type = type.toLowerCase();

        int exchangeID;
        double rate;
        switch (type) {
            case "iron":
                exchangeID = CivData.IRON_INGOT;
                rate = CivSettings.iron_rate;
                break;
            case "gold":
                exchangeID = CivData.GOLD_INGOT;
                rate = CivSettings.gold_rate;
                break;
            case "diamond":
                exchangeID = CivData.DIAMOND;
                rate = CivSettings.diamond_rate;
                break;
            case "emerald":
                exchangeID = CivData.EMERALD;
                rate = CivSettings.emerald_rate;
                break;
            default:
                throw new CivException(CivSettings.localize.localizedString("var_cmd_res_exchangeInvalid", type));
        }

        double exchangeRate;
        try {
            exchangeRate = CivSettings.getDouble(CivSettings.civConfig, "global.exchange_rate");
        } catch (InvalidConfiguration e) {
            e.printStackTrace();
            throw new CivException(CivSettings.localize.localizedString("internalException"));
        }

        ItemStack stack = ItemManager.createItemStack(exchangeID, 1);
        int total = 0;
        for (int i = 0; i < player.getInventory().getContents().length; i++) {
            ItemStack is = player.getInventory().getItem(i);
            if (is == null) {
                continue;
            }

            if (LoreCraftableMaterial.isCustom(is)) {
                continue;
            }

            if (CivGlobal.isBonusGoodie(is)) {
                throw new CivException(CivSettings.localize.localizedString("cmd_res_exchangeNoTradeGoods"));
            }
            if (LoreMaterial.hasEnhancement(is, "LoreEnhancementBuyItem")) {
                throw new CivException(CivSettings.localize.localizedString("cmd_res_exchangeNoBuyItems"));
            }

            if (ItemManager.getId(is) == exchangeID) {
                total += is.getAmount();
                break;
            }
        }

        if (total == 0) {
            throw new CivException(CivSettings.localize.localizedString("cmd_res_exchangeNotEnough") + " " + type);
        }

        if (amount > total) {
            amount = total;
        }

        stack.setAmount(amount);
        player.getInventory().removeItem(stack);
        double coins = amount * rate * exchangeRate;

        resident.getTreasury().deposit(coins);
        CivMessage.sendSuccess(player, CivSettings.localize.localizedString("var_cmd_res_exchangeSuccess", amount, type, coins, CivSettings.CURRENCY_NAME));

    }

    public void resetspawn_cmd() throws CivException {
        Player player = getPlayer();
        Location spawn = player.getWorld().getSpawnLocation();
        player.setBedSpawnLocation(spawn, true);
        CivMessage.sendSuccess(player, CivSettings.localize.localizedString("cmd_res_resetspawnSuccess"));
    }

    public void show_cmd() throws CivException {
        if (args.length < 2) {
            throw new CivException(CivSettings.localize.localizedString("cmd_res_showPrompt"));
        }

        Resident resident = getNamedResident(1);

        show(sender, resident);
    }

    public void toggle_cmd() throws CivException {
        ResidentToggleCommand cmd = new ResidentToggleCommand();
        cmd.onCommand(sender, null, "friend", this.stripArgs(args, 1));
    }

    public void friend_cmd() {
        ResidentFriendCommand cmd = new ResidentFriendCommand();
        cmd.onCommand(sender, null, "friend", this.stripArgs(args, 1));
    }

    public void paydebt_cmd() throws CivException {
        Resident resident = getResident();

        if (!resident.getTreasury().inDebt() || !resident.hasTown()) {
            throw new CivException(CivSettings.localize.localizedString("var_cmd_res_paydebtError2"));
        }

        if (!resident.getTreasury().hasEnough(resident.getTreasury().getDebt())) {
            throw new CivException(CivSettings.localize.localizedString("var_cmd_res_paydebtError1", resident.getTreasury().getDebt(), CivSettings.CURRENCY_NAME));
        }


        CivMessage.sendSuccess(sender, CivSettings.localize.localizedString("var_cmd_res_paydebtSuccess", resident.getTreasury().getDebt(), CivSettings.CURRENCY_NAME));
        resident.payOffDebt();
    }

    public void info_cmd() throws CivException {
        Resident resident = getResident();
        show(sender, resident);
    }

    @Override
    public void doDefaultAction() throws CivException {
        showHelp();
        //info_cmd();
        //CivMessage.send(sender, CivColor.LightGray+"Subcommands available: See /resident help");
    }

    @Override
    public void showHelp() {
        showBasicHelp();
    }

    @Override
    public void permissionCheck() throws CivException {

    }

}
