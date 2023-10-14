package cn.evole.plugins.civcraft.war;

import cn.evole.plugins.civcraft.camp.CampBlock;
import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.exception.InvalidConfiguration;
import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.main.CivMessage;
import cn.evole.plugins.civcraft.object.CultureChunk;
import cn.evole.plugins.civcraft.object.Resident;
import cn.evole.plugins.civcraft.object.StructureBlock;
import cn.evole.plugins.civcraft.structure.Buildable;
import cn.evole.plugins.civcraft.structure.TownHall;
import cn.evole.plugins.civcraft.threading.TaskMaster;
import cn.evole.plugins.civcraft.threading.tasks.FireWorkTask;
import cn.evole.plugins.civcraft.util.BlockCoord;
import cn.evole.plugins.civcraft.util.ChunkCoord;
import cn.evole.plugins.civcraft.util.CivColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Random;

public class WarListener implements Listener {

    public static final String RESTORE_NAME = "special:TNT";
    public static int yield;
    public static double playerDamage;
    public static int structureDamage;

    static {
        try {
            yield = CivSettings.getInteger(CivSettings.warConfig, "tnt.yield");
            playerDamage = CivSettings.getDouble(CivSettings.warConfig, "tnt.player_damage");
            structureDamage = CivSettings.getInteger(CivSettings.warConfig, "tnt.structure_damage");
        } catch (InvalidConfiguration e) {
            e.printStackTrace();
        }
    }

    ChunkCoord coord = new ChunkCoord();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (!War.isWarTime()) {
            return;
        }

        coord.setFromLocation(event.getBlock().getLocation());
        CultureChunk cc = CivGlobal.getCultureChunk(coord);

        if (cc == null) {
            return;
        }

        if (!cc.getCiv().getDiplomacyManager().isAtWar()) {
            return;
        }
        //土，草，沙子，砾石，火把，红石火把 红石 TNT,阶梯，藤曼,矿物快
        if (event.getBlock().getType().equals(Material.DIRT) ||
                event.getBlock().getType().equals(Material.GRASS) ||
                event.getBlock().getType().equals(Material.SAND) ||
                event.getBlock().getType().equals(Material.GRAVEL) ||
                event.getBlock().getType().equals(Material.TORCH) ||
                event.getBlock().getType().equals(Material.REDSTONE_TORCH_OFF) ||
                event.getBlock().getType().equals(Material.REDSTONE_TORCH_ON) ||
                event.getBlock().getType().equals(Material.REDSTONE) ||
                event.getBlock().getType().equals(Material.TNT) ||
                event.getBlock().getType().equals(Material.LADDER) ||
                event.getBlock().getType().equals(Material.VINE) ||
                event.getBlock().getType().equals(Material.IRON_BLOCK) ||
                event.getBlock().getType().equals(Material.GOLD_BLOCK) ||
                event.getBlock().getType().equals(Material.DIAMOND_BLOCK) ||
                event.getBlock().getType().equals(Material.EMERALD_BLOCK) ||
                !event.getBlock().getType().isSolid()) {
            return;
        }

        CivMessage.sendError(event.getPlayer(), CivSettings.localize.localizedString("war_mustUseTNT"));
        event.setCancelled(true);
    }

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (!War.isWarTime()) {
            return;
        }

        coord.setFromLocation(event.getBlock().getLocation());
        CultureChunk cc = CivGlobal.getCultureChunk(coord);

        if (cc == null) {
            return;
        }

        if (!cc.getCiv().getDiplomacyManager().isAtWar()) {
            return;
        }

        if (event.getBlock().getType().equals(Material.DIRT) ||
                event.getBlock().getType().equals(Material.GRASS) ||
                event.getBlock().getType().equals(Material.SAND) ||
                event.getBlock().getType().equals(Material.GRAVEL) ||
                event.getBlock().getType().equals(Material.TORCH) ||
                event.getBlock().getType().equals(Material.REDSTONE_TORCH_OFF) ||
                event.getBlock().getType().equals(Material.REDSTONE_TORCH_ON) ||
                event.getBlock().getType().equals(Material.REDSTONE) ||
                event.getBlock().getType().equals(Material.LADDER) ||
                event.getBlock().getType().equals(Material.VINE) ||
                event.getBlock().getType().equals(Material.TNT)) {

            if (event.getBlock().getLocation().subtract(0, 1, 0).getBlock().getType() != Material.AIR) {
                return;
            }

            event.getBlock().getWorld().spawnFallingBlock(event.getBlock().getLocation(), event.getBlock().getType(), (byte) 0);
            event.getBlock().setType(Material.AIR);

            return;
        }

        if (event.getBlock().getType().equals(Material.IRON_BLOCK) ||
                event.getBlock().getType().equals(Material.GOLD_BLOCK) ||
                event.getBlock().getType().equals(Material.DIAMOND_BLOCK) ||
                event.getBlock().getType().equals(Material.EMERALD_BLOCK)) {

            if (event.getBlock().getLocation().subtract(0, 1, 0).getBlock().getType() != Material.AIR) {
                return;
            }

            return;
        }

        CivMessage.sendError(event.getPlayer(), CivSettings.localize.localizedString("war_onlyBuildCertainBlocks"));
        CivMessage.sendError(event.getPlayer(), CivSettings.localize.localizedString("war_canAlsoPlaceBridgeBlocks"));
        event.setCancelled(true);
    }

    private void explodeBlock(Block b) {
        WarRegen.explodeThisBlock(b, WarListener.RESTORE_NAME);
        launchExplodeFirework(b.getLocation());
    }

    private void launchExplodeFirework(Location loc) {
        Random rand = new Random();
        int rand1 = rand.nextInt(100);

        if (rand1 > 90) {
            FireworkEffect fe = FireworkEffect.builder()
                    .withColor(Color.ORANGE)
                    .withColor(Color.YELLOW)
                    .flicker(true)
                    .with(Type.BURST)
                    .build();
            TaskMaster.syncTask(new FireWorkTask(fe, loc.getWorld(), loc, 3), 0);
        }
    }

    // 爆炸事件
    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityExplode(EntityExplodeEvent event) {

        if (War.isWarTime()) {
            event.setCancelled(false);
        } else {
            //非战时候 只能在自己的文明使用？
            if (event.getEntity() instanceof TNTPrimed) {
                TNTPrimed tnt = (TNTPrimed) event.getEntity();
                if (tnt.getSource() instanceof Player) {
                    Player p = (Player) tnt.getSource();
                    Resident res = CivGlobal.getResident(p);
                    coord.setFromLocation(event.getLocation());
                    CultureChunk cc = CivGlobal.getCultureChunk(coord);
                    if (cc != null && cc.getCiv() == res.getCiv()) {
                        event.setCancelled(false);
                        return;
                    }
                }
            }
            event.setCancelled(true);
            return;
        }

        if (event.isCancelled()) {
            return;
        }

        if (event.getEntity() == null) {
            return;
        }
        //炸空了？
        if (event.getEntityType().equals(EntityType.UNKNOWN)) {
            return;
        }

        if (event.getEntityType().equals(EntityType.PRIMED_TNT)
                || event.getEntityType().equals(EntityType.MINECART_TNT)
                || event.getEntityType().equals(EntityType.CREEPER)) {

            HashSet<Buildable> structuresHit = new HashSet<>();

            for (int y = -yield; y <= yield; y++) {
                for (int x = -yield; x <= yield; x++) {
                    for (int z = -yield; z <= yield; z++) {
                        Location loc = event.getLocation().clone().add(new Vector(x, y, z));
                        Block b = loc.getBlock();
                        if (loc.distance(event.getLocation()) < yield) {

                            BlockCoord bcoord = new BlockCoord();
                            bcoord.setFromLocation(loc);
//							StructureBlock sb = CivGlobal.getStructureBlock(bcoord);
//							if (sb == null) {
//							WarRegen.saveBlock(loc.getBlock(), Cannon.RESTORE_NAME, false);
//							}
//							if (sb.getTown() != null) {
//							WarRegen.destroyThisBlock(loc.getBlock(), sb.getTown());
//							} else {
//							ItemManager.setTypeIdAndData(loc.getBlock(), CivData.AIR, 0, false);
//							}

                            StructureBlock sb = CivGlobal.getStructureBlock(bcoord);
                            CampBlock cb = CivGlobal.getCampBlock(bcoord);
                            //没有炸到文明的东西
                            if (sb == null && cb == null) {
                                explodeBlock(b);
                                continue;
                            }

                            if (sb != null) {
                                //建筑需要可被破坏
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
                                        //一次伤害就扣一次
                                        structuresHit.add(sb.getOwner());

                                        if (sb.getOwner() instanceof TownHall) {
                                            TownHall th = (TownHall) sb.getOwner();

                                            if (th.getHitpoints() <= 0) {
                                                explodeBlock(b);
                                            } else {
                                                th.onTNTDamage(structureDamage);
                                            }
                                        } else {
                                            sb.getOwner().onDamage(structureDamage, b.getWorld(), null, sb.getCoord(), sb);
                                            CivMessage.sendCiv(sb.getCiv(), CivColor.Yellow + CivSettings.localize.localizedString("var_war_tntMsg", sb.getOwner().getDisplayName(),
                                                    ("(" + sb.getOwner().getCenterLocation().getX() + "," +
                                                            sb.getOwner().getCenterLocation().getY() + "," +
                                                            sb.getOwner().getCenterLocation().getZ() + ")"),
                                                    (sb.getOwner().getHitpoints() + "/" + sb.getOwner().getMaxHitPoints())));
                                        }
                                    }
                                } else {
                                    explodeBlock(b);
                                }
                            }
                        }
                    }
                }
            }
            event.setCancelled(true);
        }

    }

}

