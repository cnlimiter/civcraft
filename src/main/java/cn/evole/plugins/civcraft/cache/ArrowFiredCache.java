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

import cn.evole.plugins.civcraft.components.ProjectileArrowComponent;
import org.bukkit.Location;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;

import java.util.Calendar;
import java.util.UUID;

public class ArrowFiredCache {
    private ProjectileArrowComponent fromTower;
    private Location target;
    private Entity targetEntity;
    private Arrow arrow;
    private UUID uuid;
    private Calendar expired;
    private boolean hit = false;

    public ArrowFiredCache(ProjectileArrowComponent tower, Entity targetEntity, Arrow arrow) {
        this.setFromTower(tower);
        this.target = targetEntity.getLocation();
        this.targetEntity = targetEntity;
        this.setArrow(arrow);
        this.uuid = arrow.getUniqueId();
        expired = Calendar.getInstance();
        expired.add(Calendar.SECOND, 5);
    }


    /**
     * @return the target
     */
    public Location getTarget() {
        return target;
    }

    /**
     * @param target the target to set
     */
    public void setTarget(Location target) {
        this.target = target;
    }

    /**
     * @return the arrow
     */
    public Arrow getArrow() {
        return arrow;
    }

    /**
     * @param arrow the arrow to set
     */
    public void setArrow(Arrow arrow) {
        this.arrow = arrow;
    }

    public Object getUUID() {
        return uuid;
    }

    public void destroy(Arrow arrow) {
        arrow.remove();
        this.arrow = null;
        CivCache.arrowsFired.remove(this.getUUID());
        this.uuid = null;
    }


    public void destroy(Entity damager) {
        if (damager instanceof Arrow) {
            this.destroy((Arrow) damager);
        }
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


    public ProjectileArrowComponent getFromTower() {
        return fromTower;
    }


    public void setFromTower(ProjectileArrowComponent fromTower) {
        this.fromTower = fromTower;
    }


    public Entity getTargetEntity() {
        return targetEntity;
    }


    public void setTargetEntity(Entity targetEntity) {
        this.targetEntity = targetEntity;
    }


}
