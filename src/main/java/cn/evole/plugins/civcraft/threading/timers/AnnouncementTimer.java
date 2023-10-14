/**
 * [2023] Cnlimiter LLC
 * All Rights Reserved.
 * Name: civcraft
 * Author: cnlimiter
 * UpdateTime: 2023/10/14 16:15
 * Description:
 * License: ARR
 */
package cn.evole.plugins.civcraft.threading.timers;

import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.main.CivLog;
import cn.evole.plugins.civcraft.main.CivMessage;
import cn.evole.plugins.civcraft.util.CivColor;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 提示的timer
 */
public class AnnouncementTimer implements Runnable {

    List<String> announcements;
    int minutes = 5;

    public AnnouncementTimer(String filename, int interval) {
        minutes = interval;

        File file = new File(CivSettings.plugin.getDataFolder().getPath() + "/data/" + filename);
        if (!file.exists()) {
            CivLog.warning("Configuration file: " + filename + " was missing. Streaming to disk from Jar.");
            try {
                CivSettings.streamResourceToDisk("/data/" + filename);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        CivLog.info("Loading Configuration file: " + filename);


        announcements = new ArrayList<String>();

        if (!file.exists()) {
            CivLog.warning("No " + filename + " to run announcements on.");
            return;
        }

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));

            String line;
            try {
                while ((line = br.readLine()) != null) {
                    announcements.add(line);
                }

                br.close();
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }

    }


    @Override
    public void run() {

        for (String str : announcements) {
            CivMessage.sendAll(CivColor.Gold + CivSettings.localize.localizedString("TipHeading") + " " + CivColor.White + str);

            try {
                Thread.sleep(60 * minutes * 1000); //sleep for x mins
            } catch (InterruptedException e) {
                e.printStackTrace();
                return;
            }
        }
    }

}
