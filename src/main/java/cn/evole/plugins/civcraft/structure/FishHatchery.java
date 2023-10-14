package cn.evole.plugins.civcraft.structure;

import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.exception.CivException;
import cn.evole.plugins.civcraft.main.CivLog;
import cn.evole.plugins.civcraft.main.CivMessage;
import cn.evole.plugins.civcraft.object.CultureChunk;
import cn.evole.plugins.civcraft.object.StructureSign;
import cn.evole.plugins.civcraft.object.Town;
import cn.evole.plugins.civcraft.util.BlockCoord;
import cn.evole.plugins.civcraft.util.ChunkCoord;
import cn.evole.plugins.civcraft.util.CivColor;
import cn.evole.plugins.civcraft.util.SimpleBlock;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.locks.ReentrantLock;

public class FishHatchery extends Structure {
    public static final int MAX_CHANCE = CivSettings.getIntegerStructure("fishery.tierMax");
    public static final double FISH_T0_RATE = CivSettings.getDoubleStructure("fishery.t0_rate"); //100%
    public static final double FISH_T1_RATE = CivSettings.getDoubleStructure("fishery.t1_rate"); //100%
    public static final double FISH_T2_RATE = CivSettings.getDoubleStructure("fishery.t2_rate"); //100%
    public static final double FISH_T3_RATE = CivSettings.getDoubleStructure("fishery.t3_rate"); //100%
    public static final double FISH_T4_RATE = CivSettings.getDoubleStructure("fishery.t4_rate"); //100%
    public int skippedCounter = 0;
    public ReentrantLock lock = new ReentrantLock();
    private int level = 1;
    private Biome biome = null;

    protected FishHatchery(Location center, String id, Town town) throws CivException {
        super(center, id, town);
        setLevel(town.saved_fish_hatchery_level);
    }

    public FishHatchery(ResultSet rs) throws SQLException, CivException {
        super(rs);
    }

    @Override
    public String getDynmapDescription() {
        String out = "<u><b>" + this.getDisplayName() + "</u></b><br/>";
        out += CivSettings.localize.localizedString("Level") + " " + this.level;
        return out;
    }

    @Override
    public String getMarkerIconName() {
        return "cutlery";
    }

    public double getChance(double chance) {
        return this.modifyChance(chance);
    }

    private double modifyChance(Double chance) {
        //No buffs at this time
        return chance;
    }

    @Override
    public void onPostBuild(BlockCoord absCoord, SimpleBlock commandBlock) {
        this.level = getTown().saved_fish_hatchery_level;
        this.setBiome(this.getCorner().getBlock().getBiome());
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    private StructureSign getSignFromSpecialId(int special_id) {
        for (StructureSign sign : getSigns()) {
            int id = Integer.valueOf(sign.getAction());
            if (id == special_id) {
                return sign;
            }
        }
        return null;
    }


    @Override
    public void updateSignText() {
        int count = 0;

        for (count = 0; count < level; count++) {
            StructureSign sign = getSignFromSpecialId(count);
            if (sign == null) {
                CivLog.error("sign from special id was null, id:" + count);
                return;
            }
            sign.setText(CivSettings.localize.localizedString("fishery_sign_pool") + "\n" + (count + 1));
            sign.update();
        }

        for (; count < getSigns().size(); count++) {
            StructureSign sign = getSignFromSpecialId(count);
            if (sign == null) {
                CivLog.error("sign from special id was null, id:" + count);
                return;
            }
            sign.setText(CivSettings.localize.localizedString("fishery_sign_poolOffline"));
            sign.update();
        }

    }

    @Override
    public void processSignAction(Player player, StructureSign sign, PlayerInteractEvent event) {
        int special_id = Integer.valueOf(sign.getAction());
        if (special_id < this.level) {
            CivMessage.send(player, CivColor.LightGreen + CivSettings.localize.localizedString("var_fishery_pool_msg_online", (special_id + 1)));

        } else {
            CivMessage.send(player, CivColor.Rose + CivSettings.localize.localizedString("var_fishery_pool_msg_offline", (special_id + 1)));
        }
    }

    public Biome getBiome() {
        if (biome == null) {
            try {
                World world = Bukkit.getWorld("world");
                BlockCoord block = this.getCenterLocation();
                Chunk chunk = (Chunk) world.getChunkAt(block.getX(), block.getZ());
                ChunkCoord coord = new ChunkCoord(chunk);
                CultureChunk cc = new CultureChunk(this.getTown(), coord);
                biome = cc.getBiome();
                this.setBiome(cc.getBiome());
            } catch (IllegalStateException e) {

            } finally {
                biome = Biome.BIRCH_FOREST_HILLS;
            }
        }
        return biome;
    }

    public void setBiome(Biome biome) {
        this.biome = biome;
    }

}