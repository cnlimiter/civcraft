package cn.evole.plugins.civcraft.siege;

import cn.evole.plugins.civcraft.camp.CampBlock;
import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.exception.CivException;
import cn.evole.plugins.civcraft.exception.InvalidConfiguration;
import cn.evole.plugins.civcraft.main.CivData;
import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.main.CivMessage;
import cn.evole.plugins.civcraft.object.Resident;
import cn.evole.plugins.civcraft.object.StructureBlock;
import cn.evole.plugins.civcraft.structure.Buildable;
import cn.evole.plugins.civcraft.structure.TownHall;
import cn.evole.plugins.civcraft.threading.TaskMaster;
import cn.evole.plugins.civcraft.threading.tasks.FireWorkTask;
import cn.evole.plugins.civcraft.util.BlockCoord;
import cn.evole.plugins.civcraft.util.CivColor;
import cn.evole.plugins.civcraft.util.EntityProximity;
import cn.evole.plugins.civcraft.util.ItemManager;
import cn.evole.plugins.civcraft.war.WarRegen;
import net.minecraft.server.v1_12_R1.EntityPlayer;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;

public class CannonProjectile {
    public static double yield;
    public static double playerDamage;
    public static double maxRange;
    public static int controlBlockHP;
    public static BlockCoord bcoord = new BlockCoord();

    static {
        try {
            yield = CivSettings.getDouble(CivSettings.warConfig, "cannon.yield");
            playerDamage = CivSettings.getDouble(CivSettings.warConfig, "cannon.player_damage");
            maxRange = CivSettings.getDouble(CivSettings.warConfig, "cannon.max_range");
            controlBlockHP = CivSettings.getInteger(CivSettings.warConfig, "cannon.control_block_hp");
        } catch (InvalidConfiguration e) {
            e.printStackTrace();
        }
    }

    public Cannon cannon;
    public Location loc;
    public Resident whoFired;
    public double speed = 1.0f;
    private Location startLoc;

    public CannonProjectile(Cannon cannon, Location loc, Resident whoFired) {
        this.cannon = cannon;
        this.loc = loc;
        this.startLoc = loc.clone();
        this.whoFired = whoFired;
    }

    private void explodeBlock(Block b) {
        WarRegen.explodeThisBlock(b, Cannon.RESTORE_NAME);
        launchExplodeFirework(b.getLocation());
    }

    public void onHit() {
        //launchExplodeFirework(loc);

        int radius = (int) yield;
        HashSet<Buildable> structuresHit = new HashSet<Buildable>();

        for (int x = -radius; x < radius; x++) {
            for (int z = -radius; z < radius; z++) {
                for (int y = -radius; y < radius; y++) {

                    Block b = loc.getBlock().getRelative(x, y, z);
                    if (ItemManager.getId(b) == CivData.BEDROCK) {
                        continue;
                    }
                    // 球
                    if (loc.distance(b.getLocation()) <= yield) {
                        bcoord.setFromLocation(b.getLocation());
                        StructureBlock sb = CivGlobal.getStructureBlock(bcoord);
                        CampBlock cb = CivGlobal.getCampBlock(bcoord);

                        if (sb == null && cb == null) {
                            explodeBlock(b);
                            continue;
                        }

                        if (sb != null) {

                            if (!sb.isDamageable()) {
                                continue;
                            }

                            if (sb.getOwner() instanceof TownHall) {
                                TownHall th = (TownHall) sb.getOwner();
                                if (th.getControlPoints().containsKey(bcoord)) {
                                    continue;
                                }
                            }

                            if (!sb.getOwner().isDestroyed()) {
                                if (!structuresHit.contains(sb.getOwner())) {

                                    structuresHit.add(sb.getOwner());

                                    if (sb.getOwner() instanceof TownHall) {
                                        TownHall th = (TownHall) sb.getOwner();

                                        if (th.getHitpoints() == 0) {
                                            explodeBlock(b);
                                        } else {
                                            try {
                                                th.onCannonDamage(cannon.getDamage(), this);
                                            } catch (CivException e1) {
                                                // TODO Auto-generated catch block
                                                e1.printStackTrace();
                                            }
                                        }
                                    } else {
                                        Player player = null;
                                        try {
                                            player = CivGlobal.getPlayer(whoFired);
                                        } catch (CivException e) {
                                        }

                                        if (!sb.getCiv().getDiplomacyManager().atWarWith(whoFired.getCiv())) {
                                            if (player != null) {
                                                CivMessage.sendError(player, CivSettings.localize.localizedString("cannonProjectile_ErrorNotAtWar"));
                                                return;
                                            }
                                        }

                                        sb.getOwner().onDamage(cannon.getDamage(), b.getWorld(), player, sb.getCoord(), sb);
                                        CivMessage.sendCiv(sb.getCiv(), CivColor.Yellow + CivSettings.localize.localizedString("cannonProjectile_hitAnnounce", sb.getOwner().getDisplayName(),
                                                sb.getOwner().getCenterLocation().getX() + "," +
                                                        sb.getOwner().getCenterLocation().getY() + "," +
                                                        sb.getOwner().getCenterLocation().getZ())
                                                + " (" + sb.getOwner().getHitpoints() + "/" + sb.getOwner().getMaxHitPoints() + ")");
                                    }

                                    CivMessage.sendCiv(whoFired.getCiv(), CivColor.LightGreen + CivSettings.localize.localizedString("var_cannonProjectile_hitSuccess", sb.getOwner().getTown().getName(), sb.getOwner().getDisplayName()) +
                                            " (" + sb.getOwner().getHitpoints() + "/" + sb.getOwner().getMaxHitPoints() + ")");
                                }
                            } else {
                                if (!Cannon.cannonBlocks.containsKey(bcoord)) {
                                    explodeBlock(b);
                                }
                            }
                        }
                    }
                }
            }
        }

        /* Instantly kill any players caught in the blast. */
        LinkedList<Entity> players = EntityProximity.getNearbyEntities(null, loc, yield, EntityPlayer.class);
        for (Entity e : players) {
            Player player = (Player) e;
            player.damage(playerDamage);
            if (player.isDead()) {
                CivMessage.global(CivColor.LightGray + CivSettings.localize.localizedString("var_cannonProjectile_userKilled", player.getName(), whoFired.getName()));
            }
        }

    }

    private void launchExplodeFirework(Location loc) {
        Random rand = new Random();
        int rand1 = rand.nextInt(100);

        if (rand1 > 90) {
            FireworkEffect fe = FireworkEffect.builder().withColor(Color.ORANGE).withColor(Color.YELLOW).flicker(true).with(Type.BURST).build();
            TaskMaster.syncTask(new FireWorkTask(fe, loc.getWorld(), loc, 3), 0);
        }
    }

    public boolean advance() {
        Vector dir = loc.getDirection();
        dir.add(new Vector(0.0f, -0.008, 0.0f)); //Apply 'gravity'
        loc.setDirection(dir);

        loc.add(dir.multiply(speed));
        loc.getWorld().createExplosion(loc, 0.0f, false);

        if (ItemManager.getId(loc.getBlock()) != CivData.AIR) {
            return true;
        }

        if (loc.distance(startLoc) > maxRange) {
            return true;
        }

        return false;
    }

    public void fire() {
        class SyncTask implements Runnable {
            CannonProjectile proj;

            public SyncTask(CannonProjectile proj) {
                this.proj = proj;
            }

            @Override
            public void run() {
                if (proj.advance()) {
                    onHit();
                    return;
                }
                TaskMaster.syncTask(this, 1);
            }
        }

        TaskMaster.syncTask(new SyncTask(this));
    }

}
