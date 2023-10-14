package cn.evole.plugins.civcraft.command.admin;

import cn.evole.plugins.civcraft.command.CommandBase;
import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.config.ConfigPerk;
import cn.evole.plugins.civcraft.exception.CivException;
import cn.evole.plugins.civcraft.exception.InvalidConfiguration;
import cn.evole.plugins.civcraft.main.CivMessage;
import cn.evole.plugins.civcraft.util.CivColor;
import org.bukkit.configuration.InvalidConfigurationException;

import java.io.FileNotFoundException;
import java.io.IOException;

public class AdminPerkCommand extends CommandBase {

    @Override
    public void init() {
        command = "/ad perk";
        displayName = CivSettings.localize.localizedString("adcmd_perk_name");

        commands.put("list", CivSettings.localize.localizedString("adcmd_perk_listDesc"));
        commands.put("reload", CivSettings.localize.localizedString("adcmd_perk_reloadDesc"));
    }

    public void list_cmd() {
        CivMessage.sendHeading(sender, CivSettings.localize.localizedString("adcmd_perk_listHeading"));
        for (ConfigPerk perk : CivSettings.perks.values()) {
            CivMessage.send(sender, CivColor.Green + perk.display_name + CivColor.LightGreen + " id:" + CivColor.Rose + perk.id);
        }
        CivMessage.send(sender, CivColor.LightGray + CivSettings.localize.localizedString("adcmd_perk_listingSuccess"));
    }

    public void reload_cmd() throws FileNotFoundException, IOException, InvalidConfigurationException, InvalidConfiguration {
        CivSettings.reloadPerks();
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
        // TODO Auto-generated method stub

    }

}
