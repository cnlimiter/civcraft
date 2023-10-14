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
import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.object.Resident;
import cn.evole.plugins.civcraft.object.Town;

import java.util.ArrayList;

public class PlotPermissions {

    public PermissionNode build = new PermissionNode("build");
    public PermissionNode destroy = new PermissionNode("destroy");
    public PermissionNode interact = new PermissionNode("interact");
    public PermissionNode itemUse = new PermissionNode("itemUse");
    private boolean fire, spawner_mobs, custom_spawn_mobs;
    /*
     * Owner of this permission node.
     */
    private Resident owner;
    /*
     * Group for this permission node.
     */
    //private PermissionGroup group;
    private ArrayList<PermissionGroup> groups = new ArrayList<PermissionGroup>();

    public String getSaveString() {

        String ownerString = "";
        if (owner != null) {
            ownerString = owner.getName();
        }

        String groupString = "0";
        if (groups.size() != 0) {
            for (PermissionGroup grp : groups) {
                groupString += grp.getId() + ":";
            }
        }
        return build.getSaveString() + "," + destroy.getSaveString() + "," + interact.getSaveString() + "," + itemUse.getSaveString() + ","
                + ownerString + "," + groupString + "," + fire + "," + spawner_mobs + "," + custom_spawn_mobs;
    }

    public void loadFromSaveString(Town town, String src) throws CivException {
        String[] split = src.split(",");

        build.loadFromString(split[0]);
        destroy.loadFromString(split[1]);
        interact.loadFromString(split[2]);
        itemUse.loadFromString(split[3]);

        setOwner(CivGlobal.getResident(split[4]));
        String[] grpString = split[5].split(":");

        for (String gstr : grpString) {
            gstr = gstr.trim();
            if (gstr.equals("0") || gstr.equals("")) {
                continue;
            }
            PermissionGroup group = CivGlobal.getPermissionGroup(town, Integer.valueOf(gstr));
            addGroup(group);
        }

        if (split.length > 7) {
            fire = Boolean.parseBoolean(split[6]);
            spawner_mobs = Boolean.parseBoolean(split[7]);
            custom_spawn_mobs = Boolean.parseBoolean(split[8]);
        }

        //	group = CivGlobal.getPermissionGroup(town, Integer.valueOf(split[5]));

    }

    public boolean isFire() {
        return fire;
    }

    public void setFire(boolean fire) {
        this.fire = fire;
    }

    public boolean isSpawnerMobs() {
        return spawner_mobs;
    }

    public void setSpawnerMobs(boolean spawner_mobs) {
        this.spawner_mobs = spawner_mobs;
    }

    public boolean isCustomSpawnMobs() {
        return custom_spawn_mobs;
    }

    public void setCustomSpawnMobs(boolean custom_spawn_mobs) {
        this.custom_spawn_mobs = custom_spawn_mobs;
    }

    public String getBuildString() {
        return build.getString();
    }

    public String getDestroyString() {
        return destroy.getString();
    }

    public String getInteractString() {
        return interact.getString();
    }

    public String getItemUseString() {
        return itemUse.getString();
    }

    public Resident getOwner() {
        return owner;
    }

    public void setOwner(Resident owner) {
        this.owner = owner;
    }

    private boolean checkPermissionNode(PermissionNode node, Resident resident) {
        if (node != null) {
            if (owner == resident && node.isPermitOwner())
                return true;

            if (owner != null && owner.isFriend(resident) && node.isPermitOwner())
                return true;

            if (groups.size() != 0 && node.isPermitGroup()) {
                for (PermissionGroup group : groups) {
                    if (group.hasMember(resident)) {
                        return true;
                    }
                }
            }

            if (node.isPermitOthers()) {
                return true;
            }
        }
        return false;
    }

    public boolean hasPermission(Type type, Resident resident) {
        if (resident.isPermOverride()) {
            return true;
        }

        switch (type) {
            case BUILD:
                return checkPermissionNode(this.build, resident);
            case DESTROY:
                return checkPermissionNode(this.destroy, resident);
            case INTERACT:
                return checkPermissionNode(this.interact, resident);
            case ITEMUSE:
                return checkPermissionNode(this.itemUse, resident);
            default:
                break;
        }

        return false;
    }

    public void addGroup(PermissionGroup grp) {
        if (grp == null) {
            return;
        }

        if (!groups.contains(grp)) {
            groups.add(grp);
        }
    }

    public void removeGroup(PermissionGroup grp) {
        groups.remove(grp);
    }

    public ArrayList<PermissionGroup> getGroups() {
        return this.groups;
    }

    public void resetPerms() {
        build.setPermitOwner(true);
        build.setPermitGroup(true);
        build.setPermitOthers(false);

        destroy.setPermitOwner(true);
        destroy.setPermitGroup(true);
        destroy.setPermitOthers(false);

        interact.setPermitOwner(true);
        interact.setPermitGroup(true);
        interact.setPermitOthers(false);

        itemUse.setPermitOwner(true);
        itemUse.setPermitGroup(true);
        itemUse.setPermitOthers(false);
    }

    public String getGroupString() {
        String out = "";

        for (PermissionGroup grp : groups) {
            out += grp.getName() + ", ";
        }
        return out;
    }

    public void clearGroups() {
        this.groups.clear();
    }

    public void replaceGroups(PermissionGroup defaultGroup) {
        this.groups.clear();
        this.addGroup(defaultGroup);
    }

    public enum Type {
        BUILD,
        DESTROY,
        INTERACT,
        ITEMUSE
    }

}
