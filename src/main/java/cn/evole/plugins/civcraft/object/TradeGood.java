/**
 * [2023] Cnlimiter LLC
 * All Rights Reserved.
 * Name: civcraft
 * Author: cnlimiter
 * UpdateTime: 2023/10/14 16:15
 * Description:
 * License: ARR
 */
package cn.evole.plugins.civcraft.object;

import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.config.ConfigTradeGood;
import cn.evole.plugins.civcraft.database.SQL;
import cn.evole.plugins.civcraft.database.SQLUpdate;
import cn.evole.plugins.civcraft.exception.InvalidConfiguration;
import cn.evole.plugins.civcraft.exception.InvalidNameException;
import cn.evole.plugins.civcraft.items.BonusGoodie;
import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.main.CivLog;
import cn.evole.plugins.civcraft.structure.Structure;
import cn.evole.plugins.civcraft.structure.TradeOutpost;
import cn.evole.plugins.civcraft.util.BlockCoord;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

public class TradeGood extends SQLObject {

    public static final String TABLE_NAME = "TRADE_GOODS";
    private ConfigTradeGood info;
    private Town town;
    private Civilization civ;
    private BlockCoord coord;
    private BlockCoord bonusLocation;
    private Structure struct;

    public TradeGood(ConfigTradeGood good, BlockCoord coord) {
        this.info = good;
        this.coord = coord;
        try {
            this.setName(good.id);
        } catch (InvalidNameException e) {
            e.printStackTrace();
        }

        town = null;
        civ = null;
    }

    public TradeGood(ResultSet rs) throws SQLException, InvalidNameException {
        this.load(rs);
    }

    public static void init() throws SQLException {
        if (!SQL.hasTable(TABLE_NAME)) {
            String table_create = "CREATE TABLE " + SQL.tb_prefix + TABLE_NAME + " (" +
                    "`id` int(11) unsigned NOT NULL auto_increment," +
                    "`name` VARCHAR(64) NOT NULL," +
                    "`town_id` int(11)," +
                    "`structure_id` int(11), " +
                    "`coord` mediumtext DEFAULT NULL," +
                    "`bonusLocation` mediumtext DEFAULT NULL," +
                    //	 "FOREIGN KEY (town_id) REFERENCES "+SQL.tb_prefix+"TOWNS(id),"+
                    "PRIMARY KEY (`id`)" + ")";

            SQL.makeTable(table_create);
            CivLog.info("Created " + TABLE_NAME + " table");
        } else {
            CivLog.info(TABLE_NAME + " table OK!");
        }
    }

    public static double getBaseValue(TradeGood good) {
        ConfigTradeGood configTradeGood = good.getInfo();
        double value = configTradeGood.value;
        return value;
    }

    public static int getTradeGoodCount(BonusGoodie goodie, Town town) {
        int amount = 0;

        for (BonusGoodie g : town.getBonusGoodies()) {
            if (goodie.getDisplayName().equals(g.getDisplayName())) {
                amount++;
            }
        }

		/*for (TradeGood g : town.getTradeGoods()) {
			if ((g.getInfo().id.equals(good.getInfo().id))) {

				if (g.getStruct() != null) {
					CultureChunk cc = CivGlobal.getCultureChunk(g.getCoord().getLocation());
					if (cc != null && cc.getTown() == town) {
						amount++;
					}
				}
			}
		}*/
        return amount;
    }

    private static double getTradeGoodIncomeBonus(TradeGood good, Town town) {
        //TODO revist when we add wonders.
        // Search for any passives that apply.
        //	ArrayList<String> values = town.getPassives("trade_income_bonus");

		/*for (String v : values) {
			String[] split = v.split(",");
			for (String good_str : split) {
				if (good_str.equalsIgnoreCase(good.name)) {
					// First value should always be bonus.
					return Double.valueOf(split[0]);
				}
			}
		}*/

        return 0;
    }

    public static double getTradeGoodValue(BonusGoodie goodie, Town town) {

        TradeGood good = goodie.getOutpost().getGood();
        double value = getBaseValue(good);
        int goodMax;
        try {
            goodMax = CivSettings.getInteger(CivSettings.goodsConfig, "trade_good_multiplier_max");
        } catch (InvalidConfiguration e) {
            e.printStackTrace();
            return 0;
        }
        int effectiveCount = getTradeGoodCount(goodie, town);
        effectiveCount -= 1;

        if (effectiveCount > goodMax) {
            effectiveCount = goodMax;
        }

        double rate = 1.0 + (0.5 * (effectiveCount));

        //Find any passives with trade_income_bonus for this good
        rate += getTradeGoodIncomeBonus(good, town);

        value *= rate;
        if (town.getCiv().getCapitol() != null && town.getCiv().getCapitol().getBuffManager().hasBuff("level8_extraTradeTown")) {
            value *= town.getCiv().getCapitol().getBuffManager().getEffectiveDouble("level8_extraTradeTown");
        }
        return value;
    }

    public static double getTownBaseGoodPaymentViaGoodie(Town town) {
        // Find trade goods from goodies in town hall.
        double total_payment = 0.0;

        for (BonusGoodie goodie : town.getBonusGoodies()) {
            TradeOutpost outpost = goodie.getOutpost();
            if (outpost == null) {
                continue;
            }
            CultureChunk cc = CivGlobal.getCultureChunk(outpost.getCorner().getLocation());
            if (cc == null) {
                continue;
            }
            if (!outpost.isActive()) {
                continue;
            }
            double payment = getTradeGoodValue(goodie, town);
            total_payment += payment;
        }

        return total_payment;
    }

    public static double getTownTradePayment(Town town) {
        double total_payment = getTownBaseGoodPaymentViaGoodie(town);
        total_payment *= town.getTradeRate();
        if (town.getCiv().getCapitol() != null && town.getCiv().getCapitol().getBuffManager().hasBuff("level8_extraTradeTown")) {
            total_payment *= town.getCiv().getCapitol().getBuffManager().getEffectiveDouble("level8_extraTradeTown");
        }
        return total_payment;
    }

    @Override
    public void load(ResultSet rs) throws SQLException, InvalidNameException {
        this.setId(rs.getInt("id"));
        this.setName(rs.getString("name"));
        setInfo(CivSettings.goods.get(this.getName()));
        this.setTown(CivGlobal.getTownFromId(rs.getInt("town_id")));
        String bonusLocation = rs.getString("bonusLocation");
        if (bonusLocation != null) {
            this.bonusLocation = new BlockCoord(bonusLocation);
        } else {
            this.bonusLocation = null;
        }

        this.coord = new BlockCoord(rs.getString("coord"));
        this.addProtectedBlocks(this.coord);

        this.setStruct(CivGlobal.getStructureById(rs.getInt("structure_id")));

        if (this.getStruct() != null) {
            if (struct instanceof TradeOutpost) {
                TradeOutpost outpost = (TradeOutpost) this.struct;
                outpost.setGood(this);
            }
        }

        if (this.getTown() != null) {
            this.civ = this.getTown().getCiv();
        }

    }

    private void addProtectedBlocks(BlockCoord coord2) {
//		CivLog.debug("Protecting TRADE GOOD:"+coord2);
//		for (int i = 0; i < 3; i++) {
//			BlockCoord bcoord = new BlockCoord(coord2);
//
//            ProtectedBlock pb = new ProtectedBlock(bcoord, ProtectedBlock.Type.TRADE_MARKER);
//            CivGlobal.addProtectedBlock(pb);
//
//            bcoord.setY(bcoord.getY()+1);
//		}
    }

    @Override
    public void save() {
        SQLUpdate.add(this);
    }

    @Override
    public void saveNow() throws SQLException {
        HashMap<String, Object> hashmap = new HashMap<String, Object>();

        hashmap.put("name", this.getName());
        if (this.getTown() != null) {
            hashmap.put("town_id", this.getTown().getId());
        } else {
            hashmap.put("town_id", null);
        }
        if (this.bonusLocation != null) {
            hashmap.put("bonusLocation", this.bonusLocation.toString());
        } else {
            hashmap.put("bonusLocation", null);
        }
        hashmap.put("coord", this.coord.toString());
        if (this.getStruct() == null) {
            hashmap.put("structure_id", null);
        } else {
            hashmap.put("structure_id", this.getStruct().getId());

        }

        SQL.updateNamedObject(this, hashmap, TABLE_NAME);
    }

    @Override
    public void delete() throws SQLException {
    }

    public Town getTown() {
        return town;
    }

    public void setTown(Town town) {
        this.town = town;
    }

    public Civilization getCiv() {
        return civ;
    }

    public void setCiv(Civilization civ) {
        this.civ = civ;
    }

    public ConfigTradeGood getInfo() {
        return info;
    }

    public void setInfo(ConfigTradeGood info) {
        this.info = info;
    }

    public BlockCoord getCoord() {
        return coord;
    }

    public void setCoord(BlockCoord coord) {
        this.coord = coord;
    }

    public Structure getStruct() {
        return struct;
    }


    public void setStruct(Structure struct) {
        this.struct = struct;
    }


}
