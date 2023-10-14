/**
 * [2023] Cnlimiter LLC
 * All Rights Reserved.
 * Name: civcraft
 * Author: cnlimiter
 * UpdateTime: 2023/10/14 16:15
 * Description:
 * License: ARR
 */
package cn.evole.plugins.civcraft.interactive;

import cn.evole.plugins.civcraft.object.Resident;

public interface InteractiveResponse {
    void respond(String message, Resident resident);

    default boolean valid(String message) {
        if (!(message.length() >= 2 && message.length() <= 7)) {
            return false;
        }
        char[] chars = message.toCharArray();
        for (char s : chars) {
            Character.UnicodeBlock block = Character.UnicodeBlock.of(s);
            if (block != Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                    && block != Character.UnicodeBlock.BASIC_LATIN) {
                return false;
            }
        }
        return true;
    }
}
