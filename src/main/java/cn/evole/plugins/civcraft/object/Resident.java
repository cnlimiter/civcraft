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
import cn.evole.plugins.civcraft.config.*;
import cn.evole.plugins.civcraft.database.SQL;
import cn.evole.plugins.civcraft.database.SQLUpdate;
import cn.evole.plugins.civcraft.event.EventTimer;
import cn.evole.plugins.civcraft.exception.AlreadyRegisteredException;
import cn.evole.plugins.civcraft.exception.CivException;
import cn.evole.plugins.civcraft.exception.InvalidConfiguration;
import cn.evole.plugins.civcraft.exception.InvalidNameException;
import cn.evole.plugins.civcraft.interactive.InteractiveResponse;
import cn.evole.plugins.civcraft.items.units.Unit;
import cn.evole.plugins.civcraft.loregui.OpenInventoryTask;
import cn.evole.plugins.civcraft.lorestorage.LoreCraftableMaterial;
import cn.evole.plugins.civcraft.lorestorage.LoreGuiItem;
import cn.evole.plugins.civcraft.lorestorage.LoreGuiItemListener;
import cn.evole.plugins.civcraft.main.*;
import cn.evole.plugins.civcraft.permission.PermissionGroup;
import cn.evole.plugins.civcraft.road.RoadBlock;
import cn.evole.plugins.civcraft.sessiondb.SessionEntry;
import cn.evole.plugins.civcraft.structure.*;
import cn.evole.plugins.civcraft.template.Template;
import cn.evole.plugins.civcraft.threading.TaskMaster;
import cn.evole.plugins.civcraft.threading.tasks.BuildPreviewAsyncTask;
import cn.evole.plugins.civcraft.tutorial.Book;
import cn.evole.plugins.civcraft.util.*;
import cn.evole.plugins.global.perks.NotVerifiedException;
import cn.evole.plugins.global.perks.Perk;
import cn.evole.plugins.global.perks.components.CustomPersonalTemplate;
import cn.evole.plugins.global.perks.components.CustomTemplate;
import gpl.InventorySerializer;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Resident extends SQLObject {

    public static final String TABLE_NAME = "RESIDENTS";
    public static HashSet<String> allchatters = new HashSet<String>();
    public static int MAX_SPY_EXPOSURE = 1000;
    public static int POISON_DURATION = 5;
    public static int LEVITATE_DURATION = 3;
    public String desiredCivName;
    public String desiredCapitolName;
    public String desiredTownName;
    public Location desiredTownLocation = null;
    public Template desiredTemplate = null;
    public boolean allchat = false;
    /* XXX
     * This buildable is used as place to store which buildable we're working on when interacting
     * with GUI items. We want to be able to pass the buildable object to the GUI's action function,
     * but there isn't a good way to do this ATM. If we had a way to send arbitary objects it would
     * be better. Could we store it here on the resident object?
     */
    public Buildable pendingBuildable;
    public ConfigBuildableInfo pendingBuildableInfo;
    public CallbackInterface pendingCallback;
    public ConcurrentHashMap<BlockCoord, SimpleBlock> previewUndo = null;
    public LinkedHashMap<String, Perk> perks = new LinkedHashMap<String, Perk>();
    public String debugTown;
    public boolean isTeleporting = false;
    private Town town = null;
    private Camp camp = null;
    private boolean townChat = false;
    private boolean civChat = false;
    private boolean adminChat = false;
    private boolean combatInfo = true;
    private boolean titleAPI = true;
    private int languageCode = 1033;
    /* Town or civ to chat in besides your own. */
    private Town townChatOverride = null;
    private Civilization civChatOverride = null;
    private boolean permOverride = false;
    private boolean sbperm = false;
    private boolean controlBlockInstantBreak = false;
    private int townID = 0;
    private int campID = 0;
    private boolean dontSaveTown = false;
    private String timezone;
    private boolean banned = false;
    private long registered;
    private long lastOnline;
    private int daysTilEvict;
    private boolean givenKit;
    private final ConcurrentHashMap<String, Integer> friends = new ConcurrentHashMap<String, Integer>();
    private EconObject treasury;
    private boolean muted;
    private Date muteExpires = null;
    private boolean interactiveMode = false;
    private InteractiveResponse interactiveResponse = null;
    private BuildPreviewAsyncTask previewTask = null;
    private double spyExposure = 0.0;
    private boolean performingMission = false;
    private Town selectedTown = null;
    private boolean showScout = true;
    private boolean showTown = true;
    private boolean showCiv = true;
    private boolean showMap = false;
    private boolean showInfo = false;
    private String itemMode = "all";
    private String savedInventory = null;
    private boolean isProtected = false;
    private Date lastKilledTime = null;
    private String lastIP = "";
    private UUID uid;
    private double walkingModifier = CivSettings.normal_speed;
    private boolean onRoad = false;
    private long nextTeleport;
    private long poisonImmune = 0L;
    private long levitateImmune = 0L;
    private long nextPLCDamage = 0L;
    private String reportResult;
    private boolean reportChecked;
    private String desiredReportPlayerName;

    public Resident(UUID uid, String name) throws InvalidNameException {
        this.setName(name);
        this.uid = uid;
        this.treasury = CivGlobal.createEconObject(this);
        setTimezoneToServerDefault();
        loadSettings();
    }

    public Resident(ResultSet rs) throws SQLException, InvalidNameException {
        this.load(rs);
        loadSettings();
    }

    public static void init() throws SQLException {
        if (!SQL.hasTable(TABLE_NAME)) {
            String table_create = "CREATE TABLE " + SQL.tb_prefix + TABLE_NAME + " (" +
                    "`id` int(11) unsigned NOT NULL auto_increment," +
                    "`name` VARCHAR(64) NOT NULL," +
                    "`uuid` VARCHAR(256) NOT NULL DEFAULT 'UNKNOWN'," +
                    "`currentName` VARCHAR(64) DEFAULT NULL," +
                    "`town_id` int(11)," +
                    "`lastOnline` BIGINT NOT NULL," +
                    "`registered` BIGINT NOT NULL," +
                    "`friends` mediumtext," +
                    "`debt` double DEFAULT 0," +
                    "`coins` double DEFAULT 0," +
                    "`daysTilEvict` mediumint DEFAULT NULL," +
                    "`givenKit` bool NOT NULL DEFAULT '0'," +
                    "`camp_id` int(11)," +
                    "`timezone` mediumtext," +
                    "`banned` bool NOT NULL DEFAULT '0'," +
                    "`bannedMessage` mediumtext DEFAULT NULL," +
                    "`savedInventory` mediumtext DEFAULT NULL," +
                    "`isProtected` bool NOT NULL DEFAULT '0'," +
                    "`flags` mediumtext DEFAULT NULL," +
                    "`last_ip` mediumtext DEFAULT NULL," +
                    "`debug_town` mediumtext DEFAULT NULL," +
                    "`debug_civ` mediumtext DEFAULT NuLL," +
                    "`language_id` int(11) DEFAULT '1033'," +
                    "`nextTeleport` BIGINT NOT NULL DEFAULT '0'," +
                    "`reportResult` mediumtext," +
                    "`reportChecked` boolean DEFAULT false," +
                    "UNIQUE KEY (`name`), " +
                    "PRIMARY KEY (`id`)" + ")";

            SQL.makeTable(table_create);
            CivLog.info("Created " + TABLE_NAME + " table");
        } else {
            CivLog.info(TABLE_NAME + " table OK!");

            if (!SQL.hasColumn(TABLE_NAME, "uuid")) {
                CivLog.info("\tCouldn't find `uuid` for resident.");
                SQL.addColumn(TABLE_NAME, "`uuid` VARCHAR(256) NOT NULL DEFAULT 'UNKNOWN'");
            }

            if (!SQL.hasColumn(TABLE_NAME, "currentName")) {
                CivLog.info("\tCouldn't find `currentName` for resident.");
                SQL.addColumn(TABLE_NAME, "`currentName` VARCHAR(64) DEFAULT NULL");
            }

            if (!SQL.hasColumn(TABLE_NAME, "banned")) {
                CivLog.info("\tCouldn't find `banned` for resident.");
                SQL.addColumn(TABLE_NAME, "`banned` bool default 0");
            }

            if (!SQL.hasColumn(TABLE_NAME, "bannedMessage")) {
                CivLog.info("\tCouldn't find `bannedMessage` for resident.");
                SQL.addColumn(TABLE_NAME, "`bannedMessage` mediumtext default null");
            }

            if (!SQL.hasColumn(TABLE_NAME, "last_ip")) {
                CivLog.info("\tCouldn't find `last_ip` for resident.");
                SQL.addColumn(TABLE_NAME, "`last_ip` mediumtext default null");
            }

            if (!SQL.hasColumn(TABLE_NAME, "camp_id")) {
                CivLog.info("\tCouldn't find `camp_id` for resident.");
                SQL.addColumn(TABLE_NAME, "`camp_id` int(11) default 0");
            }

            if (!SQL.hasColumn(TABLE_NAME, "timezone")) {
                CivLog.info("\tCouldn't find `timezone` for resident.");
                SQL.addColumn(TABLE_NAME, "`timezone` mediumtext default null");
            }

            if (!SQL.hasColumn(TABLE_NAME, "debug_civ")) {
                CivLog.info("\tCouldn't find `debug_civ` for resident.");
                SQL.addColumn(TABLE_NAME, "`debug_civ` mediumtext default null");
            }

            if (!SQL.hasColumn(TABLE_NAME, "debug_town")) {
                CivLog.info("\tCouldn't find `debug_town` for resident.");
                SQL.addColumn(TABLE_NAME, "`debug_town` mediumtext default null");
            }

            if (!SQL.hasColumn(TABLE_NAME, "language_id")) {
                CivLog.info("\tCouldn't find `language_id` for resident.");
                SQL.addColumn(TABLE_NAME, "`language_id` int(11) default '1033'");
            }

            if (!SQL.hasColumn(TABLE_NAME, "flags")) {
                CivLog.info("\tCouldn't find `flags` for resident.");
                SQL.makeCol("flags", "mediumtext", TABLE_NAME);
            }

            if (!SQL.hasColumn(TABLE_NAME, "savedInventory")) {
                CivLog.info("\tCouldn't find `savedInventory` for resident.");
                SQL.makeCol("savedInventory", "mediumtext", TABLE_NAME);
            }

            if (!SQL.hasColumn(TABLE_NAME, "isProtected")) {
                CivLog.info("\tCouldn't find `isProtected` for resident.");
                SQL.makeCol("isProtected", "bool NOT NULL DEFAULT '0'", TABLE_NAME);
            }

            if (!SQL.hasColumn(TABLE_NAME, "reportResult")) {
                CivLog.info("\tCouldn't find `reportResult` for resident.");
                SQL.makeCol("reportResult", "mediumtext", TABLE_NAME);
            }

            if (!SQL.hasColumn(TABLE_NAME, "reportChecked")) {
                CivLog.info("\tCouldn't find `reportChecked` for resident.");
                SQL.makeCol("reportChecked", "boolean DEFAULT false", TABLE_NAME);
            }
        }
    }

    public void loadSettings() {
        this.spyExposure = 0.0;
    }

    @Override
    public void load(ResultSet rs) throws SQLException, InvalidNameException {
        this.setId(rs.getInt("id"));
        this.setName(rs.getString("name"));
        this.townID = rs.getInt("town_id");
        this.campID = rs.getInt("camp_id");
        this.lastIP = rs.getString("last_ip");
        this.debugTown = rs.getString("debug_town");

        if (rs.getString("uuid").equalsIgnoreCase("UNKNOWN")) {
            this.uid = null;
        } else {
            this.uid = UUID.fromString(rs.getString("uuid"));
        }

        this.treasury = CivGlobal.createEconObject(this);
        this.getTreasury().setBalance(rs.getDouble("coins"), false);
        this.setGivenKit(rs.getBoolean("givenKit"));
        this.setTimezone(rs.getString("timezone"));
        this.loadFlagSaveString(rs.getString("flags"));
        this.savedInventory = rs.getString("savedInventory");
        this.isProtected = rs.getBoolean("isProtected");
        this.languageCode = rs.getInt("language_id");
        this.nextTeleport = rs.getLong("nextTeleport");
        this.reportResult = rs.getString("reportResult");
        this.reportChecked = rs.getBoolean("reportChecked");

        if (this.getTimezone() == null) {
            this.setTimezoneToServerDefault();
        }

        if (this.townID != 0) {
            this.setTown(CivGlobal.getTownFromId(this.townID));
            setSelectedTown(town);
            if (this.town == null) {
                CivLog.error("COULD NOT FIND TOWN(" + this.townID + ") FOR RESIDENT(" + this.getId() + ") Name:" + this.getName());
                /*
                 * When a town fails to load, we wont be able to find it above.
                 * However this can cause a cascade effect where because we couldn't find
                 * the town above, we save this resident's town as NULL which wipes
                 * their town information from the database when the resident gets saved.
                 * Just to make sure this doesn't happen the boolean below guards resident saves.
                 * There ought to be a better way...
                 */
                if (CivGlobal.testFileFlag("cleanupDatabase")) {
                    this.saveNow();
                } else {
                    this.dontSaveTown = true;
                }
                return;
            }
        }

        if (this.campID != 0) {
            this.setCamp(CivGlobal.getCampFromId(this.campID));
            if (this.camp == null) {
                CivLog.error("COULD NOT FIND CAMP(" + this.campID + ") FOR RESIDENT(" + this.getId() + ") Name:" + this.getName());
            } else {
                camp.addMember(this);
            }
        }

        if (this.getTown() != null) {
            try {
                this.getTown().addResident(this);
            } catch (AlreadyRegisteredException e) {
                e.printStackTrace();
            }
        }

        this.setLastOnline(rs.getLong("lastOnline"));
        this.setRegistered(rs.getLong("registered"));
        this.setDaysTilEvict(rs.getInt("daysTilEvict"));
        this.getTreasury().setDebt(rs.getDouble("debt"));
        this.loadFriendsFromSaveString(rs.getString("friends"));

    }

    private void setTimezoneToServerDefault() {
        this.timezone = EventTimer.getCalendarInServerTimeZone().getTimeZone().getID();
    }

    public String getFlagSaveString() {
        String flagString = "";

        if (this.isShowMap()) {
            flagString += "map,";
        }

        if (this.isShowTown()) {
            flagString += "showtown,";
        }

        if (this.isShowCiv()) {
            flagString += "showciv,";
        }

        if (this.isShowScout()) {
            flagString += "showscout,";
        }

        if (this.isShowInfo()) {
            flagString += "info,";
        }

        if (this.combatInfo) {
            flagString += "combatinfo,";
        }
        if (this.isTitleAPI()) {
            flagString += "titleapi,";
        }

        if (this.itemMode.equals("rare")) {
            flagString += "itemModeRare,";
        } else if (this.itemMode.equals("none")) {
            flagString += "itemModeNone,";
        }

        return flagString;
    }

    public void loadFlagSaveString(String str) {
        if (str == null) {
            return;
        }

        String[] split = str.split(",");

        for (String s : split) {
            switch (s.toLowerCase()) {
                case "map":
                    this.setShowMap(true);
                    break;
                case "showtown":
                    this.setShowTown(true);
                    break;
                case "showciv":
                    this.setShowCiv(true);
                    break;
                case "showscout":
                    this.setShowScout(true);
                    break;
                case "info":
                    this.setShowInfo(true);
                    break;
                case "combatinfo":
                    this.setCombatInfo(true);
                    break;
                case "titleapi":
                    if (CivSettings.hasTitleAPI) {
                        this.setTitleAPI(true);
                    } else {
                        this.setTitleAPI(false);
                    }
                    break;
                case "itemmoderare":
                    this.itemMode = "rare";
                    break;
                case "itemmodenone":
                    this.itemMode = "none";
                    break;
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

        hashmap.put("name", this.getName());
        hashmap.put("uuid", this.getUUIDString());
        if (this.getTown() != null) {
            hashmap.put("town_id", this.getTown().getId());
        } else {
            if (!dontSaveTown) {
                hashmap.put("town_id", null);
            }
        }

        if (this.getCamp() != null) {
            hashmap.put("camp_id", this.getCamp().getId());
        } else {
            hashmap.put("camp_id", null);
        }

        hashmap.put("lastOnline", this.getLastOnline());
        hashmap.put("registered", this.getRegistered());
        hashmap.put("debt", this.getTreasury().getDebt());
        hashmap.put("daysTilEvict", this.getDaysTilEvict());
        hashmap.put("friends", this.getFriendsSaveString());
        hashmap.put("givenKit", this.isGivenKit());
        hashmap.put("coins", this.getTreasury().getBalance());
        hashmap.put("timezone", this.getTimezone());
        hashmap.put("flags", this.getFlagSaveString());
        hashmap.put("last_ip", this.getLastIP());
        hashmap.put("savedInventory", this.savedInventory);
        hashmap.put("isProtected", this.isProtected);
        hashmap.put("language_id", this.languageCode);
        hashmap.put("nextTeleport", this.nextTeleport);

        if (this.getTown() != null) {
            hashmap.put("debug_town", this.getTown().getName());

            if (this.getTown().getCiv() != null) {
                hashmap.put("debug_civ", this.getCiv().getName());
            }
        }

        SQL.updateNamedObject(this, hashmap, TABLE_NAME);
    }

    public String getTownString() {
        if (town == null) {
            return "none";
        }
        return this.getTown().getName();
    }

    public Town getTown() {
        return town;
    }

    public void setTown(Town town) {
        this.town = town;
    }

    public boolean hasTown() {
        return town != null;
    }

    public long getRegistered() {
        return registered;
    }

    public void setRegistered(long registered) {
        this.registered = registered;
    }

    public long getLastOnline() {
        return lastOnline;
    }

    public void setLastOnline(long lastOnline) {
        this.lastOnline = lastOnline;
    }

    @Override
    public void delete() throws SQLException {
        SQL.deleteByName(this.getName(), TABLE_NAME);
    }

    public EconObject getTreasury() {
        return treasury;
    }

    public void setTreasury(EconObject treasury) {
        this.treasury = treasury;
    }

    public void onEnterDebt() {
        this.daysTilEvict = CivSettings.GRACE_DAYS;
    }

    public void warnDebt() {
        Player player;
        try {
            player = CivGlobal.getPlayer(this);
            CivMessage.send(player, CivColor.Yellow + CivSettings.localize.localizedString("var_resident_debtmsg", this.getTreasury().getDebt(), CivSettings.CURRENCY_NAME));
            CivMessage.send(player, CivColor.LightGray + CivSettings.localize.localizedString("var_resident_debtEvictAlert1", this.daysTilEvict));
        } catch (CivException e) {
            //Player is not online.
        }
    }

    public int getDaysTilEvict() {
        return daysTilEvict;
    }

    public void setDaysTilEvict(int daysTilEvict) {
        this.daysTilEvict = daysTilEvict;
    }

    public void decrementGraceCounters() {
        this.daysTilEvict--;
        if (this.daysTilEvict == 0) {
            this.getTown().removeResident(this);

            try {
                CivMessage.send(CivGlobal.getPlayer(this), CivColor.Yellow + CivSettings.localize.localizedString("resident_evictedAlert"));
            } catch (CivException e) {
                // Resident not online.
            }
            return;
        }

        if (this.getTreasury().inDebt()) {
            warnDebt();
        } else {
            warnEvict();
        }

        this.save();
    }

    public double getPropertyTaxOwed() {
        double total = 0;

        if (this.getTown() == null) {
            return total;
        }

        for (TownChunk tc : this.getTown().getTownChunks()) {
            if (tc.perms.getOwner() == this) {
                double tax = tc.getValue() * this.getTown().getTaxRate();
                total += tax;
            }
        }
        return total;
    }

    public boolean isLandOwner() {
        if (this.getTown() == null)
            return false;

        for (TownChunk tc : this.getTown().getTownChunks()) {
            if (tc.perms.getOwner() == this) {
                return true;
            }
        }

        return false;
    }


    public double getFlatTaxOwed() {
        if (this.getTown() == null)
            return 0;

        return this.getTown().getFlatTax();
    }

    public boolean isTaxExempt() {
        return this.getTown().isInGroup("mayors", this) || this.getTown().isInGroup("assistants", this);
    }

    public void payOffDebt() {
        this.getTreasury().payTo(this.getTown().getTreasury(), this.getTreasury().getDebt());
        this.getTreasury().setDebt(0);
        this.daysTilEvict = -1;
        this.save();
    }

    public void addFriend(Resident resident) {
        friends.put(resident.getName(), 1);
    }

    public boolean isFriend(Resident resident) {
        return friends.containsKey(resident.getName());
    }

    public Collection<String> getFriends() {
        return friends.keySet();
    }

    private String getFriendsSaveString() {
        String out = "";
        for (String name : friends.keySet()) {
            out += name + ",";
        }
        return out;
    }

    private void loadFriendsFromSaveString(String string) {
        String[] split = string.split(",");

        for (String str : split) {
            friends.put(str, 1);
        }
    }

    public void removeFriend(Resident friendToAdd) {
        friends.remove(friendToAdd.getName());
    }

    public String getGroupsString() {
        String out = "";

        for (PermissionGroup grp : CivGlobal.getGroups()) {
            if (grp.hasMember(this)) {
                if (grp.getTown() != null) {
                    if (grp.isProtectedGroup()) {
                        out += CivColor.LightPurple;
                    } else {
                        out += CivColor.White;
                    }
                    out += grp.getName() + "(" + grp.getTown().getName() + ")";

                } else if (grp.getCiv() != null) {
                    out += CivColor.Gold + grp.getName() + "(" + grp.getCiv().getName() + ")";
                }

                out += ", ";
            }
        }

        return out;
    }

    public void warnEvict() {
        try {
            CivMessage.send(CivGlobal.getPlayer(this), CivColor.Yellow + CivSettings.localize.localizedString("var_resident_evictionNotice1", this.getDaysTilEvict()));
        } catch (CivException e) {
            //player offline.
        }
    }

    public boolean isTownChat() {
        return townChat;
    }

    public void setTownChat(boolean townChat) {
        this.townChat = townChat;
    }

    public boolean isCivChat() {
        return civChat;
    }

    public void setCivChat(boolean civChat) {
        this.civChat = civChat;
    }

    public boolean isAdminChat() {
        return adminChat;
    }

    public void setAdminChat(boolean adminChat) {
        this.adminChat = adminChat;
    }

    public Town getTownChatOverride() {
        return townChatOverride;
    }

    public void setTownChatOverride(Town townChatOverride) {
        this.townChatOverride = townChatOverride;
    }

    public Civilization getCivChatOverride() {
        return civChatOverride;
    }

    public void setCivChatOverride(Civilization civChatOverride) {
        this.civChatOverride = civChatOverride;
    }

    public boolean isPermOverride() {
        return permOverride;
    }

    public void setPermOverride(boolean permOverride) {
        this.permOverride = permOverride;
    }

    @SuppressWarnings("deprecation")
    public int takeItemsInHand(int itemId, int itemData) throws CivException {
        Player player = CivGlobal.getPlayer(this);
        Inventory inv = player.getInventory();
        if (!inv.contains(itemId)) {
            return 0;
        }

        if ((player.getInventory().getItemInMainHand().getTypeId() != itemId) &&
                (player.getInventory().getItemInMainHand().getTypeId() != itemData)) {
            return 0;
        }

        ItemStack stack = player.getInventory().getItemInMainHand();
        int count = stack.getAmount();
        inv.removeItem(stack);

        player.updateInventory();
        return count;
    }


    @SuppressWarnings("deprecation")
    public boolean takeItemInHand(int itemId, int itemData, int amount) throws CivException {
        Player player = CivGlobal.getPlayer(this);
        Inventory inv = player.getInventory();

        if (!inv.contains(itemId)) {
            return false;
        }

        if ((player.getInventory().getItemInMainHand().getTypeId() != itemId) &&
                (player.getInventory().getItemInMainHand().getTypeId() != itemData)) {
            return false;
        }

        ItemStack stack = player.getInventory().getItemInMainHand();

        if (stack.getAmount() < amount) {
            return false;
        } else if (stack.getAmount() == amount) {
            inv.removeItem(stack);
        } else {
            stack.setAmount(stack.getAmount() - amount);
        }

        player.updateInventory();
        return true;
    }

    @SuppressWarnings("deprecation")
    public boolean takeItem(int itemId, int itemData, int amount) throws CivException {
        Player player = CivGlobal.getPlayer(this);
        Inventory inv = player.getInventory();

        if (!inv.contains(itemId)) {
            return false;
        }

        HashMap<Integer, ? extends ItemStack> stacks;
        stacks = inv.all(itemId);

        for (ItemStack stack : stacks.values()) {
            if (stack.getData().getData() != (byte) itemData) {
                continue;
            }

            if (stack.getAmount() <= 0)
                continue;

            if (stack.getAmount() < amount) {
                amount -= stack.getAmount();
                stack.setAmount(0);
                inv.removeItem(stack);
                continue;
            } else {
                stack.setAmount(stack.getAmount() - amount);
                break;
            }
        }

        player.updateInventory();
        return true;
    }

    @SuppressWarnings("deprecation")
    public int giveItem(int itemId, short damage, int amount) throws CivException {
        Player player = CivGlobal.getPlayer(this);
        Inventory inv = player.getInventory();
        ItemStack stack = new ItemStack(itemId, amount, damage);
        HashMap<Integer, ItemStack> leftovers = null;
        leftovers = inv.addItem(stack);

        int leftoverAmount = 0;
        for (ItemStack i : leftovers.values()) {
            leftoverAmount += i.getAmount();
        }
        player.updateInventory();
        return amount - leftoverAmount;
    }

    public boolean buyItem(String itemName, int id, byte data, double price, int amount) throws CivException {

        if (!this.getTreasury().hasEnough(price)) {
            throw new CivException(CivSettings.localize.localizedString("resident_notEnoughMoney") + " " + CivSettings.CURRENCY_NAME);
        }

        boolean completed = true;
        int bought = 0;
        bought = giveItem(id, data, amount);
        if (bought != amount) {
            this.getTreasury().withdraw(price);
            takeItem(id, data, bought);
            completed = false;
        } else {
            this.getTreasury().withdraw(price);
        }

        if (completed) {
            return true;
        } else {
            throw new CivException(CivSettings.localize.localizedString("resident_buyInvenFull"));
        }
    }

    public Civilization getCiv() {
        if (this.getTown() == null) {
            return null;
        }
        return this.getTown().getCiv();
    }

    public boolean isGivenKit() {
        return givenKit;
    }

    public void setGivenKit(boolean givenKit) {
        this.givenKit = givenKit;
    }

    public boolean isSBPermOverride() {
        return sbperm;
    }

    public void setSBPermOverride(boolean b) {
        sbperm = b;
    }

    public void clearInteractiveMode() {
        this.interactiveMode = false;
        this.interactiveResponse = null;
    }

    public InteractiveResponse getInteractiveResponse() {
        return this.interactiveResponse;
    }

    public boolean isInteractiveMode() {
        return interactiveMode;
    }

    public void setInteractiveMode(InteractiveResponse interactive) {
        this.interactiveMode = true;
        this.interactiveResponse = interactive;
    }

    public Town getSelectedTown() {
        return selectedTown;
    }

    public void setSelectedTown(Town selectedTown) {
        this.selectedTown = selectedTown;
    }

    public Camp getCamp() {
        return camp;
    }

    public void setCamp(Camp camp) {
        this.camp = camp;
    }

    public boolean hasCamp() {
        return (this.camp != null);
    }

    public String getCampString() {
        if (this.camp == null) {
            return "none";
        }
        return this.camp.getName();
    }

    public void showWarnings(Player player) {
        /* Notify Resident of any invalid structures. */
        if (this.getTown() != null) {
            for (Buildable struct : this.getTown().invalidStructures) {
                CivMessage.send(player, CivColor.Yellow + ChatColor.BOLD +
                        CivSettings.localize.localizedString("var_resident_structInvalidAlert1", struct.getDisplayName(), struct.getCenterLocation()) +
                        " " + CivSettings.localize.localizedString("resident_structInvalidAlert2") + " " + struct.getInvalidReason());
            }

            /* Show any event messages. */
            if (this.getTown().getActiveEvent() != null) {
                CivMessage.send(player, CivColor.Yellow + CivSettings.localize.localizedString("var_resident_eventNotice1", this.getTown().getActiveEvent().configRandomEvent.name));
            }
        }


    }

    public boolean isShowScout() {
        return showScout;
    }

    public void setShowScout(boolean showScout) {
        this.showScout = showScout;
    }

    public boolean isShowTown() {
        return showTown;
    }

    public void setShowTown(boolean showTown) {
        this.showTown = showTown;
    }

    public boolean isShowCiv() {
        return showCiv;
    }

    public void setShowCiv(boolean showCiv) {
        this.showCiv = showCiv;
    }

    public boolean isShowMap() {
        return showMap;
    }

    public void setShowMap(boolean showMap) {
        this.showMap = showMap;
    }

    public void startPreviewTask(Template tpl, Block block, UUID uuid) {
        this.previewTask = new BuildPreviewAsyncTask(tpl, block, uuid);
        TaskMaster.asyncTask(previewTask, 0);
    }

    public void undoPreview() {
        if (this.previewUndo == null) {
            this.previewUndo = new ConcurrentHashMap<BlockCoord, SimpleBlock>();
            return;
        }

        if (this.previewTask != null) {
            previewTask.lock.lock();
            try {
                previewTask.aborted = true;
            } finally {
                previewTask.lock.unlock();
            }
        }

        try {
            Player player = CivGlobal.getPlayer(this);
            PlayerBlockChangeUtil util = new PlayerBlockChangeUtil();
            for (BlockCoord coord : this.previewUndo.keySet()) {
                SimpleBlock sb = this.previewUndo.get(coord);
                util.addUpdateBlock(player.getName(), coord, sb.getType(), sb.getData());
            }

            util.sendUpdate(player.getName());
        } catch (CivException e) {
            //Fall down and return.
        }

        this.previewUndo.clear();
        this.previewUndo = new ConcurrentHashMap<BlockCoord, SimpleBlock>();
    }

    public boolean isShowInfo() {
        return showInfo;
    }

    public void setShowInfo(boolean showInfo) {
        this.showInfo = showInfo;
    }

    public boolean isBanned() {
        return banned;
    }

    public void setBanned(boolean banned) {
        this.banned = banned;
    }

    public double getSpyExposure() {
        return spyExposure;
    }

    public void setSpyExposure(double spyExposure) {
        this.spyExposure = spyExposure;

        try {
            Player player = CivGlobal.getPlayer(this);
            double percentage = spyExposure / MAX_SPY_EXPOSURE;
            if (percentage > 1.0) player.setExp((float) 1.0);
            else player.setExp((float) percentage);
        } catch (CivException e) {
        }

    }

    public boolean isPerformingMission() {
        return performingMission;
    }

    public void setPerformingMission(boolean performingMission) {
        this.performingMission = performingMission;
    }

    public void onRoadTest(BlockCoord coord, Player player) {
        /* Test the block beneath us for a road, if so, set the road flag. */
        BlockCoord feet = new BlockCoord(coord);
        feet.setY(feet.getY() - 1);
        RoadBlock rb = CivGlobal.getRoadBlock(feet);

        if (rb == null) {
            onRoad = false;
//			if (player.hasPotionEffect(PotionEffectType.SPEED)) {
//				player.removePotionEffect(PotionEffectType.SPEED);
//			}
        } else {
            onRoad = true;

//			if (!player.hasPotionEffect(PotionEffectType.SPEED)) {
//				CivLog.debug("setting effect.");
//				player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 5, 5));
//			}
        }
    }

    public boolean isOnRoad() {
        return onRoad;
    }

    public void setOnRoad(boolean onRoad) {
        this.onRoad = onRoad;
    }

    public void giveTemplate(String name) {
        int perkCount;
        try {
            perkCount = CivSettings.getInteger(CivSettings.perkConfig, "system.free_perk_count");
        } catch (InvalidConfiguration e) {
            e.printStackTrace();
            return;
        }
        for (ConfigPerk p : CivSettings.perks.values()) {
            Perk perk = new Perk(p);

            if (perk.getIdent().startsWith(("tpl_" + name).toLowerCase()) || perk.getIdent().startsWith(("template_" + name).toLowerCase())) {
                perk.count = perkCount;
                this.perks.put(perk.getIdent(), perk);
            }
        }

    }

    public void giveAllFreePerks() {
        int perkCount;
        try {
            perkCount = CivSettings.getInteger(CivSettings.perkConfig, "system.free_perk_count");
        } catch (InvalidConfiguration e) {
            e.printStackTrace();
            return;
        }

        for (ConfigPerk p : CivSettings.perks.values()) {
            Perk perk = new Perk(p);

            if (perk.getIdent().startsWith("perk_")) {
                perk.count = perkCount;
                this.perks.put(perk.getIdent(), perk);
            }
        }

    }

    public void loadPerks(final Player player) {
        class AsyncTask implements Runnable {
            final Resident resident;

            public AsyncTask(Resident resident) {
                this.resident = resident;
            }

            @Override
            public void run() {
                try {
                    resident.perks.clear();
                    Player player = CivGlobal.getPlayer(resident);
                    try {
                        CivGlobal.perkManager.loadPerksForResident(resident);
                    } catch (SQLException e) {
                        CivMessage.sendError(player, CivSettings.localize.localizedString("resident_couldnotloadperks"));
                        e.printStackTrace();
                        return;
                    } catch (NotVerifiedException e) {
                        return;
                    }
                } catch (CivException e1) {
                    return;
                }
                try {

                    StringBuilder perkMessage = new StringBuilder();
                    if (CivSettings.getString(CivSettings.perkConfig, "system.free_perks").equalsIgnoreCase("true")) {
                        resident.giveAllFreePerks();
                        perkMessage = new StringBuilder(CivSettings.localize.localizedString("PlayerLoginAsync_perksMsg1") + " ");
                    } else if (CivSettings.getString(CivSettings.perkConfig, "system.free_admin_perks").equalsIgnoreCase("true")) {
                        if (player.hasPermission(CivSettings.MINI_ADMIN) || player.hasPermission(CivSettings.FREE_PERKS)) {
                            resident.giveAllFreePerks();
                            perkMessage = new StringBuilder(CivSettings.localize.localizedString("PlayerLoginAsync_perksMsg1") + ": ");
                            perkMessage.append("Weather" + ", ");
                        }
                    }

                    for (ConfigPerk p : CivSettings.templates.values()) {
                        if (player.hasPermission("civ.perk." + p.simple_name)) {
                            resident.giveTemplate(p.simple_name);
                            perkMessage.append(p.display_name).append(", ");
                        }
                    }

                    perkMessage.append(CivSettings.localize.localizedString("PlayerLoginAsync_perksMsg2"));

                    CivMessage.send(resident, CivColor.LightGreen + perkMessage);
                } catch (InvalidConfiguration e) {
                    e.printStackTrace();
                }
            }
        }

        TaskMaster.asyncTask(new AsyncTask(this), 0);
    }

    public void setRejoinCooldown(Town town) {
        String value = "" + town.getCiv().getId();
        String key = getCooldownKey();
        CivGlobal.getSessionDB().add(key, value, 0, 0, 0);
    }

    public String getCooldownKey() {
        return "cooldown:" + this.getName();
    }

    public void cleanupCooldown() {
        CivGlobal.getSessionDB().delete_all(getCooldownKey());
    }

    public void validateJoinTown(Town town) throws CivException {
        if (this.hasTown() && this.getCiv() == town.getCiv()) {
            /* allow players to join the same civ, no probs */
            return;
        }

        long cooldownTime;
        int cooldownHours;
        try {
            cooldownHours = CivSettings.getInteger(CivSettings.civConfig, "global.join_civ_cooldown");
        } catch (InvalidConfiguration e) {
            e.printStackTrace();
            return;
        }

        cooldownTime = cooldownHours * 60 * 60 * 1000; /*convert hours to milliseconds. */

        ArrayList<SessionEntry> entries = CivGlobal.getSessionDB().lookup(getCooldownKey());
        if (entries.size() > 0) {
            Civilization oldCiv = CivGlobal.getCivFromId(Integer.valueOf(entries.get(0).value));
            if (oldCiv == null) {
                /* Hmm old civ is gone. */
                cleanupCooldown();
                return;
            }

//			if (oldCiv == town.getCiv()) {
//				/* We're rejoining the old civ, allow it. */
//				return;
//			}

            /* Check if cooldown is expired. */
            Date now = new Date();
            if (now.getTime() > (entries.get(0).time + cooldownTime)) {
                /* Entry is expired, allow cooldown and cleanup. */
                cleanupCooldown();
                return;
            }

            throw new CivException(CivSettings.localize.localizedString("var_resident_cannotJoinCivJustLeft1", cooldownHours));
        }
    }

    public LinkedList<Perk> getPersonalTemplatePerks(ConfigBuildableInfo info) {
        LinkedList<Perk> templates = new LinkedList<Perk>();

        for (Perk perk : this.perks.values()) {
            CustomPersonalTemplate customTemplate = (CustomPersonalTemplate) perk.getComponent("CustomPersonalTemplate");
            if (customTemplate == null) {
                continue;
            }

            if (customTemplate.getString("id").equals(info.id)) {
                templates.add(perk);
            }
        }
        return templates;
    }

    public ArrayList<Perk> getUnboundTemplatePerks(ArrayList<Perk> alreadyBoundPerkList, ConfigBuildableInfo info) {
        ArrayList<Perk> unboundPerks = new ArrayList<Perk>();
        for (Perk ourPerk : perks.values()) {

            if (!ourPerk.getIdent().contains("template")) {
                CustomTemplate customTemplate = (CustomTemplate) ourPerk.getComponent("CustomTemplate");
                if (customTemplate == null) {
                    continue;
                }

                if (!customTemplate.getString("template").equals(info.template_base_name)) {
                    /* Not the correct template. */
                    continue;
                }

                boolean has = false;
                for (Perk perk : alreadyBoundPerkList) {
                    if (perk.getIdent().equals(ourPerk.getIdent())) {
                        /* Perk is already bound in this town, do not display for binding. */
                        has = true;
                        continue;
                    }
                }

                if (!has) unboundPerks.add(ourPerk);
            }
        }

        return unboundPerks;
    }

    public boolean isControlBlockInstantBreak() {
        return controlBlockInstantBreak;
    }

    public void setControlBlockInstantBreak(boolean controlBlockInstantBreak) {
        this.controlBlockInstantBreak = controlBlockInstantBreak;
    }


    public boolean isMuted() {
        return muted;
    }

    public void setMuted(boolean muted) {
        this.muted = muted;
    }

    public boolean isCombatInfo() {
        return combatInfo;
    }

    public void setCombatInfo(boolean combatInfo) {
        this.combatInfo = combatInfo;
    }

    public boolean isInactiveForDays(int days) {
        Calendar now = Calendar.getInstance();
        Calendar expire = Calendar.getInstance();
        expire.setTimeInMillis(this.getLastOnline());

        expire.add(Calendar.DATE, days);

        if (now.after(expire)) {
            return true;
        }

        return false;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public Inventory startTradeWith(Resident resident) {
        try {
            Player player = CivGlobal.getPlayer(this);
            if (player.isDead()) {
                throw new CivException(CivSettings.localize.localizedString("resident_tradeErrorPlayerDead"));
            }
            Inventory inv = Bukkit.createInventory(player, 9 * 5, this.getName() + " : " + resident.getName());

            /*
             * Set up top and bottom layer with buttons.
             */

            /* Top part which is for the other resident. */
            ItemStack signStack = LoreGuiItem.build("", CivData.WOOL, CivData.DATA_WOOL_WHITE, "");
            int start = 0;
            for (int i = start; i < (9 + start); i++) {
                if ((i - start) == 8) {
                    ItemStack guiStack = LoreGuiItem.build(resident.getName() + " Confirm",
                            CivData.WOOL, CivData.DATA_WOOL_RED,
                            CivColor.LightGreen + CivSettings.localize.localizedString("var_resident_tradeWait1", CivColor.LightBlue + resident.getName()),
                            CivColor.LightGray + " " + CivSettings.localize.localizedString("resident_tradeWait2"));
                    inv.setItem(i, guiStack);
                } else if ((i - start) == 7) {
                    ItemStack guiStack = LoreGuiItem.build(CivSettings.CURRENCY_NAME + " " + CivSettings.localize.localizedString("resident_tradeOffered"),
                            ItemManager.getId(Material.NETHER_BRICK_ITEM), 0,
                            CivColor.Yellow + "0 " + CivSettings.CURRENCY_NAME);
                    inv.setItem(i, guiStack);
                } else {
                    inv.setItem(i, signStack);
                }
            }

            start = 4 * 9;
            for (int i = start; i < (9 + start); i++) {
                if ((i - start) == 8) {
                    ItemStack guiStack = LoreGuiItem.build(CivSettings.localize.localizedString("resident_tradeYourConfirm"),
                            CivData.WOOL, CivData.DATA_WOOL_RED,
                            CivColor.Gold + CivSettings.localize.localizedString("resident_tradeClicktoConfirm"));
                    inv.setItem(i, guiStack);

                } else if ((i - start) == 0) {
                    ItemStack guiStack = LoreGuiItem.build(CivSettings.localize.localizedString("resident_tradeRemove") + " " + CivSettings.CURRENCY_NAME,
                            ItemManager.getId(Material.NETHER_BRICK_ITEM), 0,
                            CivColor.Gold + CivSettings.localize.localizedString("resident_tradeRemove100") + " " + CivSettings.CURRENCY_NAME,
                            CivColor.Gold + CivSettings.localize.localizedString("resident_tradeRemove1000") + " " + CivSettings.CURRENCY_NAME);
                    inv.setItem(i, guiStack);
                } else if ((i - start) == 1) {
                    ItemStack guiStack = LoreGuiItem.build(CivSettings.localize.localizedString("resident_tradeAdd") + " " + CivSettings.CURRENCY_NAME,
                            ItemManager.getId(Material.GOLD_INGOT), 0,
                            CivColor.Gold + CivSettings.localize.localizedString("resident_tradeAdd100") + " " + CivSettings.CURRENCY_NAME,
                            CivColor.Gold + CivSettings.localize.localizedString("resident_tradeAdd1000") + " " + CivSettings.CURRENCY_NAME);
                    inv.setItem(i, guiStack);
                } else if ((i - start) == 7) {
                    ItemStack guiStack = LoreGuiItem.build(CivSettings.CURRENCY_NAME + " " + CivSettings.localize.localizedString("resident_tradeOffered"),
                            ItemManager.getId(Material.NETHER_BRICK_ITEM), 0,
                            CivColor.Yellow + "0 " + CivSettings.CURRENCY_NAME);
                    inv.setItem(i, guiStack);
                } else {
                    inv.setItem(i, signStack);
                }
            }

            /*
             * Set up middle divider.
             */
            start = 2 * 9;
            for (int i = start; i < (9 + start); i++) {
                inv.setItem(i, signStack);
            }

            player.openInventory(inv);
            return inv;
        } catch (CivException e) {
            try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("possibleCheaters.txt", true)))) {
                out.println("trade:" + this.getName() + " WITH " + resident.getName() + " and was dead");
            } catch (IOException e1) {
                //exception handling left as an exercise for the reader
            }


            CivMessage.sendError(this, CivSettings.localize.localizedString("resident_tradeCouldNotTrade") + " " + e.getMessage());
            CivMessage.sendError(resident, CivSettings.localize.localizedString("resident_tradeCouldNotTrade") + " " + e.getMessage());
            return null;
        }

    }

    public boolean hasTechForItem(ItemStack stack) {
        LoreCraftableMaterial craftMat = LoreCraftableMaterial.getCraftMaterial(stack);
        if (craftMat == null) {
            return true;
        }

        if (craftMat.getConfigMaterial().required_tech == null) {
            return true;
        }

        if (!this.hasTown()) {
            return false;
        }

        /* Parse technoloies */
        String[] split = craftMat.getConfigMaterial().required_tech.split(",");
        for (String tech : split) {
            tech = tech.replace(" ", "");
            if (!this.getCiv().hasTechnology(tech)) {
                return false;
            }
        }

        return true;
    }

    public Date getLastKilledTime() {
        return lastKilledTime;
    }

    public void setLastKilledTime(Date lastKilledTime) {
        this.lastKilledTime = lastKilledTime;
    }

    public Date getMuteExpires() {
        return muteExpires;
    }

    public void setMuteExpires(Date muteExpires) {
        this.muteExpires = muteExpires;
    }

    public String getItemMode() {
        return itemMode;
    }

    public void setItemMode(String itemMode) {
        this.itemMode = itemMode;
    }

    public void toggleItemMode() {
        if (this.itemMode.equals("all")) {
            this.itemMode = "rare";
            CivMessage.send(this, CivColor.LightGreen + CivSettings.localize.localizedString("resident_toggleItemRare"));
        } else if (this.itemMode.equals("rare")) {
            this.itemMode = "none";
            CivMessage.send(this, CivColor.LightGreen + CivSettings.localize.localizedString("resident_toggleItemNone"));
        } else {
            this.itemMode = "all";
            CivMessage.send(this, CivColor.LightGreen + CivSettings.localize.localizedString("resident_toggleItemAll"));
        }
        this.save();
    }

    public String getLastIP() {
        return this.lastIP;
    }

    public void setLastIP(String hostAddress) {
        this.lastIP = hostAddress;
    }

    public void teleportHome() {
        Player player;
        try {
            player = CivGlobal.getPlayer(this);
            teleportHome(player);
        } catch (CivException e) {
            return;
        }
    }

    public void teleportHome(Player player) {
        if (this.hasTown()) {
            TownHall townhall = this.getTown().getTownHall();
            if (townhall != null) {
                BlockCoord coord = townhall.getRandomRevivePoint();
                player.teleport(coord.getLocation());
            }
        } else {
            World world = Bukkit.getWorld("world");
            player.teleport(world.getSpawnLocation());
        }
    }

    public boolean canDamageControlBlock() {
        if (this.hasTown()) {
            if (!this.getCiv().getCapitolStructure().isValid()) {
                return false;
            }
        }

        return true;
    }

    public void saveInventory() {
        try {
            Player player = CivGlobal.getPlayer(this);
            String serial = InventorySerializer.InventoryToString(player.getInventory());
            this.setSavedInventory(serial);
            this.save();
        } catch (CivException e) {
        }
    }

    public void clearInventory() {
        try {
            Player player = CivGlobal.getPlayer(this);
            player.getInventory().clear();
            player.getInventory().setArmorContents(new ItemStack[4]);
        } catch (CivException e) {
        }
    }

    public void restoreInventory() {
        if (this.savedInventory == null) {
            return;
        }

        try {
            Player player = CivGlobal.getPlayer(this);
            clearInventory();
            InventorySerializer.StringToInventory(player.getInventory(), this.savedInventory);
            this.setSavedInventory(null);
            this.save();
        } catch (CivException e) {
            // Player offline??
            e.printStackTrace();
            this.setSavedInventory(null);
            this.save();
        }
    }

    public String getSavedInventory() {
        return savedInventory;
    }

    public void setSavedInventory(String savedInventory) {
        this.savedInventory = savedInventory;
    }

    public boolean isProtected() {
        return isProtected;
    }

    public void setisProtected(boolean prot) {
        isProtected = prot;
    }

    public void showTechPage() throws CivException {
        Player player;
        try {
            player = CivGlobal.getPlayer(this);
        } catch (CivException e) {
            return;
        }
        Civilization civ = this.getCiv();
        if (civ == null) {
            throw new CivException(CivColor.Red + CivSettings.localize.localizedString("cmd_getSenderCivNoCiv"));
        }
        final int type = ItemManager.getId(Material.EMERALD_BLOCK);
        final ArrayList<ConfigTech> techs = ConfigTech.getAvailableTechs(civ);
        final Inventory inv = Bukkit.getServer().createInventory((InventoryHolder) player, 54, CivSettings.localize.localizedString("resident_techsGuiHeading"));
        for (final ConfigTech tech : techs) {
            final String techh = tech.name;
            ItemStack itemStack = LoreGuiItem.build(tech.name, type, 0,
                    "§6" + CivSettings.localize.localizedString("clicktoresearch"),
                    "§b" + CivSettings.localize.localizedString("money_requ", tech.getAdjustedTechCost(civ)),
                    "§a" + CivSettings.localize.localizedString("bealers_req", tech.getAdjustedBeakerCost(civ)),
                    "§d" + CivSettings.localize.localizedString("era_this", tech.era));
            itemStack = LoreGuiItem.setAction(itemStack, "ResearchGui");
            itemStack = LoreGuiItem.setActionData(itemStack, "info", techh);
            inv.addItem(itemStack);
        }
        player.openInventory(inv);
    }

    public void showRelationPage() throws CivException {
        if (this.getCiv() == null) {
            throw new CivException(CivColor.Red + CivSettings.localize.localizedString("cmd_getSenderCivNoCiv"));
        }
        Player player;
        try {
            player = CivGlobal.getPlayer(this);
        } catch (CivException e) {
            return;
        }
        final Inventory inventory = Bukkit.getServer().createInventory((InventoryHolder) player, 9, CivSettings.localize.localizedString("resident_relationsGuiHeading"));
        ItemStack relation = LoreGuiItem.build(CivColor.LightGreenBold + CivSettings.localize.localizedString("resident_relationsGui_ally"), ItemManager.getId(Material.EMERALD_BLOCK), 0, ChatColor.RESET + CivSettings.localize.localizedString("resident_relationsGui_allyInfo"), "§6" + CivSettings.localize.localizedString("bookReborn_clickToView"));
        relation = LoreGuiItem.setAction(relation, "RelationAllies");
        relation = LoreGuiItem.setActionData(relation, "civilization", this.getCiv().getName());
        inventory.addItem(relation);
        relation = LoreGuiItem.build(CivColor.LightGreenBold + CivSettings.localize.localizedString("resident_relationsGui_peace"), ItemManager.getId(Material.LAPIS_BLOCK), 0, ChatColor.RESET + CivSettings.localize.localizedString("resident_relationsGui_peaceInfo"), "§6" + CivSettings.localize.localizedString("bookReborn_clickToView"));
        relation = LoreGuiItem.setAction(relation, "RelationPeaces");
        relation = LoreGuiItem.setActionData(relation, "civilization", this.getCiv().getName());
        inventory.addItem(relation);
        relation = LoreGuiItem.build(CivColor.LightGreenBold + CivSettings.localize.localizedString("resident_relationsGui_hostile"), ItemManager.getId(Material.GOLD_BLOCK), 0, ChatColor.RESET + CivSettings.localize.localizedString("resident_relationsGui_hostileInfo"), "§6" + CivSettings.localize.localizedString("bookReborn_clickToView"));
        relation = LoreGuiItem.setAction(relation, "RelationHostiles");
        relation = LoreGuiItem.setActionData(relation, "civilization", this.getCiv().getName());
        inventory.addItem(relation);
        relation = LoreGuiItem.build(CivColor.LightGreenBold + CivSettings.localize.localizedString("resident_relationsGui_war"), ItemManager.getId(Material.REDSTONE_BLOCK), 0, ChatColor.RESET + CivSettings.localize.localizedString("resident_relationsGui_warInfo"), "§6" + CivSettings.localize.localizedString("bookReborn_clickToView"));
        relation = LoreGuiItem.setAction(relation, "RelationWars");
        relation = LoreGuiItem.setActionData(relation, "civilization", this.getCiv().getName());
        inventory.addItem(relation);
        player.openInventory(inventory);
    }

    public void showStructPage() throws CivException {
        Player player;
        try {
            player = CivGlobal.getPlayer(this);
        } catch (CivException e) {
            return;
        }
        Civilization civ = getCiv();
        Town town = getSelectedTown();
        if (town == null) town = getTown();
        if (town == null) {
            throw new CivException(CivColor.Red + CivSettings.localize.localizedString("cmd_notPartOfTown"));
        }
        Inventory inv = Bukkit.getServer().createInventory((InventoryHolder) player, 54, CivSettings.localize.localizedString("resident_structuresGuiHeading"));
        double rate = 1.0;
        rate -= town.getBuffManager().getEffectiveDouble("buff_rush");
        rate -= town.getBuffManager().getEffectiveDouble("buff_grandcanyon_rush");
        rate -= town.getBuffManager().getEffectiveDouble("buff_mother_tree_tile_improvement_cost");
        for (final ConfigBuildableInfo info : CivSettings.structures.values()) {
            final int type = ItemManager.getId(Material.EMERALD_BLOCK);
            final double hammerCost = Math.round(info.hammer_cost * rate);
            ItemStack itemStack;
            if (town.getMayorGroup() == null || town.getAssistantGroup() == null || civ.getLeaderGroup() == null) {
                itemStack = LoreGuiItem.build(info.displayName, ItemManager.getId(Material.REDSTONE_BLOCK), 0, "§b" + CivSettings.localize.localizedString("money_requ", Double.parseDouble(String.valueOf(info.cost))), "§a" + CivSettings.localize.localizedString("hammers_requ", hammerCost), "§d" + CivSettings.localize.localizedString("upkeep_day", info.upkeep), "§c" + CivSettings.localize.localizedString("belongtown"));
            } else if (!this.getCiv().hasTechnology(info.require_tech)) {
                final ConfigTech tech = CivSettings.techs.get(info.require_tech);
                final String techh = tech.name;
                itemStack = LoreGuiItem.build(info.displayName, ItemManager.getId(Material.REDSTONE), 0, "§b" + CivSettings.localize.localizedString("money_requ", Double.parseDouble(String.valueOf(info.cost))), "§a" + CivSettings.localize.localizedString("hammers_requ", hammerCost), "§d" + CivSettings.localize.localizedString("upkeep_day", info.upkeep), "§c" + CivSettings.localize.localizedString("req") + tech.name, "§3" + CivSettings.localize.localizedString("clicktoresearch"), "§d" + CivSettings.localize.localizedString("era_this", tech.era));
                itemStack = LoreGuiItem.setAction(itemStack, "ResearchGui");
                itemStack = LoreGuiItem.setActionData(itemStack, "info", techh);
            } else if (!town.getMayorGroup().hasMember(this) && !town.getAssistantGroup().hasMember(this) && !civ.getLeaderGroup().hasMember(this)) {
                itemStack = LoreGuiItem.build(info.displayName, ItemManager.getId(Material.REDSTONE_BLOCK), 0, "§b" + CivSettings.localize.localizedString("money_requ", Double.parseDouble(String.valueOf(info.cost))), "§a" + CivSettings.localize.localizedString("hammers_requ", hammerCost), "§d" + CivSettings.localize.localizedString("upkeep_day", info.upkeep), "§c" + CivSettings.localize.localizedString("belongtown"));
            } else if (info.isAvailable(town)) {
                if (!info.id.contains("road") && !info.id.contains("wall")) {
                    itemStack = LoreGuiItem.build(info.displayName, type, 0, "§6" + CivSettings.localize.localizedString("clicktobuild"), "§b" + CivSettings.localize.localizedString("money_requ", Double.parseDouble(String.valueOf(info.cost))), "§a" + CivSettings.localize.localizedString("hammers_requ", hammerCost), "§d" + CivSettings.localize.localizedString("upkeep_day", info.upkeep));
                    itemStack = LoreGuiItem.setAction(itemStack, "BuildChooseTemplate");
                    itemStack = LoreGuiItem.setActionData(itemStack, "info", info.id);
                } else {
                    itemStack = LoreGuiItem.build(info.displayName, type, 0, "§b" + CivSettings.localize.localizedString("money_requ", Double.parseDouble(String.valueOf(info.cost))), "§a" + CivSettings.localize.localizedString("hammers_requ", hammerCost), "§d" + CivSettings.localize.localizedString("upkeep_day", info.upkeep), "§6" + CivSettings.localize.localizedString("clicktobuild"));
                    itemStack = LoreGuiItem.setAction(itemStack, "BuildFromIdCr");
                    itemStack = LoreGuiItem.setActionData(itemStack, "buildableName", info.displayName);
                }
            } else {
                final ConfigBuildableInfo str = CivSettings.structures.get(info.require_structure);
                if (str != null) {
                    final String req_build = str.displayName;
                    itemStack = LoreGuiItem.build(info.displayName, ItemManager.getId(Material.BEDROCK), 0, "§c" + CivSettings.localize.localizedString("requ") + str.displayName, "§b" + CivSettings.localize.localizedString("money_requ", Double.parseDouble(String.valueOf(info.cost))), "§a" + CivSettings.localize.localizedString("hammers_requ", hammerCost), "§d" + CivSettings.localize.localizedString("upkeep_day", info.upkeep), "§3" + CivSettings.localize.localizedString("clicktobuild"));
                    itemStack = LoreGuiItem.setAction(itemStack, "WonderGuiBuild");
                    itemStack = LoreGuiItem.setActionData(itemStack, "info", req_build);
                } else {
                    itemStack = null;
                }
            }
            if (itemStack != null) {
                inv.addItem(itemStack);
            }
        }
        ItemStack is = LoreGuiItem.build("§e" + CivSettings.localize.localizedString("4udesa"), ItemManager.getId(Material.DIAMOND_BLOCK), 0, "§6" + CivSettings.localize.localizedString("click_to_view"));
        is = LoreGuiItem.setAction(is, "WondersGui");
        inv.setItem(53, is);
        LoreGuiItemListener.guiInventories.put(inv.getName(), inv);
        TaskMaster.syncTask(new OpenInventoryTask(player, inv));
    }

    public void showUpgradePage() throws CivException {
        Player player;
        try {
            player = CivGlobal.getPlayer(this);
        } catch (CivException e) {
            return;
        }
        Town town = getSelectedTown();
        if (town == null) town = getTown();
        if (town == null) {
            throw new CivException(CivColor.Red + CivSettings.localize.localizedString("cmd_notPartOfTown"));
        }
        final Inventory inv = Bukkit.getServer().createInventory((InventoryHolder) player, 54, CivSettings.localize.localizedString("resident_upgradesGuiHeading"));
        for (final ConfigTownUpgrade upgrade : ConfigTownUpgrade.getAllUpgrades(town)) {
            double cost = upgrade.cost;
            if (town.getCiv().getGovernment().id.equalsIgnoreCase("gov_theocracy")) {
                cost *= 0.9;
            }
            ItemStack is = null;
            if (upgrade.isAvailable(town)) {
                is = LoreGuiItem.build(upgrade.name, ItemManager.getId(Material.EMERALD_BLOCK), 0, "§b" + CivSettings.localize.localizedString("money_requ", Math.round(cost)), "§6" + CivSettings.localize.localizedString("tutorial_lore_clicktoView"));
                is = LoreGuiItem.setAction(is, "UpgradeGuiBuy");
                is = LoreGuiItem.setActionData(is, "info", upgrade.name);
            } else if (!town.hasStructure(upgrade.require_structure)) {
                final ConfigBuildableInfo structure = CivSettings.structures.get(upgrade.require_structure);
                is = LoreGuiItem.build(upgrade.name, ItemManager.getId(Material.EMERALD), 0, "§b" + CivSettings.localize.localizedString("money_requ", Math.round(cost)), "§c" + CivSettings.localize.localizedString("requ") + structure.displayName, "§3" + CivSettings.localize.localizedString("clicktobuild"));
                is = LoreGuiItem.setAction(is, "WonderGuiBuild");
                is = LoreGuiItem.setActionData(is, "info", structure.displayName);
            } else if (!town.hasUpgrade(upgrade.require_upgrade)) {
                final ConfigTownUpgrade upgrade2 = CivSettings.getUpgradeById(upgrade.require_upgrade);
                is = LoreGuiItem.build(upgrade.name, ItemManager.getId(Material.GLOWSTONE_DUST), 0, "§b" + CivSettings.localize.localizedString("money_requ", Math.round(cost)), "§c" + CivSettings.localize.localizedString("requ") + upgrade2.name, "§3" + CivSettings.localize.localizedString("clicktobuild"));
                is = LoreGuiItem.setAction(is, "UpgradeGuiBuy");
                is = LoreGuiItem.setActionData(is, "info", upgrade2.name);
            }
            if (is != null) {
                inv.addItem(is);
            }
        }
        player.openInventory(inv);
    }

    public void showPerkPage(int pageNumber) {
        Player player;
        try {
            player = CivGlobal.getPlayer(this);
        } catch (CivException e) {
            return;
        }

        Inventory inv = Bukkit.getServer().createInventory(player, Book.MAX_CHEST_SIZE * 9, CivSettings.localize.localizedString("resident_perksGuiHeading"));

        for (Perk obj : perks.values()) {

            if (obj.getIdent().startsWith("temp")) {
                ItemStack stack = LoreGuiItem.build(obj.configPerk.display_name,
                        obj.configPerk.type_id,
                        obj.configPerk.data, CivColor.LightBlue + CivSettings.localize.localizedString("resident_perksGuiClickToView"),
                        CivColor.LightBlue + CivSettings.localize.localizedString("resident_perksGuiTheseTemplates"));
                stack = LoreGuiItem.setAction(stack, "ShowTemplateType");
                stack = LoreGuiItem.setActionData(stack, "perk", obj.configPerk.id);

                inv.addItem(stack);
            } else if (obj.getIdent().startsWith("perk")) {
                ItemStack stack = LoreGuiItem.build(obj.getDisplayName(),
                        obj.configPerk.type_id,
                        obj.configPerk.data, CivColor.Gold + CivSettings.localize.localizedString("resident_perksGui_clickToActivate"),
                        "Unlimted Uses");
                stack = LoreGuiItem.setAction(stack, "ActivatePerk");
                stack = LoreGuiItem.setActionData(stack, "perk", obj.configPerk.id);

                inv.addItem(stack);

            }

        }

        player.openInventory(inv);
    }

    public void showTemplatePerks(String name) {
        Player player;
        try {
            player = CivGlobal.getPlayer(this);
        } catch (CivException e) {
            return;
        }

        Inventory inv = Bukkit.getServer().createInventory(player, Book.MAX_CHEST_SIZE * 9, CivSettings.localize.localizedString("resident_perksGui_templatesHeading") + " " + name);

        for (Object obj : perks.values()) {
            Perk p = (Perk) obj;
            if (p.getIdent().contains("tpl_" + name)) {
                ItemStack stack = LoreGuiItem.build(p.configPerk.display_name,
                        p.configPerk.type_id,
                        p.configPerk.data, CivColor.Gold + CivSettings.localize.localizedString("resident_perksGui_clickToActivate"),
                        CivColor.LightBlue + "Count: " + p.count);
                stack = LoreGuiItem.setAction(stack, "ActivatePerk");
                stack = LoreGuiItem.setActionData(stack, "perk", p.configPerk.id);

                inv.addItem(stack);
            }
        }

        player.openInventory(inv);
    }

    public UUID getUUID() {
        return uid;
    }

    public void setUUID(UUID uid) {
        this.uid = uid;
    }

    public String getUUIDString() {
        return uid.toString();
    }

    public double getWalkingModifier() {
        return walkingModifier;
    }

    public void setWalkingModifier(double walkingModifier) {
        this.walkingModifier = walkingModifier;
    }

    public void calculateWalkingModifier(Player player) {
        double speed = CivSettings.normal_speed;

        /* Set speed from armor. */
        if (Unit.isWearingFullComposite(player)) {
            speed *= CivSettings.T4_leather_speed;
        } else if (Unit.isWearingFullHardened(player)) {
            speed *= CivSettings.T3_leather_speed;
        } else if (Unit.isWearingFullRefined(player)) {
            speed *= CivSettings.T2_leather_speed;
        } else if (Unit.isWearingFullBasicLeather(player)) {
            speed *= CivSettings.T1_leather_speed;
        } else {
            if (Unit.isWearingAnyDiamond(player)) {
                speed *= CivSettings.T4_metal_speed;
            } else if (Unit.isWearingAnyGold(player)) {
                speed *= CivSettings.T3_metal_speed;
            } else if (Unit.isWearingAnyChain(player)) {
                speed *= CivSettings.T2_metal_speed;
            } else if (Unit.isWearingAnyIron(player)) {
                speed *= CivSettings.T1_metal_speed;
            }
        }
        this.walkingModifier = speed;
    }

    public boolean isTitleAPI() {
        return titleAPI;
    }

    public void setTitleAPI(boolean titleAPI) {
        this.titleAPI = titleAPI;
    }

    public int getLanguage() {
        return languageCode;
    }

    public void setLanguageCode(int code) {
        // TO-DO: Need to validate if language code is supported.
        this.languageCode = code;
    }

    public long getNextTeleport() {
        return this.nextTeleport;
    }

    public void setNextTeleport(final long nextTeleport) {
        this.nextTeleport = nextTeleport;
    }

    public String getDesiredReportPlayerName() {
        return this.desiredReportPlayerName;
    }

    public void setDesiredReportPlayerName(final String desiredReportPlayerName) {
        this.desiredReportPlayerName = desiredReportPlayerName;
    }

    public String getReportResult() {
        return this.reportResult;
    }

    public void setReportResult(final String reportResult) {
        this.reportResult = reportResult;
    }

    public boolean getReportChecked() {
        return this.reportChecked;
    }

    public void setReportChecked(final boolean reportChecked) {
        this.reportChecked = reportChecked;
    }

    public boolean isPoisonImmune() {
        return Calendar.getInstance().after(poisonImmune);
    }

    public void addPosionImmune() {
        poisonImmune = Calendar.getInstance().getTimeInMillis() + 1000L * Resident.POISON_DURATION;
    }

    public boolean isLevitateImmune() {
        return Calendar.getInstance().after(levitateImmune);
    }

    public void addLevitateImmune() {
        levitateImmune = Calendar.getInstance().getTimeInMillis() + 1000L * (Resident.LEVITATE_DURATION + 3);
    }

    public void addPLCImmune(final int seconds) {
        nextPLCDamage = System.currentTimeMillis() + TimeTools.toTicks(seconds);
    }

    public boolean isPLCImmuned() {
        return nextPLCDamage > System.currentTimeMillis();
    }

    public void lightningStrike(final boolean repeat, final Town source) {
        Player player;
        try {
            player = CivGlobal.getPlayer(this);
        } catch (CivException e) {
            return;
        }
        if (player == null) {
            return;
        }
        int dmg = 7;
        Structure tesla = source.getStructureByType("s_teslatower");
        if (tesla != null) {
            dmg = ((TeslaTower) tesla).getDamage();
        }
        tesla = source.getStructureByType("s_teslaship");
        if (tesla != null) {
            dmg = ((TeslaShip) tesla).getDamage();
        }
        final LivingEntity target = (LivingEntity) player;
        if (target.getHealth() - dmg > 0.0) {
            target.setHealth(target.getHealth() - dmg);
            target.damage(0.5);
        } else {
            target.setHealth(0.1);
            target.damage(1.0);
        }
        target.setFireTicks(60);
        if (repeat) {
            Bukkit.getScheduler().runTaskLater(CivCraft.getPlugin(), () -> this.lightningStrike(false, source), 30L);
        }
    }


}
