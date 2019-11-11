
package com.avrgaming.civcraft.util;

import java.util.Calendar;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;
import com.avrgaming.civcraft.main.CivCraft;

public class Levitate {
    public static float VELOCITY_CHANGE_PER_TICK = 0.165f;
    protected int taskID;
    protected long startTime;
    protected String name;
    private Player player;
    private long levitateTime;

    public Levitate(Player player, long levitateTime) {
        this.player = player;
        this.levitateTime = levitateTime;
        this.name = player.getName();
    }

    public void start() {
        if (!this.player.isOnline()) {
            throw new IllegalStateException("Player is offline");
        }
        this.startTime = Calendar.getInstance().getTimeInMillis();
        this.player.setAllowFlight(true);
        this.taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask((Plugin)CivCraft.getPlugin(), () -> {
            if (!this.player.isOnline() || this.player.isDead()) {
                Bukkit.getScheduler().cancelTask(this.taskID);
                return;
            }
            this.player.setVelocity(new Vector(0.0f, VELOCITY_CHANGE_PER_TICK, 0.0f));
            if (Calendar.getInstance().getTimeInMillis() - this.startTime >= this.levitateTime) {
                this.player.setFlying(false);
                this.player.setAllowFlight(false);
                Bukkit.getScheduler().cancelTask(this.taskID);
            }
        }
        , 0L, 0L);
    }

    public void stop() {
    }

    class LevitateDown {
        protected int taskDownID;
        protected long startDownTime;
        protected String plName;
        private Player playerToDown;
        private long levitateDownTime;

        public LevitateDown(Player player, long levitateTime) {
            this.playerToDown = player;
            this.levitateDownTime = levitateTime;
            this.plName = player.getName();
        }

        public void start() {
            if (!this.playerToDown.isOnline()) {
                throw new IllegalStateException("Player is offline");
            }
            this.startDownTime = Calendar.getInstance().getTimeInMillis();
            this.taskDownID = Bukkit.getScheduler().scheduleSyncRepeatingTask((Plugin)CivCraft.getPlugin(), () -> {
                if (!this.playerToDown.isOnline()) {
                    Bukkit.getScheduler().cancelTask(this.taskDownID);
                    return;
                }
                this.playerToDown.setVelocity(new Vector(0.0f, - Levitate.VELOCITY_CHANGE_PER_TICK * 2.0f, 0.0f));
                if (Calendar.getInstance().getTimeInMillis() - this.startDownTime >= this.levitateDownTime) {
                    this.playerToDown.setFallDistance(0.0f);
                    this.playerToDown.setFlying(false);
                    this.playerToDown.setAllowFlight(false);
                    Bukkit.getScheduler().cancelTask(this.taskDownID);
                }
            }
            , 0L, 0L);
        }
    }

}

