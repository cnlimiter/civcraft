package com.avrgaming.civcraft.mobs;

import com.avrgaming.civcraft.camp.Camp;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.object.TownChunk;
import com.avrgaming.civcraft.util.ChunkCoord;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MobSpawnerTimer implements Runnable {
    // TODO 更新AdminMobCommand以不使用这些命令，这样我们就可以取消它们的静态化

    // 区域上限
    public static int MOB_AREA_LIMIT = 10;
    // 统计附近怪的距离
    public static int MOB_AREA = 40;

    // 最小生成距离
    public static int MIN_SPAWN_DISTANCE = 16;
    // 最大生成距离
    public static int MAX_SPAWN_DISTANCE = 40;
    // 最小生成数量
    public static int MAX_SPAWN_AMOUNT = 20;
    public static int MIN_SPAWN_AMOUNT = 3;
    // 生成高度 （模拟下坠?
    public static int Y_SHIFT = 4;


    @Override
    public void run() {
        String name = null;
        for (Player player : Bukkit.getOnlinePlayers()) {
            // 找到玩家 谁在野外
            World world = player.getWorld();
            //和平模式
            if (!world.getAllowMonsters()) return;
            // 检查是否真的在玩
            if (!CivGlobal.isValidPlayer(player)) continue;
            List<Entity> entities = getNearbyCustomEntities(player, MOB_AREA);
            if (entities.size() > MOB_AREA_LIMIT) continue;
            int count = entities.size();
            Random random = new Random();
            int spawnAmount = random.nextInt(MAX_SPAWN_AMOUNT - MIN_SPAWN_AMOUNT) + MIN_SPAWN_AMOUNT;
            for (int j = 0; j < spawnAmount; j++) {
                int x = random.nextInt(MAX_SPAWN_DISTANCE - MIN_SPAWN_DISTANCE) + MIN_SPAWN_DISTANCE;
                if (random.nextBoolean()) {
                    x *= -1;
                }

                int z = random.nextInt(MAX_SPAWN_DISTANCE - MIN_SPAWN_DISTANCE) + MIN_SPAWN_DISTANCE;
                if (random.nextBoolean()) {
                    z *= -1;
                }

                int y = world.getHighestBlockYAt(((Double) player.getLocation().getX()).intValue() + x, ((Double) player.getLocation().getZ()).intValue() + z);
                Location loc = new Location(world, player.getLocation().getX() + x, y + Y_SHIFT, player.getLocation().getZ() + z);
                if (!loc.getChunk().isLoaded()) continue;
                ChunkCoord chunk = new ChunkCoord(loc);
                TownChunk tc = CivGlobal.getTownChunk(chunk);
                // 城镇
                if (tc != null && !tc.perms.isCustomSpawnMobs()) continue;

                Camp cc = CivGlobal.getCampChunk(chunk);
                if (cc != null) continue;
                // 范围是个圆
                if (player.getLocation().distanceSquared(loc) > Math.pow(MAX_SPAWN_DISTANCE, 2)) continue;
                // Dont spawn mobs at invalid blocks
                Location blockLoc = loc;
                blockLoc.setY(loc.getY() - Y_SHIFT);
                Material blType = blockLoc.getBlock().getRelative(BlockFace.DOWN).getType();
                if (blType == Material.LAVA || blType == Material.STATIONARY_LAVA) {
                    continue;
                }
                if (blType == Material.WATER && !blockLoc.getBlock().getBiome().name().contains("OCEAN")) continue;

                MobSpawner.spawnRandomCustomMob(loc);
                count++;
                if (count > MAX_SPAWN_AMOUNT) break;
            }
            //break;
        }
    }

    public static List<Entity> getNearbyCustomEntities(Player pl, int radius) {
        List<Entity> ents = new ArrayList<>();

        for (Entity ent : pl.getNearbyEntities(radius, radius, radius)) {
            if (ent.hasMetadata("civ_custommob")) ents.add(ent);
        }

        return ents;
    }
}
