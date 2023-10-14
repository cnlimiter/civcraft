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
import cn.evole.plugins.civcraft.config.ConfigGovernment;
import cn.evole.plugins.civcraft.exception.CivException;
import cn.evole.plugins.civcraft.main.CivMessage;
import cn.evole.plugins.civcraft.object.Civilization;
import cn.evole.plugins.civcraft.util.CivColor;

import java.util.ArrayList;

public class CivGovCommand extends CommandBase {

    @Override
    public void init() {
        command = "/civ gov";
        displayName = CivSettings.localize.localizedString("cmd_civ_gov_name");

        commands.put("info", CivSettings.localize.localizedString("cmd_civ_gov_infoDesc"));
        commands.put("change", CivSettings.localize.localizedString("cmd_civ_gov_changeDesc"));
        commands.put("list", CivSettings.localize.localizedString("cmd_civ_gov_listDesc"));
    }

    public void change_cmd() throws CivException {
        Civilization civ = getSenderCiv();

        if (args.length < 2) {
            throw new CivException(CivSettings.localize.localizedString("cmd_civ_gov_changePrompt"));
        }

        ConfigGovernment gov = ConfigGovernment.getGovernmentFromName(args[1]);
        if (gov == null) {
            throw new CivException(CivSettings.localize.localizedString("cmd_civ_gov_changeInvalid") + " " + args[1]);
        }

        if (!gov.isAvailable(civ)) {
            throw new CivException(CivSettings.localize.localizedString("var_cmd_civ_gov_changeNotHere", gov.displayName));
        }

        civ.changeGovernment(civ, gov, false);
        CivMessage.sendSuccess(sender, CivSettings.localize.localizedString("cmd_civ_gov_changeSuccess"));
    }

    public void list_cmd() throws CivException {
        Civilization civ = getSenderCiv();

        CivMessage.sendHeading(sender, CivSettings.localize.localizedString("cmd_civ_gov_listHeading"));
        ArrayList<ConfigGovernment> govs = ConfigGovernment.getAvailableGovernments(civ);

        for (ConfigGovernment gov : govs) {
            if (gov == civ.getGovernment()) {
                CivMessage.send(sender, CivColor.Gold + gov.displayName + " " + "(" + CivSettings.localize.localizedString("currentGovernment") + ")");
            } else {
                CivMessage.send(sender, CivColor.Green + gov.displayName);
            }
        }

    }

    public void info_cmd() throws CivException {
        Civilization civ = getSenderCiv();

        CivMessage.sendHeading(sender, CivSettings.localize.localizedString("cmd_civ_gov_infoHading") + " " + civ.getGovernment().displayName);
        CivMessage.send(sender, CivColor.Green + CivSettings.localize.localizedString("cmd_civ_gov_infoTrade") + " " + CivColor.LightGreen + civ.getGovernment().trade_rate +
                CivColor.Green + " " + CivSettings.localize.localizedString("cmd_civ_gov_infoCottage") + " " + CivColor.LightGreen + civ.getGovernment().cottage_rate);
        CivMessage.send(sender, CivColor.Green + CivSettings.localize.localizedString("cmd_civ_gov_infoUpkeep") + " " + CivColor.LightGreen + civ.getGovernment().upkeep_rate +
                CivColor.Green + " " + CivSettings.localize.localizedString("cmd_civ_gov_infoGrowth") + " " + CivColor.LightGreen + civ.getGovernment().growth_rate);
        CivMessage.send(sender, CivColor.Green + CivSettings.localize.localizedString("cmd_civ_gov_infoHammer") + " " + CivColor.LightGreen + civ.getGovernment().hammer_rate +
                CivColor.Green + " " + CivSettings.localize.localizedString("cmd_civ_gov_infoBeaker") + " " + CivColor.LightGreen + civ.getGovernment().beaker_rate);
        CivMessage.send(sender, CivColor.Green + CivSettings.localize.localizedString("cmd_civ_gov_infoCulture") + " " + CivColor.LightGreen + civ.getGovernment().culture_rate +
                CivColor.Green + " " + CivSettings.localize.localizedString("cmd_civ_gov_infoMaxTax") + " " + CivColor.LightGreen + civ.getGovernment().maximum_tax_rate);

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
        validLeaderAdvisor();
    }

}
