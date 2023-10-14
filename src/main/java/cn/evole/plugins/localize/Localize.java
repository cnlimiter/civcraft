package cn.evole.plugins.localize;

/*
 * This plugin needs a default_lang.yml file in the jar file. This file includes the default strings.
 */

import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.main.CivLog;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.IllegalFormatException;

public class Localize {
    private final JavaPlugin plugin;
    public String languageFile;
    private FileConfiguration localizedStrings = null;
    private FileConfiguration defaultLocalizedStrings = null;

    public Localize(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public Localize(JavaPlugin plugin, String langFile) {
        this.plugin = plugin;
        this.setLanguageFile(langFile);
    }

    public String getLanguageFile() {
        return this.languageFile;
    }

    public void setLanguageFile(String langFile) {
        if (langFile.isEmpty()) {
            this.languageFile = "zh_lang.yml";
        } else {
            this.languageFile = langFile;
        }
        this.reloadDefaultLocalizedStrings();
        this.reloadLocalizedStrings();
    }

    public Boolean isDefault() {
        return languageFile.equalsIgnoreCase("default_lang.yml");
    }

    public String localizedString(String pathToString) {
        if (this.isDefault()) {
            Object value = this.getDefaultLocalizedStrings().get(pathToString);
            if (value == null) return pathToString;
            else return (String) value;
        } else {
            Object value = this.getLocalizedStrings().get(pathToString);
            if (value == null) {
                value = this.getDefaultLocalizedStrings().get(pathToString);
                if (value == null) return pathToString;
                else return (String) value;
            } else return (String) value;
        }
    }

    public String localizedString(String pathToString, Object... args) {
        String localString = localizedString(pathToString);
        if (args.length >= 1) {
            if (localString.equalsIgnoreCase(pathToString)) {
                return localString;
            }
            localString = compounded(localString, args);
        }
        return localString;
    }

    private String compounded(String string, Object... args) {
        try {
            for (int arg = 0; arg < args.length; ++arg) {
                Object replacementString = args[arg];
                string = string.replace("[%" + arg + "]", "" + replacementString);

            }
            return string;
        } catch (IllegalFormatException e1) {
            return string + " - [" + localizedString("stringFormattingError") + "]";
        }
    }

    public void reloadDefaultLocalizedStrings() {
        String defaultLanguageFile = "default_lang.yml";
        File defaultLocalizedStringsFile = new File(plugin.getDataFolder().getPath() + "/localization/" + defaultLanguageFile);
        CivLog.warning("配置文件:" + defaultLanguageFile + " 已被使用. 从Jar重新释放到硬盘.");
        try {
            CivSettings.streamResourceToDisk("/localization/" + defaultLanguageFile);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        defaultLocalizedStrings = YamlConfiguration.loadConfiguration(defaultLocalizedStringsFile);

        CivLog.info("加载配置文件:" + defaultLanguageFile);
        // read the config.yml into memory
        YamlConfiguration cfg = new YamlConfiguration();
        try {
            cfg.load(defaultLocalizedStringsFile);
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        } catch (InvalidConfigurationException e1) {
            e1.printStackTrace();
        }
        defaultLocalizedStrings.setDefaults(cfg);

    }

    public void reloadLocalizedStrings() {
        File localizedStringsFile = new File(plugin.getDataFolder().getPath() + "/localization/" + languageFile);
        if (this.isDefault()) {
            if (defaultLocalizedStrings == null) {
                localizedStrings = defaultLocalizedStrings;
            }
            return;
        } else if (!localizedStringsFile.exists()) {

            CivLog.warning("配置文件:" + languageFile + " 消失了. 你必须创建目录 plugins/Civcraft/localization/");
            CivLog.warning("使用 default_lang.yml");
            this.setLanguageFile("");
            return;
        }
        localizedStrings = YamlConfiguration.loadConfiguration(localizedStringsFile);

        CivLog.info("加载配置文件:" + languageFile);
        // read the config.yml into memory
        YamlConfiguration cfg = new YamlConfiguration();
        try {
            cfg.load(localizedStringsFile);
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        } catch (InvalidConfigurationException e1) {
            e1.printStackTrace();
        }
        localizedStrings.setDefaults(cfg);

    }

    private FileConfiguration getLocalizedStrings() {
        if (localizedStrings == null) {
            reloadLocalizedStrings();
        }
        return localizedStrings;
    }

    private FileConfiguration getDefaultLocalizedStrings() {
        if (defaultLocalizedStrings == null) {
            reloadDefaultLocalizedStrings();
        }
        return defaultLocalizedStrings;
    }

}