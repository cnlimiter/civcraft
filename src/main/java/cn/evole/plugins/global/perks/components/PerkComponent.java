package cn.evole.plugins.global.perks.components;

import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.main.CivMessage;
import cn.evole.plugins.civcraft.object.Resident;
import cn.evole.plugins.civcraft.util.CivColor;
import cn.evole.plugins.global.perks.NotVerifiedException;
import cn.evole.plugins.global.perks.Perk;

import java.sql.SQLException;
import java.util.HashMap;


public class PerkComponent {

    private final HashMap<String, String> attributes = new HashMap<String, String>();
    private String name;
    private Perk parent;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getString(String key) {
        return attributes.get(key);
    }

    public double getDouble(String key) {
        return Double.valueOf(attributes.get(key));
    }

    public void setAttribute(String key, String value) {
        attributes.put(key, value);
    }

    public Perk getParent() {
        return parent;
    }

    public void setParent(Perk parent) {
        this.parent = parent;
    }

    public void markAsUsed(Resident resident) {
        this.getParent().count--;
        if (this.getParent().count <= 0) {
            resident.perks.remove(this.getParent().getIdent());
        }

        try {
            CivGlobal.perkManager.markAsUsed(resident, this.getParent());
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (NotVerifiedException e) {
            CivMessage.send(resident, CivColor.Rose + CivSettings.localize.localizedString("PerkComponent_notValidated"));
            e.printStackTrace();
        }
    }

    public void onActivate(Resident resident) {
    }

    public void createComponent() {
    }

}
