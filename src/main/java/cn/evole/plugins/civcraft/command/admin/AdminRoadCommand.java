package cn.evole.plugins.civcraft.command.admin;

import cn.evole.plugins.civcraft.command.CommandBase;
import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.exception.CivException;
import cn.evole.plugins.civcraft.main.CivMessage;
import cn.evole.plugins.civcraft.object.Town;
import cn.evole.plugins.civcraft.road.Road;
import cn.evole.plugins.civcraft.structure.Buildable;
import org.bukkit.entity.Player;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AdminRoadCommand extends CommandBase {

    @Override
    public void init() {
        command = "/ad road";
        displayName = CivSettings.localize.localizedString("adcmd_road_name");

        //	commands.put("destroy", "Destroys nearest road.");
        commands.put("setraidtime", CivSettings.localize.localizedString("adcmd_road_setRaidTimeDesc"));
    }

    public void setraidtime_cmd() throws CivException {
        Town town = getNamedTown(1);
        Player player = getPlayer();

        if (args.length < 3) {
            throw new CivException(CivSettings.localize.localizedString("adcmd_road_setRaidTimePrompt"));
        }

        Buildable buildable = town.getNearestBuildable(player.getLocation());
        Road road;
        if (!(buildable instanceof Road)) {
            throw new CivException(CivSettings.localize.localizedString("var_adcmd_road_setRaidTimeNotRoad", buildable.getDisplayName()));
        }

        road = (Road) buildable;

        String dateStr = args[2];
        SimpleDateFormat parser = new SimpleDateFormat("d:M:y:H:m");

        Date next;
        try {
            next = parser.parse(dateStr);
            road.setNextRaidDate(next);
            CivMessage.sendSuccess(sender, CivSettings.localize.localizedString("adcmd_road_setRaidTimeEnterTime"));
        } catch (ParseException e) {
            throw new CivException(CivSettings.localize.localizedString("var_adcmd_road_setRaidTimeError", args[2]));
        }

    }


    @Override
    public void doDefaultAction() throws CivException {
        showHelp();
    }

    @Override
    public void showHelp() {
        showBasicHelp();
    }

    @Override
    public void permissionCheck() throws CivException {

    }

}
