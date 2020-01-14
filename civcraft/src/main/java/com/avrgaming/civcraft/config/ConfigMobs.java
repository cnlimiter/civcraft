package com.avrgaming.civcraft.config;

import com.avrgaming.civcraft.main.CivLog;
import org.bukkit.attribute.Attributable;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEntity;
import org.bukkit.entity.Damageable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Vasilis
 * @date 2019-12-30 -10:16
 */

public class ConfigMobs {

    public String id;
    public String name;
    public Boolean visible;
    public String entity;
    public Double max_health; // √
    public Double move_speed; // √
    public Double attack_dmg; // √
    public Double defense_dmg; // √
    public Double follow_range; // √
    public Double kb_resistance; // √
    public Integer exp_min; // √
    public Integer exp_max; // √
    public Double res_exp;
    public Double exp_mod;
    public String mod_type;
    public List<String> biomes;
    public List<String> drops;


    public static void loadConfig(FileConfiguration cfg, Map<String, ConfigMobs> mobs) {
        mobs.clear();
        List<Map<?, ?>> mobs_list = cfg.getMapList("custom_mobs");
        for (Map<?, ?> cl : mobs_list) {
            ConfigMobs mob = new ConfigMobs();
            mob.id = (String) cl.get("id");
            mob.name = (String) cl.get("name");
            mob.visible = (Boolean) cl.get("visible");
            mob.entity = (String) cl.get("entity");
            mob.max_health = (Double) cl.get("max_health");
            mob.move_speed = (Double) cl.get("move_speed");
            mob.attack_dmg = (Double) cl.get("attack_dmg");
            mob.defense_dmg = (Double) cl.get("defense_dmg");
            mob.follow_range = (Double) cl.get("follow_range");
            mob.kb_resistance = (Double) cl.get("kb_resistance");
            mob.exp_min = (Integer) cl.get("exp_min");
            mob.exp_max = (Integer) cl.get("exp_max");
            mob.res_exp = (Double) cl.get("res_exp");
            mob.exp_mod = (Double) cl.get("exp_mod");
            mob.mod_type = (String) cl.get("mod_type");

            @SuppressWarnings("unchecked")
            List<String> biomes = (List<String>) cl.get("biomes");
            if (biomes != null) {
                mob.biomes = new ArrayList<>(biomes);
            }
            @SuppressWarnings("unchecked")
            List<String> drops = (List<String>) cl.get("drops");
            if (drops != null) {
                mob.drops = new ArrayList<>(drops);
            }
            mobs.put(mob.id, mob);
        }
        CivLog.info("Loaded " + mobs.size() + " Custom Mobs.");
    }

    public void setMaxHealth(CraftEntity ent, double health) {
        ((Attributable) ent).getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(health);
        ((Damageable) ent).setHealth(health);
    }

    public void modifySpeed(CraftEntity ent, double percent) {
        double speed = (((Attributable) ent).getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getValue()) * percent;
        ((Attributable) ent).getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(speed);
    }

    public void setAttack(CraftEntity ent, double attack) {
        ((Attributable) ent).getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(attack);
    }

    public void setDefense(CraftEntity ent, double defense) {
        ((Attributable) ent).getAttribute(Attribute.GENERIC_ARMOR).setBaseValue(defense);
    }

    public void setFollowRange(CraftEntity ent, double range) {
        ((Attributable) ent).getAttribute(Attribute.GENERIC_FOLLOW_RANGE).setBaseValue(range);
    }

    public void setKnockbackResistance(CraftEntity ent, double resist) {
        ((Attributable) ent).getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE).setBaseValue(resist);
    }
}
