package cn.evole.plugins.civcraft.listener;

import cn.evole.plugins.civcraft.main.CivCraft;
import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.util.Schematic;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkPopulateEvent;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class WorldListener implements Listener {
    public static List<Schematic> schematics = new ArrayList<Schematic>();
    int skipCount = 0;

    public static void buildRuin(Location location) {
        Schematic schematic = schematics.get(CivCraft.civRandom.nextInt(schematics.size()));
        schematic.paste(location);
    }

    public static void loadRuins() throws IOException {
        for (File file : Objects.requireNonNull(new File("plugins/CivCraft/schematics").listFiles())) {
            if (!file.getName().endsWith(".schematic")) continue;
            schematics.add(Schematic.loadSchematic(file));
        }
    }

    public static int intRange(int from, int to) {
        int min = Math.min(from, to);
        int max = Math.max(from, to);
        return min + CivCraft.civRandom.nextInt(max - min + 1);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onChunkGenerate(ChunkPopulateEvent event) {
        Random rand = CivCraft.civRandom;
        if (rand.nextInt(10000000) < 500000) {
            ++this.skipCount;
            if (this.skipCount % 2 != 0) {
                return;
            }
            int x = event.getChunk().getX() * 16 + rand.nextInt(16);
            int z = event.getChunk().getZ() * 16 + rand.nextInt(16);
            for (int y = event.getWorld().getMaxHeight(); y > 30; --y) {
                Block current = event.getWorld().getBlockAt(x, y, z);
                if (current.getType().isSolid()) continue;
                boolean flat = true;
                for (int i = 0; i < 6; ++i) {
                    for (int j = 0; j < 6; ++j) {
                        for (int k = 0; k < 8; ++k) {
                            if (!current.getRelative(i, k, j).getType().isSolid() && !current.getRelative(i, k, j).getType().toString().contains("LEAVES") && current.getRelative(i, -1, j).getType().isSolid())
                                continue;
                            flat = false;
                        }
                    }
                }
                if (!flat) continue;
                WorldListener.buildRuin(new Location(event.getWorld(), x, y, z));
                ++CivGlobal.ruinsGenerated;
                break;
            }
        }
    }
}

