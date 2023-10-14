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

import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.config.ConfigBuff;

import java.text.DecimalFormat;

public class Buff {

    /* Quick redefines for id/name from yml. */
    public static final String FINE_ART = "buff_fine_art";
    public static final String CONSTRUCTION = "buff_construction";
    public static final String GROWTH_RATE = "buff_year_of_plenty";
    public static final String TRADE = "buff_monopoly";
    public static final String REDUCE_CONSUME = "buff_preservative";
    public static final String SCIENCE_RATE = "buff_innovation";
    public static final String ADVANCED_TOOLING = "buff_advanced_tooling";
    public static final String BARRICADE = "buff_barricade";
    public static final String BARTER = "buff_barter";
    public static final String EXTRACTION = "buff_extraction";
    public static final String FIRE_BOMB = "buff_fire_bomb";
    public static final String FISHING = "buff_fishing";
    public static final String MEDICINE = "buff_medicine";
    public static final String RUSH = "buff_rush";
    public static final String DEBUFF_PYRAMID_LEECH = "debuff_pyramid_leech";

    private ConfigBuff config;
    private String source;
    private String key;

    public Buff(String buffkey, String buffId, String source) {
        config = CivSettings.buffs.get(buffId);
        setKey(buffkey);
        this.source = source;
    }

    @Override
    public int hashCode() {
        return config.id.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof Buff) {
            Buff otherBuff = (Buff) other;
            if (otherBuff.getConfig().id.equals(this.getConfig().id)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return the source
     */
    public String getSource() {
        return source;
    }

    /**
     * @param source the source to set
     */
    public void setSource(String source) {
        this.source = source;
    }

    /**
     * @return the config
     */
    public ConfigBuff getConfig() {
        return config;
    }

    /**
     * @param config the config to set
     */
    public void setConfig(ConfigBuff config) {
        this.config = config;
    }

    public boolean isStackable() {
        return config.stackable;
    }

    public String getId() {
        return config.id;
    }

    public String getValue() {
        return config.value;
    }

    public String getDisplayDouble() {
        try {
            double d = Double.parseDouble(config.value);
            DecimalFormat df = new DecimalFormat();
            return df.format(d * 100) + "%";
        } catch (NumberFormatException e) {
            return "NAN!";
        }
    }

    public String getDisplayInt() {
        try {
            int i = Integer.parseInt(config.value);
            return "" + i;
        } catch (NumberFormatException e) {
            return "NAN!";
        }
    }

    public String getDisplayName() {
        return config.name;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
