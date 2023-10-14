/**
 * [2023] Cnlimiter LLC
 * All Rights Reserved.
 * Name: civcraft
 * Author: cnlimiter
 * UpdateTime: 2023/10/14 16:15
 * Description:
 * License: ARR
 */
package cn.evole.plugins.civcraft.command.town;

import cn.evole.plugins.civcraft.command.CommandBase;
import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.exception.CivException;
import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.main.CivMessage;
import cn.evole.plugins.civcraft.object.Civilization;
import cn.evole.plugins.civcraft.object.Resident;
import cn.evole.plugins.civcraft.object.Town;
import cn.evole.plugins.civcraft.threading.TaskMaster;
import cn.evole.plugins.civcraft.threading.tasks.TownAddOutlawTask;
import cn.evole.plugins.civcraft.util.CivColor;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.UUID;

public class TownOutlawCommand extends CommandBase {

    @Override
    public void init() {
        command = "/town outlaw";
        displayName = CivSettings.localize.localizedString("cmd_town_outlaw_name");

        commands.put("add", CivSettings.localize.localizedString("cmd_town_outlaw_addDesc"));
        commands.put("remove", CivSettings.localize.localizedString("cmd_town_outlaw_removeDesc"));
        commands.put("list", CivSettings.localize.localizedString("cmd_town_outlaw_listDesc"));
        commands.put("addall", CivSettings.localize.localizedString("cmd_town_outlaw_addallDesc"));
        commands.put("removeall", CivSettings.localize.localizedString("cmd_town_outlaw_removeallDesc"));
        commands.put("addallciv", CivSettings.localize.localizedString("cmd_town_outlaw_addallcivDesc"));
        commands.put("removeallciv", CivSettings.localize.localizedString("cmd_town_outlaw_removeallcivDesc"));
    }

    public void addall_cmd() throws CivException {
        Town town = getSelectedTown();
        Town targetTown = getNamedTown(1);

        for (Resident resident : targetTown.getResidents()) {

            try {
                Player player = CivGlobal.getPlayer(args[1]);
                CivMessage.send(player, CivColor.Yellow + ChatColor.BOLD + CivSettings.localize.localizedString("var_cmd_town_outlaw_addAllAlert1", town.getName()));
            } catch (CivException e) {
            }
            TaskMaster.asyncTask(new TownAddOutlawTask(resident.getName(), town), 1000);
        }
        CivMessage.sendSuccess(sender, CivSettings.localize.localizedString("var_cmd_town_outlaw_addallalert3", args[1]));
    }

    public void removeall_cmd() throws CivException {
        Town town = getSelectedTown();
        Town targetTown = getNamedTown(1);

        for (Resident resident : targetTown.getResidents()) {
            town.removeOutlaw(resident.getName());
        }
        town.save();
    }

    public void addallciv_cmd() throws CivException {
        Town town = getSelectedTown();
        Civilization targetCiv = getNamedCiv(1);

        for (Town targetTown : targetCiv.getTowns()) {
            for (Resident resident : targetTown.getResidents()) {

                try {
                    Player player = CivGlobal.getPlayer(args[1]);
                    CivMessage.send(player, CivColor.Yellow + ChatColor.BOLD + CivSettings.localize.localizedString("var_cmd_town_outlaw_addAllAlert1", town.getName()));
                } catch (CivException e) {
                }
                TaskMaster.asyncTask(new TownAddOutlawTask(resident.getName(), town), 1000);
            }
        }
        CivMessage.sendSuccess(sender, CivSettings.localize.localizedString("var_cmd_town_outlaw_addallalert3", args[1]));
    }

    public void removeallciv_cmd() throws CivException {
        Town town = getSelectedTown();
        Civilization targetCiv = getNamedCiv(1);

        for (Town targetTown : targetCiv.getTowns()) {
            for (Resident resident : targetTown.getResidents()) {
                town.removeOutlaw(resident.getName());
            }
        }
        town.save();
    }

    public void add_cmd() throws CivException {
        Town town = getSelectedTown();

        if (args.length < 2) {
            throw new CivException(CivSettings.localize.localizedString("cmd_town_outlaw_addPrompt"));
        }

        Resident resident = getNamedResident(1);
        if (resident.getTown() == town) {
            throw new CivException(CivSettings.localize.localizedString("cmd_town_outlaw_addError"));
        }

        try {
            Player player = CivGlobal.getPlayer(args[1]);
            CivMessage.send(player, CivColor.Yellow + ChatColor.BOLD + CivSettings.localize.localizedString("var_cmd_town_outlaw_addAllAlert1", town.getName()));
        } catch (CivException e) {
        }

        CivMessage.sendSuccess(sender, CivSettings.localize.localizedString("var_cmd_town_outlaw_addallalert3", args[1]));
        TaskMaster.asyncTask(new TownAddOutlawTask(args[1], town), 1000);
    }

    public void remove_cmd() throws CivException {
        Town town = getSelectedTown();

        if (args.length < 2) {
            throw new CivException(CivSettings.localize.localizedString("cmd_town_outlaw_removePrompt"));
        }

        town.removeOutlaw(args[1]);
        town.save();

        CivMessage.sendSuccess(sender, CivSettings.localize.localizedString("var_cmd_town_outlaw_removeSuccess", args[1]));
    }

    public void list_cmd() throws CivException {
        Town town = getSelectedTown();

        CivMessage.sendHeading(sender, CivSettings.localize.localizedString("cmd_town_outlaw_listHeading"));

        String out = "";
        for (String outlaw : town.outlaws) {
            if (outlaw.length() >= 2) {
                Resident res = CivGlobal.getResidentViaUUID(UUID.fromString(outlaw));
                out += res.getName() + ",";
            }
        }

        CivMessage.send(sender, out);

    }

    @Override
    public void doDefaultAction() throws CivException {
        showHelp();
    }

    @Override
    public void showHelp() {
        showBasicHelp();
    }

    @Override
    public void permissionCheck() throws CivException {
        validMayorAssistantLeader();
    }

}
