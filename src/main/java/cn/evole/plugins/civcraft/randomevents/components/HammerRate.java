package cn.evole.plugins.civcraft.randomevents.components;

import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.object.Town;
import cn.evole.plugins.civcraft.randomevents.RandomEventComponent;

import java.text.DecimalFormat;

public class HammerRate extends RandomEventComponent {

    public static String getKey(Town town) {
        return "randomevent:hammerrate" + town.getId();
    }

    @Override
    public void process() {
        double rate = this.getDouble("value");
        int duration = Integer.valueOf(this.getString("duration"));

        CivGlobal.getSessionDB().add(getKey(this.getParentTown()), rate + ":" + duration, this.getParentTown().getCiv().getId(), this.getParentTown().getId(), 0);
        DecimalFormat df = new DecimalFormat();

        if (rate > 1.0) {
            sendMessage(CivSettings.localize.localizedString("var_re_hammers_increase", df.format((rate - 1.0) * 100)));
        } else {
            sendMessage(CivSettings.localize.localizedString("var_re_hammers_decrease", df.format((1.0 - rate) * 100)));
        }
    }

}
