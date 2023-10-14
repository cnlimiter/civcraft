package cn.evole.plugins.civcraft.randomevents.components;

import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.object.Town;
import cn.evole.plugins.civcraft.randomevents.RandomEventComponent;

public class Unhappiness extends RandomEventComponent {

    public static String getKey(Town town) {
        return "randomevent:unhappiness:" + town.getId();
    }


    @Override
    public void process() {

        int unhappiness = Integer.valueOf(this.getString("value"));
        int duration = Integer.valueOf(this.getString("duration"));

        CivGlobal.getSessionDB().add(getKey(this.getParentTown()), unhappiness + ":" + duration, this.getParentTown().getCiv().getId(), this.getParentTown().getId(), 0);
        sendMessage(CivSettings.localize.localizedString("var_re_unhappiness1", unhappiness, duration));

    }

}
