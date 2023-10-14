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

public class GetChestRequest extends AsyncRequest {

    public int block_x;
    public int block_y;
    public int block_z;
    public String worldName;
    public GetChestRequest(ReentrantLock lock) {
        super(lock);
    }

}
