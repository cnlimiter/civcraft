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

                double memberHours = 0;

                boolean noanarchy = false;
                for (Town t : civ.getTowns()) {
                    //Get the Count of Residents in each town
                    double residentHours = t.getResidentCount();
                    double modifier = 1.0;
                    //If the town has a broadcast tower, reduce the modifer by the buff_reduced_anarchy value
                    //如果镇上有广播塔，则通过减少buff减少无政府状态值来减少修饰符
                    if (t.getBuffManager().hasBuff("buff_reduced_anarchy")) {
                        modifier -= t.getBuffManager().getEffectiveDouble("buff_reduced_anarchy");
                    }

                    //If the civ has a Notre Dame, reduce the modifer by the buff_notre_dame_no_anarchy value
                    if (t.getBuffManager().hasBuff("buff_notre_dame_no_anarchy")) {
                        modifier -= t.getBuffManager().getEffectiveDouble("buff_notre_dame_no_anarchy");
                        noanarchy = true;
                    }
                    //Reduce the number of resident hours by the modifier, then add it to the member hours
                    memberHours += (residentHours * modifier*2);
                }
                //Get the maxAnarchy from the governments.yml (Dmefault 24 hours)
                //从governments.yml获取maxAnarchy（默认24小时）
                double maxAnarchy = CivSettings.getIntegerGovernment("anarchy_duration");

                if (noanarchy) {
                    //If the civ has completed Notre Dame, reduce the maxAnarchy to the lower penalty from the governments.yml (Default 2 hours)
                    //如果文明完成了巴黎圣母院，则将maxAnarchy降低至governments.yml的较低罚款（默认2小时）(屁 明明是12
                    maxAnarchy = CivSettings.getIntegerGovernment("notre_dame_max_anarchy");
                }
                //Finally, calculate the number of hours, taking the lower memberHours or maxAnarchy
                // 最后，计算小时数，取较低的MemberHours或maxAnarc hy
                double anarchyHours = Math.min(memberHours, maxAnarchy);

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
