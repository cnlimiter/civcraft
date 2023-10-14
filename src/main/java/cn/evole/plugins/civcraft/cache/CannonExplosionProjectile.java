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

import cn.evole.plugins.civcraft.components.PlayerProximityComponent;
import cn.evole.plugins.civcraft.exception.CivException;
import cn.evole.plugins.civcraft.main.CivData;
import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.structure.Buildable;
import cn.evole.plugins.civcraft.threading.TaskMaster;
import cn.evole.plugins.civcraft.threading.tasks.FireWorkTask;
import cn.evole.plugins.civcraft.util.BlockCoord;
import cn.evole.plugins.civcraft.util.ItemManager;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.LinkedList;
import java.util.Queue;

public class CannonExplosionProjectile {
    Location loc;
    Location target;
    int speed = 6;
    int damage = 40;
    int splash = 30;
    Buildable buildable;
    PlayerProximityComponent proximityComponent;

    public CannonExplosionProjectile(Buildable buildable, Location target) {
        proximityComponent = new PlayerProximityComponent();
        proximityComponent.createComponent(buildable);
        proximityComponent.setCenter(new BlockCoord(target));
    }

    public Vector getVectorBetween(Location to, Location from) {
        Vector dir = new Vector();

        dir.setX(to.getX() - from.getX());
        dir.setY(to.getY() - from.getY());
        dir.setZ(to.getZ() - from.getZ());

        return dir;
    }

    public boolean advance() {
        Vector dir = getVectorBetween(target, loc).normalize();
        double distance = loc.distanceSquared(target);
        dir.multiply(speed);

        loc.add(dir);
        loc.getWorld().createExplosion(loc, 0.0f, false);
        distance = loc.distanceSquared(target);
        BlockCoord center = proximityComponent.getCenter();
        center.setFromLocation(loc);

        if (distance < speed * 1.5) {
            loc.setX(target.getX());
            loc.setY(target.getY());
            loc.setZ(target.getZ());
            this.onHit();
            return true;
        }

        return false;
    }

    public void onHit() {

        int spread = 3;
        int[][] offset = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        for (int i = 0; i < 4; i++) {
            int x = offset[i][0] * spread;
            int y = 0;
            int z = offset[i][1] * spread;

            Location location = new Location(loc.getWorld(), loc.getX(), loc.getY(), loc.getZ());
            location = location.add(x, y, z);

            launchExplodeFirework(location);
            //loc.getWorld().createExplosion(location, 1.0f, true);
            setFireAt(location, spread);
        }

        launchExplodeFirework(loc);
        //loc.getWorld().createExplosion(loc, 1.0f, true);
        damagePlayers(loc, splash);
        setFireAt(loc, spread);
    }

    private void damagePlayers(Location loc, int radius) {
        class SyncTask implements Runnable {
            Queue<Player> playerQueue;
            Queue<Double> damageQueue;

            public SyncTask(Queue<Player> playerList, Queue<Double> damageList) {
                this.playerQueue = playerList;
                this.damageQueue = damageList;
            }

            @Override
            public void run() {
                Player player = playerQueue.poll();
                Double damage = damageQueue.poll();

                while (player != null && damage != null) {
                    player.damage(damage);

                    player = playerQueue.poll();
                    damage = damageQueue.poll();
                }
            }
        }

        class AsyncTask implements Runnable {

            int radius;

            public AsyncTask(int radius) {
                this.radius = radius;
            }

            @Override
            public void run() {
                Queue<Player> playerList = new LinkedList<Player>();
                Queue<Double> damageList = new LinkedList<Double>();

                //PlayerLocationCache.lock.lock();
                try {
                    for (PlayerLocationCache pc : PlayerLocationCache.getCache()) {
                        if (pc.getCoord().distanceSquared(new BlockCoord(target)) < radius) {
                            try {
                                Player player = CivGlobal.getPlayer(pc.getName());
                                playerList.add(player);
                                damageList.add(Double.valueOf(damage));
                            } catch (CivException e) {
                                //player offline
                            }

                        }
                    }

                    TaskMaster.syncTask(new SyncTask(playerList, damageList));

                } finally {
                    //	PlayerLocationCache.lock.unlock();
                }
            }
        }

        TaskMaster.asyncTask(new AsyncTask(radius), 0);
    }

    private void setFireAt(Location loc, int radius) {
        //Set the entire area on fire.
        for (int x = -radius; x < radius; x++) {
            for (int y = -3; y < 3; y++) {
                for (int z = -radius; z < radius; z++) {
                    Block block = loc.getWorld().getBlockAt(loc.getBlockX() + x, loc.getBlockY() + y, loc.getBlockZ() + z);
                    if (ItemManager.getId(block) == CivData.AIR) {
                        ItemManager.setTypeId(block, CivData.FIRE);
                        ItemManager.setData(block, 0, true);
                    }
                }
            }
        }
    }

    private void launchExplodeFirework(Location loc) {
        FireworkEffect fe = FireworkEffect.builder().withColor(Color.ORANGE).withColor(Color.YELLOW).flicker(true).with(Type.BURST).build();
        TaskMaster.syncTask(new FireWorkTask(fe, loc.getWorld(), loc, 3), 0);
    }


    public void setLocation(Location turretLoc) {
        this.loc = turretLoc;
    }

    public void setTargetLocation(Location location) {
        this.target = location;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public void setDamage(int damage) {
        this.damage = damage;
    }

    public void setSplash(int splash) {
        this.splash = splash;
    }

}
