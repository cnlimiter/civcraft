package cn.evole.plugins.global.perks.components;

import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.exception.CivException;
import cn.evole.plugins.civcraft.interactive.InteractiveConfirmWeatherChange;
import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.main.CivMessage;
import cn.evole.plugins.civcraft.object.Resident;
import cn.evole.plugins.civcraft.util.CivColor;
import org.bukkit.entity.Player;

public class ChangeWeather extends PerkComponent {

    @Override
    public void onActivate(Resident resident) {
        Player player;
        try {
            player = CivGlobal.getPlayer(resident);
        } catch (CivException e) {
            return;
        }
        if (!player.getWorld().isThundering() && !player.getWorld().hasStorm()) {
            CivMessage.sendError(resident, CivSettings.localize.localizedString("weather_isSunny"));
            return;
        }

        CivMessage.sendHeading(resident, CivSettings.localize.localizedString("weather_heading"));
        CivMessage.send(resident, CivColor.Green + CivSettings.localize.localizedString("weather_confirmPrompt"));
        CivMessage.send(resident, CivColor.LightGray + CivSettings.localize.localizedString("weather_confirmPrompt2"));
        resident.setInteractiveMode(new InteractiveConfirmWeatherChange(this));
    }
}
