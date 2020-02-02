package com.avrgaming.civcraft.mobs;

import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.config.ConfigMobs;
import com.avrgaming.civcraft.main.CivCraft;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivLog;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.object.CultureChunk;
import com.avrgaming.civcraft.object.Resident;
import com.avrgaming.civcraft.object.Town;
import com.avrgaming.civcraft.object.TownChunk;
import com.avrgaming.civcraft.threading.TaskMaster;
import com.avrgaming.civcraft.util.BukkitObjects;
import com.avrgaming.civcraft.util.CivColor;
import net.minecraft.server.v1_12_R1.Entity;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.ArrayList;
import java.util.Random;

public class MobSpawner {

    public static void despawnAllCustom() {
        for (Entity mob : CustomMobListener.customMobs.values()) {
            mob.getBukkitEntity().remove();
        }
    }

    public static void despawnMobs(Player p, boolean custom, boolean hostile, boolean townBorders, boolean civBorders, boolean onlinePlayersOnly, boolean loaded, boolean msg) {
        int countCustom = 0;
        int countTotal = 0;
        World w = Bukkit.getWorlds().get(0);
        Difficulty d = w.getDifficulty();
        w.setDifficulty(Difficulty.PEACEFUL);

        if (custom) {
            for (Entity e : CustomMobListener.customMobs.values()) {
                CustomMobListener.customMobs.remove(e.getUniqueID());
                CustomMobListener.mobList.remove(e.getUniqueID());
                e.getBukkitEntity().remove();
                countCustom++;
                countTotal++;
            }
        }

        if (hostile) {
            if (townBorders) {
                if (onlinePlayersOnly) {
                    ArrayList<Town> townsToClear = new ArrayList<Town>();
                    for (Player onp : Bukkit.getOnlinePlayers()) {
                        Resident res = CivGlobal.getResident(onp);
                        if (res.hasTown() && !townsToClear.contains(res.getTown())) townsToClear.add(res.getTown());
                    }

                    for (Town t : townsToClear) {
                        for (TownChunk tc : t.getTownChunks()) {
                            Chunk c = tc.getChunkCoord().getChunk();
                            if (c.getEntities().length < 1) continue;
                            for (org.bukkit.entity.Entity e : c.getEntities()) {
                                boolean r = false;
                                if (CivSettings.restrictedSpawns.contains(e.getType()) || CivSettings.vanillaHostileMobs.contains(e.getType())) {
                                    e.remove();
                                    countTotal++;
                                    r = true;
                                }

                                if (CustomMobListener.customMobs.containsKey(e.getUniqueId())) {
                                    CustomMobListener.customMobs.remove(e.getUniqueId());
                                    CustomMobListener.mobList.remove(e.getUniqueId());
                                    countCustom++;
                                    if (!r) countTotal++;
                                }
                            }
                        }
                    }
                } else {
                    for (TownChunk tc : CivGlobal.getTownChunks()) {
                        Chunk c = tc.getChunkCoord().getChunk();
                        if (c.getEntities().length < 1) continue;
                        for (org.bukkit.entity.Entity e : c.getEntities()) {
                            boolean r = false;
                            if (CivSettings.restrictedSpawns.contains(e.getType()) || CivSettings.vanillaHostileMobs.contains(e.getType())) {
                                e.remove();
                                countTotal++;
                                r = true;
                            }

                            if (CustomMobListener.customMobs.containsKey(e.getUniqueId())) {
                                CustomMobListener.customMobs.remove(e.getUniqueId());
                                CustomMobListener.mobList.remove(e.getUniqueId());
                                countCustom++;
                                if (!r) countTotal++;
                            }
                        }
                    }
                }

                if (civBorders) {
                    if (onlinePlayersOnly) {
                        ArrayList<Town> civsToClear = new ArrayList<Town>();
                        for (Player onp : Bukkit.getOnlinePlayers()) {
                            Resident res = CivGlobal.getResident(onp);
                            if (res.hasTown() && !civsToClear.contains(res.getTown())) civsToClear.add(res.getTown());
                        }

                        for (Town t : civsToClear) {
                            for (CultureChunk cc : t.getCultureChunks()) {
                                Chunk c = cc.getChunkCoord().getChunk();
                                if (c.getEntities().length < 1) continue;
                                for (org.bukkit.entity.Entity e : c.getEntities()) {
                                    boolean r = false;
                                    if (CivSettings.restrictedSpawns.contains(e.getType()) || CivSettings.vanillaHostileMobs.contains(e.getType())) {
                                        e.remove();
                                        countTotal++;
                                        r = true;
                                    }

                                    if (CustomMobListener.customMobs.containsKey(e.getUniqueId())) {
                                        CustomMobListener.customMobs.remove(e.getUniqueId());
                                        CustomMobListener.mobList.remove(e.getUniqueId());
                                        countCustom++;
                                        if (!r) countTotal++;
                                    }
                                }
                            }
                        }
                    } else {
                        for (CultureChunk cc : CivGlobal.getCultureChunks()) {
                            Chunk c = cc.getChunkCoord().getChunk();
                            if (c.getEntities().length < 1) continue;
                            for (org.bukkit.entity.Entity e : c.getEntities()) {
                                boolean r = false;
                                if (CivSettings.restrictedSpawns.contains(e.getType()) || CivSettings.vanillaHostileMobs.contains(e.getType())) {
                                    e.remove();
                                    countTotal++;
                                    r = true;
                                }

                                if (CustomMobListener.customMobs.containsKey(e.getUniqueId())) {
                                    CustomMobListener.customMobs.remove(e.getUniqueId());
                                    CustomMobListener.mobList.remove(e.getUniqueId());
                                    countCustom++;
                                    if (!r) countTotal++;
                                }
                            }
                        }
                    }
                }
            }

            if (loaded) {
                for (Chunk c : w.getLoadedChunks()) {
                    if (c.getEntities().length < 1) continue;
                    for (org.bukkit.entity.Entity e : c.getEntities()) {
                        if (CustomMobListener.customMobs.containsKey(e.getUniqueId())) {
                            CustomMobListener.customMobs.remove(e.getUniqueId());
                            CustomMobListener.mobList.remove(e.getUniqueId());
                            e.remove();
                            countCustom++;
                            countTotal++;
                            continue;
                        }

                        if (CivSettings.restrictedSpawns.contains(e.getType()) || CivSettings.vanillaHostileMobs.contains(e.getType())) {
                            e.remove();
                            countTotal++;
                        }
                    }
                }
            }
        }

        if (msg) {
            if (p != null) {
                CivMessage.sendSuccess(p, "Removed " + countCustom + " custom mobs, grand total of " + countTotal + ".");
            }
            CivLog.adminlog("CONSOLE", "Removed " + countCustom + " custom mobs, grand total of " + countTotal + ".");
        }
        w.setDifficulty(d);
    }

    public static void despawnAllHostileInChunk(Player p, Chunk c, boolean msg) {
        int countCustom = 0;
        int countTotal = 0;

        for (org.bukkit.entity.Entity e : c.getEntities()) {
            if (CustomMobListener.customMobs.containsKey(e.getUniqueId())) {
                CustomMobListener.customMobs.remove(e.getUniqueId());
                CustomMobListener.mobList.remove(e.getUniqueId());
                e.remove();
                countCustom++;
                countTotal++;
                continue;
            }

            if (CivSettings.restrictedSpawns.contains(e.getType())
                    || CivSettings.vanillaHostileMobs.contains(e.getType())) {
                e.remove();
                countTotal++;
            }
        }

        if (msg) {
            if (p != null) {
                CivMessage.sendSuccess(p, "Removed " + countCustom + " custom mobs, grand total of " + countTotal + " at Chunk x" + c.getX() + " z" + c.getZ() + ".");
            }
            CivLog.adminlog("CONSOLE", "Removed " + countCustom + " custom mobs, grand total of " + countTotal + " at Chunk x" + c.getX() + " z" + c.getZ() + ".");
        }
    }

    @SuppressWarnings("deprecation")
    private static void spawnCustomMob(ConfigMobs cmob, Location loc) {
        CraftWorld world = (CraftWorld) loc.getWorld();
//        world.loadChunk(loc.getChunk());

        Entity ent = world.createEntity(loc, EntityType.valueOf(cmob.entity).getEntityClass());

        cmob.setMaxHealth(ent.getBukkitEntity(), cmob.max_health);
        cmob.modifySpeed(ent.getBukkitEntity(), cmob.move_speed);
        if (ent.getBukkitEntity().getType() != EntityType.SLIME) {
            cmob.setAttack(ent.getBukkitEntity(), cmob.attack_dmg);
        }
//        cmob.setDefense(ent.getBukkitEntity(), cmob.defense_dmg);
        cmob.setFollowRange(ent.getBukkitEntity(), cmob.follow_range);
        cmob.setKnockbackResistance(ent.getBukkitEntity(), cmob.kb_resistance);

        if (cmob.name != null && !cmob.name.equals("")) {
            ent.setCustomName(CivColor.colorize(cmob.name));
        }
        ent.setCustomNameVisible(cmob.visible);

        ent.getBukkitEntity().setMetadata("civ_custommob", new FixedMetadataValue(CivCraft.getPlugin(), "true"));
        BukkitObjects.getScheduler().callSyncMethod(CivCraft.getPlugin(), () -> {
            return world.addEntity(ent, CreatureSpawnEvent.SpawnReason.CUSTOM);
        });
        CustomMobListener.customMobs.put(ent.getUniqueID(), ent);
        CustomMobListener.mobList.put(ent.getUniqueID(), cmob);

        class SyncTask implements Runnable {

            Entity ent;

            public SyncTask(Entity ent) {
                this.ent = ent;
            }

            @Override
            public void run() {
                LivingEntity lent = (LivingEntity) ent.getBukkitEntity();
                // Equipment
                lent.getEquipment().setHelmet(null);
                lent.getEquipment().setChestplate(null);
                lent.getEquipment().setLeggings(null);
                lent.getEquipment().setBoots(null);
                lent.getEquipment().setItemInMainHand(null);
                lent.getEquipment().setItemInOffHand(null);
                lent.getEquipment().clear();
            }
        }
        TaskMaster.syncTask(new SyncTask(ent), 2);
    }

    public static void spawnRandomCustomMob(Location loc) {
        ArrayList<ConfigMobs> validMobs = new ArrayList<>();
        for (ConfigMobs cmob : CivSettings.customMobs.values()) {
            for (String s : cmob.biomes) {
                if (s.toUpperCase().equals(loc.getBlock().getBiome().toString().toUpperCase())) {
                    validMobs.add(cmob);
                }
            }
        }

        if (validMobs.size() < 1) return;

        Random random = new Random();
        int idx = random.nextInt(validMobs.size());
        spawnCustomMob(validMobs.get(idx), loc);
    }

}
