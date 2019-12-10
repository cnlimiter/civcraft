
package com.avrgaming.civcraft.interactive;

import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.config.ConfigTownUpgrade;
import com.avrgaming.civcraft.exception.CivException;
import com.avrgaming.civcraft.lorestorage.LoreCraftableMaterial;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.object.Civilization;
import com.avrgaming.civcraft.object.Resident;
import com.avrgaming.civcraft.object.Town;
import com.avrgaming.civcraft.structure.Barracks;
import com.avrgaming.civcraft.util.CivColor;
import gpl.AttributeUtil;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class InteractiveConfirmScroll implements InteractiveResponse {
    @Override
    public void respond(String message, Resident resident) {
        Player player;
        try {
            player = CivGlobal.getPlayer(resident);
        } catch (CivException e) {
            return;
        }
        if (!message.equalsIgnoreCase("yes")) {
            CivMessage.send((Object) player, CivSettings.localize.localizedString("interactive_scroll_cancel"));
            resident.clearInteractiveMode();
            return;
        }
        Town town = resident.getSelectedTown();
        Civilization civ = resident.getCiv();
        ItemStack itemStack = player.getInventory().getItemInMainHand();
        LoreCraftableMaterial craftMat = LoreCraftableMaterial.getCraftMaterial(itemStack);
        if (craftMat == null || !craftMat.getConfigId().equals("mat_scroll")) {
            CivMessage.sendError(resident, CivSettings.localize.localizedString("var_processScroll_takeStroll"));
            resident.clearInteractiveMode();
            return;
        }
        if (civ == null) {
            CivMessage.sendError(resident, CivSettings.localize.localizedString("var_processScroll_noCiv"));
            resident.clearInteractiveMode();
            return;
        }
        String[] lore = new AttributeUtil(itemStack).getLore();
        String nd = lore[1];
        if (lore[0].contains("500 Culture")) {
            town.addAccumulatedCulture(500.0);
            CivMessage.sendCiv(civ, CivSettings.localize.localizedString("var_processScroll_addedCulture", player.getDisplayName(), CivColor.LightGreenBold + 500 + CivColor.RESET, CivColor.GoldBold + town.getName() + CivColor.RESET));
        } else if (lore[0].contains("Bonus")) {
            if (lore[0].contains("500 Hammers")) {
                String scrollTill = town.getScrollTill();
                if (town.hasScroll()) {
                    CivMessage.sendError(resident, CivSettings.localize.localizedString("var_processScroll_arleadyActivated", CivColor.RoseBold + scrollTill, CivColor.GoldBold + town.getName() + CivColor.RESET));
                    resident.clearInteractiveMode();
                    return;
                }
                long time = 3600000 * Integer.parseInt(nd.replaceAll("[^\\d]", ""));
                town.addScroll(time);
                SimpleDateFormat sdf = new SimpleDateFormat("M/dd h:mm:ss a z");
                CivMessage.sendCiv(civ, CivSettings.localize.localizedString("var_processScroll_addedHammers", player.getDisplayName(), CivColor.LightGreenBold + 500 + CivColor.RESET, CivColor.GoldBold + town.getName() + CivColor.RESET, CivColor.RoseBold + sdf.format(time + Calendar.getInstance().getTimeInMillis()) + CivColor.RESET));
            } else if (nd.contains("Settler")) {
                if (town.hasStructure("s_barracks")) {
                    CivMessage.sendError(resident, CivSettings.localize.localizedString("var_processScroll_noBarracks", CivColor.LightGreenBold + 500 + CivColor.RESET, CivColor.GoldBold + town.getName() + CivColor.RESET));
                    resident.clearInteractiveMode();
                    return;
                }
                Barracks barracks = (Barracks) town.getStructureByType("s_barracks");
                if (barracks.getTrainingUnit() == null) {
                    CivMessage.sendError(resident, CivSettings.localize.localizedString("var_processScroll_noBarracksInProgress", CivColor.LightGreenBold + 500 + CivColor.RESET, CivColor.GoldBold + town.getName() + CivColor.RESET));
                    resident.clearInteractiveMode();
                    return;
                }
                if (!barracks.getTrainingUnit().id.equalsIgnoreCase("u_settler")) {
                    CivMessage.sendError(resident, CivSettings.localize.localizedString("var_processScroll_noBarracksNotSettler", CivColor.LightGreenBold + 500 + CivColor.RESET, CivColor.GoldBold + town.getName() + CivColor.RESET, CivColor.LightBlueBold + barracks.getTrainingUnit().name + CivColor.RESET));
                    resident.clearInteractiveMode();
                    return;
                }
                barracks.addHammers(500.0);
                CivMessage.sendCiv(civ, CivSettings.localize.localizedString("var_processScroll_addedHammersSettler", player.getDisplayName(), CivColor.LightGreenBold + 500 + CivColor.RESET, CivColor.GoldBold + town.getName() + CivColor.RESET));
            } else {
                int percent = Integer.parseInt(lore[0].replaceAll("[^\\d]", ""));
                String techName = nd;
                if (civ.getResearchTech() == null) {
                    CivMessage.sendError(resident, CivSettings.localize.localizedString("var_processScroll_noTechInPrg", CivColor.LightGreenBold + percent + CivColor.RESET, CivColor.YellowBold + techName + CivColor.RESET));
                    resident.clearInteractiveMode();
                    return;
                }
                if (!civ.getResearchTech().name.equalsIgnoreCase(techName)) {
                    CivMessage.sendError(resident, CivSettings.localize.localizedString("var_processScroll_notThisTech", CivColor.LightGreenBold + percent + CivColor.RESET, CivColor.YellowBold + techName + CivColor.RESET, CivColor.LightGrayBold + civ.getResearchTech().name + CivColor.RESET));
                    resident.clearInteractiveMode();
                    return;
                }
                double beakers = civ.getResearchTech().beaker_cost * ((double) percent / 100.0);
                civ.addBeakers(beakers);
                CivMessage.sendCiv(civ, CivSettings.localize.localizedString("var_processScroll_addedTech", player.getDisplayName(), CivColor.LightGreenBold + new DecimalFormat("#.##").format(beakers) + CivColor.RESET, CivColor.YellowBold + techName + CivColor.RESET));
            }
        } else if (lore[0].contains("Instant Upgrade")) {
            String st = lore[0];
            int level = Integer.parseInt(nd.replaceAll("[^\\d]", ""));
            ArrayList<ConfigTownUpgrade> upgradeList = new ArrayList<ConfigTownUpgrade>();
            if (st.contains("bank")) {
                String upgrade = "upgrade_bank_level" + level;
                if (!town.hasStructure("s_bank")) {
                    CivMessage.sendError(resident, CivSettings.localize.localizedString("var_processScroll_upgradeBankArleadyNo", CivColor.LightGreenBold + level + CivColor.RESET, CivColor.GoldBold + town.getName() + CivColor.RESET));
                    resident.clearInteractiveMode();
                    return;
                }
                if (town.hasUpgrade(upgrade)) {
                    CivMessage.sendError(resident, CivSettings.localize.localizedString("var_processScroll_upgradeBankArleady", CivColor.LightGreenBold + level + CivColor.RESET, CivColor.GoldBold + town.getName() + CivColor.RESET));
                    resident.clearInteractiveMode();
                    return;
                }
                upgradeList.add(CivSettings.getUpgradeById("upgrade_bank_level_2"));
                if (level == 3) {
                    upgradeList.add(CivSettings.getUpgradeById("upgrade_bank_level_3"));
                }
                try {
                    for (ConfigTownUpgrade upgrade1 : upgradeList) {
                        town.addUpgrade(upgrade1);
                    }
                    town.saveNow();
                    CivMessage.sendCiv(civ, CivSettings.localize.localizedString("var_processScroll_proceedBank", player.getDisplayName(), CivColor.LightGreenBold + level + CivColor.RESET, CivColor.GoldBold + town.getName() + CivColor.RESET));
                } catch (SQLException e) {
                    e.printStackTrace();
                    resident.clearInteractiveMode();
                    return;
                }
            }
            if (st.contains("town")) {
                String upgrade = "upgrade_town_level_" + level;
                if (town.hasUpgrade(upgrade)) {
                    CivMessage.sendError(resident, CivSettings.localize.localizedString("var_processScroll_upgradeTownArleady", CivColor.LightGreenBold + level + CivColor.RESET, CivColor.GoldBold + town.getName() + CivColor.RESET));
                    resident.clearInteractiveMode();
                    return;
                }
                upgradeList.add(CivSettings.getUpgradeById("upgrade_town_level_2"));
                if (level == 3) {
                    upgradeList.add(CivSettings.getUpgradeById("upgrade_town_level_3"));
                }
                for (ConfigTownUpgrade upgrade1 : upgradeList) {
                    town.addUpgrade(upgrade1);
                }
                CivMessage.sendCiv(civ, CivSettings.localize.localizedString("var_processScroll_proceedTown", player.getDisplayName(), CivColor.LightGreenBold + level + CivColor.RESET, CivColor.GoldBold + town.getName() + CivColor.RESET));
            }
        }
        if (itemStack.getAmount() == 1) {
            player.getInventory().setItemInMainHand(null);
        } else {
            itemStack.setAmount(itemStack.getAmount() - 1);
        }
        player.updateInventory();
        resident.clearInteractiveMode();
    }
}

