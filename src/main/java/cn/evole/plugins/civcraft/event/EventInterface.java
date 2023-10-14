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

import java.util.Calendar;

public interface EventInterface {
    void process();

    Calendar getNextDate() throws InvalidConfiguration;
}
