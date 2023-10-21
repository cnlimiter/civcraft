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

import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.exception.InvalidConfiguration;
import cn.evole.plugins.civcraft.main.CivLog;
import cn.evole.plugins.civcraft.main.CivMessage;
import cn.evole.plugins.civcraft.util.CivColor;
import cn.evole.plugins.civcraft.war.War;
import org.bukkit.Bukkit;

import java.io.*;
import java.util.Calendar;

public class DisableTeleportEvent implements EventInterface {

    public static void disableTeleport() throws IOException {
        if (War.hasWars()) {
            File file = new File(CivSettings.plugin.getDataFolder().getPath() + "/data/teleportsOff.txt");
            if (!file.exists()) {
                CivLog.warning("Configuration file: teleportsOff.txt was missing. Streaming to disk from Jar.");
                CivSettings.streamResourceToDisk("/data/teleportsOff.txt");
            }

            CivLog.info("Loading Configuration file: teleportsOff.txt");

            try {
                BufferedReader br = new BufferedReader(new FileReader(file));

                String line;
                try {
                    CivMessage.globalHeading(CivColor.BOLD + CivSettings.localize.localizedString(CivSettings.localize.localizedString("warteleportDisable")));
                    while ((line = br.readLine()) != null && !line.startsWith("#")) {
                        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), line);
                    }

                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public static void enableTeleport() throws IOException {

        File file = new File(CivSettings.plugin.getDataFolder().getPath() + "/data/teleportsOn.txt");
        if (!file.exists()) {
            CivLog.warning("Configuration file: teleportsOn.txt was missing. Streaming to disk from Jar.");
            CivSettings.streamResourceToDisk("/data/teleportsOn.txt");
        }

        CivLog.info("Loading Configuration file: teleportsOn.txt");

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));

            String line;
            try {

                CivMessage.globalHeading(CivColor.BOLD + CivSettings.localize.localizedString("warteleportEnable"));
                while ((line = br.readLine()) != null && !line.startsWith("#")) {
                    Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), line);
                }

                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void process() {
        CivLog.info("TimerEvent: DisableTeleportEvent -------------------------------------");

        try {
            disableTeleport();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public Calendar getNextDate() throws InvalidConfiguration {
        Calendar cal = EventTimer.getCalendarInServerTimeZone();

        int dayOfWeek = CivSettings.getInteger(CivSettings.warConfig, "war.disable_tp_time_day");
        int hourBeforeWar = CivSettings.getInteger(CivSettings.warConfig, "war.disable_tp_time_hour");

        cal.set(Calendar.DAY_OF_WEEK, dayOfWeek);
        cal.set(Calendar.HOUR_OF_DAY, hourBeforeWar);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);

        Calendar now = Calendar.getInstance();
        if (now.after(cal)) {
            cal.add(Calendar.WEEK_OF_MONTH, 1);
            cal.set(Calendar.DAY_OF_WEEK, dayOfWeek);
            cal.set(Calendar.HOUR_OF_DAY, hourBeforeWar);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
        }

        return cal;
    }


}
