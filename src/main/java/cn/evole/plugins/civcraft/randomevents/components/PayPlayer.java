package cn.evole.plugins.civcraft.randomevents.components;


import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.main.CivLog;
import cn.evole.plugins.civcraft.main.CivMessage;
import cn.evole.plugins.civcraft.object.Resident;
import cn.evole.plugins.civcraft.randomevents.RandomEventComponent;

public class PayPlayer extends RandomEventComponent {

    @Override
    public void process() {
        String playerName = this.getParent().componentVars.get(getString("playername_var"));
        if (playerName == null) {
            CivLog.warning("No playername var for pay player.");
            return;
        }

        Resident resident = CivGlobal.getResident(playerName);
        double coins = this.getDouble("amount");
        resident.getTreasury().deposit(coins);
        CivMessage.send(resident, CivSettings.localize.localizedString("resident_paid") + " " + coins + " " + CivSettings.CURRENCY_NAME);
    }

}
