/**
 * [2023] Cnlimiter LLC
 * All Rights Reserved.
 * Name: civcraft
 * Author: cnlimiter
 * UpdateTime: 2023/10/14 16:15
 * Description:
 * License: ARR
 */
package cn.evole.plugins.civcraft.command.camp;

import cn.evole.plugins.civcraft.camp.Camp;
import cn.evole.plugins.civcraft.command.CommandBase;
import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.config.ConfigCampUpgrade;
import cn.evole.plugins.civcraft.exception.CivException;
import cn.evole.plugins.civcraft.main.CivMessage;
import cn.evole.plugins.civcraft.util.CivColor;
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
            return list.stream().filter(s -> s.startsWith(args[1])).collect(Collectors.toList());
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
