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
import cn.evole.plugins.civcraft.exception.CivException;
import cn.evole.plugins.civcraft.main.CivMessage;
import cn.evole.plugins.civcraft.object.Civilization;
import cn.evole.plugins.civcraft.object.Resident;
import cn.evole.plugins.civcraft.util.CivColor;

public class CivMotdCommand extends CommandBase {

    @Override
    public void init() {
        command = "/civ motd";
        displayName = CivSettings.localize.localizedString("cmd_civ_motd_name");

        commands.put("set", CivSettings.localize.localizedString("cmd_civ_motd_setDesc"));
        commands.put("remove", CivSettings.localize.localizedString("cmd_civ_motd_removeDesc"));
    }

    public void set_cmd() throws CivException {
        Resident resident = getResident();
        Civilization civ = getSenderCiv();

        if (!civ.getLeaderGroup().hasMember(resident) && !civ.getAdviserGroup().hasMember(resident)) {
            throw new CivException(CivSettings.localize.localizedString("cmd_civ_motd_notallowed"));
        }

        if (args.length < 2) {
            throw new CivException(CivSettings.localize.localizedString("cmd_civ_motd_setPrompt"));
        }

        String motd = combineArgs(this.stripArgs(args, 1));
        civ.setMotd(motd);
        civ.save();

        CivMessage.sendCiv(civ, "MOTD:" + " " + motd);
    }

    public void remove_cmd() throws CivException {
        Resident resident = getResident();
        Civilization civ = getSenderCiv();

        if (!civ.getLeaderGroup().hasMember(resident) && !civ.getAdviserGroup().hasMember(resident)) {
            throw new CivException(CivSettings.localize.localizedString("cmd_civ_motd_notallowed"));
        }
        civ.setMotd(null);
        civ.save();
        CivMessage.sendSuccess(sender, CivSettings.localize.localizedString("cmd_civ_motd_removeSuccess"));
    }

    @Override
    public void doDefaultAction() throws CivException {
        showHelp();
        Resident resident = getResident();
        Civilization civ = getSenderCiv();
        if (civ.MOTD() != null) {
            CivMessage.send(resident, CivColor.LightPurple + "[Civ MOTD] " + CivColor.White + resident.getCiv().MOTD());
        } else {
            CivMessage.send(resident, CivColor.LightPurple + "[Civ MOTD] " + CivColor.White + CivSettings.localize.localizedString("cmd_civ_motd_noneSet"));
        }

    }

    @Override
    public void showHelp() {
        showBasicHelp();
    }

    @Override
    public void permissionCheck() throws CivException {

    }

}
