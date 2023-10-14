/**
 * [2023] Cnlimiter LLC
 * All Rights Reserved.
 * Name: civcraft
 * Author: cnlimiter
 * UpdateTime: 2023/10/14 16:15
 * Description:
 * License: ARR
 */
package cn.evole.plugins.civcraft.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class CivCache {

    /* Arrows fired that need to be updated. */
    public static Map<UUID, ArrowFiredCache> arrowsFired = new HashMap<UUID, ArrowFiredCache>();

    /* Cannon balls fired that need to be updated. */
    public static Map<UUID, CannonFiredCache> cannonBallsFired = new HashMap<UUID, CannonFiredCache>();


}
