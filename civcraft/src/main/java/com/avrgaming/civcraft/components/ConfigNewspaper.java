// 
// Decompiled by Procyon v0.5.30
// 

package com.avrgaming.civcraft.components;

import com.avrgaming.civcraft.main.CivLog;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;
import java.util.Map;

public class ConfigNewspaper {
    public String id;
    public Integer item;
    public Integer iData;
    public Integer guiData;
    public String headline;
    public String lineotd;
    public String date;
    public String line1;
    public String line2;
    public String line3;
    public String line4;
    public String line5;
    public String line6;
    public String line7;
    public String line8;
    public String line9;
    public String line10;
    public String line11;
    public String line12;
    public String version;

    public static void loadConfig(final FileConfiguration cfg, final Map<String, ConfigNewspaper> newspapers) {
        newspapers.clear();
        final List<Map<?, ?>> confignewspapers = (List<Map<?, ?>>) cfg.getMapList("newspaper");
        for (final Map<?, ?> b : confignewspapers) {
            final ConfigNewspaper newspaper = new ConfigNewspaper();
            newspaper.id = (String) b.get("id");
            newspaper.item = (Integer) b.get("item");
            newspaper.iData = (Integer) b.get("iData");
            newspaper.guiData = (Integer) b.get("guiData");
            newspaper.headline = (String) b.get("headline");
            newspaper.lineotd = (String) b.get("lineotd");
            newspaper.date = (String) b.get("date");
            newspaper.line1 = (String) b.get("line1");
            newspaper.line2 = (String) b.get("line2");
            newspaper.line3 = (String) b.get("line3");
            newspaper.line4 = (String) b.get("line4");
            newspaper.line5 = (String) b.get("line5");
            newspaper.line6 = (String) b.get("line6");
            newspaper.line7 = (String) b.get("line7");
            newspaper.line8 = (String) b.get("line8");
            newspaper.line9 = (String) b.get("line9");
            newspaper.line10 = (String) b.get("line10");
            newspaper.line11 = (String) b.get("line11");
            newspaper.line12 = (String) b.get("line12");
            newspaper.version = (String) b.get("version");
            newspapers.put(newspaper.id.toLowerCase(), newspaper);
        }
        CivLog.info("Loaded " + newspapers.size() + " newspaper issues.");
    }
}
