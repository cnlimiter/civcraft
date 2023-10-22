package cn.evole.plugins.civcraft.randomevents;


import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.database.SQL;
import cn.evole.plugins.civcraft.database.SQLUpdate;
import cn.evole.plugins.civcraft.exception.CivException;
import cn.evole.plugins.civcraft.exception.InvalidNameException;
import cn.evole.plugins.civcraft.exception.InvalidObjectException;
import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.main.CivLog;
import cn.evole.plugins.civcraft.main.CivMessage;
import cn.evole.plugins.civcraft.object.SQLObject;
import cn.evole.plugins.civcraft.object.Town;
import cn.evole.plugins.civcraft.randomevents.components.HammerRate;
import cn.evole.plugins.civcraft.randomevents.components.Happiness;
import cn.evole.plugins.civcraft.randomevents.components.Unhappiness;
import cn.evole.plugins.civcraft.sessiondb.SessionEntry;
import cn.evole.plugins.civcraft.util.CivColor;
import cn.hutool.core.codec.Base64Decoder;
import cn.hutool.core.codec.Base64Encoder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class RandomEvent extends SQLObject {

    public static final String TABLE_NAME = "RANDOMEVENTS";
    public ConfigRandomEvent configRandomEvent;
    public HashMap<String, RandomEventComponent> actions = new HashMap<String, RandomEventComponent>();
    public HashMap<String, RandomEventComponent> requirements = new HashMap<String, RandomEventComponent>();
    public HashMap<String, RandomEventComponent> success = new HashMap<String, RandomEventComponent>();
    public HashMap<String, RandomEventComponent> failure = new HashMap<String, RandomEventComponent>();
    /*
     * Components can communicate with each other by saving variables in this hashmap.
     */
    public HashMap<String, String> componentVars = new HashMap<String, String>();
    public LinkedList<String> savedMessages = new LinkedList<String>();
    private Town town = null;
    private Date startDate = null;
    private boolean active = false;

    public RandomEvent(ConfigRandomEvent config) {
        this.configRandomEvent = config;
        buildComponents();
    }

    public RandomEvent(ResultSet rs) throws SQLException, InvalidNameException, InvalidObjectException, CivException {
        this.load(rs);

        /* Place ourselves back in the town we just loaded. */
        this.town.setActiveEvent(this);
    }

    public static void init() throws SQLException {
        if (!SQL.hasTable(TABLE_NAME)) {
            String table_create = "CREATE TABLE " + SQL.tb_prefix + TABLE_NAME + " (" +
                    "`id` int(11) unsigned NOT NULL auto_increment," +
                    "`config_id` mediumtext," +
                    "`town_id` int(11)," +
                    "`start_date` long NOT NULL," +
                    "`active` boolean DEFAULT false," +
                    "`component_vars` mediumtext," +
                    "`saved_messages` mediumtext," +
                    "PRIMARY KEY (`id`)" + ")";

            SQL.makeTable(table_create);
            CivLog.info("Created " + TABLE_NAME + " table");
        } else {
            SQL.makeCol("active", "boolean", TABLE_NAME);
        }
    }

    public static double getUnhappiness(Town town) {
        //	CivGlobal.getSessionDB().add("randomevent:unhappiness", unhappiness+":"+duration, this.getParentTown().getCiv().getId(), this.getParentTown().getId(), 0);

        ArrayList<SessionEntry> entries = CivGlobal.getSessionDB().lookup(Unhappiness.getKey(town));
        double unhappy = 0.0;

        ArrayList<SessionEntry> removed = new ArrayList<SessionEntry>();
        for (SessionEntry entry : entries) {
            String[] split = entry.value.split(":");
            int unhappiness = Integer.valueOf(split[0]);
            int duration = Integer.valueOf(split[1]);


            Date start = new Date(entry.time);
            Date now = new Date();

            if (now.getTime() > (start.getTime() + (duration * RandomEventSweeper.MILLISECONDS_PER_HOUR))) {
                /* Entry is expired, delete it and continue. */
                removed.add(entry);
                continue;
            }

            unhappy += unhappiness;
        }

        /* Remove any expired entries */
        for (SessionEntry entry : removed) {
            CivGlobal.getSessionDB().delete(entry.request_id, entry.key);
        }

        return unhappy;
    }

    public static double getHappiness(Town town) {
        ArrayList<SessionEntry> entries = CivGlobal.getSessionDB().lookup(Happiness.getKey(town));
        double happy = 0.0;

        ArrayList<SessionEntry> removed = new ArrayList<SessionEntry>();
        for (SessionEntry entry : entries) {
            String[] split = entry.value.split(":");
            int happiness = Integer.valueOf(split[0]);
            int duration = Integer.valueOf(split[1]);


            Date start = new Date(entry.time);
            Date now = new Date();

            if (now.getTime() > (start.getTime() + (duration * RandomEventSweeper.MILLISECONDS_PER_HOUR))) {
                /* Entry is expired, delete it and continue. */
                removed.add(entry);
                continue;
            }

            happy += happiness;
        }

        /* Remove any expired entries */
        for (SessionEntry entry : removed) {
            CivGlobal.getSessionDB().delete(entry.request_id, entry.key);
        }

        return happy;
    }

    public static double getHammerRate(Town town) {
        ArrayList<SessionEntry> entries = CivGlobal.getSessionDB().lookup(HammerRate.getKey(town));
        double hammerrate = 1.0;

        ArrayList<SessionEntry> removed = new ArrayList<SessionEntry>();
        for (SessionEntry entry : entries) {
            String[] split = entry.value.split(":");
            double rate = Double.valueOf(split[0]);
            int duration = Integer.valueOf(split[1]);


            Date start = new Date(entry.time);
            Date now = new Date();

            if (now.getTime() > (start.getTime() + (duration * RandomEventSweeper.MILLISECONDS_PER_HOUR))) {
                /* Entry is expired, delete it and continue. */
                removed.add(entry);
                continue;
            }

            hammerrate *= rate;
        }

        /* Remove any expired entries */
        for (SessionEntry entry : removed) {
            CivGlobal.getSessionDB().delete(entry.request_id, entry.key);
        }

        return hammerrate;
    }

    @Override
    public void load(ResultSet rs) throws SQLException, InvalidNameException,
            InvalidObjectException, CivException {
        this.setId(rs.getInt("id"));
        this.configRandomEvent = CivSettings.randomEvents.get(rs.getString("config_id"));
        if (this.configRandomEvent == null) {
            /* Delete the random event. */
            this.delete();
            throw new CivException("Couldn't find random event config id:" + rs.getString("config_id"));
        }

        this.town = CivGlobal.getTownFromId(rs.getInt("town_id"));
        if (this.town == null) {
            this.delete();
            throw new CivException("Couldn't find town id:" + rs.getInt("town_id") + " while loading random event.");
        }

        this.startDate = new Date(rs.getLong("start_date"));
        this.active = rs.getBoolean("active");

        loadComponentVars(rs.getString("component_vars"));
        loadSavedMessages(rs.getString("saved_messages"));

        /* Re-run the on start to re-enable any listeners. */
        /* Loop through all components for onStart() */
        buildComponents();
        for (RandomEventComponent comp : this.actions.values()) {
            comp.onStart();
        }
        for (RandomEventComponent comp : this.requirements.values()) {
            comp.onStart();
        }
        for (RandomEventComponent comp : this.success.values()) {
            comp.onStart();
        }
        for (RandomEventComponent comp : this.failure.values()) {
            comp.onStart();
        }

        RandomEventSweeper.register(this);
    }

    private void loadComponentVars(String input) {
        if (input == null || input.equals("")) {
            return;
        }
        String[] keyValues = input.split(",");

        for (String kvs : keyValues) {
            String[] split = kvs.split(":");
            String keyEncoded = split[0];
            String valueEncoded = split[1];

            String key = Base64Decoder.decodeStr(keyEncoded);
            String value = Base64Decoder.decodeStr(valueEncoded);

            this.componentVars.put(key, value);
        }
    }

    private void loadSavedMessages(String input) {
        String[] messages = input.split(",");

        for (String encodedMessage : messages) {
            String message = Base64Decoder.decodeStr(encodedMessage);
            this.savedMessages.add(message);
        }
    }

    @Override
    public void save() {
        SQLUpdate.add(this);
    }

    @Override
    public void saveNow() throws SQLException {
        HashMap<String, Object> hashmap = new HashMap<String, Object>();

        hashmap.put("config_id", this.configRandomEvent.id);
        hashmap.put("town_id", this.getTown().getId());
        hashmap.put("start_date", this.startDate.getTime());
        hashmap.put("component_vars", this.getComponentVarsSaveString());
        hashmap.put("saved_messages", this.getSavedMessagesSaveString());
        hashmap.put("active", this.active);

        SQL.updateNamedObject(this, hashmap, TABLE_NAME);
    }

    private String getComponentVarsSaveString() {
        StringBuilder out = new StringBuilder();

        for (String key : this.componentVars.keySet()) {
            String value = this.componentVars.get(key);

            String keyEncoded = Base64Encoder.encode(key);
            String valueEncoded = Base64Encoder.encode(value);

            out.append(keyEncoded).append(":").append(valueEncoded).append(",");

        }

        return out.toString();
    }

    private String getSavedMessagesSaveString() {
        StringBuilder out = new StringBuilder();

        for (String message : this.savedMessages) {

            String msgEncoded = Base64Encoder.encode(message);
            out.append(",").append(msgEncoded);
        }

        return out.toString();
    }

    @Override
    public void delete() throws SQLException {
        SQL.deleteNamedObject(this, TABLE_NAME);
    }

    public void buildComponents() {
        buildComponents("cn.evole.plugins.civcraft.randomevents.components.", configRandomEvent.actions, actions);
        buildComponents("cn.evole.plugins.civcraft.randomevents.components.", configRandomEvent.requirements, requirements);
        buildComponents("cn.evole.plugins.civcraft.randomevents.components.", configRandomEvent.success, success);
        buildComponents("cn.evole.plugins.civcraft.randomevents.components.", configRandomEvent.failure, failure);
    }

    public void buildComponents(String classPath, List<HashMap<String, String>> compInfoList, HashMap<String, RandomEventComponent> components) {
        if (compInfoList != null) {
            for (HashMap<String, String> compInfo : compInfoList) {
                String className = classPath + compInfo.get("name");
                Class<?> someClass;

                try {
                    someClass = Class.forName(className);
                    RandomEventComponent perkCompClass;
                    perkCompClass = (RandomEventComponent) someClass.newInstance();
                    perkCompClass.setName(compInfo.get("name"));

                    for (String key : compInfo.keySet()) {
                        perkCompClass.setAttribute(key, compInfo.get(key));
                    }

                    perkCompClass.createComponent(this);
                    components.put(perkCompClass.getName(), perkCompClass);
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /*
     * Private for now, since we only allow random events on towns atm.
     */
    private void start() {

        /* Loop through all components for onStart() */
        for (RandomEventComponent comp : this.actions.values()) {
            comp.onStart();
        }
        for (RandomEventComponent comp : this.requirements.values()) {
            comp.onStart();
        }
        for (RandomEventComponent comp : this.success.values()) {
            comp.onStart();
        }
        for (RandomEventComponent comp : this.failure.values()) {
            comp.onStart();
        }

        /* Start by processing all of the action components. */
        boolean requireActivation = false;
        for (RandomEventComponent comp : this.actions.values()) {
            if (!comp.requiresActivation()) {
                comp.process();
            } else {
                requireActivation = true;
                CivMessage.sendTown(this.town, CivColor.Yellow + CivSettings.localize.localizedString("re_activationRequired"));
            }
        }

        if (!requireActivation) {
            this.active = true;
        }

        /* Register this random event with the sweeper until complete. */
        RandomEventSweeper.register(this);

        /* Setup start date. */
        this.startDate = new Date();

        this.save();
    }

    public Town getTown() {
        return town;
    }

    public void setTown(Town town) {
        this.town = town;
    }

    public void cleanup() {
        /* Loop through all components for cleanup */
        for (RandomEventComponent comp : this.actions.values()) {
            comp.onCleanup();
        }
        for (RandomEventComponent comp : this.requirements.values()) {
            comp.onCleanup();
        }
        for (RandomEventComponent comp : this.success.values()) {
            comp.onCleanup();
        }
        for (RandomEventComponent comp : this.failure.values()) {
            comp.onCleanup();
        }

        town.setActiveEvent(null);
        try {
            this.delete();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public int getLength() {
        return this.configRandomEvent.length;
    }

    public void start(Town town) {
        this.town = town;

        /* Show message to town */
        CivMessage.sendTownHeading(town, "Event: " + this.configRandomEvent.name);
        for (String str : this.configRandomEvent.message) {
            CivMessage.sendTown(town, str);
            savedMessages.add(str);
        }


        town.setActiveEvent(this);
        this.start();
    }

    public List<String> getMessages() {
        return savedMessages;
    }

    public Date getEndDate() {
        Date end = new Date(this.startDate.getTime() + (this.configRandomEvent.length * RandomEventSweeper.MILLISECONDS_PER_HOUR));
        return end;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void activate() throws CivException {
        if (this.active) {
            throw new CivException(CivSettings.localize.localizedString("re_alreadyActive"));
        }

        this.active = true;
        /* Start by processing all of the action components. */
        for (RandomEventComponent comp : this.actions.values()) {
            comp.process();
        }

        this.save();
    }
}
