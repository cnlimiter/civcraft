/**
 * [2023] Cnlimiter LLC
 * All Rights Reserved.
 * Name: civcraft
 * Author: cnlimiter
 * UpdateTime: 2023/10/14 16:15
 * Description:
 * License: ARR
 */
package cn.evole.plugins.civcraft.config;

import cn.evole.plugins.civcraft.main.CivLog;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ConfigWonderBuff {

    public String id;
    public ArrayList<ConfigBuff> buffs = new ArrayList<ConfigBuff>();

    public static void loadConfig(FileConfiguration cfg, Map<String, ConfigWonderBuff> wbuffs) {
        wbuffs.clear();
        List<Map<?, ?>> ConfigWonderBuff = cfg.getMapList("wonder_buffs");
        for (Map<?, ?> b : ConfigWonderBuff) {
            ConfigWonderBuff buff = new ConfigWonderBuff();
            buff.id = (String) b.get("id");

            List<?> buffStrings = (List<?>) b.get("buffs");
            for (Object obj : buffStrings) {
                if (obj instanceof String) {
                    String str = (String) obj;

                    ConfigBuff cfgBuff = CivSettings.buffs.get(str);

                    if (cfgBuff != null) {
                        buff.buffs.add(cfgBuff);
                    } else {
                        CivLog.warning("Unknown buff id:" + str);
                    }

                }
            }

            wbuffs.put(buff.id, buff);
        }

        CivLog.info("Loaded " + wbuffs.size() + " Wonder Buffs.");
    }
}
