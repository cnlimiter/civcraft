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

import cn.evole.plugins.civcraft.camp.Camp;
import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.config.ConfigTownLevel;
import cn.evole.plugins.civcraft.database.SQL;
import cn.evole.plugins.civcraft.database.SQLUpdate;
import cn.evole.plugins.civcraft.exception.AlreadyRegisteredException;
import cn.evole.plugins.civcraft.exception.CivException;
import cn.evole.plugins.civcraft.exception.InvalidConfiguration;
import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.main.CivLog;
import cn.evole.plugins.civcraft.main.CivMessage;
import cn.evole.plugins.civcraft.permission.PermissionGroup;
import cn.evole.plugins.civcraft.permission.PlotPermissions;
import cn.evole.plugins.civcraft.util.ChunkCoord;
import cn.evole.plugins.civcraft.util.CivColor;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.HashMap;

public class TownChunk extends SQLObject {

    public static final String TABLE_NAME = "TOWNCHUNKS";
    public PlotPermissions perms = new PlotPermissions();
    private ChunkCoord chunkLocation;
    private Town town;
    private boolean forSale;
    /*
     * Price vs value, price is what the owner is currently selling it for,
     * value is the amount that it was last purchased at, used for taxes.
     */
    private double value;
    private double price;
    private boolean outpost;
    private boolean canUnclaim = true;

    public TownChunk(ResultSet rs) throws SQLException, CivException {
        this.load(rs);
    }

    public TownChunk(Town newTown, Location location) {
        ChunkCoord coord = new ChunkCoord(location);
        setTown(newTown);
        setChunkCord(coord);
        perms.addGroup(newTown.getDefaultGroup());
    }

    public TownChunk(Town newTown, ChunkCoord chunkLocation) {
        setTown(newTown);
        setChunkCord(chunkLocation);
        perms.addGroup(newTown.getDefaultGroup());
    }

    public static void init() throws SQLException {
        if (!SQL.hasTable(TABLE_NAME)) {
            String table_create = "CREATE TABLE " + SQL.tb_prefix + TABLE_NAME + " (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                    "`town_id` int(11) NOT NULL," +
                    "`world` VARCHAR(32) NOT NULL," +
                    "`x` bigint(20) NOT NULL," +
                    "`z` bigint(20) NOT NULL," +
                    "`owner_id` int(11) DEFAULT NULL," +
                    "`groups` mediumtext DEFAULT NULL," +
                    "`permissions` mediumtext NOT NULL," +
                    "`for_sale` boolean NOT NULL DEFAULT false," +
                    "`value` float NOT NULL DEFAULT '0'," +
                    "`price` float NOT NULL DEFAULT '0'," +
                    "`canunclaim` boolean DEFAULT false," +
                    "`outpost` boolean DEFAULT false" +
                    //	 "FOREIGN KEY (owner_id) REFERENCES "+SQL.tb_prefix+Resident.TABLE_NAME+"(id),"+
                    //	 "FOREIGN KEY (town_id) REFERENCES "+SQL.tb_prefix+Town.TABLE_NAME+"(id),"+
                    ")";

            SQL.makeTable(table_create);
            CivLog.info("Created " + TABLE_NAME + " table");
        } else {
            CivLog.info(TABLE_NAME + " table OK!");
        }
    }

    public static double getNextPlotCost(Town town) {

        ConfigTownLevel effectiveTownLevel = CivSettings.townLevels.get(CivSettings.townLevels.size());
        int currentPlotCount = town.getTownChunks().size();

        for (ConfigTownLevel lvl : CivSettings.townLevels.values()) {
            if (currentPlotCount < lvl.plots) {
                if (effectiveTownLevel.plots > lvl.plots) {
                    effectiveTownLevel = lvl;
                }
            }
        }


        return effectiveTownLevel.plot_cost;
    }

    public static TownChunk claim(Town town, ChunkCoord coord, boolean outpost) throws CivException {
        if (CivGlobal.getTownChunk(coord) != null) {
            throw new CivException(CivSettings.localize.localizedString("town_chunk_errorClaimed"));
        }

        double cost;
        cost = getNextPlotCost(town);

        if (!town.hasEnough(cost)) {
            throw new CivException(CivSettings.localize.localizedString("var_town_chunk_claimTooPoor", cost, CivSettings.CURRENCY_NAME));
        }

        CultureChunk cultureChunk = CivGlobal.getCultureChunk(coord);
        if (cultureChunk == null || cultureChunk.getCiv() != town.getCiv()) {
            throw new CivException(CivSettings.localize.localizedString("town_chunk_claimOutsideCulture"));
        }
        if (cultureChunk.getTown() != town) {
            throw new CivException(CivSettings.localize.localizedString("town_chunk_notOwnCultureChunk"));
        }

        TownChunk tc = new TownChunk(town, coord);
        tc.setCanUnclaim(true);

        if (!outpost) {
            if (!tc.isOnEdgeOfOwnership()) {
                throw new CivException(CivSettings.localize.localizedString("town_chunk_claimTooFar"));
            }

            if (!town.canClaim()) {
                throw new CivException(CivSettings.localize.localizedString("town_chunk_claimTooMany"));
            }
        }

        //Test that we are not too close to another civ
        try {
            int min_distance = CivSettings.getInteger(CivSettings.civConfig, "civ.min_distance");

            for (TownChunk cc : CivGlobal.getTownChunks()) {
                if (cc.getCiv() != town.getCiv()) {
                    double dist = coord.distance(cc.getChunkCoord());
                    if (dist <= min_distance) {
                        DecimalFormat df = new DecimalFormat();
                        throw new CivException(CivSettings.localize.localizedString("var_town_chunk_claimTooClose", cc.getCiv().getName(), df.format(dist), min_distance));
                    }
                }
            }
        } catch (InvalidConfiguration e1) {
            e1.printStackTrace();
            throw new CivException(CivSettings.localize.localizedString("internalException"));
        }

        //Test that we are not too far protruding from our own town chunks
//		try {
//			int max_protrude = CivSettings.getInteger(CivSettings.townConfig, "town.max_town_chunk_protrude");
//			if (max_protrude != 0) {
//				if (isTownChunkProtruding(tc, 0, max_protrude, new HashSet<ChunkCoord>())) {
//					throw new CivException("You cannot claim here, too far away from the rest of your town chunks.");
//				}
//			}
//		} catch (InvalidConfiguration e1) {
//			e1.printStackTrace();
//			throw new CivException("Internal configuration exception.");
//		}

        if (!outpost) {
            try {
                town.addTownChunk(tc);
            } catch (AlreadyRegisteredException e1) {
                e1.printStackTrace();
                throw new CivException(CivSettings.localize.localizedString("internalCommandException"));

            }
        } else {
            try {
                town.addOutpostChunk(tc);
            } catch (AlreadyRegisteredException e) {
                e.printStackTrace();
                throw new CivException(CivSettings.localize.localizedString("internalCommandException"));
            }
        }

        Camp camp = CivGlobal.getCampChunk(coord);
        if (camp != null) {
            CivMessage.sendCamp(camp, CivColor.Yellow + ChatColor.BOLD + CivSettings.localize.localizedString("var_town_chunk_dibandCamp", town.getName()));
            camp.disband();
        }

        tc.setOutpost(outpost);
        tc.save();
        town.withdraw(cost);
        CivGlobal.addTownChunk(tc);
        CivGlobal.processCulture();
        return tc;
    }

    public static TownChunk claim(Town town, Player player, boolean outpost) throws CivException {
        double cost = getNextPlotCost(town);
        TownChunk tc = claim(town, new ChunkCoord(player.getLocation()), outpost);
        CivMessage.sendSuccess(player, CivSettings.localize.localizedString("var_town_chunk_success", tc.getChunkCoord(), CivColor.Yellow + cost + CivColor.LightGreen, CivSettings.CURRENCY_NAME));
        return tc;
    }

    /*
     * XXX This claim is only called when a town hall is building and needs to be claimed.
     * We do not save here since its going to be saved in-order using the SQL save in order
     * task. Also certain types of validation and cost cacluation are skipped.
     */
    public static TownChunk townHallClaim(Town town, ChunkCoord coord) throws CivException {
        //This is only called when the town hall is built and needs to be claimed.

        if (CivGlobal.getTownChunk(coord) != null) {
            throw new CivException(CivSettings.localize.localizedString("town_chunk_errorClaimed"));
        }

        TownChunk tc = new TownChunk(town, coord);

        tc.setCanUnclaim(false);

        try {
            town.addTownChunk(tc);
        } catch (AlreadyRegisteredException e1) {
            e1.printStackTrace();
            throw new CivException(CivSettings.localize.localizedString("internalCommandException"));

        }

        Camp camp = CivGlobal.getCampChunk(coord);
        if (camp != null) {
            CivMessage.sendCamp(camp, CivColor.Yellow + ChatColor.BOLD + CivSettings.localize.localizedString("var_town_chunk_dibandCamp", town.getName()));
            camp.disband();
        }

        CivGlobal.addTownChunk(tc);
        tc.save();
        return tc;
    }

    public static void unclaim(TownChunk tc) throws CivException {

        //TODO check that its not the last chunk
        //TODO make sure that its not owned by someone else.


        tc.getTown().removeTownChunk(tc);
        try {
            tc.delete();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new CivException(CivSettings.localize.localizedString("internalDatabaseException"));
        }

    }

    @Override
    public void load(ResultSet rs) throws SQLException, CivException {
        this.setId(rs.getInt("id"));
        this.setTown(CivGlobal.getTownFromId(rs.getInt("town_id")));
        if (this.getTown() == null) {
            CivLog.warning("TownChunk tried to load without a town...");
            if (CivGlobal.testFileFlag("cleanupDatabase")) {
                CivLog.info("CLEANING");
                this.delete();
            }
            throw new CivException("No town(" + rs.getInt("town_id") + ") to load this town chunk(" + rs.getInt("id") + ")");
        }

        ChunkCoord cord = new ChunkCoord(rs.getString("world"), rs.getInt("x"), rs.getInt("z"));
        this.setChunkCord(cord);

        try {
            this.perms.loadFromSaveString(town, rs.getString("permissions"));
        } catch (CivException e) {
            e.printStackTrace();
        }

        this.perms.setOwner(CivGlobal.getResidentFromId(rs.getInt("owner_id")));
        //this.perms.setGroup(CivGlobal.getPermissionGroup(this.getTown(), rs.getInt("groups")));
        String grpString = rs.getString("groups");
        if (grpString != null) {
            String[] groups = grpString.split(":");
            for (String grp : groups) {
                this.perms.addGroup(CivGlobal.getPermissionGroup(this.getTown(), Integer.valueOf(grp)));
            }
        }

        this.forSale = rs.getBoolean("for_sale");
        this.value = rs.getDouble("value");
        this.price = rs.getDouble("price");
        this.outpost = rs.getBoolean("outpost");
        this.setCanUnclaim(rs.getBoolean("canunclaim"));

        if (!this.outpost) {
            try {
                this.getTown().addTownChunk(this);
            } catch (AlreadyRegisteredException e1) {
                e1.printStackTrace();
            }
        } else {
            try {
                this.getTown().addOutpostChunk(this);
            } catch (AlreadyRegisteredException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void save() {
        SQLUpdate.add(this);
    }

    @Override
    public void saveNow() throws SQLException {
        HashMap<String, Object> hashmap = new HashMap<String, Object>();

        hashmap.put("id", this.getId());
        hashmap.put("town_id", this.getTown().getId());
        hashmap.put("world", this.getChunkCoord().getWorldname());
        hashmap.put("x", this.getChunkCoord().getX());
        hashmap.put("z", this.getChunkCoord().getZ());
        hashmap.put("permissions", perms.getSaveString());
        hashmap.put("for_sale", this.isForSale());
        hashmap.put("value", this.getValue());
        hashmap.put("price", this.getPrice());
        hashmap.put("outpost", this.outpost);

        if (this.perms.getOwner() != null) {
            hashmap.put("owner_id", this.perms.getOwner().getId());
        } else {
            hashmap.put("owner_id", null);
        }

        if (!this.perms.getGroups().isEmpty()) {
            String out = "";
            for (PermissionGroup grp : this.perms.getGroups()) {
                out += grp.getId() + ":";
            }
            hashmap.put("groups", out);
        } else {
            hashmap.put("groups", null);
        }

        SQL.updateNamedObject(this, hashmap, TABLE_NAME);
    }

    public Town getTown() {
        return town;
    }

    public void setTown(Town town) {
        this.town = town;
    }


    /* Returns true if this townchunk is outside our protrude limits. */
//	private static boolean isTownChunkProtruding(TownChunk start, int protrude_count, int max_protrude, 
//			HashSet<ChunkCoord> closedList) {
//		
//		if (protrude_count > max_protrude) {
//			return true;
//		}
//		
//		int[][] offset = { { -1, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 } };
//		ChunkCoord coord = new ChunkCoord(start.getChunkCoord().getWorldname(), 
//				start.getChunkCoord().getX(), start.getChunkCoord().getZ());
//		closedList.add(coord);
//		
//		TownChunk nextChunk = null;
//		for (int i = 0; i < 4; i++) {
//			coord.setX(coord.getX() + offset[i][0]);
//			coord.setZ(coord.getZ() + offset[i][1]);
//			
//			if (closedList.contains(coord)) {
//				continue;
//			}
//			
//			TownChunk tc = CivGlobal.getTownChunk(coord);
//			if (tc == null) {
//				continue;
//			}
//			
//			if (nextChunk == null) {
//				nextChunk = tc;
//			} else {
//				/* We found another chunk next to us, this chunk doesnt protrude. */
//				return false;
//			}
//		}
//		
//		if (nextChunk == null) {
//			/* We found no chunk next to us at all.. shouldn't happen but this chunk doesnt protrude. */
//			return false;
//		}
//		
//		return isTownChunkProtruding(nextChunk, protrude_count + 1, max_protrude, closedList);
//	}

    public ChunkCoord getChunkCoord() {
        return chunkLocation;
    }

    public void setChunkCord(ChunkCoord chunkLocation) {
        this.chunkLocation = chunkLocation;
    }

    private Civilization getCiv() {
        return this.getTown().getCiv();
    }

    private boolean isOnEdgeOfOwnership() {

        int[][] offset = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

        for (int i = 0; i < 4; i++) {
            TownChunk tc = CivGlobal.getTownChunk(new ChunkCoord(this.getChunkCoord().getWorldname(),
                    this.getChunkCoord().getX() + offset[i][0],
                    this.getChunkCoord().getZ() + offset[i][1]));
            if (tc != null &&
                    tc.getTown() == this.getTown() &&
                    !tc.isOutpost()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void delete() throws SQLException {
        SQL.deleteNamedObject(this, TABLE_NAME);
        CivGlobal.removeTownChunk(this);
    }

    public boolean isForSale() {
        return forSale;
    }

    public void setForSale(boolean forSale) {
        this.forSale = forSale;
    }

    public double getValue() {
        return value;
    }

    /* Called when a player enters this plot. */
    public String getOnEnterString(Player player, TownChunk fromTc) {
        String out = "";

        if (this.perms.getOwner() != null) {
            out += CivColor.LightGray + "[" + CivSettings.localize.localizedString("town_chunk_status_owned") + " " + CivColor.LightGreen + this.perms.getOwner().getName() + CivColor.LightGray + "]";
        }

        if (this.perms.getOwner() == null && fromTc != null && fromTc.perms.getOwner() != null) {
            out += CivColor.LightGray + "[" + CivSettings.localize.localizedString("town_chunk_status_unowned") + "]";
        }

        if (this.isForSale()) {
            out += CivColor.Yellow + "[" + CivSettings.localize.localizedString("town_chunk_status_forSale") + " " + this.price + " " + CivSettings.CURRENCY_NAME + "]";
        }

        return out;
    }

    public void purchase(Resident resident) throws CivException {

        if (!resident.getTreasury().hasEnough(this.price)) {
            throw new CivException(CivSettings.localize.localizedString("var_town_chunk_purchase_tooPoor", this.price, CivSettings.CURRENCY_NAME));
        }

        if (this.perms.getOwner() == null) {
            resident.getTreasury().payTo(this.getTown().getTreasury(), this.price);
        } else {
            resident.getTreasury().payTo(this.perms.getOwner().getTreasury(), this.price);
        }

        this.value = this.price;
        this.price = 0;
        this.forSale = false;
        this.perms.setOwner(resident);
        this.perms.clearGroups();

        this.save();
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getCenterString() {
        /*
         * Since the chunk is the floor of the block coords divided by 16.
         * The middle of the chunk is 8 more from there....
         */
        //int blockx = (this.chunkLocation.getX()*16)+8;
        //int blockz = (this.chunkLocation.getZ()*16)+8;
        //TODO work out the bugs with this.

        return this.chunkLocation.toString();
    }

    public boolean isEdgeBlock() {

        int[][] offset = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

        if (this.isOutpost()) {
            return false;
        }

        for (int i = 0; i < 4; i++) {
            TownChunk next = CivGlobal.getTownChunk(new ChunkCoord(this.chunkLocation.getWorldname(),
                    this.chunkLocation.getX() + offset[i][0],
                    this.chunkLocation.getZ() + offset[i][1]));
            if (next == null || next.isOutpost()) {
                return true;
            }
        }

        return false;
    }

    public boolean isOutpost() {
        return outpost;
    }

    public void setOutpost(boolean outpost) {
        this.outpost = outpost;
    }

    public boolean getCanUnclaim() {
        return canUnclaim;
    }

    public void setCanUnclaim(boolean canUnclaim) {
        this.canUnclaim = canUnclaim;
    }


}
