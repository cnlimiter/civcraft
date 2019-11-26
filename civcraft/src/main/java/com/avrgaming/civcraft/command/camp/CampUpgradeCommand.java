/*************************************************************************
 *
 * AVRGAMING LLC
 * __________________
 *
 *  [2013] AVRGAMING LLC
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of AVRGAMING LLC and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to AVRGAMING LLC
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from AVRGAMING LLC.
 */
package com.avrgaming.civcraft.command.camp;

import com.avrgaming.civcraft.camp.Camp;
import com.avrgaming.civcraft.command.CommandBase;
import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.config.ConfigCampUpgrade;
import com.avrgaming.civcraft.exception.CivException;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.util.CivColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CampUpgradeCommand extends CommandBase {
    @Override
    public void init() {
        command = "/camp upgrade";
        displayName = CivSettings.localize.localizedString("cmd_camp_upgrade_name");


        commands.put("list", CivSettings.localize.localizedString("cmd_camp_upgrade_listDesc"));
        commands.put("purchased", CivSettings.localize.localizedString("cmd_camp_upgrade_purchasedDesc"));
        commands.put("buy", CivSettings.localize.localizedString("cmd_camp_upgrade_buyDesc"));

    }

    public void purchased_cmd() throws CivException {
        Camp camp = this.getCurrentCamp();
        CivMessage.sendHeading(sender, CivSettings.localize.localizedString("cmd_camp_upgrade_purchasedSuccess"));

        String out = "";
        for (ConfigCampUpgrade upgrade : camp.getUpgrades()) {
            out += upgrade.name + ", ";
        }

        CivMessage.send(sender, out);
    }

    private void upgradeList(Camp camp) throws CivException {
        for (ConfigCampUpgrade upgrade : CivSettings.campUpgrades.values()) {
            if (upgrade.isAvailable(camp)) {
                CivMessage.send(sender, upgrade.name + " " + CivColor.LightGray + CivSettings.localize.localizedString("Cost") + " " + CivColor.Yellow + upgrade.cost);
            }
        }
    }

    public void list_cmd() throws CivException {
        Camp camp = this.getCurrentCamp();
        CivMessage.sendHeading(sender, CivSettings.localize.localizedString("cmd_camp_upgrade_list"));
        upgradeList(camp);
    }

    public void buy_cmd() throws CivException {
        Camp camp = this.getCurrentCamp();

        if (args.length < 2) {
            CivMessage.sendHeading(sender, CivSettings.localize.localizedString("cmd_camp_upgrade_list"));
            upgradeList(camp);
            CivMessage.send(sender, CivSettings.localize.localizedString("cmd_camp_upgrade_buyHeading"));
            return;
        }

        String combinedArgs = "";
        args = this.stripArgs(args, 1);
        for (String arg : args) {
            combinedArgs += arg + " ";
        }
        combinedArgs = combinedArgs.trim();

        ConfigCampUpgrade upgrade = CivSettings.getCampUpgradeByNameRegex(camp, combinedArgs);
        if (upgrade == null) {
            throw new CivException(CivSettings.localize.localizedString("var_cmd_camp_upgrade_buyInvalid", combinedArgs));
        }

        if (camp.hasUpgrade(upgrade.id)) {
            throw new CivException(CivSettings.localize.localizedString("cmd_camp_upgrade_buyOwned"));
        }

        camp.purchaseUpgrade(upgrade);
        CivMessage.sendSuccess(sender, CivSettings.localize.localizedString("var_cmd_camp_upgrade_buySuccess", upgrade.name));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        // 对buy的科技支持
        if (args.length == 2 && "buy".equalsIgnoreCase(args[0])) {
            List<String> list = new ArrayList<>();
            try {
                for (ConfigCampUpgrade upgrade : CivSettings.campUpgrades.values()) {
                    if (upgrade.isAvailable(getCurrentCamp())) {
                        list.add(upgrade.name);
                    }
                }

            } catch (CivException e) {
                e.printStackTrace();
            }
            return list.stream().filter(s -> s.startsWith(args[0])).collect(Collectors.toList());
        }
        return super.onTabComplete(sender, command, alias, args);
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
        this.validCampOwner();
    }
}
