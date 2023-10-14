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
import cn.evole.plugins.civcraft.structure.Library;
import cn.evole.plugins.civcraft.structure.Store;

import java.util.ArrayList;

public class TownResetCommand extends CommandBase {

    @Override
    public void init() {
        command = "/town reset";
        displayName = CivSettings.localize.localizedString("cmd_town_reset_name");

        commands.put("library", CivSettings.localize.localizedString("cmd_town_reset_libraryDesc"));
        commands.put("store", CivSettings.localize.localizedString("cmd_town_reset_storeDesc"));
    }

    public void library_cmd() throws CivException {
        Town town = getSelectedTown();

        Library library = (Library) town.findStructureByConfigId("s_library");
        if (library == null) {
            throw new CivException(CivSettings.localize.localizedString("cmd_town_reset_libraryNone"));
        }

        ArrayList<ConfigTownUpgrade> removeUs = new ArrayList<ConfigTownUpgrade>();
        for (ConfigTownUpgrade upgrade : town.getUpgrades().values()) {
            if (upgrade.action.contains("enable_library_enchantment")) {
                removeUs.add(upgrade);
            }
        }

        for (ConfigTownUpgrade upgrade : removeUs) {
            town.removeUpgrade(upgrade);
        }

        library.reset();

        town.save();
        CivMessage.sendSuccess(sender, CivSettings.localize.localizedString("cmd_town_reset_librarySuccess"));
    }

    public void store_cmd() throws CivException {
        Town town = getSelectedTown();

        Store store = (Store) town.findStructureByConfigId("s_store");
        if (store == null) {
            throw new CivException(CivSettings.localize.localizedString("cmd_town_reset_storeNone"));
        }

        ArrayList<ConfigTownUpgrade> removeUs = new ArrayList<ConfigTownUpgrade>();
        for (ConfigTownUpgrade upgrade : town.getUpgrades().values()) {
            if (upgrade.action.contains("set_store_material")) {
                removeUs.add(upgrade);
            }
        }

        for (ConfigTownUpgrade upgrade : removeUs) {
            town.removeUpgrade(upgrade);
        }

        store.reset();

        town.save();
        CivMessage.sendSuccess(sender, CivSettings.localize.localizedString("cmd_town_reset_storeSuccess"));
    }

    @Override
    public void doDefaultAction() throws CivException {
        showHelp();
    }

    @Override
    public void showHelp() {
        this.showBasicHelp();
    }

    @Override
    public void permissionCheck() throws CivException {
        this.validMayorAssistantLeader();
    }

}
