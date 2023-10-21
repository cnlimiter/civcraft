/**
 * [2023] Cnlimiter LLC
 * All Rights Reserved.
 * Name: civcraft
 * Author: cnlimiter
 * UpdateTime: 2023/10/14 16:15
 * Description:
 * License: ARR
 */
package cn.evole.plugins.civcraft.event;

import cn.evole.plugins.civcraft.database.SQL;
import cn.evole.plugins.civcraft.exception.InvalidConfiguration;
import cn.evole.plugins.civcraft.main.CivLog;
import cn.evole.plugins.civcraft.randomevents.RandomEventTimer;
import cn.evole.plugins.civcraft.threading.TaskMaster;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.HashMap;

public class EventTimer {

    public static HashMap<String, EventTimer> timers = new HashMap<String, EventTimer>();
    public static String TABLE_NAME = "TIMERS";
    private Calendar next;
    private Calendar last;
    /* Number of seconds this event repeats. */
    private String name;
    private EventInterface eventFunction;

    public EventTimer(String name, EventInterface eventFunction, Calendar start) {
        try {
            this.load(name, eventFunction, start);
        } catch (SQLException e) {
            e.printStackTrace();
        }
//		this.name = name;
//		this.eventFunction = eventFunction;
//		this.peroid = peroid;
//		this.next = start;
//		register();
    }

    public static void init() throws SQLException {
        if (!SQL.hasTable(TABLE_NAME)) {
            String table_create = "CREATE TABLE " + SQL.tb_prefix + TABLE_NAME + " (" +
                    "`name` VARCHAR(64) PRIMARY KEY NOT NULL," +
                    "`nextEvent` long," +
                    "`lastEvent` long" +
                    ")";

            SQL.makeTable(table_create);
            CivLog.info("Created " + TABLE_NAME + " table");
        } else {
            CivLog.info(TABLE_NAME + " table OK!");
        }
    }

    public static void loadGlobalEvents() {
//		TestEvent test = new TestEvent();
//		try {
//			new EventTimer("test", test, test.getNextDate());
//		} catch (InvalidConfiguration e1) {
//			e1.printStackTrace();
//		}

        /* Setup daily upkeep event. */
        try {
            DailyEvent upkeepEvent = new DailyEvent();
            new EventTimer("daily", upkeepEvent, upkeepEvent.getNextDate());
        } catch (InvalidConfiguration e) {
            e.printStackTrace();
        }

        /* Setup Hourly tick event. */
        try {
            HourlyTickEvent hourlyTickEvent = new HourlyTickEvent();
            new EventTimer("hourly", hourlyTickEvent, hourlyTickEvent.getNextDate());
        } catch (InvalidConfiguration e) {
            e.printStackTrace();
        }

        // Spawn regen event
        try {
            SpawnRegenEvent spawnRegenEvent = new SpawnRegenEvent();
            new EventTimer("spawn-regen", spawnRegenEvent, spawnRegenEvent.getNextDate());
        } catch (InvalidConfiguration e) {
            e.printStackTrace();
        }

        /* Setup war event. */
        try {
            WarEvent WarEvent = new WarEvent();
            new EventTimer("war", WarEvent, WarEvent.getNextDate());
        } catch (InvalidConfiguration e) {
            e.printStackTrace();
        }

        /* Setup disable Teleport event. */
        try {
            DisableTeleportEvent DisableTeleportEvent = new DisableTeleportEvent();
            new EventTimer("disabletp", DisableTeleportEvent, DisableTeleportEvent.getNextDate());
        } catch (InvalidConfiguration e) {
            e.printStackTrace();
        }

        /* Setup repo event. */
        try {
            GoodieRepoEvent repoEvent = new GoodieRepoEvent();
            new EventTimer("repo-goodies", repoEvent, repoEvent.getNextDate());
        } catch (InvalidConfiguration e) {
            e.printStackTrace();
        }

        /* Setup random event timer. */
        try {
            RandomEventTimer randEvent = new RandomEventTimer();
            new EventTimer("random", randEvent, randEvent.getNextDate());
        } catch (InvalidConfiguration e) {
            e.printStackTrace();
        }

    }

    public static Calendar getCalendarInServerTimeZone() {
        Calendar cal = Calendar.getInstance();
        // This doesnt fucking work. IDK why. But when I "Add" time to a calendar after setting the time zone
        // I get really strange results... Example:
        // 11/25 9:36:31 PM PST, setting to 0 seconds
        // gets set to 11/25 7:36:00 PM PST
        // FFS WHY?!

        //try {
        //cal.setTimeZone(TimeZone.getTimeZone(CivSettings.getStringBase("server_timezone")));
        //} catch (InvalidConfiguration e) {
        //	e.printStackTrace();
        //}

        return cal;
    }

    public void load(String timerName, EventInterface eventFunction, Calendar start) throws SQLException {
        Connection context = null;
        ResultSet rs = null;
        PreparedStatement ps = null;

        try {
            String query = "SELECT * FROM `" + SQL.tb_prefix + TABLE_NAME + "` WHERE `name` = ?";
            context = SQL.getGameConnection();
            ps = context.prepareStatement(query);
            ps.setString(1, timerName);
            rs = ps.executeQuery();

            this.name = timerName;
            this.eventFunction = eventFunction;

            if (rs.next()) {
                this.last = EventTimer.getCalendarInServerTimeZone();
                this.last.setTimeInMillis(rs.getLong("lastEvent"));

                this.next = EventTimer.getCalendarInServerTimeZone();
                this.next.setTimeInMillis(rs.getLong("nextEvent"));
            } else {
                this.last = EventTimer.getCalendarInServerTimeZone();
                this.last.setTimeInMillis(0);

                this.next = start;
                this.save();
            }
            register();
        } finally {
            SQL.close(rs, ps, context);
        }
    }

    private void register() {
        timers.put(this.name, this);
    }

    public void save() {
        class SaveLater implements Runnable {
            EventTimer timer;

            SaveLater(EventTimer timer) {
                this.timer = timer;
            }

            public void run() {
                try {
                    timer.saveNow();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        TaskMaster.asyncTask(new SaveLater(this), 0);
    }

    public void saveNow() throws SQLException {
        Connection context = null;
        PreparedStatement ps = null;

        try {
            String query =
                    "insert or replace into `" + SQL.tb_prefix + TABLE_NAME + "` (`name`, `nextEvent`, `lastEvent`) "
                            +"VALUES (?," +
                            " COALESCE((SELECT `nextEvent` FROM `" + SQL.tb_prefix + TABLE_NAME + "` WHERE `name` = ?), ?)," +
                            " COALESCE((SELECT `lastEvent` FROM `" + SQL.tb_prefix + TABLE_NAME + "` WHERE `name` = ?), ?))"
                    ;


//                    "INSERT INTO `" + SQL.tb_prefix + TABLE_NAME + "` (`name`, `nextEvent`, `lastEvent`) " +
//                    "VALUES (?, ?, ?) ON conflict(`name`) do update set `nextEvent`=?, `lastEvent`=?";
            context = SQL.getGameConnection();
            ps = context.prepareStatement(query);

            ps.setString(1, this.name);
            ps.setString(2, this.name);
            ps.setLong(3, next.getTime().getTime());
            ps.setString(4, this.name);
            ps.setLong(5, last.getTime().getTime());


            int rs = ps.executeUpdate();
            if (rs == 0) {
                throw new SQLException("Could not execute SQL code:" + query);
            }
        } finally {
            SQL.close(null, ps, context);
        }

    }

    public Calendar getNext() {
        return next;
    }

    public void setNext(Calendar next2) {
        this.next = next2;
    }

    public Calendar getLast() {
        return last;
    }

    public void setLast(Calendar last) {
        this.last = last;
    }

    public EventInterface getEventFunction() {
        return eventFunction;
    }

    public void setEventFunction(EventInterface eventFunction) {
        this.eventFunction = eventFunction;
    }

    public String getName() {
        return name;
    }

}
