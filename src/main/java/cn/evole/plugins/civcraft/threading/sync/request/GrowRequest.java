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

import cn.evole.plugins.civcraft.structure.farm.FarmChunk;
import cn.evole.plugins.civcraft.structure.farm.GrowBlock;

import java.util.LinkedList;
import java.util.concurrent.locks.ReentrantLock;

public class GrowRequest extends AsyncRequest {

    public LinkedList<GrowBlock> growBlocks;
    public FarmChunk farmChunk;
    public GrowRequest(ReentrantLock lock) {
        super(lock);
    }

}
