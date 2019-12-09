/*************************************************************************
 *
 * AVRGAMING LLC
 * __________________
 *
 *  [2013] AVRGAMING LLC
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of AVRGAMING LLC and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to AVRGAMING LLC
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from AVRGAMING LLC.
 */
package com.avrgaming.civcraft.threading.timers;

import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.object.Civilization;
import com.avrgaming.civcraft.object.Town;
import com.avrgaming.civcraft.sessiondb.SessionEntry;

import java.util.ArrayList;

/**
 * 更改政体的timer
 */
public class ChangeGovernmentTimer implements Runnable {

    @Override
    public void run() {

        // For each town in anarchy, search the session DB for it's timer.
        // 对于无政府状态的每个城镇，在session数据库中搜索其计时器。
        for (Civilization civ : CivGlobal.getCivs()) {
            if (civ.getGovernment().id.equalsIgnoreCase("gov_anarchy")) {
                String key = "changegov_" + civ.getId();
                ArrayList<SessionEntry> entries;

                entries = CivGlobal.getSessionDB().lookup(key);
                if (entries == null || entries.size() < 1) {
                    //We are in anarchy but didn't have a sessiondb entry? huh...
                    // 我们处于无政府状态，但是没有sessiondb条目？ 嗯...
                    civ.setGovernment("gov_tribalism");
                    return;
                    //throw new TownyException("Town "+town.getName()+" in anarchy but cannot find its session DB entry with key:"+key);
                }

                SessionEntry se = entries.get(0);
                // Our Hour
                int duration = 3600;
                if (CivGlobal.testFileFlag("debug")) {
                    duration = 1;
                }
                double townHours = 0;
                boolean noanarchy = false;
                double notreDameValue = 0.0;
                for (Town t : civ.getTowns()) {
                    double modifier = 1.0;
                    if (t.getBuffManager().hasBuff("buff_reduced_anarchy")) {
                        modifier -= t.getBuffManager().getEffectiveDouble("buff_reduced_anarchy");
                    }

                    if (t.getBuffManager().hasBuff("buff_notre_dame_no_anarchy")) {
                        notreDameValue = t.getBuffManager().getEffectiveDouble("buff_notre_dame_no_anarchy");
                        noanarchy = true;
                    }
                    townHours += modifier * 4;
                }
                double baseAnarchy = CivSettings.getIntegerGovernment("base_anarchy_duration");
                double anarchyHours = baseAnarchy + townHours;
                if (noanarchy) {
                    anarchyHours *= 1 - notreDameValue;
                }
                double maxAnarchy = CivSettings.getIntegerGovernment("max_anarchy");
                anarchyHours = Math.min(anarchyHours, maxAnarchy);
                //Check if enough time has elapsed in seconds since the anarchy started
                //检查自无政府状态开始以来是否已经过去了足够的时间
                if (CivGlobal.hasTimeElapsed(se, anarchyHours * duration)) {
                    civ.setGovernment(se.value);
                    CivMessage.global(CivSettings.localize.localizedString("var_gov_emergeFromAnarchy", civ.getName(), CivSettings.governments.get(se.value).displayName));
                    CivGlobal.getSessionDB().delete_all(key);
                    civ.save();
                }
            }
        }
    }

}
