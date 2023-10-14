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
import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.main.CivMessage;
import cn.evole.plugins.civcraft.object.Civilization;
import cn.evole.plugins.civcraft.object.Town;
import cn.evole.plugins.civcraft.questions.DiplomacyGiftResponse;
import cn.evole.plugins.civcraft.questions.QuestionResponseInterface;
import cn.evole.plugins.civcraft.threading.TaskMaster;
import cn.evole.plugins.civcraft.threading.tasks.CivQuestionTask;
import cn.evole.plugins.civcraft.util.CivColor;
import cn.evole.plugins.civcraft.war.War;
import org.bukkit.ChatColor;

public class CivDiplomacyGiftCommand extends CommandBase {

    @Override
    public void init() {
        command = "/civ dip gift";
        displayName = CivSettings.localize.localizedString("cmd_civ_dipgift_name");

        commands.put("entireciv", CivSettings.localize.localizedString("cmd_civ_dipgift_entirecivDesc"));
        commands.put("town", CivSettings.localize.localizedString("cmd_civ_dipgift_townDesc"));

    }

    private void sendGiftRequest(Civilization toCiv, Civilization fromCiv, String message,
                                 QuestionResponseInterface finishedFunction) throws CivException {
        CivQuestionTask task = CivGlobal.civQuestions.get(toCiv.getName());
        if (task != null) {
            /* Civ already has a question pending. Lets deny this question until it times out
             * this will allow questions to come in on a pseduo 'first come first serve' and
             * prevents question spamming.
             */
            throw new CivException(CivSettings.localize.localizedString("cmd_civ_dipgift_sendHasPending"));
        }

        task = new CivQuestionTask(toCiv, fromCiv, message, 30000, finishedFunction);
        CivGlobal.civQuestions.put(toCiv.getName(), task);
        TaskMaster.asyncTask("", task, 0);
    }

    public void entireciv_cmd() throws CivException {
        this.validLeader();
        Civilization fromCiv = getSenderCiv();
        Civilization toCiv = getNamedCiv(1);

        if (fromCiv == toCiv) {
            throw new CivException(CivSettings.localize.localizedString("cmd_civ_dipgift_entirecivSelf"));
        }

        if (fromCiv.getDiplomacyManager().isAtWar() || toCiv.getDiplomacyManager().isAtWar()) {
            throw new CivException(CivSettings.localize.localizedString("cmd_civ_dipgift_entirecivAtWar"));
        }

        fromCiv.validateGift();
        toCiv.validateGift();

        if (War.isWarTime()) {
            throw new CivException(CivSettings.localize.localizedString("cmd_civ_dipgift_entirecivDuringWar"));
        }

        if (War.isWithinWarDeclareDays()) {
            throw new CivException(CivSettings.localize.localizedString("cmd_civ_dipgift_entirecivClostToWar1") + " " + War.time_declare_days + " " + CivSettings.localize.localizedString("cmd_civ_dip_declareTooCloseToWar4"));
        }


        DiplomacyGiftResponse dipResponse = new DiplomacyGiftResponse();
        dipResponse.giftedObject = fromCiv;
        dipResponse.fromCiv = fromCiv;
        dipResponse.toCiv = toCiv;

        sendGiftRequest(toCiv, fromCiv,
                CivColor.Yellow + ChatColor.BOLD + CivSettings.localize.localizedString("var_cmd_civ_dipgift_entirecivRequest1", fromCiv.getName()) +
                        " " + CivSettings.localize.localizedString("var_cmd_civ_dipgift_entirecivRequest2", fromCiv.getMergeCost(), CivSettings.CURRENCY_NAME), dipResponse);
        CivMessage.sendSuccess(sender, CivSettings.localize.localizedString("cmd_civ_dipgift_entirecivSuccess"));
    }

    public void town_cmd() throws CivException {
        this.validLeader();
        Civilization fromCiv = getSenderCiv();
        Town giftedTown = getNamedTown(1);
        Civilization toCiv = getNamedCiv(2);

        if (giftedTown.getCiv() != fromCiv) {
            throw new CivException(CivSettings.localize.localizedString("cmd_civ_dipgift_townNotYours"));
        }

        if (giftedTown.getCiv() == toCiv) {
            throw new CivException(CivSettings.localize.localizedString("cmd_civ_dipgift_townNotInCiv"));
        }

        if (giftedTown.getMotherCiv() != null && toCiv != giftedTown.getMotherCiv()) {
            throw new CivException(CivSettings.localize.localizedString("cmd_civ_dipgift_townNotMother"));
        }

        if (giftedTown.isCapitol()) {
            throw new CivException(CivSettings.localize.localizedString("cmd_civ_dipgift_townNotCapitol"));
        }

        if (War.isWarTime()) {
            throw new CivException(CivSettings.localize.localizedString("cmd_civ_dipgift_townNotDuringWar"));
        }

        if (fromCiv.getDiplomacyManager().isAtWar() || toCiv.getDiplomacyManager().isAtWar()) {
            throw new CivException(CivSettings.localize.localizedString("cmd_civ_dipgift_townNotAtWar"));
        }

        fromCiv.validateGift();
        toCiv.validateGift();
        giftedTown.validateGift();

        DiplomacyGiftResponse dipResponse = new DiplomacyGiftResponse();
        dipResponse.giftedObject = giftedTown;
        dipResponse.fromCiv = fromCiv;
        dipResponse.toCiv = toCiv;

        sendGiftRequest(toCiv, fromCiv,
                CivSettings.localize.localizedString("var_cmd_civ_dipgift_townRequest1", fromCiv.getName(), giftedTown.getName(), giftedTown.getGiftCost(), CivSettings.CURRENCY_NAME), dipResponse);
        CivMessage.sendSuccess(sender, CivSettings.localize.localizedString("cmd_civ_dipgift_entirecivSuccess"));

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
        // permission checked in parent command.
    }

}
