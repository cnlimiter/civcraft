/**
 * [2023] Cnlimiter LLC
 * All Rights Reserved.
 * Name: civcraft
 * Author: cnlimiter
 * UpdateTime: 2023/10/14 16:15
 * Description:
 * License: ARR
 */
package cn.evole.plugins.civcraft.command;

import cn.evole.plugins.civcraft.camp.Camp;
import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.exception.CivException;
import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.main.CivMessage;
import cn.evole.plugins.civcraft.object.Civilization;
import cn.evole.plugins.civcraft.object.Resident;
import cn.evole.plugins.civcraft.object.Town;
import cn.evole.plugins.civcraft.object.TownChunk;
import cn.evole.plugins.civcraft.permission.PermissionGroup;
import cn.evole.plugins.civcraft.util.CivColor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public abstract class CommandBase implements TabExecutor {

    private static final int MATCH_LIMIT = 5;
    public Town senderTownOverride = null;
    public Civilization senderCivOverride = null;
    //子命令
    protected HashMap<String, String> commands = new HashMap<>();
    //参数
    protected String[] args;
    //发送人
    protected CommandSender sender;
    protected String command = "FIXME";
    protected String displayName = "FIXME";
    protected boolean sendUnknownToDefault = false;
    //数字格式化
    protected DecimalFormat df = new DecimalFormat();

    public abstract void init();

    /* Called when no arguments are passed. */
    /* 未传递参数时调用. */
    public abstract void doDefaultAction() throws CivException;

    /* Called on syntax error. */
    /* 因语法错误而调用. */
    public abstract void showHelp();

    /* Called before command is executed to check permissions. */
    /* 在执行命令之前调用,检查权限. */
    public abstract void permissionCheck() throws CivException;

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        init();

        this.args = args;
        this.sender = sender;

        try {
            permissionCheck();
        } catch (CivException e1) {
            CivMessage.sendError(sender, e1.getMessage());
            return false;
        }

        doLogging();

        if (args.length == 0) {
            try {
                doDefaultAction();
            } catch (CivException e) {
                CivMessage.sendError(sender, e.getMessage());
            }
            return false;
        }

        if ("help".equalsIgnoreCase(args[0])) {
            showHelp();
            return true;
        }

        for (String c : commands.keySet()) {
            if (c.equalsIgnoreCase(args[0])) {
                try {
                    //获取同名_cmd方法
                    Method method = this.getClass().getMethod(args[0].toLowerCase() + "_cmd");
                    try {
                        method.invoke(this);
                        return true;
                    } catch (IllegalAccessException | IllegalArgumentException e) {
                        e.printStackTrace();
                        CivMessage.sendError(sender, CivSettings.localize.localizedString("internalCommandException"));
                    } catch (InvocationTargetException e) {
                        if (e.getCause() instanceof CivException) {
                            CivMessage.sendError(sender, e.getCause().getMessage());
                        } else {
                            e.getCause().printStackTrace();
                            CivMessage.sendError(sender, CivSettings.localize.localizedString("internalCommandException"));
                        }
                    }

                } catch (NoSuchMethodException e) {
                    if (sendUnknownToDefault) {
                        try {
                            doDefaultAction();
                        } catch (CivException e1) {
                            CivMessage.sendError(sender, e.getMessage());
                        }
                        return false;
                    }
                    CivMessage.sendError(sender, CivSettings.localize.localizedString("cmd_unknwonMethod") + " " + args[0]);
                }
                return true;
            }
        }

        if (sendUnknownToDefault) {
            try {
                doDefaultAction();
            } catch (CivException e) {
                CivMessage.sendError(sender, e.getMessage());
            }
            return false;
        }

        CivMessage.sendError(sender, CivSettings.localize.localizedString("cmd_unknownCommand") + " " + args[0]);
        return false;
    }


    public void doLogging() {
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        //camp upgrade
        if (args.length == 0) {
            return new ArrayList<>(commands.keySet());
        }
        // camp upgrade o..
        if (args.length == 1) {
            return commands.keySet()
                    .stream()
                    .filter(s -> s.startsWith(args[0]))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    public void showBasicHelp() {
        CivMessage.sendHeading(sender, displayName + " " + CivSettings.localize.localizedString("cmd_CommandHelpTitle"));
        for (String c : commands.keySet()) {
            String info = commands.get(c);

            info = info.replace("[", CivColor.Yellow + "[");
            info = info.replace("]", "]" + CivColor.LightGray);
            info = info.replace("(", CivColor.Yellow + "(");
            info = info.replace(")", ")" + CivColor.LightGray);

            CivMessage.send(sender, CivColor.LightPurple + command + " " + c + CivColor.LightGray + " " + info);
        }
    }

    public Resident getResident() throws CivException {
        Player player = getPlayer();
        Resident res = CivGlobal.getResident(player);
        if (res == null) {
            throw new CivException(CivSettings.localize.localizedString("var_Resident_CouldNotBeFound", player.getName()));
        }
        return res;
    }

    public Player getPlayer() throws CivException {
        if (sender instanceof Player) {
            return (Player) sender;
        }
        throw new CivException(CivSettings.localize.localizedString("cmd_MustBePlayer"));
    }

    public Town getSelectedTown() throws CivException {
        if (senderTownOverride != null) {
            return senderTownOverride;
        }

        if (sender instanceof Player) {
            Player player = (Player) sender;
            Resident res = CivGlobal.getResident(player);
            if (res != null && res.getTown() != null) {

                if (res.getSelectedTown() != null) {
                    try {
                        res.getSelectedTown().validateResidentSelect(res);
                    } catch (CivException e) {
                        CivMessage.send(player, CivColor.Yellow + CivSettings.localize.localizedString("var_cmd_townDeselectedInvalid", res.getSelectedTown().getName(), res.getTown().getName()));
                        res.setSelectedTown(res.getTown());
                        return res.getTown();
                    }

                    return res.getSelectedTown();
                } else {
                    return res.getTown();
                }
            }
        }
        throw new CivException(CivSettings.localize.localizedString("cmd_notPartOfTown"));
    }

    public TownChunk getStandingTownChunk() throws CivException {
        Player player = getPlayer();

        TownChunk tc = CivGlobal.getTownChunk(player.getLocation());
        if (tc == null) {
            throw new CivException(CivSettings.localize.localizedString("cmd_plotNotOwned"));
        }
        return tc;
    }

    protected String[] stripArgs(String[] someArgs, int amount) {
        if (amount >= someArgs.length) {
            return new String[0];
        }

        String[] argsLeft = new String[someArgs.length - amount];
        System.arraycopy(someArgs, amount, argsLeft, 0, argsLeft.length);

        return argsLeft;
    }

    protected String combineArgs(String[] someArgs) {
        StringBuilder combined = new StringBuilder();
        for (String str : someArgs) {
            combined.append(str).append(" ");
        }
        return combined.toString().trim();
    }

    /* 校验城镇管理 */
    public void validMayor() throws CivException {
        Player player = getPlayer();
        Town town = getSelectedTown();

        if (!town.playerIsInGroupName("mayors", player)) {
            throw new CivException(CivSettings.localize.localizedString("cmd_MustBeMayor"));
        }
        //if (this.)
    }

    /* 校验文明领导 */
    public void validMayorAssistantLeader() throws CivException {
        Resident resident = getResident();
        Town town = getSelectedTown();
        Civilization civ;

        /*
         * If we're using a selected town that isn't ours validate based on the mother civ.
         * 如果我们使用的是一个选定的城镇，而不是我们基于母文明身份验证的城镇
         */
        if (town.getMotherCiv() != null) {
            civ = town.getMotherCiv();
        } else {
            civ = getSenderCiv();
        }

        if (town.getMayorGroup() == null || town.getAssistantGroup() == null ||
                civ.getLeaderGroup() == null) {
            throw new CivException(CivSettings.localize.localizedString("var_cmd_townOrCivMissingGroup1", town.getName(), civ.getName()));
        }

        if (!town.getMayorGroup().hasMember(resident) && !town.getAssistantGroup().hasMember(resident) &&
                !civ.getLeaderGroup().hasMember(resident)) {
            throw new CivException(CivSettings.localize.localizedString("cmd_NeedHigherTownOrCivRank"));
        }
    }

    public void validLeaderAdvisor() throws CivException {
        Resident res = getResident();
        Civilization civ = getSenderCiv();


        if (!civ.getLeaderGroup().hasMember(res) && !civ.getAdviserGroup().hasMember(res)) {
            throw new CivException(CivSettings.localize.localizedString("cmd_NeedHigherCivRank"));
        }
    }

    public void validLeader() throws CivException {
        Resident res = getResident();
        Civilization civ = getSenderCiv();

        if (!civ.getLeaderGroup().hasMember(res)) {
            throw new CivException(CivSettings.localize.localizedString("cmd_NeedHigherCivRank2"));
        }
    }

    public void validPlotOwner() throws CivException {
        Resident resident = getResident();
        TownChunk tc = getStandingTownChunk();

        if (tc.perms.getOwner() == null) {
            validMayorAssistantLeader();
            if (tc.getTown() != resident.getTown()) {
                throw new CivException(CivSettings.localize.localizedString("cmd_validPlotOwnerFalse"));
            }
        } else {
            if (resident != tc.perms.getOwner()) {
                throw new CivException(CivSettings.localize.localizedString("cmd_validPlotOwnerFalse2"));
            }
        }
    }

    public Civilization getSenderCiv() throws CivException {

        if (this.senderCivOverride != null) {
            return this.senderCivOverride;
        }

        Resident resident = getResident();

        if (resident.getTown() == null) {
            throw new CivException(CivSettings.localize.localizedString("cmd_getSenderCivNoCiv"));
        }

        if (resident.getTown().getCiv() == null) {
            //This should never happen but....
            throw new CivException(CivSettings.localize.localizedString("cmd_getSenderCivNoCiv"));
        }

        return resident.getTown().getCiv();
    }

    protected Double getNamedDouble(int index) throws CivException {
        if (args.length < (index + 1)) {
            throw new CivException(CivSettings.localize.localizedString("cmd_enterNumber"));
        }

        try {
            Double number = Double.valueOf(args[index]);
            return number;
        } catch (NumberFormatException e) {
            throw new CivException(args[index] + " " + CivSettings.localize.localizedString("cmd_enterNumerError"));
        }

    }

    protected Integer getNamedInteger(int index) throws CivException {
        if (args.length < (index + 1)) {
            throw new CivException(CivSettings.localize.localizedString("cmd_enterNumber"));
        }

        try {
            Integer number = Integer.valueOf(args[index]);
            return number;
        } catch (NumberFormatException e) {
            throw new CivException(args[index] + " " + CivSettings.localize.localizedString("cmd_enterNumerError2"));
        }

    }

    protected Resident getNamedResident(int index) throws CivException {
        if (args.length < (index + 1)) {
            throw new CivException(CivSettings.localize.localizedString("EnterResidentName"));
        }

        String name = args[index].toLowerCase();
        name = name.replace("%", "(\\w*)");

        ArrayList<Resident> potentialMatches = new ArrayList<Resident>();
        for (Resident resident : CivGlobal.getResidents()) {
            String str = resident.getName().toLowerCase();
            try {
                if (str.matches(name)) {
                    potentialMatches.add(resident);
                }
            } catch (Exception e) {
                throw new CivException(CivSettings.localize.localizedString("cmd_invalidPattern"));
            }

            if (potentialMatches.size() > MATCH_LIMIT) {
                throw new CivException(CivSettings.localize.localizedString("cmd_TooManyResults"));
            }
        }

        if (potentialMatches.size() == 0) {
            throw new CivException(CivSettings.localize.localizedString("cmd_NameNoResults"));
        }

        if (potentialMatches.size() != 1) {
            CivMessage.send(sender, CivColor.LightPurple + ChatColor.UNDERLINE + CivSettings.localize.localizedString("cmd_NameMoreThan1"));
            CivMessage.send(sender, " ");
            String out = "";
            for (Resident resident : potentialMatches) {
                out += resident.getName() + ", ";
            }

            CivMessage.send(sender, CivColor.LightBlue + ChatColor.ITALIC + out);
            throw new CivException(CivSettings.localize.localizedString("cmd_NameMoreThan2"));
        }

        return potentialMatches.get(0);
    }

    protected Civilization getNamedCiv(int index) throws CivException {
        if (args.length < (index + 1)) {
            throw new CivException(CivSettings.localize.localizedString("EnterCivName"));
        }

        String name = args[index].toLowerCase();
        name = name.replace("%", "(\\w*)");

        ArrayList<Civilization> potentialMatches = new ArrayList<Civilization>();
        for (Civilization civ : CivGlobal.getCivs()) {
            String str = civ.getName().toLowerCase();
            try {
                if (str.matches(name)) {
                    potentialMatches.add(civ);
                }
            } catch (Exception e) {
                throw new CivException(CivSettings.localize.localizedString("cmd_invalidPattern"));
            }

            if (potentialMatches.size() > MATCH_LIMIT) {
                throw new CivException(CivSettings.localize.localizedString("cmd_TooManyResults"));
            }
        }

        if (potentialMatches.size() == 0) {
            throw new CivException(CivSettings.localize.localizedString("cmd_NameNoResults") + " '" + args[index] + "'");
        }

        if (potentialMatches.size() != 1) {
            CivMessage.send(sender, CivColor.LightPurple + ChatColor.UNDERLINE + CivSettings.localize.localizedString("cmd_NameMoreThan1"));
            CivMessage.send(sender, " ");
            String out = "";
            for (Civilization civ : potentialMatches) {
                out += civ.getName() + ", ";
            }

            CivMessage.send(sender, CivColor.LightBlue + ChatColor.ITALIC + out);
            throw new CivException(CivSettings.localize.localizedString("cmd_NameMoreThan2"));
        }

        return potentialMatches.get(0);
    }

    protected Civilization getNamedCapturedCiv(int index) throws CivException {
        if (args.length < (index + 1)) {
            throw new CivException(CivSettings.localize.localizedString("EnterCivName"));
        }

        String name = args[index].toLowerCase();
        name = name.replace("%", "(\\w*)");

        ArrayList<Civilization> potentialMatches = new ArrayList<Civilization>();
        for (Civilization civ : CivGlobal.getConqueredCivs()) {
            String str = civ.getName().toLowerCase();
            try {
                if (str.matches(name)) {
                    potentialMatches.add(civ);
                }
            } catch (Exception e) {
                throw new CivException(CivSettings.localize.localizedString("cmd_invalidPattern"));
            }

            if (potentialMatches.size() > MATCH_LIMIT) {
                throw new CivException(CivSettings.localize.localizedString("cmd_TooManyResults"));
            }
        }

        if (potentialMatches.size() == 0) {
            throw new CivException(CivSettings.localize.localizedString("cmd_NameNoResults") + " '" + args[index] + "'");
        }

        if (potentialMatches.size() != 1) {
            CivMessage.send(sender, CivColor.LightPurple + ChatColor.UNDERLINE + CivSettings.localize.localizedString("cmd_NameMoreThan1"));
            CivMessage.send(sender, " ");
            String out = "";
            for (Civilization civ : potentialMatches) {
                out += civ.getName() + ", ";
            }

            CivMessage.send(sender, CivColor.LightBlue + ChatColor.ITALIC + out);
            throw new CivException(CivSettings.localize.localizedString("cmd_NameMoreThan2"));
        }

        return potentialMatches.get(0);
    }
//	protected Town getNamedTown(int index) throws CivException {
//		if (args.length < (index+1)) {
//			throw new CivException("Enter a town name");
//		}
//		
//		Town town = CivGlobal.getTown(args[index]);
//		if (town == null) {
//			throw new CivException("No town named:"+args[index]);
//		}
//		
//		return town;
//	}

    protected Town getNamedTown(int index) throws CivException {
        if (args.length < (index + 1)) {
            throw new CivException(CivSettings.localize.localizedString("EnterTownName"));
        }

        String name = args[index].toLowerCase();
        name = name.replace("%", "(\\w*)");

        ArrayList<Town> potentialMatches = new ArrayList<Town>();
        for (Town town : CivGlobal.getTowns()) {
            String str = town.getName().toLowerCase();
            try {
                if (str.matches(name)) {
                    potentialMatches.add(town);
                }
            } catch (Exception e) {
                throw new CivException(CivSettings.localize.localizedString("cmd_invalidPattern"));
            }

            if (potentialMatches.size() > MATCH_LIMIT) {
                throw new CivException(CivSettings.localize.localizedString("cmd_TooManyResults"));
            }
        }

        if (potentialMatches.size() == 0) {
            throw new CivException(CivSettings.localize.localizedString("cmd_NameNoResults"));
        }

        if (potentialMatches.size() != 1) {
            CivMessage.send(sender, CivColor.LightPurple + ChatColor.UNDERLINE + CivSettings.localize.localizedString("cmd_NameMoreThan1"));
            CivMessage.send(sender, " ");
            String out = "";
            for (Town town : potentialMatches) {
                out += town.getName() + ", ";
            }

            CivMessage.send(sender, CivColor.LightBlue + ChatColor.ITALIC + out);
            throw new CivException(CivSettings.localize.localizedString("cmd_NameMoreThan2"));
        }

        return potentialMatches.get(0);
    }

    public String getNamedString(int index, String message) throws CivException {
        if (args.length < (index + 1)) {
            throw new CivException(message);
        }

        return args[index];
    }

    @SuppressWarnings("deprecation")
    protected OfflinePlayer getNamedOfflinePlayer(int index) throws CivException {
        if (args.length < (index + 1)) {
            throw new CivException(CivSettings.localize.localizedString("EnterPlayerName"));
        }

        OfflinePlayer offplayer = Bukkit.getOfflinePlayer(args[index]);
        if (offplayer == null) {
            throw new CivException(CivSettings.localize.localizedString("cmd_NameNoResults") + " " + args[index]);
        }

        return offplayer;
    }

    public String makeInfoString(HashMap<String, String> kvs, String lowColor, String highColor) {

        String out = "";
        for (String key : kvs.keySet()) {
            out += lowColor + key + ": " + highColor + kvs.get(key) + " ";
        }

        return out;
    }

    protected PermissionGroup getNamedPermissionGroup(Town town, int index) throws CivException {
        if (args.length < (index + 1)) {
            throw new CivException(CivSettings.localize.localizedString("EnterGroupName"));
        }

        PermissionGroup grp = CivGlobal.getPermissionGroupFromName(town, args[index]);
        if (grp == null) {
            throw new CivException(CivSettings.localize.localizedString("var_cmd_NameNoResults", args[index], town.getName()));
        }

        return grp;
    }

    protected void validCampOwner() throws CivException {
        Resident resident = getResident();

        if (!resident.hasCamp()) {
            throw new CivException(CivSettings.localize.localizedString("cmd_campBase_NotInCamp"));
        }

        if (resident.getCamp().getOwner() != resident) {
            throw new CivException(CivSettings.localize.localizedString("cmd_campBase_NotOwner") + " (" + resident.getCamp().getOwnerName() + ")");
        }
    }

    protected Camp getCurrentCamp() throws CivException {
        Resident resident = getResident();

        if (!resident.hasCamp()) {
            throw new CivException(CivSettings.localize.localizedString("cmd_campBase_NotInCamp"));
        }

        return resident.getCamp();
    }

    protected Camp getNamedCamp(int index) throws CivException {
        if (args.length < (index + 1)) {
            throw new CivException(CivSettings.localize.localizedString("EnterCampName"));
        }

        String name = args[index].toLowerCase();
        name = name.replace("%", "(\\w*)");

        ArrayList<Camp> potentialMatches = new ArrayList<Camp>();
        for (Camp camp : CivGlobal.getCamps()) {
            String str = camp.getName().toLowerCase();
            try {
                if (str.matches(name)) {
                    potentialMatches.add(camp);
                }
            } catch (Exception e) {
                throw new CivException(CivSettings.localize.localizedString("cmd_invalidPattern"));
            }

            if (potentialMatches.size() > MATCH_LIMIT) {
                throw new CivException(CivSettings.localize.localizedString("cmd_TooManyResults"));
            }
        }

        if (potentialMatches.size() == 0) {
            throw new CivException(CivSettings.localize.localizedString("cmd_NameNoResults"));
        }


        if (potentialMatches.size() != 1) {
            CivMessage.send(sender, CivColor.LightPurple + ChatColor.UNDERLINE + CivSettings.localize.localizedString("cmd_NameMoreThan1"));
            CivMessage.send(sender, " ");
            String out = "";
            for (Camp camp : potentialMatches) {
                out += camp.getName() + ", ";
            }

            CivMessage.send(sender, CivColor.LightBlue + ChatColor.ITALIC + out);
            throw new CivException(CivSettings.localize.localizedString("cmd_NameMoreThan2"));
        }

        return potentialMatches.get(0);
    }

    protected Civilization getNamedCiv(final int index, final String text) throws CivException {
        if (this.args.length < index + 1) {
            throw new CivException(text);
        }
        String name = this.args[index].toLowerCase();
        name = name.replace("%", "(\\w*)");
        final ArrayList<Civilization> potentialMatches = new ArrayList<Civilization>();
        for (final Civilization civ : CivGlobal.getCivs()) {
            final String str = civ.getName().toLowerCase();
            try {
                if (str.matches(name)) {
                    potentialMatches.add(civ);
                }
            } catch (Exception e) {
                throw new CivException(CivSettings.localize.localizedString("cmd_invalidPattern"));
            }
            if (potentialMatches.size() > 5) {
                throw new CivException(CivSettings.localize.localizedString("cmd_TooManyResults"));
            }
        }
        if (potentialMatches.size() == 0) {
            throw new CivException(CivSettings.localize.localizedString("cmd_NameNoResults") + " '" + this.args[index] + "'");
        }
        if (potentialMatches.size() != 1) {
            CivMessage.send(this.sender, "§d" + ChatColor.UNDERLINE + CivSettings.localize.localizedString("cmd_NameMoreThan1"));
            CivMessage.send(this.sender, " ");
            final StringBuilder out = new StringBuilder();
            for (final Civilization civ2 : potentialMatches) {
                out.append(civ2.getName()).append(", ");
            }
            CivMessage.send(this.sender, "§b" + ChatColor.ITALIC + (Object) out);
            throw new CivException(CivSettings.localize.localizedString("cmd_NameMoreThan2"));
        }
        return potentialMatches.get(0);
    }

}
