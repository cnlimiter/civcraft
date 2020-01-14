package com.avrgaming.civcraft.mobs;

import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.config.ConfigMobs;
import com.avrgaming.civcraft.lorestorage.LoreCraftableMaterial;
import com.avrgaming.civcraft.lorestorage.LoreMaterial;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivLog;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.util.CivColor;
import com.avrgaming.civcraft.util.ItemManager;
import net.minecraft.server.v1_12_R1.Entity;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Random;

public class MobListener implements Listener {

    @EventHandler(priority = EventPriority.NORMAL)
    public void onChunkUnloadEvent(ChunkUnloadEvent event) {
        MobSpawner.despawnAllHostileInChunk(null, event.getChunk(), false);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onChunkLoadEvent(ChunkLoadEvent event) {
        MobSpawner.despawnAllHostileInChunk(null, event.getChunk(), false);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityCatchFire(EntityCombustEvent event) {
        if (event.getEntityType() == EntityType.ZOMBIE
                || event.getEntityType() == EntityType.ZOMBIE_VILLAGER
                || event.getEntityType() == EntityType.SKELETON) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onAttackAndDefense(EntityDamageByEntityEvent event) {
        if (event.isCancelled()) return;

        Entity mob_attack = CustomMobListener.customMobs.get(event.getDamager().getUniqueId());
        if (mob_attack != null) {
            if (CivGlobal.getTownChunk(event.getDamager().getLocation()) != null
                    || CivGlobal.getCampChunk(event.getDamager().getLocation()) != null) {
                CustomMobListener.customMobs.remove(event.getDamager().getUniqueId());
                CustomMobListener.mobList.remove(event.getDamager().getUniqueId());
                event.getDamager().remove();
                event.setCancelled(true);
                return;
            }
        }

        Entity mob = CustomMobListener.customMobs.get(event.getEntity().getUniqueId());
        if (mob == null) return;

        ConfigMobs cmob = CivSettings.customMobs.get(ChatColor.stripColor(mob.getCustomName()).toUpperCase().replaceAll(" ", "_"));
        // Entity invalid? Remove.
        if (cmob == null) {
            CivLog.warning("Invalid mob found? " + mob.getCustomName() + "; removed.");
            CustomMobListener.customMobs.remove(event.getEntity().getUniqueId());
            CustomMobListener.mobList.remove(event.getEntity().getUniqueId());
            event.getEntity().remove();
            event.setCancelled(true);
            return;
        }

        if (CivGlobal.getTownChunk(event.getEntity().getLocation()) != null) {
            CustomMobListener.customMobs.remove(event.getEntity().getUniqueId());
            CustomMobListener.mobList.remove(event.getEntity().getUniqueId());
            event.getEntity().remove();
            event.setCancelled(true);
            return;
        }

        double damage = event.getDamage() - cmob.defense_dmg;
        if (damage < 0.5) {
            Player player = null;
            if (event.getDamager() instanceof Arrow) {
                if (event.getDamager() instanceof Player) {
                    player = (Player) ((Arrow) event.getDamager()).getShooter();
                }
            } else if (event.getDamager() instanceof Player) {
                player = (Player) event.getDamager();
            }

            Random rand = new Random();
            if (rand.nextInt(2) == 0) {
                if (player != null) {
                    damage = (rand.nextInt(3) / 2) + 0.5;
                    CivMessage.send(player, CivColor.Gray + "Attack grazed by " + damage + " HP");
                }
            } else {
                if (player != null) {
                    damage *= -1;
                    CivMessage.send(player, CivColor.Gray + "Attack ineffective by " + damage + " HP");
                    damage = 0.0;
                }
            }
        }
        event.setDamage(damage);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.isCancelled()) return;
        Entity mob = CustomMobListener.customMobs.get(event.getEntity().getUniqueId());
        if (mob == null) return;

        switch (event.getCause()) {
            case SUFFOCATION:
                Location loc = event.getEntity().getLocation();
                int y = loc.getWorld().getHighestBlockAt(loc.getBlockX(), loc.getBlockZ()).getY() + 4;
                loc.setY(y);
                event.getEntity().teleport(loc);
            case CONTACT:
            case FALL:
            case FIRE:
            case FIRE_TICK:
            case LAVA:
            case MELTING:
            case DROWNING:
            case FALLING_BLOCK:
            case BLOCK_EXPLOSION:
            case ENTITY_EXPLOSION:
            case LIGHTNING:
            case MAGIC:
                event.setCancelled(true);
                break;
            default:
                break;
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDeath(EntityDeathEvent event) {
        ConfigMobs cmob = CustomMobListener.mobList.get(event.getEntity().getUniqueId());
        if (cmob != null) {
            if (event.getEntity().getKiller() != null) {
                Player pl = event.getEntity().getKiller();
                Random rand = new Random();
                int coins = rand.nextInt(cmob.exp_max);
                if (coins < cmob.exp_min) coins = cmob.exp_min;
                event.setDroppedExp(coins);

                event.getDrops().clear();
                ArrayList<String> dropped = new ArrayList<>();
                for (String values : cmob.drops) { // Specific Mob's Drops
                    String[] drops = values.split(",");
                    double dc = Double.parseDouble(drops[4]);
                    int chance = rand.nextInt(10000);
                    if (chance < (dc * 10000)) {
                        dropped.add(values);
                    }
                }
                if (dropped.size() != 0) {
                    for (String items : dropped) {
                        if (items == null) continue;
                        String[] drops = items.split(",");
                        String mat = drops[0];
                        mat = mat.replace("[", "").replace("]", "");
                        int dropAmt;
                        int dropMin = Integer.parseInt(drops[2]);
                        int dropMax = Integer.parseInt(drops[3]);
                        // 掠夺
                        if (pl.getInventory().getItemInMainHand().containsEnchantment(Enchantment.LOOT_BONUS_MOBS)) {
                            dropMax += pl.getInventory().getItemInMainHand().getEnchantmentLevel(Enchantment.LOOT_BONUS_MOBS);
                        }

                        int amtToRand = dropMax - dropMin;
                        dropAmt = rand.nextInt(amtToRand + 1) + dropMin; // + (Integer.valueOf(drops[7])
                        if (dropAmt > dropMax) {
                            dropAmt = dropMax;
                        }

                        if (dropAmt > 0) {
                            LoreCraftableMaterial craftMat = LoreCraftableMaterial.getCraftMaterialFromId(mat);
                            if (craftMat != null) {
                                ItemStack item = LoreMaterial.spawn(LoreMaterial.materialMap.get(craftMat.getConfigId()), dropAmt);
                                event.getDrops().add(item);
                            } else {
                                ItemStack item = ItemManager.createItemStack(Integer.parseInt(mat), dropAmt);
                                //	CivMessage.global(item.getType().toString());
                                event.getDrops().add(item);
                            }
                        }
                    }
                }

                CustomMobListener.customMobs.remove(event.getEntity().getUniqueId());
                CustomMobListener.mobList.remove(event.getEntity().getUniqueId());
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerLeash(PlayerLeashEntityEvent event) {
        if (CustomMobListener.mobList.get(event.getEntity().getUniqueId()) != null) {
            CivMessage.sendError(event.getPlayer(), "This beast cannot be tamed.");
            event.setCancelled(true);
        }
    }

}
