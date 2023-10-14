/**
 * [2023] Cnlimiter LLC
 * All Rights Reserved.
 * Name: civcraft
 * Author: cnlimiter
 * UpdateTime: 2023/10/14 16:15
 * Description:
 * License: ARR
 */
package cn.evole.plugins.civcraft.object;

import cn.evole.plugins.civcraft.exception.InvalidNameException;


public class NamedObject {

    /* Unique Id of named object. */
    private int id;

    /* Display name of the object. */
    private String name;

    public String getName() {
        return this.name;
    }

    public void setName(String newname) throws InvalidNameException {
        validateName(newname);
        this.name = newname;
    }

    public int getId() {
        return id;
    }

    public void setId(int i) {
        this.id = i;
    }

    private void validateName(String name) throws InvalidNameException {
        if (name == null) {
            throw new InvalidNameException();
        }

        switch (name.toLowerCase()) {
            case "":
            case "null":
            case "none":
            case "town":
            case "group":
            case "civ":
            case "resident":
                throw new InvalidNameException(name);
        }
    }
}
