// 
// Decompiled by Procyon v0.5.30
// 

package cn.evole.plugins.civcraft.config;

import cn.evole.plugins.civcraft.main.CivLog;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;
import java.util.Map;

public class ConfigBankLevel {
    public int level; // Level of the bank
    public double exchange_rate; // Rate for this level

    public static void loadConfig(FileConfiguration cfg, Map<Integer, ConfigBankLevel> tasks) {
        tasks.clear();
        List<Map<?, ?>> bank_tasks = cfg.getMapList("bank_levels");
        for (Map<?, ?> task : bank_tasks) {
            ConfigBankLevel bank_task = new ConfigBankLevel();
            bank_task.level = (Integer) task.get("level");
            bank_task.exchange_rate = (Double) task.get("exchange_rate");
            tasks.put(bank_task.level, bank_task);
        }
        CivLog.info("Loaded " + tasks.size() + " Bank Levels.");
    }
}
