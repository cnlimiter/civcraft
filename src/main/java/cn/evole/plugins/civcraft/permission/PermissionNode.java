/**
 * [2023] Cnlimiter LLC
 * All Rights Reserved.
 * Name: civcraft
 * Author: cnlimiter
 * UpdateTime: 2023/10/14 16:15
 * Description:
 * License: ARR
 */
package cn.evole.plugins.civcraft.permission;

import cn.evole.plugins.civcraft.exception.CivException;

/*
 * Modeled after Unix permissions, each node has an owner, and group. The permission node is named
 * so we can create any class of permissions we need. Each node can be configured to allow the owner, group,
 * or others.
 *
 * For example, if we had a "build" permission, and "netizen539" was the owner, and "toydolls0101" and "toyguest"
 * were in the group. And we set permitOwner to true, and permitGroup to true and permitOthers to false.
 *
 * The netizen, toydolls0101, and toyguest could all "build" but robosnail could not.
 *
 */
public class PermissionNode {

    /*
     * Named type of permission. For example "build" or "destroy"
     */
    private String type;

    /*
     * Permissions flags, equivalent to Unix 'r,w,x'
     */
    private boolean permitOwner;
    private boolean permitGroup;
    private boolean permitOthers;


    public PermissionNode(String type) {
        this.setType(type);
        permitOwner = true;
        permitGroup = true;
        permitOthers = false;
    }

    public String getSaveString() {
        return type + ":" + permitOwner + ":" + permitGroup + ":" + permitOthers;
    }

    public void loadFromString(String src) throws CivException {
        String[] split = src.split(":");
        setType(split[0]);

        permitOwner = Boolean.valueOf(split[1]);
        permitGroup = Boolean.valueOf(split[2]);
        permitOthers = Boolean.valueOf(split[3]);

    }

    public boolean isPermitOwner() {
        return permitOwner;
    }

    public void setPermitOwner(boolean permitOwner) {
        this.permitOwner = permitOwner;
    }

    public boolean isPermitGroup() {
        return permitGroup;
    }

    public void setPermitGroup(boolean permitGroup) {
        this.permitGroup = permitGroup;
    }

    public boolean isPermitOthers() {
        return permitOthers;
    }

    public void setPermitOthers(boolean permitOthers) {
        this.permitOthers = permitOthers;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getString() {
        String ret = "";
        if (isPermitOwner())
            ret += "Owner: yes ";
        else
            ret += "Owner: no ";

        if (isPermitGroup())
            ret += "Group: yes ";
        else
            ret += "Group: no ";

        if (isPermitOthers())
            ret += "Others: yes ";
        else
            ret += "Others: no ";

        return ret;
    }

}
