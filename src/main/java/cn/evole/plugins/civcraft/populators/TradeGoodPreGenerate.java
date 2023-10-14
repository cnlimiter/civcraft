/**
 * [2023] Cnlimiter LLC
 * All Rights Reserved.
 * Name: civcraft
 * Author: cnlimiter
 * UpdateTime: 2023/10/14 16:15
 * Description:
 * License: ARR
 */
package cn.evole.plugins.civcraft.populators;

import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.config.ConfigHemisphere;
import cn.evole.plugins.civcraft.config.ConfigTradeGood;
import cn.evole.plugins.civcraft.exception.InvalidConfiguration;
import cn.evole.plugins.civcraft.main.CivLog;
import cn.evole.plugins.civcraft.util.ChunkCoord;
import org.bukkit.Bukkit;

import java.util.*;

public class TradeGoodPreGenerate {

    // Maybe all we need is a hashset?
    public Map<ChunkCoord, TradeGoodPick> goodPicks = new HashMap<ChunkCoord, TradeGoodPick>();
    private int chunks_min;
    private int chunks_max;
    private int chunks_x;
    private int chunks_z;
    private int seed;
    private String worldName;

    public TradeGoodPreGenerate() {

    }


    private boolean validHemisphere(ConfigHemisphere hemi, int x, int z) {
        if (hemi.x_max != 0 && x > hemi.x_max) {
            return false;
        }
        if (hemi.x_min != 0 && x < hemi.x_min) {
            return false;
        }
        if (hemi.z_max != 0 && z > hemi.z_max) {
            return false;
        }
        if (hemi.z_min != 0 && z < hemi.z_min) {
            return false;
        }
        return true;
    }


    private TreeSet<ConfigTradeGood> getValidTradeGoods(int x, int z, Map<String, ConfigTradeGood> goods) {

        TreeSet<ConfigTradeGood> validGoods = new TreeSet<ConfigTradeGood>();
        for (ConfigTradeGood good : goods.values()) {
            String hemiString = good.hemiString;
            if (hemiString == null) {
                //No hemis selected means valid everywhere, add it.
                validGoods.add(good);
                continue;
            }

            String[] hemiStrs = hemiString.split(",");
            for (String str : hemiStrs) {
                ConfigHemisphere hemi = CivSettings.hemispheres.get(str);
                if (hemi == null) {
                    CivLog.warning("Invalid hemisphere:" + str + " detected for trade good generation.");
                    continue; //ignore invalid hemisphere
                }

                if (validHemisphere(hemi, x, z)) {
                    validGoods.add(good);
                }
            }
        }

        return validGoods;
    }

    /*
     * Pre-generate the locations of the trade goods so that we can
     * validate their positions relative to each other. Once generated
     * save results to a file, and load if that file exists.
     * 以前生成的位置贸易商品,这样我们就可以, 验证他们的位置相对于彼此。一旦生成, 将结果保存到一个文件,并加载如果文件存在
     */
    public void preGenerate() {
        try {
            chunks_min = CivSettings.getInteger(CivSettings.goodsConfig, "generation.chunks_min");
            chunks_max = CivSettings.getInteger(CivSettings.goodsConfig, "generation.chunks_max");
            chunks_x = CivSettings.getInteger(CivSettings.goodsConfig, "generation.chunks_x");
            chunks_z = CivSettings.getInteger(CivSettings.goodsConfig, "generation.chunks_z");
            seed = CivSettings.getInteger(CivSettings.goodsConfig, "generation.seed");
            this.worldName = Bukkit.getWorlds().get(0).getName();

        } catch (InvalidConfiguration e) {
            e.printStackTrace();
        }

        Random rand = new Random();
        rand.setSeed(seed);
        CivLog.info("Generating Trade Goodie Locations.");
        for (int x = -chunks_x; x < chunks_x; x += chunks_min) {
            for (int z = -chunks_z; z < chunks_z; z += chunks_min) {
                int diff = chunks_max - chunks_min;
                int randX = x;
                int randZ = z;

                if (diff > 0) {
                    if (rand.nextBoolean()) {
                        randX += rand.nextInt(diff);
                    } else {
                        randX -= rand.nextInt(diff);
                    }

                    if (rand.nextBoolean()) {
                        randZ += rand.nextInt(diff);
                    } else {
                        randZ -= rand.nextInt(diff);
                    }
                }


                ChunkCoord cCoord = new ChunkCoord(worldName, randX, randZ);
                pickFromCoord(cCoord);
            }
        }

        CivLog.info("Done.");


    }

    private ConfigTradeGood pickFromSet(TreeSet<ConfigTradeGood> set, int rand) {

        ArrayList<ConfigTradeGood> pickList = new ArrayList<ConfigTradeGood>();
        //Find goodies that are == generated random value
        for (ConfigTradeGood good : set) {
            if (good.rarity <= rand + 1) {
                pickList.add(good);
            }
        }

        // Pick a random good from this list.
        Random random = new Random();
        return pickList.get(random.nextInt(pickList.size()));

    }

    private void pickFromCoord(ChunkCoord cCoord) {
        TreeSet<ConfigTradeGood> validLandGoods;
        TreeSet<ConfigTradeGood> validWaterGoods;
        TradeGoodPick pick = new TradeGoodPick();

        validLandGoods = this.getValidTradeGoods(cCoord.getX(), cCoord.getZ(), CivSettings.landGoods);
        validWaterGoods = this.getValidTradeGoods(cCoord.getX(), cCoord.getZ(), CivSettings.waterGoods);

        pick.chunkCoord = cCoord;

        Random random = new Random();
        int randRange = 4;
        int rand = random.nextInt(randRange);

        pick.landPick = pickFromSet(validLandGoods, rand);
        pick.waterPick = pickFromSet(validWaterGoods, rand);

        /*
         * Do not allow two of the same goodie within
         * 4 chunks of each other.
         */
        for (int x = -4; x < 4; x++) {
            for (int z = -4; z < 4; z++) {
                ChunkCoord n = new ChunkCoord(cCoord.getWorldname(), cCoord.getX(), cCoord.getZ());
                n.setX(n.getX() + x);
                n.setZ(n.getZ() + z);

                TradeGoodPick nearby = goodPicks.get(n);
                if (nearby == null) {
                    continue;
                }

                if (nearby.landPick == pick.landPick) {
                    if (validLandGoods.size() <= 1) {
                        /* Dont generate anything here. */
                        return;
                    } else {
                        while (nearby.landPick == pick.landPick) {
                            rand = random.nextInt(randRange);
                            pick.landPick = pickFromSet(validLandGoods, rand);
                        }
                    }
                }

                if (nearby.waterPick == pick.waterPick) {
                    if (validWaterGoods.size() <= 1) {
                        /* Dont generate anything here. */
                        return;
                    } else {
                        while (nearby.waterPick == pick.waterPick) {
                            rand = random.nextInt(randRange);
                            pick.waterPick = pickFromSet(validWaterGoods, rand);
                        }
                    }
                }
            }
        }


        this.goodPicks.put(cCoord, pick);
    }


}
