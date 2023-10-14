package cn.evole.plugins.civcraft.endgame;

import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.config.ConfigEndCondition;
import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.main.CivLog;
import cn.evole.plugins.civcraft.main.CivMessage;
import cn.evole.plugins.civcraft.object.Civilization;
import cn.evole.plugins.civcraft.sessiondb.SessionEntry;
import cn.evole.plugins.civcraft.util.CivColor;

import java.util.ArrayList;
import java.util.HashMap;

public abstract class EndGameCondition {

    public static ArrayList<EndGameCondition> endConditions = new ArrayList<EndGameCondition>();
    public HashMap<String, String> attributes = new HashMap<String, String>();
    private String id;
    private String victoryName;

    public EndGameCondition() {
    }

    public static void init() {
        for (ConfigEndCondition configEnd : CivSettings.endConditions.values()) {
            String className = "cn.evole.plugins.civcraft.endgame." + configEnd.className;
            Class<?> someClass;

            try {
                someClass = Class.forName(className);
                EndGameCondition endCompClass;
                endCompClass = (EndGameCondition) someClass.newInstance();
                endCompClass.setId(configEnd.id);
                endCompClass.setVictoryName(configEnd.victoryName);
                endCompClass.attributes = configEnd.attributes;

                endCompClass.onLoad();
                endConditions.add(endCompClass);
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public static Civilization getCivFromSessionData(String data) {
        String[] split = data.split(":");
        return CivGlobal.getCivFromId(Integer.valueOf(split[0]));
    }

    public static void onCivilizationWarDefeat(Civilization civ) {
        for (EndGameCondition end : endConditions) {
            end.onWarDefeat(civ);
        }
    }

    public static EndGameCondition getEndCondition(String name) {
        for (EndGameCondition cond : endConditions) {
            if (cond.getId().equals(name)) {
                return cond;
            }
        }
        return null;
    }

    /* Called on start up to load any data in. */
    public abstract void onLoad();

    /* Returns true if the civilization given has met an end-game condition. */
    public abstract boolean check(Civilization civ);

    public abstract String getSessionKey();

    public void onVictoryReset(Civilization civ) {
    }

    /* Do one last check to see if it's ok to win.
     * Science and diplomatic victories require you to have the most
     * beakers/votes so this is needed.
     *
     * Returns true if its ok to win.
     */
    public boolean finalWinCheck(Civilization civ) {
        return true;
    }

    public void onSuccess(Civilization civ) {
        this.checkForWin(civ);
    }

    public void onFailure(Civilization civ) {
        ArrayList<SessionEntry> entries = CivGlobal.getSessionDB().lookup(getSessionKey());
        if (entries.size() == 0) {
            return;
        }

        for (SessionEntry entry : entries) {
            if (civ == EndGameCondition.getCivFromSessionData(entry.value)) {
                CivMessage.global(CivSettings.localize.localizedString("var_end_warLoss", CivColor.LightBlue + CivColor.BOLD + civ.getName() + CivColor.White, CivColor.LightPurple + CivColor.BOLD + this.victoryName + CivColor.White));
                CivGlobal.getSessionDB().delete(entry.request_id, entry.key);
                onVictoryReset(civ);
                return;
            }
        }

        CivLog.error("Couldn't find civilization:" + civ.getName() + " with id:" + civ.getId() + " to fail end condition:" + this.victoryName);
    }

    public String getString(String key) {
        return attributes.get(key);
    }

    public double getDouble(String key) {
        return Double.valueOf(attributes.get(key));
    }

    public void setAttribute(String key, String value) {
        attributes.put(key, value);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVictoryName() {
        return victoryName;
    }

    public void setVictoryName(String victoryName) {
        this.victoryName = victoryName;
    }

    /*
     * Returns true if this civ is currently awaiting a 2 week countdown after
     * meeting winning conditions.
     */
    public boolean isActive(Civilization civ) {
        ArrayList<SessionEntry> entries = CivGlobal.getSessionDB().lookup(getSessionKey());

        if (entries.size() == 0) {
            return false;
        }

        return true;
    }

    public int getDaysLeft(Civilization civ) {
        ArrayList<SessionEntry> entries = CivGlobal.getSessionDB().lookup(getSessionKey());
        if (entries.size() == 0) {
            return -1;
        }

        int daysToHold = getDaysToHold();
        Integer daysHeld = Integer.valueOf(entries.get(0).value);

        return daysToHold - daysHeld;
    }

    public int getDaysToHold() {
        return Integer.parseInt(this.getString("days_held"));
    }

    public void checkForWin(Civilization civ) {
        /* All win conditions are met, now check for time left. */
        // 满足所有获胜条件，现在检查剩余时间
        ArrayList<SessionEntry> entries = CivGlobal.getSessionDB().lookup(getSessionKey());

        int daysToHold = getDaysToHold();

        if (entries.size() == 0) {
            /* No entry yet, first time we hit the win condition, save entry. */
            // 尚无条目，我们第一次达到获胜条件时，保存条目
            civ.sessionAdd(getSessionKey(), getSessionData(civ, 0));
            civ.winConditionWarning(this, daysToHold);
        } else {
            /* Entries exists, check if enough days have passed. */
            // 条目存在，请检查是否已经过去了足够的时间。
            for (SessionEntry entry : entries) {
                /* this function only checks non-conquered civs. should be good enough for vic cond. */
                // 他的功能仅检查未征服的文明。 对于vic cond应该足够好
                if (EndGameCondition.getCivFromSessionData(entry.value) != civ) {
                    continue;
                }

                Integer daysHeld = this.getDaysHeldFromSessionData(entry.value);
                daysHeld++;

                if (daysHeld < daysToHold) {
                    civ.winConditionWarning(this, daysToHold - daysHeld);
                } else {
                    if (this.finalWinCheck(civ)) {
                        civ.declareAsWinner(this);
                    }
                }

                CivGlobal.getSessionDB().update(entries.get(0).request_id, entries.get(0).key, getSessionData(civ, daysHeld));
            }
        }

        return;
    }

    public String getSessionData(Civilization civ, Integer daysHeld) {
        return civ.getId() + ":" + daysHeld;
    }

    public Integer getDaysHeldFromSessionData(String data) {
        String[] split = data.split(":");
        return Integer.valueOf(split[1]);
    }

    protected abstract void onWarDefeat(Civilization civ);

}
