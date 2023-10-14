package cn.evole.plugins.civcraft.randomevents.components;

import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.object.Town;
import cn.evole.plugins.civcraft.randomevents.RandomEventComponent;

public class Happiness extends RandomEventComponent {

    public static String getKey(Town town) {
        return "randomevent:happiness:" + town.getId();
    }

    @Override
    public void process() {
        int happiness = Integer.valueOf(this.getString("value"));
        int duration = Integer.valueOf(this.getString("duration"));

        CivGlobal.getSessionDB().add(getKey(this.getParentTown()), happiness + ":" + duration, this.getParentTown().getCiv().getId(), this.getParentTown().getId(), 0);
        sendMessage(CivSettings.localize.localizedString("var_re_happiness1", happiness, duration));
    }

}
