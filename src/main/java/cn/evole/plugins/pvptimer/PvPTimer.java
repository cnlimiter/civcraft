package cn.evole.plugins.pvptimer;

import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.exception.InvalidConfiguration;
import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.main.CivMessage;
import cn.evole.plugins.civcraft.object.Resident;
import cn.evole.plugins.civcraft.util.CivColor;
import cn.evole.plugins.civcraft.util.DateUtil;

import java.util.Date;

public class PvPTimer implements Runnable {

    @Override
    public void run() {

        for (Resident resident : CivGlobal.getResidents()) {
            if (!resident.isProtected()) {
                continue;
            }

            int mins;
            try {
                mins = CivSettings.getInteger(CivSettings.civConfig, "global.pvp_timer");
                if (DateUtil.isAfterMins(new Date(resident.getRegistered()), mins)) {
                    //if (DateUtil.isAfterSeconds(new Date(resident.getRegistered()), mins)) {
                    resident.setisProtected(false);
                    CivMessage.send(resident, CivColor.LightGray + CivSettings.localize.localizedString("pvpTimerEnded"));
                }
            } catch (InvalidConfiguration e) {
                e.printStackTrace();
                return;
            }
        }
    }
}
