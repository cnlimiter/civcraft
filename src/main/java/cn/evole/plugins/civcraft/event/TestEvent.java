/**
 * [2023] Cnlimiter LLC
 * All Rights Reserved.
 * Name: civcraft
 * Author: cnlimiter
 * UpdateTime: 2023/10/14 16:15
 * Description:
 * License: ARR
 */
package cn.evole.plugins.civcraft.event;

import cn.evole.plugins.civcraft.exception.InvalidConfiguration;
import cn.evole.plugins.civcraft.main.CivMessage;

import java.util.Calendar;

public class TestEvent implements EventInterface {

    @Override
    public void process() {
        CivMessage.global("This is a test event firing!");
    }

    @Override
    public Calendar getNextDate() throws InvalidConfiguration {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.SECOND, 60);
        return cal;
    }

}
