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

import java.util.concurrent.locks.ReentrantLock;

public class LoadChunkRequest extends AsyncRequest {

    public String worldName;
    public int x;
    public int z;
    public LoadChunkRequest(ReentrantLock lock) {
        super(lock);
    }

}
