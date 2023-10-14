/**
 * [2023] Cnlimiter LLC
 * All Rights Reserved.
 * Name: civcraft
 * Author: cnlimiter
 * UpdateTime: 2023/10/14 16:15
 * Description:
 * License: ARR
 */
package cn.evole.plugins.civcraft.components;

import cn.evole.plugins.civcraft.structure.Buildable;

import java.util.ArrayList;

public class RegisterComponentAsync implements Runnable {

    public Buildable buildable;
    public Component component;
    public String name;
    boolean register;

    public RegisterComponentAsync(Buildable buildable, Component component, String name, boolean register) {
        this.buildable = buildable;
        this.component = component;
        this.name = name;
        this.register = register;
    }


    @Override
    public void run() {

        if (register) {
            Component.componentsLock.lock();
            try {
                ArrayList<Component> components = Component.componentsByType.get(name);

                if (components == null) {
                    components = new ArrayList<Component>();
                }

                components.add(component);
                Component.componentsByType.put(name, components);
                if (buildable != null) {
                    buildable.attachedComponents.add(component);
                }
            } finally {
                Component.componentsLock.unlock();
            }
        } else {
            Component.componentsLock.lock();
            try {
                ArrayList<Component> components = Component.componentsByType.get(name);

                if (components == null) {
                    return;
                }

                components.remove(component);
                Component.componentsByType.put(name, components);
                if (buildable != null) {
                    buildable.attachedComponents.remove(component);
                }
            } finally {
                Component.componentsLock.unlock();
            }
        }

    }


}
