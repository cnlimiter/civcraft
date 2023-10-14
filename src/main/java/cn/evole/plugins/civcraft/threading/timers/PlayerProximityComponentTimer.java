/**
 * [2023] Cnlimiter LLC
 * All Rights Reserved.
 * Name: civcraft
 * Author: cnlimiter
 * UpdateTime: 2023/10/14 16:15
 * Description:
 * License: ARR
 */
package cn.evole.plugins.civcraft.threading.timers;

import cn.evole.plugins.civcraft.cache.PlayerLocationCache;
import cn.evole.plugins.civcraft.components.Component;
import cn.evole.plugins.civcraft.components.PlayerProximityComponent;

import java.util.ArrayList;

/**
 * 1s一次
 * 修复组件什么的？ 新版本没有
 */
public class PlayerProximityComponentTimer implements Runnable {

    @Override
    public void run() {

        /*
         * Grab all of the player proximity components and update them, this task
         * runs asynchronously once per tick and gathers all of the player locations
         * into an async friendly data-structure.
         */

        Component.componentsLock.lock();
        try {
            ArrayList<Component> proximityComponents = Component.componentsByType.get(PlayerProximityComponent.class.getName());

            if (proximityComponents == null) {
                return;
            }

            /*
             * Wait for the lock to free up before we continue;
             */
            for (Component comp : proximityComponents) {
                if (comp instanceof PlayerProximityComponent) {
                    PlayerProximityComponent ppc = (PlayerProximityComponent) comp;

                    if (ppc.lock.tryLock()) {
                        try {
                            ppc.buildNearbyPlayers(PlayerLocationCache.getCache());
                        } finally {
                            ppc.lock.unlock();
                        }
                    }
                }
            }
        } finally {
            Component.componentsLock.unlock();
        }
    }

}
