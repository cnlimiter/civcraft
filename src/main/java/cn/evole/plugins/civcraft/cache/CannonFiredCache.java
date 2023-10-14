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

import cn.evole.plugins.civcraft.structure.Buildable;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;

import java.util.Calendar;
import java.util.UUID;

public class CannonFiredCache {
    private Buildable whoFired;
    private Location target;
    private Fireball fireball;
    private UUID uuid;
    private Calendar expired;
    private boolean hit = false;

    public CannonFiredCache(Buildable whoFired, Location target, Fireball fireball) {
        this.whoFired = whoFired;
        this.target = target;
        this.fireball = fireball;
        this.uuid = fireball.getUniqueId();
        expired = Calendar.getInstance();
        expired.set(Calendar.SECOND, 30);
    }

    public Buildable getWhoFired() {
        return whoFired;
    }

    public void setWhoFired(Buildable whoFired) {
        this.whoFired = whoFired;
    }

    public Location getTarget() {
        return target;
    }

    public void setTarget(Location target) {
        this.target = target;
    }

    public Fireball getFireball() {
        return fireball;
    }

    public void setFireball(Fireball fireball) {
        this.fireball = fireball;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public Calendar getExpired() {
        return expired;
    }

    public void setExpired(Calendar expired) {
        this.expired = expired;
    }

    public boolean isHit() {
        return hit;
    }

    public void setHit(boolean hit) {
        this.hit = hit;
    }

    public void destroy(Entity damager) {
        fireball.remove();
        this.fireball = null;
        CivCache.cannonBallsFired.remove(this.getUuid());
        this.uuid = null;
    }


}
