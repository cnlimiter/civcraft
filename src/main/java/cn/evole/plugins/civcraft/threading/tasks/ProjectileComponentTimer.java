/**
 * [2023] Cnlimiter LLC
 * All Rights Reserved.
 * Name: civcraft
 * Author: cnlimiter
 * UpdateTime: 2023/10/14 16:15
 * Description:
 * License: ARR
 */
package cn.evole.plugins.civcraft.threading.tasks;

import cn.evole.plugins.civcraft.components.Component;
import cn.evole.plugins.civcraft.components.ProjectileComponent;
import cn.evole.plugins.civcraft.main.CivGlobal;

import java.util.ArrayList;

public class ProjectileComponentTimer implements Runnable {

    @Override
    public void run() {

        try {
            if (!CivGlobal.towersEnabled) {
                return;
            }

            Component.componentsLock.lock();
            try {
                ArrayList<Component> projectileComponents = Component.componentsByType.get(ProjectileComponent.class.getName());

                if (projectileComponents == null) {
                    return;
                }

                for (Component c : projectileComponents) {
                    ProjectileComponent projectileComponent = (ProjectileComponent) c;
                    projectileComponent.process();
                }
            } finally {
                Component.componentsLock.unlock();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
