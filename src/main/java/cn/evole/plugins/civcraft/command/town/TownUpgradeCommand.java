/**
 * [2023] Cnlimiter LLC
 * All Rights Reserved.
 * Name: civcraft
 * Author: cnlimiter
 * UpdateTime: 2023/10/14 16:15
 * Description:
 * License: ARR
 */
package cn.evole.plugins.civcraft.command.town;

import cn.evole.plugins.civcraft.command.CommandBase;
import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.config.ConfigTownUpgrade;
import cn.evole.plugins.civcraft.exception.CivException;
import cn.evole.plugins.civcraft.main.CivMessage;
import cn.evole.plugins.civcraft.object.Town;
import cn.evole.plugins.civcraft.util.CivColor;
import org.apache.commons.lang.WordUtils;

public class TownUpgradeCommand extends CommandBase {

    @Override
    public void init() {
        command = "/town upgrade";
        displayName = CivSettings.localize.localizedString("cmd_town_upgrade_name");

        commands.put("list", CivSettings.localize.localizedString("cmd_town_upgrade_listDesc"));
        commands.put("purchased", CivSettings.localize.localizedString("cmd_town_upgrade_purchasedDesc"));
        commands.put("buy", CivSettings.localize.localizedString("cmd_town_upgrade_buyDesc"));

    }

    public void purchased_cmd() throws CivException {
        Town town = this.getSelectedTown();
        CivMessage.sendHeading(sender, CivSettings.localize.localizedString("cmd_town_upgrade_purchasedHeading"));

        String out = "";
        for (ConfigTownUpgrade upgrade : town.getUpgrades().values()) {
            out += upgrade.name + ", ";
        }

        CivMessage.send(sender, out);
    }

    private void list_upgrades(String category, Town town) throws CivException {
        if (!ConfigTownUpgrade.categories.containsKey(category.toLowerCase()) && !category.equalsIgnoreCase("all")) {
            throw new CivException(CivSettings.localize.localizedString("var_cmd_town_upgrade_listnoCat", category));
        }

        for (ConfigTownUpgrade upgrade : CivSettings.townUpgrades.values()) {
            if (category.equalsIgnoreCase("all") || upgrade.category.equalsIgnoreCase(category)) {
                if (upgrade.isAvailable(town)) {
                    CivMessage.send(sender, upgrade.name + " " + CivColor.LightGray + CivSettings.localize.localizedString("Cost") + " " + CivColor.Yellow + upgrade.cost);
                }
            }
        }
    }

    public void list_cmd() throws CivException {
        Town town = this.getSelectedTown();

        CivMessage.sendHeading(sender, CivSettings.localize.localizedString("cmd_town_upgrade_listHeading"));

        if (args.length < 2) {
            CivMessage.send(sender, "- " + CivColor.Gold + CivSettings.localize.localizedString("cmd_town_upgrade_listAllHeading") + " " +
                    CivColor.LightBlue + "(" + ConfigTownUpgrade.getAvailableCategoryCount("all", town) + ")");
            for (String category : ConfigTownUpgrade.categories.keySet()) {
                CivMessage.send(sender, "- " + CivColor.Gold + WordUtils.capitalize(category) +
                        CivColor.LightBlue + " (" + ConfigTownUpgrade.getAvailableCategoryCount(category, town) + ")");
            }
            return;
        }

        list_upgrades(args[1], town);

    }

    public void buy_cmd() throws CivException {
        if (args.length < 2) {
            list_upgrades("all", getSelectedTown());
            CivMessage.send(sender, CivSettings.localize.localizedString("cmd_town_upgrade_buyHeading"));
            return;
        }

        Town town = this.getSelectedTown();

        String combinedArgs = "";
        args = this.stripArgs(args, 1);
        for (String arg : args) {
            combinedArgs += arg + " ";
        }
        combinedArgs = combinedArgs.trim();

        ConfigTownUpgrade upgrade = CivSettings.getUpgradeByNameRegex(town, combinedArgs);
        if (upgrade == null) {
            throw new CivException(CivSettings.localize.localizedString("cmd_town_upgrade_buyInvalid") + " " + combinedArgs);
        }

        if (town.hasUpgrade(upgrade.id)) {
            throw new CivException(CivSettings.localize.localizedString("cmd_town_upgrade_buyOwned"));
        }

        //TODO make upgrades take time by using hammers.
        town.purchaseUpgrade(upgrade);
        CivMessage.sendSuccess(sender, CivSettings.localize.localizedString("var_cmd_town_upgrade_buySuccess", upgrade.name));
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
        this.validMayorAssistantLeader();
    }

}
