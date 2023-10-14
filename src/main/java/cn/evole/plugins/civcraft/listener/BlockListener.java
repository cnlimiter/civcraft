/**
 * [2023] Cnlimiter LLC
 * All Rights Reserved.
 * Name: civcraft
 * Author: cnlimiter
 * UpdateTime: 2023/10/14 16:15
 * Description:
 * License: ARR
 */
package cn.evole.plugins.civcraft.listener;

import cn.evole.plugins.civcraft.cache.ArrowFiredCache;
import cn.evole.plugins.civcraft.cache.CannonFiredCache;
import cn.evole.plugins.civcraft.cache.CivCache;
import cn.evole.plugins.civcraft.camp.Camp;
import cn.evole.plugins.civcraft.camp.CampBlock;
import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.exception.CivException;
import cn.evole.plugins.civcraft.exception.InvalidConfiguration;
import cn.evole.plugins.civcraft.main.CivData;
import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.main.CivLog;
import cn.evole.plugins.civcraft.main.CivMessage;
import cn.evole.plugins.civcraft.object.*;
import cn.evole.plugins.civcraft.permission.PlotPermissions;
import cn.evole.plugins.civcraft.road.Road;
import cn.evole.plugins.civcraft.road.RoadBlock;
import cn.evole.plugins.civcraft.structure.*;
import cn.evole.plugins.civcraft.structure.farm.FarmChunk;
import cn.evole.plugins.civcraft.structure.wonders.GrandShipIngermanland;
import cn.evole.plugins.civcraft.threading.CivAsyncTask;
import cn.evole.plugins.civcraft.threading.TaskMaster;
import cn.evole.plugins.civcraft.threading.tasks.FireWorkTask;
import cn.evole.plugins.civcraft.threading.tasks.StructureBlockHitEvent;
import cn.evole.plugins.civcraft.util.*;
import cn.evole.plugins.civcraft.war.War;
import cn.evole.plugins.civcraft.war.WarRegen;
import gpl.HorseModifier;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.Chunk;
import org.bukkit.*;
import org.bukkit.Material;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftLivingEntity;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.List;
import java.util.Random;

//import Temple;

public class BlockListener implements Listener {

    /* Experimental, reuse the same object because it is single threaded. */
    public static ChunkCoord coord = new ChunkCoord("", 0, 0);
    public static BlockCoord bcoord = new BlockCoord("", 0, 0, 0);
    private final BlockFace[] faces = new BlockFace[]{
            BlockFace.DOWN,
            BlockFace.NORTH,
            BlockFace.EAST,
            BlockFace.SOUTH,
            BlockFace.WEST,
            BlockFace.SELF,
            BlockFace.UP
    };

    public static void OnPlayerSwitchEvent(PlayerInteractEvent event) {

        if (event.getClickedBlock() == null) {
            return;
        }

        Resident resident = CivGlobal.getResident(event.getPlayer().getName());

        if (resident == null) {
            event.setCancelled(true);
            return;
        }

        bcoord.setFromLocation(event.getClickedBlock().getLocation());
        CampBlock cb = CivGlobal.getCampBlock(bcoord);
        if (cb != null && !resident.isPermOverride()) {
            if (!cb.getCamp().hasMember(resident.getName())) {
                CivMessage.sendError(event.getPlayer(), CivSettings.localize.localizedString("blockUse_errorNotInCamp"));
                event.setCancelled(true);
                return;
            }
        }

        coord.setFromLocation(event.getClickedBlock().getLocation());
        TownChunk tc = CivGlobal.getTownChunk(coord);

        if (tc == null) {
            return;
        }

        if (resident.hasTown()) {
            if (War.isWarTime()) {
                if (tc.getTown().getCiv().getDiplomacyManager().atWarWith(resident.getTown().getCiv())) {

                    switch (event.getClickedBlock().getType()) {
                        case WOODEN_DOOR:
                        case IRON_DOOR:
                        case SPRUCE_DOOR:
                        case BIRCH_DOOR:
                        case JUNGLE_DOOR:
                        case ACACIA_DOOR:
                        case DARK_OAK_DOOR:
                        case ACACIA_FENCE_GATE:
                        case BIRCH_FENCE_GATE:
                        case DARK_OAK_FENCE_GATE:
                        case FENCE_GATE:
                        case SPRUCE_FENCE_GATE:
                        case JUNGLE_FENCE_GATE:
                            return;
                        default:
                            break;
                    }
                }
            }
        }

        event.getClickedBlock().getType();

        if (!tc.perms.hasPermission(PlotPermissions.Type.INTERACT, resident)) {
            event.setCancelled(true);

            if (War.isWarTime() && resident.hasTown() &&
                    resident.getTown().getCiv().getDiplomacyManager().atWarWith(tc.getTown().getCiv())) {
                WarRegen.destroyThisBlock(event.getClickedBlock(), tc.getTown());
            } else {
                CivMessage.sendErrorNoRepeat(event.getPlayer(), CivSettings.localize.localizedString("blockUse_errorGeneric") + " " + event.getClickedBlock().getType().toString());
            }
        }

        return;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityTameEvent(EntityTameEvent event) {
        if (event.getEntity() instanceof Wolf) {
            Wolf wolf = (Wolf) event.getEntity();
            if (wolf.getName().contains("Direwolf")) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onSlimeSplitEvent(SlimeSplitEvent event) {
        if (event.getEntity() instanceof Slime) {
            Slime slime = (Slime) event.getEntity();
            if (slime.getName().contains("Brutal") ||
                    slime.getName().contains("Elite") ||
                    slime.getName().contains("Greater") ||
                    slime.getName().contains("Lesser")) {
                slime.setSize(0);
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockIgniteEvent(BlockIgniteEvent event) {
        //	CivLog.debug("block ignite event");

        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    Block b = event.getBlock().getRelative(x, y, z);
                    bcoord.setFromLocation(b.getLocation());
                    StructureBlock sb = CivGlobal.getStructureBlock(bcoord);
                    if (sb != null) {
                        if (b.getType().isBurnable()) {
                            event.setCancelled(true);
                        }
                        return;
                    }

                    RoadBlock rb = CivGlobal.getRoadBlock(bcoord);
                    if (rb != null) {
                        event.setCancelled(true);
                        return;
                    }

                    CampBlock cb = CivGlobal.getCampBlock(bcoord);
                    if (cb != null) {
                        event.setCancelled(true);
                        return;
                    }

                    StructureSign structSign = CivGlobal.getStructureSign(bcoord);
                    if (structSign != null) {
                        event.setCancelled(true);
                        return;
                    }

                    StructureChest structChest = CivGlobal.getStructureChest(bcoord);
                    if (structChest != null) {
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        }


        coord.setFromLocation(event.getBlock().getLocation());
        TownChunk tc = CivGlobal.getTownChunk(coord);

        if (tc == null) {
            return;
        }

        if (tc.perms.isFire() == false) {
            CivMessage.sendError(event.getPlayer(), CivSettings.localize.localizedString("fireDisabledInChunk"));
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityBlockChange(EntityChangeBlockEvent event) {
        bcoord.setFromLocation(event.getBlock().getLocation());

        StructureBlock sb = CivGlobal.getStructureBlock(bcoord);
        if (sb != null) {
            event.setCancelled(true);
            return;
        }

        RoadBlock rb = CivGlobal.getRoadBlock(bcoord);
        if (rb != null) {
            event.setCancelled(true);
            return;
        }

        CampBlock cb = CivGlobal.getCampBlock(bcoord);
        if (cb != null) {
            event.setCancelled(true);
            return;
        }

        return;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onBlockBurnEvent(BlockBurnEvent event) {
        bcoord.setFromLocation(event.getBlock().getLocation());

        StructureBlock sb = CivGlobal.getStructureBlock(bcoord);
        if (sb != null) {
            event.setCancelled(true);
            return;
        }

        RoadBlock rb = CivGlobal.getRoadBlock(bcoord);
        if (rb != null) {
            event.setCancelled(true);
            return;
        }

        CampBlock cb = CivGlobal.getCampBlock(bcoord);
        if (cb != null) {
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onProjectileHitEvent(ProjectileHitEvent event) {
        if (event.getEntity() instanceof Arrow) {
            ArrowFiredCache afc = CivCache.arrowsFired.get(event.getEntity().getUniqueId());
            if (afc != null) {
                afc.setHit(true);
            }
        }

        if (event.getEntity() instanceof Fireball) {
            CannonFiredCache cfc = CivCache.cannonBallsFired.get(event.getEntity().getUniqueId());
            if (cfc != null) {

                cfc.setHit(true);

                FireworkEffect fe = FireworkEffect.builder().withColor(Color.RED).withColor(Color.BLACK).flicker(true).with(Type.BURST).build();

                Random rand = new Random();
                int spread = 30;
                for (int i = 0; i < 15; i++) {
                    int x = rand.nextInt(spread) - spread / 2;
                    int y = rand.nextInt(spread) - spread / 2;
                    int z = rand.nextInt(spread) - spread / 2;


                    Location loc = event.getEntity().getLocation();
                    Location location = new Location(loc.getWorld(), loc.getX(), loc.getY(), loc.getZ());
                    location.add(x, y, z);

                    TaskMaster.syncTask(new FireWorkTask(fe, loc.getWorld(), loc, 5), rand.nextInt(30));
                }

            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
        /* Protect the Protected Item Frames! */
        if (event.getEntity() instanceof ItemFrame) {
            ItemFrameStorage iFrameStorage = CivGlobal.getProtectedItemFrame(event.getEntity().getUniqueId());
            if (iFrameStorage != null) {
                event.setCancelled(true);
                return;
            }
        }

        if (event.getDamager() instanceof LightningStrike) {
//			CivLog.debug("onEntityDamageByEntityEvent LightningStrike: "+event.getDamager().getUniqueId());
            try {
                event.setDamage(CivSettings.getInteger(CivSettings.warConfig, "tesla_tower.damage"));
            } catch (InvalidConfiguration e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        if (event.getDamager() instanceof Arrow) {

        }

        if (event.getDamager() instanceof Fireball) {
            CannonFiredCache cfc = CivCache.cannonBallsFired.get(event.getDamager().getUniqueId());
            if (cfc != null) {
                cfc.setHit(true);
                cfc.destroy(event.getDamager());
                Buildable whoFired = cfc.getWhoFired();
                if (whoFired.getConfigId().equals("s_cannontower")) {
                    event.setDamage((double) ((CannonTower) whoFired).getDamage());
                } else if (whoFired.getConfigId().equals("s_cannonship")) {
                    event.setDamage((double) ((CannonShip) whoFired).getDamage());
                } else if (whoFired.getConfigId().equals("w_grand_ship_ingermanland")) {
                    event.setDamage((double) ((GrandShipIngermanland) whoFired).getCannonDamage());
                }
            }
        }

        if (event.getEntity() instanceof Player) {

            /* Only protect against players and entities that players can throw. */
            if (!CivSettings.playerEntityWeapons.contains(event.getDamager().getType())) {
                return;
            }

            Player defender = (Player) event.getEntity();

            coord.setFromLocation(event.getEntity().getLocation());
            TownChunk tc = CivGlobal.getTownChunk(coord);
            boolean allowPVP = false;
            String denyMessage = "";

            if (tc == null) {
                /* In the wilderness, anything goes. */
                allowPVP = true;
            } else {
                Player attacker = null;
                if (event.getDamager() instanceof Player) {
                    attacker = (Player) event.getDamager();
                } else if (event.getDamager() instanceof Projectile) {
                    LivingEntity shooter = (LivingEntity) ((Projectile) event.getDamager()).getShooter();
                    if (shooter instanceof Player) {
                        attacker = (Player) shooter;
                    }
                }

                if (attacker == null) {
                    /* Attacker wasnt a player or known projectile, allow it. */
                    allowPVP = true;
                } else {
                    switch (playersCanPVPHere(attacker, defender, tc)) {
                        case ALLOWED:
                            allowPVP = true;
                            break;
                        case NOT_AT_WAR:
                            allowPVP = false;
                            denyMessage = CivSettings.localize.localizedString("var_pvpError1", defender.getName());
                            break;
                        case NEUTRAL_IN_WARZONE:
                            allowPVP = false;
                            denyMessage = CivSettings.localize.localizedString("var_pvpError2", defender.getName());
                            break;
                        case NON_PVP_ZONE:
                            allowPVP = false;
                            denyMessage = CivSettings.localize.localizedString("var_pvpError3", defender.getName());
                            break;
                        case ATTACKER_PROTECTED:
                            allowPVP = false;
                            denyMessage = CivSettings.localize.localizedString("pvpListenerError");
                            break;
                        case DEFENDER_PROTECTED:
                            allowPVP = false;
                            denyMessage = CivSettings.localize.localizedString("pvpListenerError2");
                            break;
                    }
                }

                if (!allowPVP) {
                    CivMessage.sendError(attacker, denyMessage);
                    event.setCancelled(true);
                }
            }
        }

        return;
    }

    /**
     * 繁殖
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void OnCreateSpawnEvent(CreatureSpawnEvent event) {
        LivingEntity entity = event.getEntity();
        EntityType type = event.getEntityType();
        SpawnReason reason = event.getSpawnReason();
        // 禁止刷怪蛋生成
        if (reason.equals(SpawnReason.SPAWNER_EGG)) {
            event.setCancelled(true);
            return;
        }
        if (reason.equals(SpawnReason.CUSTOM)) {
            // 插件生成
            if (event.isCancelled()) {
                return;
            }
            // TODO：设置小僵尸
            if (type.equals(EntityType.ZOMBIE)) {
                if (entity.getCustomName() != null) {
//                    ((Zombie) entity).setBaby(true);
                }
            } else if (type.equals(EntityType.ZOMBIE_VILLAGER)) {
                if (entity.getCustomName() != null) {
//                    ((ZombieVillager) entity).setBaby(true);
                }
            } else if (type == EntityType.SLIME) {
                ((Slime) entity).setSize(3);
            }
            return;
        }
        // 禁止生成禁止的怪
        if (CivSettings.restrictedSpawns.contains(type)) {
            event.setCancelled(true);
            return;
        }
        // 城镇区块不刷怪
        TownChunk tc = CivGlobal.getTownChunk(event.getLocation());
        if (tc != null) {
            if (CivSettings.vanillaHostileMobs.contains(type)) {
                event.setCancelled(true);
                return;
            }
        }
        // 禁止马系列的自然生成
        if ((type.equals(EntityType.HORSE)
                || type.equals(EntityType.DONKEY)
                || type.equals(EntityType.MULE)
                || type.equals(EntityType.SKELETON_HORSE)
                || type.equals(EntityType.ZOMBIE_HORSE)
                || type.equals(EntityType.LLAMA))
                && (reason.equals(SpawnReason.DEFAULT) || reason.equals(SpawnReason.NATURAL))) {
            class SyncTask implements Runnable {

                private LivingEntity ent;

                private SyncTask(LivingEntity ent) {
                    this.ent = ent;
                }

                @Override
                public void run() {
                    if (ent != null) {
                        if (!HorseModifier.isCivCraftHorse(ent)) {
                            CivLog.warning("Removing a normally spawned horse.");
                            ent.remove();
                        }
                    }
                }
            }
            TaskMaster.syncTask(new SyncTask(entity));

            CivLog.warning("Canceling horse spawn reason: " + reason);
            event.setCancelled(true);
            return;
        }
        // 牧场繁殖
        if (reason.equals(SpawnReason.BREEDING)) {
            ChunkCoord coord = new ChunkCoord(entity.getLocation());
            Pasture pasture = Pasture.pastureChunks.get(coord);
            if (pasture != null) {
                pasture.onBreed(event.getEntity());
            } else {
                // 自热情况下？
                event.setCancelled(true);
                return;
            }
        }
        if (type.equals(EntityType.ZOMBIE) || type.equals(EntityType.ZOMBIE_VILLAGER)
                || type.equals(EntityType.HUSK)) {
            if (reason.equals(SpawnReason.JOCKEY)) {
                event.setCancelled(true);
                return;
            }
        }

        if (type.equals(EntityType.CHICKEN)) {
            if (reason.equals(SpawnReason.EGG) || reason.equals(SpawnReason.MOUNT)) {
                event.setCancelled(true);
                return;
            }
        }

        if (type.equals(EntityType.IRON_GOLEM) && reason.equals(SpawnReason.BUILD_IRONGOLEM)) {
            event.setCancelled(true);
            return;
        }


        coord.setFromLocation(event.getLocation());


    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void OnEntityExplodeEvent(EntityExplodeEvent event) {

        if (event.getEntity() == null) {
            return;
        }
        /* prevent ender dragons from breaking blocks. */
        if (event.getEntityType().equals(EntityType.COMPLEX_PART)) {
            event.setCancelled(true);
        } else if (event.getEntityType().equals(EntityType.ENDER_DRAGON)) {
            event.setCancelled(true);
        }

        for (Block block : event.blockList()) {
            bcoord.setFromLocation(block.getLocation());
            StructureBlock sb = CivGlobal.getStructureBlock(bcoord);
            if (sb != null) {
                event.setCancelled(true);
                return;
            }

            RoadBlock rb = CivGlobal.getRoadBlock(bcoord);
            if (rb != null) {
                event.setCancelled(true);
                return;
            }

            CampBlock cb = CivGlobal.getCampBlock(bcoord);
            if (cb != null) {
                event.setCancelled(true);
                return;
            }

            StructureSign structSign = CivGlobal.getStructureSign(bcoord);
            if (structSign != null) {
                event.setCancelled(true);
                return;
            }

            StructureChest structChest = CivGlobal.getStructureChest(bcoord);
            if (structChest != null) {
                event.setCancelled(true);
                return;
            }

            coord.setFromLocation(block.getLocation());

            HashSet<Wall> walls = CivGlobal.getWallChunk(coord);
            if (walls != null) {
                for (Wall wall : walls) {
                    if (wall.isProtectedLocation(block.getLocation())) {
                        event.setCancelled(true);
                        return;
                    }
                }
            }

            TownChunk tc = CivGlobal.getTownChunk(coord);
            if (tc == null) {
                continue;
            }
            event.setCancelled(true);
            return;
        }

    }

//    private static void destroyLiquidRecursive(Block source) {
//    	//source.setTypeIdAndData(CivData.AIR, (byte)0, false);
//    	NMSHandler nms = new NMSHandler();
//    	nms.setBlockFast(source.getWorld(), source.getX(), source.getY(), source.getZ(), 0, (byte)0);
//    	
//    	for (BlockFace face : BlockFace.values()) {
//    		Block relative = source.getRelative(face);
//    		if (relative == null) {
//    			continue;
//    		}
//    		
//    		if (!isLiquid(relative.getTypeId())) {
//    			continue;
//    		}
//    		
//    		destroyLiquidRecursive(relative);
//    	}
//    }

//    private static boolean isLiquid(int id) {
//    	return (id >= CivData.WATER && id <= CivData.LAVA);
//    }

    public BlockCoord generatesCobble(int id, Block b) {
        int mirrorID1 = (id == CivData.WATER_RUNNING || id == CivData.WATER ? CivData.LAVA_RUNNING : CivData.WATER_RUNNING);
        int mirrorID2 = (id == CivData.WATER_RUNNING || id == CivData.WATER ? CivData.LAVA : CivData.WATER);
        int mirrorID3 = (id == CivData.WATER_RUNNING || id == CivData.WATER ? CivData.LAVA_RUNNING : CivData.WATER);
        int mirrorID4 = (id == CivData.WATER_RUNNING || id == CivData.WATER ? CivData.LAVA : CivData.WATER_RUNNING);
        for (BlockFace face : faces) {
            Block r = b.getRelative(face, 1);
            if (ItemManager.getId(r) == mirrorID1 || ItemManager.getId(r) == mirrorID2 ||
                    ItemManager.getId(r) == mirrorID3 || ItemManager.getId(r) == mirrorID4) {
                return new BlockCoord(r);
            }
        }

        return null;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void OnBlockFromToEvent(BlockFromToEvent event) {
        /* Disable cobblestone and obsidian generators. */
        int id = ItemManager.getId(event.getBlock());
        if (id >= CivData.WATER && id <= CivData.LAVA) {
            Block b = event.getToBlock();
            bcoord.setFromLocation(b.getLocation());

            int toid = ItemManager.getId(b);
            if (toid == CivData.COBBLESTONE || toid == CivData.OBSIDIAN) {
                BlockCoord other = generatesCobble(id, b);
                if (other != null && other.getBlock().getType() != Material.AIR) {
                    other.getBlock().setType(Material.NETHERRACK);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerBucketEmpty(final PlayerBucketEmptyEvent event) {
        final Resident resident = CivGlobal.getResident(event.getPlayer());
        try {
            if (War.isWarTime() && CivGlobal.getCultureChunk(event.getBlockClicked().getLocation()).getCiv().isAdminCiv()) {
                event.setCancelled(true);
                CivMessage.sendError(event.getPlayer(), CivSettings.localize.localizedString("var_moblistener_spawn"));
                return;
            }
        } catch (NullPointerException ex) {
        }
        if (resident == null) {
            event.setCancelled(true);
            return;
        }
        if (resident.isSBPermOverride()) {
            return;
        }
        BlockListener.coord.setFromLocation(event.getBlockClicked().getLocation());
        final TownChunk tc = CivGlobal.getTownChunk(BlockListener.coord);
        if (tc != null) {
            if (!tc.getTown().getCiv().isAdminCiv()) {
                if (!tc.perms.hasPermission(PlotPermissions.Type.BUILD, resident)) {
                    event.setCancelled(true);
                    CivMessage.sendError(event.getPlayer(), CivSettings.localize.localizedString("blockPlace_errorBukkit") + " " + tc.getTown().getName());
                }
            } else if (!event.getPlayer().isOp()) {
                event.setCancelled(true);
                CivMessage.sendError(event.getPlayer(), CivSettings.localize.localizedString("blockPlace_errorBukkit_spawn") + " " + tc.getTown().getName());
            } else {
                event.setCancelled(false);
            }
        }
    }

    // Prevent Ice from Melting in Towns. Gotta stop Arctic Templates from melting somehow.
    @EventHandler(priority = EventPriority.NORMAL)
    public void OnBlockFadeEvent(BlockFadeEvent event) {
        Block block = event.getBlock();
        if (block.getType() == Material.ICE || block.getType() == Material.PACKED_ICE || block.getType() == Material.FROSTED_ICE) {
            BlockListener.coord.setFromLocation(block.getLocation());

            final TownChunk tc = CivGlobal.getTownChunk(BlockListener.coord);
            if (tc != null) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void OnBlockFormEvent(BlockFormEvent event) {
        /* Disable cobblestone generators. */
        if (ItemManager.getId(event.getNewState()) == CivData.COBBLESTONE
                || ItemManager.getId(event.getNewState()) == CivData.OBSIDIAN) {
            ItemManager.setTypeId(event.getNewState(), CivData.NETHERRACK);
            return;
        }

        Chunk spreadChunk = event.getNewState().getChunk();
        coord.setX(spreadChunk.getX());
        coord.setZ(spreadChunk.getZ());
        coord.setWorldname(spreadChunk.getWorld().getName());

        TownChunk tc = CivGlobal.getTownChunk(coord);
        if (tc == null) {
            return;
        }

        if (!tc.perms.isFire()) {
            if (event.getNewState().getType() == Material.FIRE) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void OnBlockPlaceEvent(BlockPlaceEvent event) {
        Resident resident = CivGlobal.getResident(event.getPlayer());

        if (resident == null) {
            event.setCancelled(true);
            return;
        }

        if (resident.isSBPermOverride()) {
            return;
        }

        bcoord.setFromLocation(event.getBlockAgainst().getLocation());
        StructureSign sign = CivGlobal.getStructureSign(bcoord);
        if (sign != null) {
            event.setCancelled(true);
            return;
        }

        bcoord.setFromLocation(event.getBlock().getLocation());
        StructureBlock sb = CivGlobal.getStructureBlock(bcoord);
        if (sb != null) {
            event.setCancelled(true);
            CivMessage.sendError(event.getPlayer(),
                    CivSettings.localize.localizedString("blockBreak_errorStructure") + " " + sb.getOwner().getDisplayName() + " " + CivSettings.localize.localizedString("blockBreak_errorOwnedBy") + " " + sb.getTown().getName());
            return;
        }

        RoadBlock rb = CivGlobal.getRoadBlock(bcoord);
        if (rb != null) {
            if (rb.isAboveRoadBlock()) {
                if (resident.getCiv() != rb.getRoad().getCiv()) {
                    event.setCancelled(true);
                    CivMessage.sendError(event.getPlayer(),
                            CivSettings.localize.localizedString("blockPlace_errorRoad1") + " " + (Road.HEIGHT - 1) + " " + CivSettings.localize.localizedString("blockPlace_errorRoad2"));
                }
            }
            return;
        }

        CampBlock cb = CivGlobal.getCampBlock(bcoord);
        if (cb != null && !cb.canBreak(event.getPlayer().getName())) {
            event.setCancelled(true);
            CivMessage.sendError(event.getPlayer(), CivSettings.localize.localizedString("blockBreak_errorCamp1") + " " + cb.getCamp().getName() + " " + CivSettings.localize.localizedString("blockBreak_errorOwnedBy") + " " + cb.getCamp().getOwner().getName());
            return;
        }

        coord.setFromLocation(event.getBlock().getLocation());
        TownChunk tc = CivGlobal.getTownChunk(coord);
        if (CivSettings.blockPlaceExceptions.get(event.getBlock().getType()) != null) {
            return;
        }

        if (tc != null) {
            if (!tc.perms.hasPermission(PlotPermissions.Type.BUILD, resident)) {
                if (War.isWarTime() && resident.hasTown() &&
                        resident.getTown().getCiv().getDiplomacyManager().atWarWith(tc.getTown().getCiv())) {
                    if (WarRegen.canPlaceThisBlock(event.getBlock())) {
                        WarRegen.saveBlock(event.getBlock(), tc.getTown().getName(), true);
                        return;
                    } else {
                        CivMessage.sendErrorNoRepeat(event.getPlayer(), CivSettings.localize.localizedString("blockPlace_errorWar"));
                        event.setCancelled(true);
                        return;
                    }
                } else {
                    event.setCancelled(true);
                    CivMessage.sendError(event.getPlayer(), CivSettings.localize.localizedString("blockPlace_errorPermission") + " " + tc.getTown().getName());
                }
            }
        }

        /* Check if we're going to break too many structure blocks beneath a structure. */
        //LinkedList<StructureBlock> sbList = CivGlobal.getStructureBlocksAt(bcoord.getWorldname(), bcoord.getX(), bcoord.getZ());
        HashSet<Buildable> buildables = CivGlobal.getBuildablesAt(bcoord);
        if (buildables != null) {
            for (Buildable buildable : buildables) {
                if (!buildable.validated) {
                    try {
                        buildable.validate(event.getPlayer());
                    } catch (CivException e) {
                        e.printStackTrace();
                    }
                    continue;
                }

                /* Building is validated, grab the layer and determine if this would set it over the limit. */
                BuildableLayer layer = buildable.layerValidPercentages.get(bcoord.getY());
                if (layer == null) {
                    continue;
                }

                /* Update the layer. */
                layer.current += Buildable.getReinforcementValue(ItemManager.getId(event.getBlockPlaced()));
                if (layer.current < 0) {
                    layer.current = 0;
                }
                buildable.layerValidPercentages.put(bcoord.getY(), layer);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void OnBlockBreakEvent(BlockBreakEvent event) {
        Resident resident = CivGlobal.getResident(event.getPlayer());

        if (resident == null) {
            event.setCancelled(true);
            return;
        }

        if (resident.isSBPermOverride()) {
            return;
        }

        bcoord.setFromLocation(event.getBlock().getLocation());
        StructureBlock sb = CivGlobal.getStructureBlock(bcoord);

        if (sb != null) {
            event.setCancelled(true);
            TaskMaster.syncTask(new StructureBlockHitEvent(event.getPlayer().getName(), bcoord, sb, event.getBlock().getWorld()), 0);
            return;
        }

        RoadBlock rb = CivGlobal.getRoadBlock(bcoord);
        if (rb != null && !rb.isAboveRoadBlock()) {
            if (War.isWarTime()) {
                /* Allow blocks to be 'destroyed' during war time. */
                WarRegen.destroyThisBlock(event.getBlock(), rb.getTown());
                event.setCancelled(true);
                return;
            } else {
                event.setCancelled(true);
                rb.onHit(event.getPlayer());
                return;
            }
        }

        ProtectedBlock pb = CivGlobal.getProtectedBlock(bcoord);
        if (pb != null) {
            event.setCancelled(true);
            CivMessage.sendError(event.getPlayer(), CivSettings.localize.localizedString("blockBreak_errorProtected"));
            return;
        }

        CampBlock cb = CivGlobal.getCampBlock(bcoord);
        if (cb != null && !cb.canBreak(event.getPlayer().getName())) {
            ControlPoint cBlock = cb.getCamp().controlBlocks.get(bcoord);
            if (cBlock != null) {
                cb.getCamp().onDamage(1, event.getBlock().getWorld(), event.getPlayer(), bcoord, null);
                event.setCancelled(true);
                return;
            } else {
                event.setCancelled(true);
                CivMessage.sendError(event.getPlayer(), CivSettings.localize.localizedString("blockBreak_errorCamp1") + " " +
                        cb.getCamp().getName() + " " + CivSettings.localize.localizedString("blockBreak_errorOwnedBy") + " " +
                        cb.getCamp().getOwner().getName());
                return;
            }
        }

        StructureSign structSign = CivGlobal.getStructureSign(bcoord);
        if (structSign != null && !resident.isSBPermOverride()) {
            event.setCancelled(true);
            CivMessage.sendError(event.getPlayer(), CivSettings.localize.localizedString("blockBreak_errorStructureSign"));
            return;
        }

        StructureChest structChest = CivGlobal.getStructureChest(bcoord);
        if (structChest != null && !resident.isSBPermOverride()) {
            event.setCancelled(true);
            CivMessage.sendError(event.getPlayer(), CivSettings.localize.localizedString("blockBreak_errorStructureChests"));
            return;
        }

        coord.setFromLocation(event.getBlock().getLocation());
        HashSet<Wall> walls = CivGlobal.getWallChunk(coord);

        if (walls != null) {
            for (Wall wall : walls) {
                if (wall.isProtectedLocation(event.getBlock().getLocation())) {
                    if (resident == null || !resident.hasTown() || resident.getTown().getCiv() != wall.getTown().getCiv() && !resident.isSBPermOverride()) {

                        StructureBlock tmpStructureBlock = new StructureBlock(bcoord, wall);
                        tmpStructureBlock.setAlwaysDamage(true);
                        TaskMaster.syncTask(new StructureBlockHitEvent(event.getPlayer().getName(), bcoord, tmpStructureBlock, event.getBlock().getWorld()), 0);
                        //CivMessage.sendError(event.getPlayer(), "Cannot destroy this block, protected by a wall, destroy it first.");
                        event.setCancelled(true);
                        return;
                    } else {
                        CivMessage.send(event.getPlayer(), CivColor.LightGray + CivSettings.localize.localizedString("blockBreak_wallAlert") + " " +
                                resident.getTown().getCiv().getName());
                        break;
                    }
                }
            }
        }

        TownChunk tc = CivGlobal.getTownChunk(coord);
        if (tc != null) {
            if (!tc.perms.hasPermission(PlotPermissions.Type.DESTROY, resident)) {
                event.setCancelled(true);

                if (War.isWarTime() && resident.hasTown() &&
                        resident.getTown().getCiv().getDiplomacyManager().atWarWith(tc.getTown().getCiv())) {
                    WarRegen.destroyThisBlock(event.getBlock(), tc.getTown());
                } else {
                    CivMessage.sendErrorNoRepeat(event.getPlayer(), CivSettings.localize.localizedString("blockBreak_errorPermission") + " " + tc.getTown().getName());
                }
            }
        }

        /* Check if we're going to break too many structure blocks beneath a structure. */
        //LinkedList<StructureBlock> sbList = CivGlobal.getStructureBlocksAt(bcoord.getWorldname(), bcoord.getX(), bcoord.getZ());
        HashSet<Buildable> buildables = CivGlobal.getBuildablesAt(bcoord);
        if (buildables != null) {
            for (Buildable buildable : buildables) {
                if (!buildable.validated) {
                    try {
                        buildable.validate(event.getPlayer());
                    } catch (CivException e) {
                        e.printStackTrace();
                    }
                    continue;
                }

                /* Building is validated, grab the layer and determine if this would set it over the limit. */
                BuildableLayer layer = buildable.layerValidPercentages.get(bcoord.getY());
                if (layer == null) {
                    continue;
                }

                double current = layer.current - Buildable.getReinforcementValue(ItemManager.getId(event.getBlock()));
                if (current < 0) {
                    current = 0;
                }
                Double percentValid = (double) (current) / (double) layer.max;

                if (percentValid < Buildable.validPercentRequirement) {
                    CivMessage.sendError(event.getPlayer(), CivSettings.localize.localizedString("blockBreak_errorSupport") + " " + buildable.getDisplayName());
                    event.setCancelled(true);
                    return;
                }

                /* Update the layer. */
                layer.current = (int) current;
                buildable.layerValidPercentages.put(bcoord.getY(), layer);
            }
        }

    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void OnEntityInteractEvent(EntityInteractEvent event) {
        if (event.getBlock() != null) {
            if (CivSettings.switchItems.contains(event.getBlock().getType())) {
                coord.setFromLocation(event.getBlock().getLocation());
                TownChunk tc = CivGlobal.getTownChunk(coord);

                if (tc == null) {
                    return;
                }

                /* A non-player entity is trying to trigger something, if interact permission is
                 * off for others then disallow it.
                 */
                if (tc.perms.interact.isPermitOthers()) {
                    return;
                }

                if (event.getEntity() instanceof Player) {
                    CivMessage.sendErrorNoRepeat((Player) event.getEntity(), CivSettings.localize.localizedString("blockUse_errorPermission"));
                }

                event.setCancelled(true);
            }
        }

    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void OnPlayerConsumeEvent(PlayerItemConsumeEvent event) {
        ItemStack stack = event.getItem();

        /* Disable notch apples */
        if (ItemManager.getId(event.getItem()) == ItemManager.getId(Material.GOLDEN_APPLE)) {
            if (event.getItem().getDurability() == (short) 0x1) {
                CivMessage.sendError(event.getPlayer(), CivSettings.localize.localizedString("itemUse_errorGoldenApple"));
                event.setCancelled(true);
                return;
            }
        }

        if (stack.getType().equals(Material.POTION)) {
            int effect = event.getItem().getDurability() & 0x000F;
            if (effect == 0xE) {
                event.setCancelled(true);
                CivMessage.sendError(event.getPlayer(), CivSettings.localize.localizedString("itemUse_errorInvisPotion"));
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockDispenseEvent(BlockDispenseEvent event) {
        ItemStack stack = event.getItem();
        if (stack != null) {
            if (event.getItem().getType().equals(Material.POTION)) {
                int effect = event.getItem().getDurability() & 0x000F;
                if (effect == 0xE) {
                    event.setCancelled(true);
                    return;
                }
            }

            if (event.getItem().getType().equals(Material.INK_SACK)) {
                //if (event.getItem().getDurability() == 15) {
                event.setCancelled(true);
                return;
                //}
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void OnPlayerInteractEvent(PlayerInteractEvent event) {
        Resident resident = CivGlobal.getResident(event.getPlayer());

        if (resident == null) {
            event.setCancelled(true);
            return;
        }

        if (event.isCancelled()) {
            // Fix for bucket bug.
            if (event.getAction() == Action.RIGHT_CLICK_AIR) {
                Integer item = ItemManager.getId(event.getPlayer().getInventory().getItemInMainHand());
                // block cheats for placing water/lava/fire/lighter use.
                if (item == 326 || item == 327 || item == 259 || (item >= 8 && item <= 11) || item == 51) {
                    event.setCancelled(true);
                }
            }
            return;
        }

        if (event.hasItem()) {

            if (event.getItem().getType().equals(Material.POTION)) {
                int effect = event.getItem().getDurability() & 0x000F;
                if (effect == 0xE) {
                    event.setCancelled(true);
                    CivMessage.sendError(event.getPlayer(), CivSettings.localize.localizedString("itemUse_errorInvisPotion"));
                    return;
                }
            }

            if (event.getItem().getType().equals(Material.INK_SACK) && event.getItem().getDurability() == 15) {
                Block clickedBlock = event.getClickedBlock();
                if (ItemManager.getId(clickedBlock) == CivData.WHEAT ||
                        ItemManager.getId(clickedBlock) == CivData.CARROTS ||
                        ItemManager.getId(clickedBlock) == CivData.POTATOES) {
                    event.setCancelled(true);
                    CivMessage.sendError(event.getPlayer(), CivSettings.localize.localizedString("itemUse_errorBoneMeal"));
                    return;
                }
            }
        }

        // prevent players trampling crops
        Block soilBlock1 = event.getPlayer().getLocation().getBlock().getRelative(0, 0, 0);
        Block soilBlock2 = event.getPlayer().getLocation().getBlock().getRelative(0, -1, 0);
        if ((event.getAction() == Action.PHYSICAL)) {
            if (ItemManager.getId(soilBlock1) == CivData.FARMLAND || ItemManager.getId(soilBlock2) == CivData.FARMLAND) {
                event.setCancelled(true);
                return;
            }
        }

        /*
         * Right clicking causes some dupe bugs for some reason with items that have "actions" such as swords.
         * It also causes block place events on top of signs. So we'll just only allow signs to work with left click.
         */
        boolean leftClick = event.getAction().equals(Action.LEFT_CLICK_AIR) || event.getAction().equals(Action.LEFT_CLICK_BLOCK);

        if (event.getClickedBlock() != null) {

            if (MarkerPlacementManager.isPlayerInPlacementMode(event.getPlayer())) {
                Block block;
                if (event.getBlockFace().equals(BlockFace.UP)) {
                    block = event.getClickedBlock().getRelative(event.getBlockFace());
                } else {
                    block = event.getClickedBlock();
                }

                try {
                    MarkerPlacementManager.setMarker(event.getPlayer(), block.getLocation());
                    CivMessage.send(event.getPlayer(), CivColor.LightGreen + CivSettings.localize.localizedString("itemUse_marked"));
                } catch (CivException e) {
                    CivMessage.send(event.getPlayer(), CivColor.Rose + e.getMessage());
                }

                event.setCancelled(true);
                return;
            }

            // Check for clicked structure signs.
            bcoord.setFromLocation(event.getClickedBlock().getLocation());
            StructureSign sign = CivGlobal.getStructureSign(bcoord);
            if (sign != null) {

                if (leftClick || sign.isAllowRightClick()) {
                    if (sign.getOwner() != null && sign.getOwner().isActive()) {
                        try {
                            sign.getOwner().processSignAction(event.getPlayer(), sign, event);
                            event.setCancelled(true);
                        } catch (CivException e) {
                            CivMessage.send(event.getPlayer(), CivColor.Rose + e.getMessage());
                            event.setCancelled(true);
                            return;
                        }
                    }
                }
                return;
            }
            if (CivSettings.switchItems.contains(event.getClickedBlock().getType())) {
                OnPlayerSwitchEvent(event);
                if (event.isCancelled()) {
                    return;
                }
            }
        }

        if (event.hasItem()) {

            if (event.getItem() == null) {
            } else {
                if (CivSettings.restrictedItems.containsKey(event.getItem().getType())) {
                    OnPlayerUseItem(event);
                    if (event.isCancelled()) {
                        return;
                    }
                }
            }
        }

    }

    public void OnPlayerBedEnterEvent(PlayerBedEnterEvent event) {

        Resident resident = CivGlobal.getResident(event.getPlayer().getName());

        if (resident == null) {
            event.setCancelled(true);
            return;
        }

        coord.setFromLocation(event.getPlayer().getLocation());
        Camp camp = CivGlobal.getCampChunk(coord);
        if (camp != null) {
            if (!camp.hasMember(event.getPlayer().getName())) {
                CivMessage.sendError(event.getPlayer(), CivSettings.localize.localizedString("bedUse_errorNotInCamp"));
                event.setCancelled(true);
                return;
            }
        }
    }

    private void OnPlayerUseItem(PlayerInteractEvent event) {
        Location loc = (event.getClickedBlock() == null) ?
                event.getPlayer().getLocation() :
                event.getClickedBlock().getLocation();

        ItemStack stack = event.getItem();

        coord.setFromLocation(event.getPlayer().getLocation());
        Camp camp = CivGlobal.getCampChunk(coord);
        if (camp != null) {
            if (!camp.hasMember(event.getPlayer().getName())) {
                CivMessage.sendError(event.getPlayer(), CivSettings.localize.localizedString("itemUse_errorCamp") + " " + stack.getType().toString());
                event.setCancelled(true);
                return;
            }
        }

        TownChunk tc = CivGlobal.getTownChunk(loc);
        if (tc == null) {
            return;
        }

        Resident resident = CivGlobal.getResident(event.getPlayer().getName());

        if (resident == null) {
            event.setCancelled(true);
        }

        if (!tc.perms.hasPermission(PlotPermissions.Type.ITEMUSE, resident)) {
            event.setCancelled(true);
            CivMessage.sendErrorNoRepeat(event.getPlayer(), CivSettings.localize.localizedString("itemUse_errorGeneric") + " " + stack.getType().toString() + " ");
        }

        return;
    }

    /*
     * Handles rotating of itemframes
     * 点击实体调用
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void OnPlayerInteractEntityEvent(PlayerInteractEntityEvent event) {

        if (event.getRightClicked().getType().equals(EntityType.HORSE)
                || event.getRightClicked().getType().equals(EntityType.SKELETON_HORSE)) {
            if (!HorseModifier.isCivCraftHorse((LivingEntity) event.getRightClicked())) {
                CivMessage.sendError(event.getPlayer(), CivSettings.localize.localizedString("horseUse_invalidHorse"));
                event.setCancelled(true);
                event.getRightClicked().remove();
                return;
            }
        }
        // 禁用村民交互
        if (event.getRightClicked().getType().equals(EntityType.VILLAGER)) {
            event.setCancelled(true);
            return;
        }

        ItemStack inHand = event.getPlayer().getInventory().getItemInMainHand();
        if (inHand != null) {
            boolean denyBreeding = false;
            switch (event.getRightClicked().getType()) {
                case COW:
                case SHEEP:
                case MUSHROOM_COW:
                    if (inHand.getType().equals(Material.WHEAT)) {
                        denyBreeding = true;
                    }
                    break;
                case PIG:
                    if (inHand.getType().equals(Material.CARROT_ITEM)
                            || inHand.getType().equals(Material.POTATO_ITEM)
                            || inHand.getType().equals(Material.BEETROOT)
                            || inHand.getType().equals(Material.GOLDEN_CARROT)) {
                        denyBreeding = true;
                    }
                    break;
                case HORSE:
                    if (inHand.getType().equals(Material.GOLDEN_APPLE) ||
                            inHand.getType().equals(Material.GOLDEN_CARROT)) {
                        CivMessage.sendError(event.getPlayer(), CivSettings.localize.localizedString("itemUse_errorNoHorseBreeding"));
                        event.setCancelled(true);
                        return;
                    }
                    break;
                case CHICKEN:
                    if (inHand.getType().equals(Material.SEEDS)
                            || inHand.getType().equals(Material.MELON_SEEDS)
                            || inHand.getType().equals(Material.PUMPKIN_SEEDS)
                            || inHand.getType().equals(Material.BEETROOT_SEEDS)) {
                        denyBreeding = true;
                    }
                    break;
                case RABBIT:
                    if (inHand.getType().equals(Material.CARROT_ITEM) ||
                            inHand.getType().equals(Material.GOLDEN_CARROT) ||
                            inHand.getType().equals(Material.YELLOW_FLOWER)) {
                        denyBreeding = true;
                    }
                    break;
                case WOLF:
                    if (inHand.getType().equals(Material.RAW_BEEF)
                            || inHand.getType().equals(Material.COOKED_BEEF)
                            || inHand.getType().equals(Material.RAW_CHICKEN)
                            || inHand.getType().equals(Material.COOKED_CHICKEN)
                            || inHand.getType().equals(Material.MUTTON)
                            || inHand.getType().equals(Material.COOKED_MUTTON)
                            || inHand.getType().equals(Material.COOKED_RABBIT)
                            || inHand.getType().equals(Material.RABBIT)
                            || inHand.getType().equals(Material.GRILLED_PORK)
                            || inHand.getType().equals(Material.PORK)) {
                        denyBreeding = true;
                    }
                    break;
                case LLAMA:
                    if (inHand.getType().equals(Material.HAY_BLOCK)) {
                        denyBreeding = true;
                    }
                    break;
                default:
                    break;
            }

            if (denyBreeding) {
                ChunkCoord coord = new ChunkCoord(event.getPlayer().getLocation());
                Pasture pasture = Pasture.pastureChunks.get(coord);

                if (pasture == null) {
                    CivMessage.sendError(event.getPlayer(), CivSettings.localize.localizedString("itemUse_errorNoWildBreeding"));
                    event.setCancelled(true);
                } else {
                    int loveTicks;
                    NBTTagCompound tag = new NBTTagCompound();
                    ((CraftEntity) event.getRightClicked()).getHandle().c(tag);
                    loveTicks = tag.getInt("InLove");

                    if (loveTicks == 0) {
                        if (!pasture.processMobBreed(event.getPlayer(), event.getRightClicked().getType())) {
                            event.setCancelled(true);
                        }
                    } else {
                        event.setCancelled(true);
                    }
                }

                return;
            }
        }
        if (!(event.getRightClicked() instanceof ItemFrame) && !(event.getRightClicked() instanceof Painting)) {
            return;
        }

        coord.setFromLocation(event.getPlayer().getLocation());
        TownChunk tc = CivGlobal.getTownChunk(coord);
        if (tc == null) {
            return;
        }

        Resident resident = CivGlobal.getResident(event.getPlayer().getName());
        if (resident == null) {
            return;
        }

        if (!tc.perms.hasPermission(PlotPermissions.Type.INTERACT, resident)) {
            event.setCancelled(true);
            CivMessage.sendErrorNoRepeat(event.getPlayer(), CivSettings.localize.localizedString("itemUse_errorPaintingOrFrame"));
        }

    }


    /*
     * Handles breaking of paintings and itemframes.
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void OnHangingBreakByEntityEvent(HangingBreakByEntityEvent event) {
        //	CivLog.debug("hanging painting break event");

        ItemFrameStorage frameStore = CivGlobal.getProtectedItemFrame(event.getEntity().getUniqueId());
        if (frameStore != null) {
//			if (!(event.getRemover() instanceof Player)) {
//				event.setCancelled(true);
//				return;
//			}
//			
//			if (frameStore.getTown() != null) {
//				Resident resident = CivGlobal.getResident((Player)event.getRemover());
//				if (resident == null) {
//					event.setCancelled(true);
//					return;
//				}
//				
//				if (resident.hasTown() == false || resident.getTown() != frameStore.getTown()) {
//					event.setCancelled(true);
//					CivMessage.sendError((Player)event.getRemover(), "Cannot remove item from protected item frame. Belongs to another town.");
//					return;
//				}
//			}
//			
//			CivGlobal.checkForEmptyDuplicateFrames(frameStore);
//			
//			ItemStack stack = ((ItemFrame)event.getEntity()).getItem();
//			if (stack != null && !stack.getType().equals(Material.AIR)) {
//				BonusGoodie goodie = CivGlobal.getBonusGoodie(stack);
//				if (goodie != null) {
//					frameStore.getTown().onGoodieRemoveFromFrame(frameStore, goodie);
//				}
//				frameStore.clearItem();
//				TaskMaster.syncTask(new DelayItemDrop(stack, event.getEntity().getLocation()));
//			}
            if (event.getRemover() instanceof Player) {
                CivMessage.sendError((Player) event.getRemover(), CivSettings.localize.localizedString("blockBreak_errorItemFrame"));
            }
            event.setCancelled(true);
            return;
        }

        if (event.getRemover() instanceof Player) {
            Player player = (Player) event.getRemover();

            coord.setFromLocation(player.getLocation());
            TownChunk tc = CivGlobal.getTownChunk(coord);

            if (tc == null) {
                return;
            }

            Resident resident = CivGlobal.getResident(player.getName());
            if (resident == null) {
                event.setCancelled(true);
            }

            if (!tc.perms.hasPermission(PlotPermissions.Type.DESTROY, resident)) {
                event.setCancelled(true);
                CivMessage.sendErrorNoRepeat(player, CivSettings.localize.localizedString("blockBreak_errorFramePermission"));
            }
        }


    }


    @EventHandler(priority = EventPriority.NORMAL)
    public void onChunkLoadEvent(ChunkLoadEvent event) {
        ChunkCoord coord = new ChunkCoord(event.getChunk());
        FarmChunk fc = CivGlobal.getFarmChunk(coord);
        if (fc == null) {
            return;
        }

        for (org.bukkit.entity.Entity ent : event.getChunk().getEntities()) {
            if (ent.getType().equals(EntityType.ZOMBIE)) {
                ent.remove();
            }
        }

        class AsyncTask extends CivAsyncTask {

            FarmChunk fc;

            public AsyncTask(FarmChunk fc) {
                this.fc = fc;
            }

            @Override
            public void run() {
                if (fc.getMissedGrowthTicks() > 0) {
                    fc.processMissedGrowths(false, this);
                    fc.getFarm().saveMissedGrowths();
                }
            }

        }

        TaskMaster.syncTask(new AsyncTask(fc), 500);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDeath(EntityDeathEvent event) {

        Pasture pasture = Pasture.pastureEntities.get(event.getEntity().getUniqueId());
        if (pasture != null) {
            pasture.onEntityDeath(event.getEntity());
        }

        return;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockGrowEvent(BlockGrowEvent event) {
        bcoord.setFromLocation(event.getBlock().getLocation().add(0, -1, 0));
        if (CivGlobal.vanillaGrowthLocations.contains(bcoord)) {
            /* Allow vanilla growth on these plots. */
            return;
        }

        Block b = event.getBlock();

        if (Farm.isBlockControlled(b)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityBreakDoor(EntityBreakDoorEvent event) {
        bcoord.setFromLocation(event.getBlock().getLocation());
        StructureBlock sb = CivGlobal.getStructureBlock(bcoord);
        if (sb != null) {
            event.setCancelled(true);
        }

        CampBlock cb = CivGlobal.getCampBlock(bcoord);
        if (cb != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onCreatureSpawnEvent(CreatureSpawnEvent event) {
        // war，不是马 不是繁殖， 就取消生成
        if (War.isWarTime() && !event.getEntity().getType().equals(EntityType.HORSE)) {
            if (!event.getSpawnReason().equals(SpawnReason.BREEDING)) {
                event.setCancelled(true);
                return;
            }
        }

        if (event.getEntity().getType().equals(EntityType.CHICKEN)) {
            if (event.getSpawnReason().equals(SpawnReason.EGG)) {
                event.setCancelled(true);
                return;
            }

            NBTTagCompound compound = new NBTTagCompound();
            if (compound.getBoolean("IsChickenJockey")) {
                event.setCancelled(true);
                return;
            }
        }

        if (event.getEntity().getType().equals(EntityType.IRON_GOLEM) &&
                event.getSpawnReason().equals(SpawnReason.BUILD_IRONGOLEM)) {
            event.setCancelled(true);
            return;
        }

//		if (event.getEntity().getType().equals(EntityType.ZOMBIE) ||
//			event.getEntity().getType().equals(EntityType.SKELETON) ||
//			event.getEntity().getType().equals(EntityType.BAT) ||
//			event.getEntity().getType().equals(EntityType.CAVE_SPIDER) ||
//			event.getEntity().getType().equals(EntityType.SPIDER) ||
//			event.getEntity().getType().equals(EntityType.CREEPER) ||
//			event.getEntity().getType().equals(EntityType.WOLF) ||
//			event.getEntity().getType().equals(EntityType.SILVERFISH) ||
//			event.getEntity().getType().equals(EntityType.OCELOT) ||
//			event.getEntity().getType().equals(EntityType.WITCH) ||
//			event.getEntity().getType().equals(EntityType.ENDERMAN)) {
//
//			event.setCancelled(true);
//			return;
//		}

        if (event.getSpawnReason().equals(SpawnReason.SPAWNER)) {
            event.setCancelled(true);
            return;
        }
    }

    public boolean allowPistonAction(Location loc) {
        bcoord.setFromLocation(loc);
        StructureBlock sb = CivGlobal.getStructureBlock(bcoord);
        if (sb != null) {
            return false;
        }

        RoadBlock rb = CivGlobal.getRoadBlock(bcoord);
        if (rb != null) {
            return false;
        }

        CampBlock cb = CivGlobal.getCampBlock(bcoord);
        if (cb != null) {
            return false;
        }

        /*
         * If we're next to an attached protected item frame. Disallow
         * we cannot break protected item frames.
         *
         * Only need to check blocks directly next to us.
         */
        BlockCoord bcoord2 = new BlockCoord(bcoord);
        bcoord2.setX(bcoord.getX() - 1);
        if (ItemFrameStorage.attachedBlockMap.containsKey(bcoord2)) {
            return false;
        }

        bcoord2.setX(bcoord.getX() + 1);
        if (ItemFrameStorage.attachedBlockMap.containsKey(bcoord2)) {
            return false;
        }

        bcoord2.setZ(bcoord.getZ() - 1);
        if (ItemFrameStorage.attachedBlockMap.containsKey(bcoord2)) {
            return false;
        }

        bcoord2.setZ(bcoord.getZ() + 1);
        if (ItemFrameStorage.attachedBlockMap.containsKey(bcoord2)) {
            return false;
        }

        coord.setFromLocation(loc);
        HashSet<Wall> walls = CivGlobal.getWallChunk(coord);

        if (walls != null) {
            for (Wall wall : walls) {
                if (wall.isProtectedLocation(loc)) {
                    return false;
                }
            }
        }

        return true;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPistonExtendEvent(BlockPistonExtendEvent event) {

        /* UGH. If we extend into 'air' it doesnt count them as blocks...
         * we need to check air to prevent breaking of item frames...
         */
        final int PISTON_EXTEND_LENGTH = 13;
        Block currentBlock = event.getBlock().getRelative(event.getDirection());
        for (int i = 0; i < PISTON_EXTEND_LENGTH; i++) {
            if (ItemManager.getId(currentBlock) == CivData.AIR) {
                if (!allowPistonAction(currentBlock.getLocation())) {
                    event.setCancelled(true);
                    return;
                }
            }

            currentBlock = currentBlock.getRelative(event.getDirection());
        }

        if (War.isWarTime()) {
            event.setCancelled(true);
            return;
        }

//		if (event.getBlocks().size() == 0) {
//			Block extendInto = event.getBlock().getRelative(event.getDirection());
//			if (!allowPistonAction(extendInto.getLocation())) {
//				event.setCancelled(true);
//				return;
//			}
//		}
        coord.setFromLocation(event.getBlock().getLocation());
        FarmChunk fc = CivGlobal.getFarmChunk(coord);
        if (fc == null) {
            event.setCancelled(true);

        }

        for (Block block : event.getBlocks()) {
            if (!allowPistonAction(block.getLocation())) {
                event.setCancelled(true);
                break;

            }
        }

    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPistonRetractEvent(BlockPistonRetractEvent event) {
        for (Block block : event.getBlocks()) {
            if (!allowPistonAction(block.getLocation())) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onProjectileLaunchEvent(ProjectileLaunchEvent event) {
        if (!(event.getEntity() instanceof ThrownPotion)) {
            return;
        }
        ThrownPotion potion = (ThrownPotion) event.getEntity();
        if (!(potion.getShooter() instanceof Player)) {
            //Get Ruffian type here and change damage type based on the potion thrown
            //Also change effect based on ruffian type
            String entityName = null;
            LivingEntity shooter = (LivingEntity) potion.getShooter();
            Witch witch = (Witch) shooter;

            if (!(witch.getTarget() instanceof Player)) {
                return;
            }
            if (potion.getShooter() instanceof LivingEntity) {
                entityName = shooter.getCustomName();
            }
            if (entityName != null && entityName.endsWith(" Ruffian")) {
                EntityInsentient nmsEntity = (EntityInsentient) ((CraftLivingEntity) shooter).getHandle();
                AttributeInstance attribute = nmsEntity.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE);
                Double damage = attribute.getValue();

                class RuffianProjectile {
                    Location loc;
                    Location target;
                    org.bukkit.entity.Entity attacker;
                    int speed = 1;
                    double damage;
                    int splash = 6;

                    public RuffianProjectile(Location loc, Location target, org.bukkit.entity.Entity attacker, double damage) {
                        this.loc = loc;
                        this.target = target;
                        this.attacker = attacker;
                        this.damage = damage;
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
                            //setFireAt(location, spread);
                        }

                        launchExplodeFirework(loc);
                        //loc.getWorld().createExplosion(loc, 1.0f, true);
                        damagePlayers(loc, splash);
                        //setFireAt(loc, spread);
                    }

                    @SuppressWarnings("deprecation")
                    private void damagePlayers(Location loc, int radius) {
                        double x = loc.getX() + 0.5;
                        double y = loc.getY() + 0.5;
                        double z = loc.getZ() + 0.5;
                        double r = (double) radius;

                        CraftWorld craftWorld = (CraftWorld) attacker.getWorld();

                        AxisAlignedBB bb = AxisAlignedBB(x - r, y - r, z - r, x + r, y + r, z + r);

                        List<net.minecraft.server.v1_12_R1.Entity> entities = craftWorld.getHandle().getEntities(((CraftEntity) attacker).getHandle(), bb);

                        for (net.minecraft.server.v1_12_R1.Entity e : entities) {
                            if (e instanceof EntityPlayer) {
                                EntityDamageByEntityEvent event = new EntityDamageByEntityEvent(attacker, ((EntityPlayer) e).getBukkitEntity(), DamageCause.ENTITY_ATTACK, damage);
                                Bukkit.getServer().getPluginManager().callEvent(event);
                                e.damageEntity(DamageSource.GENERIC, (float) event.getDamage());
                            }
                        }

                    }


//					private void setFireAt(Location loc, int radius) {
//						//Set the entire area on fire.
//						for (int x = -radius; x < radius; x++) {
//							for (int y = -3; y < 3; y++) {
//								for (int z = -radius; z < radius; z++) {
//									Block block = loc.getWorld().getBlockAt(loc.getBlockX()+x, loc.getBlockY()+y, loc.getBlockZ()+z);
//									if (ItemManager.getId(block) == CivData.AIR) {
//										ItemManager.setTypeId(block, CivData.FIRE);
//										ItemManager.setData(block, 0, true);
//									}
//								}
//							}
//						}
//					}

                    private AxisAlignedBB AxisAlignedBB(double d, double e,
                                                        double f, double g, double h, double i) {
                        return new AxisAlignedBB(d, e, f, g, h, i);
//						return null;
                    }

                    private void launchExplodeFirework(Location loc) {
                        FireworkEffect fe = FireworkEffect.builder().withColor(Color.ORANGE).withColor(Color.YELLOW).flicker(true).with(Type.BURST).build();
                        TaskMaster.syncTask(new FireWorkTask(fe, loc.getWorld(), loc, 3), 0);
                    }
                }


                class SyncFollow implements Runnable {
                    public RuffianProjectile proj;

                    @Override
                    public void run() {

                        if (proj.advance()) {
                            proj = null;
                            return;
                        }
                        TaskMaster.syncTask(this, 1);
                    }
                }

                SyncFollow follow = new SyncFollow();
                RuffianProjectile proj = new RuffianProjectile(shooter.getLocation(),
                        witch.getTarget().getLocation(), (org.bukkit.entity.Entity) potion.getShooter(), damage);
                follow.proj = proj;
                TaskMaster.syncTask(follow);


                event.setCancelled(true);
            }
            return;
        }
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPotionSplashEvent(PotionSplashEvent event) {
        ThrownPotion potion = event.getPotion();
        if (!(potion.getShooter() instanceof Player)) {
            return;
        }

        Player attacker = (Player) potion.getShooter();

        for (PotionEffect effect : potion.getEffects()) {
            if (effect.getType().equals(PotionEffectType.INVISIBILITY)) {
                event.setCancelled(true);
                return;
            }
        }

        boolean protect = false;
        for (PotionEffect effect : potion.getEffects()) {
            if (effect.getType().equals(PotionEffectType.BLINDNESS) ||
                    effect.getType().equals(PotionEffectType.CONFUSION) ||
                    effect.getType().equals(PotionEffectType.HARM) ||
                    effect.getType().equals(PotionEffectType.POISON) ||
                    effect.getType().equals(PotionEffectType.SLOW) ||
                    effect.getType().equals(PotionEffectType.SLOW_DIGGING) ||
                    effect.getType().equals(PotionEffectType.WEAKNESS) ||
                    effect.getType().equals(PotionEffectType.WITHER)) {

                protect = true;
                break;
            }
        }

        if (!protect) {
            return;
        }

        for (LivingEntity entity : event.getAffectedEntities()) {
            if (entity instanceof Player) {
                Player defender = (Player) entity;
                coord.setFromLocation(entity.getLocation());
                TownChunk tc = CivGlobal.getTownChunk(coord);
                if (tc == null) {
                    continue;
                }

                switch (playersCanPVPHere(attacker, defender, tc)) {
                    case ALLOWED:
                        continue;
                    case NOT_AT_WAR:
                        CivMessage.send(attacker, CivColor.Rose + CivSettings.localize.localizedString("var_itemUse_potionError1", defender.getName()));
                        event.setCancelled(true);
                        return;
                    case NEUTRAL_IN_WARZONE:
                        CivMessage.send(attacker, CivColor.Rose + CivSettings.localize.localizedString("var_itemUse_potionError2", defender.getName()));
                        event.setCancelled(true);
                        return;
                    case NON_PVP_ZONE:
                        CivMessage.send(attacker, CivColor.Rose + CivSettings.localize.localizedString("var_itemUse_potionError3", defender.getName()));
                        event.setCancelled(true);
                        return;
                    case ATTACKER_PROTECTED:
                        CivMessage.send(attacker, CivColor.Rose + CivSettings.localize.localizedString("pvpListenerError"));
                        event.setCancelled(true);
                        return;
                    case DEFENDER_PROTECTED:
                        CivMessage.send(attacker, CivColor.Rose + CivSettings.localize.localizedString("pvpListenerError2"));
                        event.setCancelled(true);
                        return;
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockRedstoneEvent(BlockRedstoneEvent event) {

        bcoord.setFromLocation(event.getBlock().getLocation());

        CampBlock cb = CivGlobal.getCampBlock(bcoord);
        if (cb != null) {
            if (ItemManager.getId(event.getBlock()) == CivData.WOOD_DOOR ||
                    ItemManager.getId(event.getBlock()) == CivData.IRON_DOOR ||
                    ItemManager.getId(event.getBlock()) == CivData.SPRUCE_DOOR ||
                    ItemManager.getId(event.getBlock()) == CivData.BIRCH_DOOR ||
                    ItemManager.getId(event.getBlock()) == CivData.JUNGLE_DOOR ||
                    ItemManager.getId(event.getBlock()) == CivData.ACACIA_DOOR ||
                    ItemManager.getId(event.getBlock()) == CivData.DARK_OAK_DOOR) {
                event.setNewCurrent(0);
                return;
            }
        }

        if (War.isWarTime()) {
            event.setNewCurrent(0);
            return;
        }

    }

    private PVPDenyReason playersCanPVPHere(Player attacker, Player defender, TownChunk tc) {
        Resident defenderResident = CivGlobal.getResident(defender);
        Resident attackerResident = CivGlobal.getResident(attacker);
        PVPDenyReason reason = PVPDenyReason.NON_PVP_ZONE;

        /* Outlaws can only pvp each other if they are declared at this location. */
        if (CivGlobal.isOutlawHere(defenderResident, tc) ||
                CivGlobal.isOutlawHere(attackerResident, tc)) {
            return PVPDenyReason.ALLOWED;
        }

        if (defenderResident.isProtected()) {
            return PVPDenyReason.DEFENDER_PROTECTED;
        }
        if (attackerResident.isProtected()) {
            return PVPDenyReason.ATTACKER_PROTECTED;
        }

        /*
         * If it is WarTime and the town we're in is at war, allow neutral players to be
         * targeted by anybody.
         */
        if (War.isWarTime()) {
            if (tc.getTown().getCiv().getDiplomacyManager().isAtWar()) {
                /*
                 * The defender is neutral if he is not in a town/civ, or not in his own civ AND not 'at war'
                 * with the attacker.
                 */
                if (!defenderResident.hasTown() || (!defenderResident.getTown().getCiv().getDiplomacyManager().atWarWith(tc.getTown().getCiv()) &&
                        defenderResident.getTown().getCiv() != tc.getTown().getCiv())) {
                    /* Allow neutral players to be hurt, but not hurt them back. */
                    return PVPDenyReason.ALLOWED;
                } else if (!attackerResident.hasTown() || (!attackerResident.getTown().getCiv().getDiplomacyManager().atWarWith(tc.getTown().getCiv()) &&
                        attackerResident.getTown().getCiv() != tc.getTown().getCiv())) {
                    reason = PVPDenyReason.NEUTRAL_IN_WARZONE;
                }
            }
        }

        boolean defenderAtWarWithAttacker = false;
        if (defenderResident != null && defenderResident.hasTown()) {
            defenderAtWarWithAttacker = defenderResident.getTown().getCiv().getDiplomacyManager().atWarWith(attacker);
            /*
             * If defenders are at war with attackers allow PVP. Location doesnt matter. Allies should be able to help
             * defend each other regardless of where they are currently located.
             */
            if (defenderAtWarWithAttacker) {
                //if (defenderResident.getTown().getCiv() == tc.getTown().getCiv() ||
                //	attackerResident.getTown().getCiv() == tc.getTown().getCiv()) {
                return PVPDenyReason.ALLOWED;
                //}
            } else if (reason.equals(PVPDenyReason.NON_PVP_ZONE)) {
                reason = PVPDenyReason.NOT_AT_WAR;
            }
        }

        return reason;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onEntityPortalCreate(EntityCreatePortalEvent event) {
        event.setCancelled(true);
    }

    private enum PVPDenyReason {
        ALLOWED,
        NON_PVP_ZONE,
        NOT_AT_WAR,
        NEUTRAL_IN_WARZONE,
        DEFENDER_PROTECTED,
        ATTACKER_PROTECTED
    }

}
