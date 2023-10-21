package cn.evole.plugins.global.perks;

import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.config.ConfigPerk;
import cn.evole.plugins.civcraft.object.Resident;
import cn.evole.plugins.global.perks.components.PerkComponent;

import java.util.HashMap;
import java.util.List;

public class Perk {

    public static HashMap<String, Perk> staticPerks = new HashMap<String, Perk>();
    private final HashMap<String, PerkComponent> components = new HashMap<String, PerkComponent>();
    public ConfigPerk configPerk;
    public int count = 0;
    public String provider;
    private String ident;

    public Perk(ConfigPerk config) {
        this.configPerk = config;
        this.ident = config.id;
        this.count = 1;
        buildComponents();
    }

    public static void init() {
        for (ConfigPerk configPerk : CivSettings.perks.values()) {
            Perk p = new Perk(configPerk);
            staticPerks.put(p.getIdent(), p);
        }
    }

    public String getIdent() {
        return ident;
    }

    public void setIdent(String ident) {
        this.ident = ident;
    }

    private void buildComponents() {
        List<HashMap<String, String>> compInfoList = this.configPerk.components;
        if (compInfoList != null) {
            for (HashMap<String, String> compInfo : compInfoList) {
                String className = "cn.evole.plugins.global.perks.components." + compInfo.get("name");
                Class<?> someClass;

                try {
                    someClass = Class.forName(className);
                    PerkComponent perkCompClass;
                    perkCompClass = (PerkComponent) someClass.newInstance();
                    perkCompClass.setName(compInfo.get("name"));
                    perkCompClass.setParent(this);

                    for (String key : compInfo.keySet()) {
                        perkCompClass.setAttribute(key, compInfo.get(key));
                    }

                    perkCompClass.createComponent();
                    this.components.put(perkCompClass.getName(), perkCompClass);
                } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void onActivate(Resident resident) {
        for (PerkComponent perk : this.components.values()) {
            perk.onActivate(resident);
        }
    }

    public String getDisplayName() {
        return configPerk.display_name;
    }

    public PerkComponent getComponent(String key) {
        return this.components.get(key);
    }

}
