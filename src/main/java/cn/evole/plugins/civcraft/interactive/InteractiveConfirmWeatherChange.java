package cn.evole.plugins.civcraft.interactive;

import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.exception.CivException;
import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.main.CivMessage;
import cn.evole.plugins.civcraft.object.Resident;
import cn.evole.plugins.civcraft.util.TimeTools;
import cn.evole.plugins.global.perks.components.ChangeWeather;
import org.bukkit.entity.Player;

public class InteractiveConfirmWeatherChange implements InteractiveResponse {

    ChangeWeather perk;

    public InteractiveConfirmWeatherChange(ChangeWeather perk) {
        this.perk = perk;
    }

    @Override
    public void respond(String message, Resident resident) {
        resident.clearInteractiveMode();

        if (message.equalsIgnoreCase("yes")) {
            Player player;
            try {
                player = CivGlobal.getPlayer(resident);
                player.getWorld().setStorm(false);
                player.getWorld().setThundering(false);
                player.getWorld().setWeatherDuration((int) TimeTools.toTicks(20 * 60));
                CivMessage.global(CivSettings.localize.localizedString("var_interactive_weather_success", resident.getName()));
                perk.markAsUsed(resident);
            } catch (CivException e) {
            }
        } else {
            CivMessage.send(resident, CivSettings.localize.localizedString("interactive_weather_cancel"));
        }

    }

}
