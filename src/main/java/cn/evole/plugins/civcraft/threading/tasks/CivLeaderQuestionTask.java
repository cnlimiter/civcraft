package cn.evole.plugins.civcraft.threading.tasks;

import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.main.CivMessage;
import cn.evole.plugins.civcraft.object.Civilization;
import cn.evole.plugins.civcraft.object.Resident;
import cn.evole.plugins.civcraft.questions.QuestionBaseTask;
import cn.evole.plugins.civcraft.questions.QuestionResponseInterface;
import cn.evole.plugins.civcraft.util.CivColor;
import org.bukkit.entity.Player;

public class CivLeaderQuestionTask extends QuestionBaseTask implements Runnable {
    protected String response = new String(); /* Response to the question. */
    protected Boolean responded = new Boolean(false); /*Question was answered. */
    Civilization askedCivilization; /* player who is being asked a question. */
    Player questionPlayer; /* player who has asked the question. */
    String question; /* Question being asked. */
    long timeout; /* Timeout after question expires. */
    //	RunnableWithArg finishedTask; /* Task to run when a response has been generated. */
    QuestionResponseInterface finishedFunction;
    Resident responder;


    public CivLeaderQuestionTask(Civilization askedplayer, Player questionplayer, String question, long timeout,
                                 QuestionResponseInterface finishedFunction) {

        this.askedCivilization = askedplayer;
        this.questionPlayer = questionplayer;
        this.question = question;
        this.timeout = timeout;
        this.finishedFunction = finishedFunction;

    }

    @Override
    public void run() {

        for (Resident resident : askedCivilization.getLeaderGroup().getMemberList()) {
            CivMessage.send(resident, CivColor.LightGray + CivSettings.localize.localizedString("civleaderQtast_prompt1") + " " + CivColor.LightBlue + questionPlayer.getName());
            CivMessage.send(resident, CivColor.LightPurple + CivColor.BOLD + question);
            CivMessage.send(resident, CivColor.LightGray + CivSettings.localize.localizedString("civleaderQtast_prompt2"));
        }

        try {
            synchronized (this) {
                this.wait(timeout);
            }
        } catch (InterruptedException e) {
            CivMessage.send(questionPlayer, CivColor.LightGray + CivSettings.localize.localizedString("civleaderQtast_interrupted"));
            cleanup();
            return;
        }

        if (responded) {
            finishedFunction.processResponse(response, responder);
            cleanup();
            return;
        }

        CivMessage.send(questionPlayer, CivColor.LightGray + CivSettings.localize.localizedString("civleaderQtast_noResponse"));
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
        CivGlobal.removeQuestion("civ:" + askedCivilization.getName());
    }

    public void setResponder(Resident resident) {
        this.responder = resident;
    }
}
