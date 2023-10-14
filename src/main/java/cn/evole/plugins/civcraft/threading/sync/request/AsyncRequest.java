/**
 * [2023] Cnlimiter LLC
 * All Rights Reserved.
 * Name: civcraft
 * Author: cnlimiter
 * UpdateTime: 2023/10/14 16:15
 * Description:
 * License: ARR
 */
package cn.evole.plugins.civcraft.threading.sync.request;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class AsyncRequest {

    public Condition condition;
    public Boolean finished = false;
    public Object result = null;
    public AsyncRequest(ReentrantLock lock) {
        condition = lock.newCondition();
    }

}
