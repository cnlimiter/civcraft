/**
 * [2023] Cnlimiter LLC
 * All Rights Reserved.
 * Name: civcraft
 * Author: cnlimiter
 * UpdateTime: 2023/10/14 16:15
 * Description:
 * License: ARR
 */
package cn.evole.plugins.civcraft.threading.tasks;

import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.exception.CivException;
import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.main.CivMessage;
import cn.evole.plugins.civcraft.object.Civilization;
import cn.evole.plugins.civcraft.object.Resident;
import cn.evole.plugins.civcraft.questions.QuestionResponseInterface;
import cn.evole.plugins.civcraft.util.CivColor;
import org.bukkit.entity.Player;

public class CivQuestionTask implements Runnable {

    Civilization askedCiv; /* player who is being asked a question. */
    Civilization questionCiv; /* player who has asked the question. */
    String question; /* Question being asked. */
    long timeout; /* Timeout after question expires. */
    QuestionResponseInterface finishedFunction;

    private String response = new String(); /* Response to the question. */
    private Boolean responded = new Boolean(false); /*Question was answered. */

    public CivQuestionTask(Civilization askedciv, Civilization questionciv, String question, long timeout,
                           QuestionResponseInterface finishedFunction) {

        this.askedCiv = askedciv;
        this.questionCiv = questionciv;
        this.question = question;
        this.timeout = timeout;
        this.finishedFunction = finishedFunction;

    }

    public void askPlayer(Player player) {
        CivMessage.send(player, CivColor.LightGray + CivSettings.localize.localizedString("civleaderQtast_prompt1") + " " + CivColor.LightBlue + questionCiv.getName());
        CivMessage.send(player, question);
        CivMessage.send(player, CivColor.LightGray + CivSettings.localize.localizedString("civQtast_prompt2"));
    }

    public void notifyExpired(Player player) {
        CivMessage.send(player, CivColor.LightGray + CivSettings.localize.localizedString("var_civQtast_offerExpired", questionCiv.getName()));
    }


    @Override
    public void run() {
        for (Resident res : askedCiv.getLeaderGroup().getMemberList()) {
            try {
                askPlayer(CivGlobal.getPlayer(res));
            } catch (CivException e) {
            }
        }

        for (Resident res : askedCiv.getAdviserGroup().getMemberList()) {
            try {
                askPlayer(CivGlobal.getPlayer(res));
            } catch (CivException e) {
            }
        }

        try {
            synchronized (this) {
                this.wait(timeout);
            }
        } catch (InterruptedException e) {
            cleanup();
            return;
        }

        if (responded) {
            finishedFunction.processResponse(response);
            cleanup();
            return;
        }

        for (Resident res : askedCiv.getLeaderGroup().getMemberList()) {
            try {
                notifyExpired(CivGlobal.getPlayer(res));
            } catch (CivException e) {
            }
        }

        for (Resident res : askedCiv.getAdviserGroup().getMemberList()) {
            try {
                notifyExpired(CivGlobal.getPlayer(res));
            } catch (CivException e) {
            }
        }

        CivMessage.sendCiv(questionCiv, CivColor.LightGray + CivSettings.localize.localizedString("var_civQtast_NoResponse", askedCiv.getName()));
        cleanup();
    }

    public Boolean getResponded() {
        synchronized (responded) {
            return responded;
        }
    }

    public void setResponded(Boolean response) {
        synchronized (this.responded) {
            this.responded = response;
        }
    }

    public String getResponse() {
        synchronized (response) {
            return response;
        }
    }

    public void setResponse(String response) {
        synchronized (this.response) {
            setResponded(true);
            this.response = response;
        }
    }

    /* When this task finishes, remove itself from the hashtable. */
    private void cleanup() {
        CivGlobal.removeRequest(askedCiv.getName());
    }
}
