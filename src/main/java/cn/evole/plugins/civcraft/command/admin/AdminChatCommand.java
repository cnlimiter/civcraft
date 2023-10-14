/**
 * [2023] Cnlimiter LLC
 * All Rights Reserved.
 * Name: civcraft
 * Author: cnlimiter
 * UpdateTime: 2023/10/14 16:15
 * Description:
 * License: ARR
 */
package cn.evole.plugins.civcraft.command.admin;

import cn.evole.plugins.civcraft.command.CommandBase;
import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.exception.CivException;
import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.main.CivMessage;
import cn.evole.plugins.civcraft.object.Civilization;
import cn.evole.plugins.civcraft.object.Resident;
import cn.evole.plugins.civcraft.object.Town;

public class AdminChatCommand extends CommandBase {

    @Override
    public void init() {
        command = "/ad chat";
        displayName = CivSettings.localize.localizedString("adcmd_chat_name");

        commands.put("tc", CivSettings.localize.localizedString("adcmd_chat_tcDesc"));
        commands.put("cc", CivSettings.localize.localizedString("adcmd_chat_ccDesc"));
        commands.put("cclisten", CivSettings.localize.localizedString("adcmd_chat_cclisten"));
        commands.put("tclisten", CivSettings.localize.localizedString("adcmd_chat_tclisten"));
        commands.put("listenoff", CivSettings.localize.localizedString("adcmd_chat_listenOffDesc"));
        commands.put("cclistenall", CivSettings.localize.localizedString("adcmd_chat_listenAllDesc"));
        commands.put("tclistenall", CivSettings.localize.localizedString("adcmd_chat_tclistenAllDesc"));
        commands.put("banwordon", CivSettings.localize.localizedString("adcmd_chat_banWordOnDesc"));
        commands.put("banwordoff", CivSettings.localize.localizedString("adcmd_chat_banWordOffDesc"));
        commands.put("banwordadd", CivSettings.localize.localizedString("admcd_chat_banwordaddDesc"));
        commands.put("banwordremove", CivSettings.localize.localizedString("adcmd_chat_banwordremoveDesc"));
        commands.put("banwordtoggle", CivSettings.localize.localizedString("adcmd_chat_banwordToggleDesc"));

    }

    public void tclistenall_cmd() throws CivException {
        Resident resident = getResident();

        for (Town t : CivGlobal.getTowns()) {
            CivMessage.addExtraTownChatListener(t, resident.getName());
        }

        CivMessage.sendSuccess(sender, CivSettings.localize.localizedString("adcmd_chat_tclistenAllSuccess"));
    }

    public void cclistenall_cmd() throws CivException {
        Resident resident = getResident();

        for (Civilization civ : CivGlobal.getCivs()) {
            CivMessage.addExtraCivChatListener(civ, resident.getName());
        }

        CivMessage.sendSuccess(sender, CivSettings.localize.localizedString("adcmd_chat_cclistenAllSuccess"));
    }

    public void listenoff_cmd() throws CivException {
        Resident resident = getResident();

        for (Town t : CivGlobal.getTowns()) {
            CivMessage.removeExtraTownChatListener(t, resident.getName());
        }

        for (Civilization civ : CivGlobal.getCivs()) {
            CivMessage.removeExtraCivChatListener(civ, resident.getName());
        }

        CivMessage.sendSuccess(sender, CivSettings.localize.localizedString("adcmd_chat_listenOffSuccess"));
    }

    public void cclisten_cmd() throws CivException {
        if (args.length < 2) {
            throw new CivException(CivSettings.localize.localizedString("EnterCivName"));
        }

        Resident resident = getResident();

        Civilization civ = getNamedCiv(1);

        for (String str : CivMessage.getExtraCivChatListeners(civ)) {
            if (str.equalsIgnoreCase(resident.getName())) {
                CivMessage.removeExtraCivChatListener(civ, str);
                CivMessage.sendSuccess(sender, CivSettings.localize.localizedString("adcmd_chat_noLongerListenCiv") + " " + civ.getName());
                return;
            }
        }

        CivMessage.addExtraCivChatListener(civ, resident.getName());
        CivMessage.sendSuccess(sender, CivSettings.localize.localizedString("adcmd_chat_listenCivSuccess") + " " + civ.getName());
    }

    public void tclisten_cmd() throws CivException {
        if (args.length < 2) {
            throw new CivException(CivSettings.localize.localizedString("EnterTownName"));
        }

        Resident resident = getResident();

        Town town = getNamedTown(1);

        for (String str : CivMessage.getExtraTownChatListeners(town)) {
            if (str.equalsIgnoreCase(resident.getName())) {
                CivMessage.removeExtraTownChatListener(town, str);
                CivMessage.sendSuccess(sender, CivSettings.localize.localizedString("adcmd_chat_noLongerListenTown") + " " + town.getName());
                return;
            }
        }

        CivMessage.addExtraTownChatListener(town, resident.getName());
        CivMessage.sendSuccess(sender, CivSettings.localize.localizedString("adcmd_chat_listenTownSuccess") + " " + town.getName());
    }

    public void tc_cmd() throws CivException {
        Resident resident = getResident();
        if (args.length < 2) {
            resident.setTownChat(false);
            resident.setTownChatOverride(null);
            CivMessage.sendSuccess(sender, CivSettings.localize.localizedString("adcmd_chat_noLongerChattingInTown"));
            return;
        }

        Town town = getNamedTown(1);

        resident.setTownChat(true);
        resident.setTownChatOverride(town);
        CivMessage.sendSuccess(sender, CivSettings.localize.localizedString("adcmd_chat_nowChattingInTown") + " " + town.getName());
    }

    public void cc_cmd() throws CivException {
        Resident resident = getResident();
        if (args.length < 2) {
            resident.setCivChat(false);
            resident.setCivChatOverride(null);
            CivMessage.sendSuccess(sender, CivSettings.localize.localizedString("adcmd_chat_noLongerChattingInCiv"));
            return;
        }

        Civilization civ = getNamedCiv(1);

        resident.setCivChat(true);
        resident.setCivChatOverride(civ);
        CivMessage.sendSuccess(sender, CivSettings.localize.localizedString("adcmd_chat_nowChattingInCiv") + " " + civ.getName());
    }

    public void banwordon_cmd() {
        CivGlobal.banWordsActive = true;
        CivMessage.sendSuccess(sender, CivSettings.localize.localizedString("adcmd_chat_banwordsActivated"));
    }

    public void banwordoff_cmd() {
        CivGlobal.banWordsActive = false;
        CivMessage.sendSuccess(sender, CivSettings.localize.localizedString("adcmd_chat_banwordsDeactivated"));

    }

    public void banwordadd_cmd() throws CivException {
        if (args.length < 2) {
            throw new CivException(CivSettings.localize.localizedString("adcmd_chat_addBanwordPrompt"));
        }

        CivGlobal.banWords.add(args[1]);
        CivMessage.sendSuccess(sender, CivSettings.localize.localizedString("var_adcmd_chat_banwordadded", args[1]));
    }

    public void banwordremove_cmd() throws CivException {
        if (args.length < 2) {
            throw new CivException(CivSettings.localize.localizedString("adcmd_chat_removeBanwordPrompt"));
        }

        CivGlobal.banWords.remove(args[1]);
        CivMessage.sendSuccess(sender, CivSettings.localize.localizedString("var_adcmd_chat_banwordremoved", args[1]));
    }

    public void banwordtoggle() throws CivException {

        CivGlobal.banWordsAlways = !CivGlobal.banWordsAlways;
        CivMessage.sendSuccess(sender, CivSettings.localize.localizedString("admcd_chat_banwordAlways") + " " + CivGlobal.banWordsAlways);
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

    }

}
