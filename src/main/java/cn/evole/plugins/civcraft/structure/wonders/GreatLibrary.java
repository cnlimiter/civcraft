/**
 * [2023] Cnlimiter LLC
 * All Rights Reserved.
 * Name: civcraft
 * Author: cnlimiter
 * UpdateTime: 2023/10/14 16:15
 * Description:
 * License: ARR
 */
package cn.evole.plugins.civcraft.structure.wonders;

import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.config.ConfigEnchant;
import cn.evole.plugins.civcraft.exception.CivException;
import cn.evole.plugins.civcraft.loreenhancements.LoreEnhancement;
import cn.evole.plugins.civcraft.lorestorage.LoreMaterial;
import cn.evole.plugins.civcraft.main.CivData;
import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.main.CivMessage;
import cn.evole.plugins.civcraft.object.Resident;
import cn.evole.plugins.civcraft.object.StructureSign;
import cn.evole.plugins.civcraft.object.Town;
import cn.evole.plugins.civcraft.util.CivColor;
import cn.evole.plugins.civcraft.util.ItemManager;
import org.bukkit.Location;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.sql.ResultSet;
import java.sql.SQLException;

public class GreatLibrary extends Wonder {

    public GreatLibrary(Location center, String id, Town town)
            throws CivException {
        super(center, id, town);
    }

    public GreatLibrary(ResultSet rs) throws SQLException, CivException {
        super(rs);
    }

    @Override
    public void onLoad() {
        if (this.isActive()) {
            addBuffs();
        }
    }

    @Override
    public void onComplete() {
        addBuffs();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        removeBuffs();
    }

    @Override
    protected void removeBuffs() {
        this.removeBuffFromCiv(this.getCiv(), "buff_greatlibrary_extra_beakers");
        this.removeBuffFromTown(this.getTown(), "buff_greatlibrary_double_tax_beakers");
    }

    @Override
    protected void addBuffs() {
        this.addBuffToCiv(this.getCiv(), "buff_greatlibrary_extra_beakers");
        this.addBuffToTown(this.getTown(), "buff_greatlibrary_double_tax_beakers");
    }


    @Override
    public void updateSignText() {

        for (StructureSign sign : getSigns()) {
            ConfigEnchant enchant;
            switch (sign.getAction().toLowerCase()) {
                case "0":
                    enchant = CivSettings.enchants.get("ench_fire_aspect");
                    sign.setText(enchant.name + "\n\n" + CivColor.LightGreen + enchant.cost + " " + CivSettings.CURRENCY_NAME);
                    break;
                case "1":
                    enchant = CivSettings.enchants.get("ench_fire_protection");
                    sign.setText(enchant.name + "\n\n" + CivColor.LightGreen + enchant.cost + " " + CivSettings.CURRENCY_NAME);
                    break;
                case "2":
                    enchant = CivSettings.enchants.get("ench_flame");
                    sign.setText(enchant.name + "\n\n" + CivColor.LightGreen + enchant.cost + " " + CivSettings.CURRENCY_NAME);
                    break;
                case "3":
                    enchant = CivSettings.enchants.get("ench_punchout");
                    sign.setText(enchant.name + "\n\n" + CivColor.LightGreen + enchant.cost + " " + CivSettings.CURRENCY_NAME);
                    break;
            }

            sign.update();
        }
    }

    @Override
    public void processSignAction(Player player, StructureSign sign, PlayerInteractEvent event) {
        //int special_id = Integer.valueOf(sign.getAction());
        Resident resident = CivGlobal.getResident(player);

        if (resident == null) {
            return;
        }

        if (resident.getCiv() != this.getCiv()) {
            CivMessage.sendError(player, CivSettings.localize.localizedString("var_greatLibrary_nonMember", this.getCiv().getName()));
            return;
        }

        ItemStack hand = player.getInventory().getItemInMainHand();
        ConfigEnchant configEnchant;

        switch (sign.getAction()) {
            case "0": /* fire aspect */
                if (!Enchantment.FIRE_ASPECT.canEnchantItem(hand)) {
                    CivMessage.sendError(player, CivSettings.localize.localizedString("library_enchant_cannotEnchant"));
                    return;
                }

                configEnchant = CivSettings.enchants.get("ench_fire_aspect");
                if (!resident.getTreasury().hasEnough(configEnchant.cost)) {
                    CivMessage.send(player, CivColor.Rose + CivSettings.localize.localizedString("var_library_enchant_cannotAfford", configEnchant.cost, CivSettings.CURRENCY_NAME));
                    return;
                }

                resident.getTreasury().withdraw(configEnchant.cost);
                hand.addEnchantment(Enchantment.FIRE_ASPECT, 2);
                break;
            case "1": /* fire protection */
                if (!Enchantment.PROTECTION_FIRE.canEnchantItem(hand)) {
                    CivMessage.sendError(player, CivSettings.localize.localizedString("library_enchant_cannotEnchant"));
                    return;
                }

                configEnchant = CivSettings.enchants.get("ench_fire_protection");
                if (!resident.getTreasury().hasEnough(configEnchant.cost)) {
                    CivMessage.send(player, CivColor.Rose + CivSettings.localize.localizedString("var_library_enchant_cannotAfford", configEnchant.cost, CivSettings.CURRENCY_NAME));
                    return;
                }

                resident.getTreasury().withdraw(configEnchant.cost);
                hand.addEnchantment(Enchantment.PROTECTION_FIRE, 2);
                break;
            case "2": /* flame */
                if (!Enchantment.ARROW_FIRE.canEnchantItem(hand)) {
                    CivMessage.sendError(player, CivSettings.localize.localizedString("library_enchant_cannotEnchant"));
                    return;
                }

                configEnchant = CivSettings.enchants.get("ench_flame");
                if (!resident.getTreasury().hasEnough(configEnchant.cost)) {
                    CivMessage.send(player, CivColor.Rose + CivSettings.localize.localizedString("var_library_enchant_cannotAfford", configEnchant.cost, CivSettings.CURRENCY_NAME));
                    return;
                }

                resident.getTreasury().withdraw(configEnchant.cost);
                hand.addEnchantment(Enchantment.ARROW_FIRE, 1);
                break;
            case "3":
                switch (ItemManager.getId(hand)) {
                    case CivData.WOOD_PICKAXE:
                    case CivData.STONE_PICKAXE:
                    case CivData.IRON_PICKAXE:
                    case CivData.DIAMOND_PICKAXE:
                    case CivData.GOLD_PICKAXE:
                        configEnchant = CivSettings.enchants.get("ench_punchout");

                        if (!LoreMaterial.isCustom(hand)) {
                            CivMessage.sendError(player, CivSettings.localize.localizedString("library_enchant_nonEnchantable"));
                            return;
                        }

                        if (LoreMaterial.hasEnhancement(hand, configEnchant.enchant_id)) {
                            CivMessage.sendError(player, CivSettings.localize.localizedString("library_enchant_hasEnchantment"));
                            return;
                        }

                        if (!resident.getTreasury().hasEnough(configEnchant.cost)) {
                            CivMessage.send(player, CivColor.Rose + CivSettings.localize.localizedString("var_library_enchant_cannotAfford", configEnchant.cost, CivSettings.CURRENCY_NAME));
                            return;
                        }

                        resident.getTreasury().withdraw(configEnchant.cost);
                        ItemStack newItem = LoreMaterial.addEnhancement(hand, LoreEnhancement.enhancements.get(configEnchant.enchant_id));
                        player.getInventory().setItemInMainHand(newItem);
                        break;
                    default:
                        CivMessage.sendError(player, CivSettings.localize.localizedString("library_enchant_cannotEnchant"));
                        return;
                }
                break;
            default:
                return;
        }

        CivMessage.sendSuccess(player, CivSettings.localize.localizedString("library_enchantment_success"));
    }

}
