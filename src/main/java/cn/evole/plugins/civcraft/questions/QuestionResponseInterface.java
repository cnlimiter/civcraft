/**
 * [2023] Cnlimiter LLC
 * All Rights Reserved.
 * Name: civcraft
 * Author: cnlimiter
 * UpdateTime: 2023/10/14 16:15
 * Description:
 * License: ARR
 */
package cn.evole.plugins.civcraft.questions;

import cn.evole.plugins.civcraft.object.Resident;

public interface QuestionResponseInterface {
    void processResponse(String param);

    void processResponse(String response, Resident responder);
}
