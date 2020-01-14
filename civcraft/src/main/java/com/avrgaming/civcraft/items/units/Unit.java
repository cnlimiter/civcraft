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
package com.avrgaming.civcraft.items.units;

import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.config.ConfigMission;
import com.avrgaming.civcraft.config.ConfigUnit;
import com.avrgaming.civcraft.exception.CivException;
import com.avrgaming.civcraft.lorestorage.LoreCraftableMaterial;
import com.avrgaming.civcraft.lorestorage.LoreMaterial;
import com.avrgaming.civcraft.main.CivData;
import com.avrgaming.civcraft.util.ItemManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public abstract class Unit {

    public static Spy SPY_UNIT;
    public static Settler SETTLER_UNIT;
    public static ArrayList<MissionBook> SPY_MISSIONS = new ArrayList<>();
    public static MissionBook SPY_INVESTIGATE_TOWN;
    public static MissionBook SPY_STEAL_TREASURY;
    public static MissionBook SPY_SUBVERT_GOVERNMENT;
    public static MissionBook SPY_POISON_GRANARY;
    public static MissionBook SPY_PIRATE;
    public static MissionBook SPY_SABOTAGE;
    public static Archer ARCHER_ARTIFACT;
    public static Warrior WARRIOR_ARTIFACT;
    public static Berserker BERSERKER_ARTIFACT;
    public static BootsOfTravel BOOTSOFTRAVEL;
    public static RabbitFeet RABBITFEET_ARTIFACT;
    public static HeroesShield HEROESSHIELD_ARTIFACT;
    public static AssasinsStigma ASSASINSSTIGMA_ARTIFACT;
    public static MinersAmulet MINERSAMULET_ARTIFACT;
    public static Diver DIVER_ARTIFACT;
    public static CacheOfGluttons CACHEOFGLUTTONS_ARTIFACT;
    public static WitchTrick WITCHTRICK_ARTIFACT;
    public static Archer2 ARCHER2_ARTIFACT;
    public static Engineer ENGINEER_ARTIFACT;
    public static AdvancedTools ADVANCED_TOOLS_ARTIFACT;
    public static Invader Invader_ARTIFACT;
    public static Crossbowman CROSSBOWMAN_ARTIFACT;
    public static Swordsman SWORDSMAN_ARTIFACT;
    public static SpearMan SPEAR_ARTIFACT;
    public static Slinger SLINGER_ARTIFACT;
    public static Musketman MUSKETMAN_ARTIFACT;
    public static Knight KNIGHT_ARTIFACT;

    public static void init() {

        SPY_UNIT = new Spy("u_spy", CivSettings.units.get("u_spy"));

        for (ConfigMission mission : CivSettings.missions.values()) {
            if (mission.slot > 0) {
                MissionBook book = new MissionBook(mission.id, Spy.BOOK_ID, (short) 0);
                book.setName(mission.name);
                book.setupLore(book.getId());
                book.setParent(SPY_UNIT);
                book.setSocketSlot(mission.slot);
                SPY_UNIT.addMissionBook(book);
                SPY_MISSIONS.add(book);
            }
        }

        SETTLER_UNIT = new Settler("u_settler", CivSettings.units.get("u_settler"));
        ARCHER_ARTIFACT = new Archer("a_archer", CivSettings.units.get("a_archer"));
        WARRIOR_ARTIFACT = new Warrior("a_warrior", CivSettings.units.get("a_warrior"));
        BERSERKER_ARTIFACT = new Berserker("a_berserker", CivSettings.units.get("a_berserker"));
        BOOTSOFTRAVEL = new BootsOfTravel("a_bootsOfTravel", CivSettings.units.get("ax_bootsOfTravel"));
        RABBITFEET_ARTIFACT = new RabbitFeet("a_rabbitFeet", CivSettings.units.get("ax_rabbitFeet"));
        HEROESSHIELD_ARTIFACT = new HeroesShield("a_heroesShield", CivSettings.units.get("a_heroesShield"));
        ASSASINSSTIGMA_ARTIFACT = new AssasinsStigma("a_assasinsStigma", CivSettings.units.get("a_assasinsStigma"));
        MINERSAMULET_ARTIFACT = new MinersAmulet("a_minersAmulet", CivSettings.units.get("ax_minersAmulet"));
        DIVER_ARTIFACT = new Diver("a_diver", CivSettings.units.get("ax_diver"));
        CACHEOFGLUTTONS_ARTIFACT = new CacheOfGluttons("a_cacheOfGluttons", CivSettings.units.get("ax_cacheOfGluttons"));
        WITCHTRICK_ARTIFACT = new WitchTrick("a_witchTrick", CivSettings.units.get("ax_witchTrick"));
        ARCHER2_ARTIFACT = new Archer2("ax_archer2", CivSettings.units.get("ax_archer2"));
        ENGINEER_ARTIFACT = new Engineer("ax_engineer", CivSettings.units.get("ax_engineer"));
        ADVANCED_TOOLS_ARTIFACT = new AdvancedTools("ax_advanced_tools", CivSettings.units.get("ax_advanced_tools"));
        Invader_ARTIFACT = new Invader("ax_invader", CivSettings.units.get("ax_invader"));
        CROSSBOWMAN_ARTIFACT = new Crossbowman("a_crossbowman", CivSettings.units.get("a_crossbowman"));
        SWORDSMAN_ARTIFACT = new Swordsman("a_swordsman", CivSettings.units.get("a_swordsman"));
        SPEAR_ARTIFACT = new SpearMan("a_spearman", CivSettings.units.get("a_spearman"));
        SLINGER_ARTIFACT = new Slinger("a_slinger", CivSettings.units.get("a_slinger"));
        MUSKETMAN_ARTIFACT = new Musketman("a_musketman", CivSettings.units.get("a_musketman"));
        KNIGHT_ARTIFACT = new Knight("a_knight", CivSettings.units.get("a_knight"));
    }

    public Unit() {
    }


    public Unit(Inventory inv) throws CivException {

    }


    protected static boolean addItemNoStack(Inventory inv, ItemStack stack) {

        ItemStack[] contents = inv.getContents();
        for (int i = 0; i < contents.length; i++) {
            if (contents[i] == null) {
                contents[i] = stack;
                inv.setContents(contents);
                return true;
            }
        }

        return false;
    }

    public static ConfigUnit getPlayerUnit(Player player) {

        for (ItemStack stack : player.getInventory().getContents()) {
            if (stack == null) {
                continue;
            }

            LoreMaterial material = LoreMaterial.getMaterial(stack);
            if (material != null && (material instanceof UnitMaterial)) {

                if (!UnitMaterial.validateUnitUse(player, stack)) {
                    return null;
                }
                return ((UnitMaterial) material).getUnit();
            }
        }

        return null;
    }

    public static void removeUnit(Player player) {

        for (ItemStack stack : player.getInventory().getContents()) {
            if (stack != null) {
                LoreMaterial material = LoreMaterial.getMaterial(stack);
                if (material != null) {
                    if (material instanceof UnitMaterial) {
                        player.getInventory().remove(stack);
                        continue;
                    }

                    if (material instanceof UnitItemMaterial) {
                        player.getInventory().remove(stack);
                        continue;
                    }

                }
            }
        }
        player.updateInventory();
    }

    public static boolean isWearingFullLeather(Player player) {

        try {
            if (ItemManager.getId(player.getEquipment().getBoots()) != CivData.LEATHER_BOOTS) {
                return false;
            }

            if (ItemManager.getId(player.getEquipment().getChestplate()) != CivData.LEATHER_CHESTPLATE) {
                return false;
            }

            if (ItemManager.getId(player.getEquipment().getHelmet()) != CivData.LEATHER_HELMET) {
                return false;
            }

            if (ItemManager.getId(player.getEquipment().getLeggings()) != CivData.LEATHER_LEGGINGS) {
                return false;
            }

        } catch (NullPointerException e) {
            return false;
        }
        return true;
    }

    public static boolean isWearingFullComposite(Player player) {

        for (ItemStack stack : player.getInventory().getArmorContents()) {

            LoreCraftableMaterial craftMat = LoreCraftableMaterial.getCraftMaterial(stack);
            if (craftMat == null) {
                return false;
            }

            if ((!craftMat.getConfigId().contains("mat_composite_leather"))) {
                return false;
            }
        }
        return true;
    }

    public static boolean isWearingFullHardened(Player player) {

        for (ItemStack stack : player.getInventory().getArmorContents()) {

            LoreCraftableMaterial craftMat = LoreCraftableMaterial.getCraftMaterial(stack);
            if (craftMat == null) {
                return false;
            }

            if ((!craftMat.getConfigId().contains("mat_hardened_leather"))) {
                return false;
            }
        }
        return true;
    }

    public static boolean isWearingFullRefined(Player player) {

        for (ItemStack stack : player.getInventory().getArmorContents()) {

            LoreCraftableMaterial craftMat = LoreCraftableMaterial.getCraftMaterial(stack);
            if (craftMat == null) {
                return false;
            }

            if ((!craftMat.getConfigId().contains("mat_refined_leather"))) {
                return false;
            }

        }
        return true;
    }

    public static boolean isWearingFullBasicLeather(Player player) {

        for (ItemStack stack : player.getInventory().getArmorContents()) {

            LoreCraftableMaterial craftMat = LoreCraftableMaterial.getCraftMaterial(stack);
            if (craftMat == null) {
                return false;
            }

            if ((!craftMat.getConfigId().contains("mat_leather_"))) {
                return false;
            }


        }
        return true;
    }

    public static boolean isWearingAnyMetal(Player player) {
        return isWearingAnyChain(player) || isWearingAnyGold(player) || isWearingAnyIron(player) || isWearingAnyDiamond(player);
    }

    public static boolean isWearingAnyChain(Player player) {

        if (player.getEquipment().getBoots() != null) {
            if (player.getEquipment().getBoots().getType().equals(Material.CHAINMAIL_BOOTS)) {
                return true;
            }
        }

        if (player.getEquipment().getChestplate() != null) {
            if (player.getEquipment().getChestplate().getType().equals(Material.CHAINMAIL_CHESTPLATE)) {
                return true;
            }
        }

        if (player.getEquipment().getHelmet() != null) {
            if (player.getEquipment().getHelmet().getType().equals(Material.CHAINMAIL_HELMET)) {
                return true;
            }
        }

        if (player.getEquipment().getLeggings() != null) {
            if (player.getEquipment().getLeggings().getType().equals(Material.CHAINMAIL_LEGGINGS)) {
                return true;
            }
        }

        return false;
    }


    public static boolean isWearingAnyGold(Player player) {

        if (player.getEquipment().getBoots() != null) {
            if (player.getEquipment().getBoots().getType().equals(Material.GOLD_BOOTS)) {
                return true;
            }
        }

        if (player.getEquipment().getChestplate() != null) {
            if (player.getEquipment().getChestplate().getType().equals(Material.GOLD_CHESTPLATE)) {
                return true;
            }
        }

        if (player.getEquipment().getHelmet() != null) {
            if (player.getEquipment().getHelmet().getType().equals(Material.GOLD_HELMET)) {
                return true;
            }
        }

        if (player.getEquipment().getLeggings() != null) {
            if (player.getEquipment().getLeggings().getType().equals(Material.GOLD_LEGGINGS)) {
                return true;
            }
        }

        return false;
    }


    public static boolean isWearingAnyIron(Player player) {

        if (player.getEquipment().getBoots() != null) {
            if (ItemManager.getId(player.getEquipment().getBoots()) == CivData.IRON_BOOTS) {
                return true;
            }
        }

        if (player.getEquipment().getChestplate() != null) {
            if (ItemManager.getId(player.getEquipment().getChestplate()) == CivData.IRON_CHESTPLATE) {
                return true;
            }
        }

        if (player.getEquipment().getHelmet() != null) {
            if (ItemManager.getId(player.getEquipment().getHelmet()) == CivData.IRON_HELMET) {
                return true;
            }
        }

        if (player.getEquipment().getLeggings() != null) {
            if (ItemManager.getId(player.getEquipment().getLeggings()) == CivData.IRON_LEGGINGS) {
                return true;
            }
        }

        return false;
    }

    public static boolean isWearingAnyDiamond(Player player) {

        if (player.getEquipment().getBoots() != null) {
            if (ItemManager.getId(player.getEquipment().getBoots()) == CivData.DIAMOND_BOOTS) {
                return true;
            }
        }

        if (player.getEquipment().getChestplate() != null) {
            if (ItemManager.getId(player.getEquipment().getChestplate()) == CivData.DIAMOND_CHESTPLATE) {
                return true;
            }
        }

        if (player.getEquipment().getHelmet() != null) {
            if (ItemManager.getId(player.getEquipment().getHelmet()) == CivData.DIAMOND_HELMET) {
                return true;
            }
        }

        if (player.getEquipment().getLeggings() != null) {
            if (ItemManager.getId(player.getEquipment().getLeggings()) == CivData.DIAMOND_LEGGINGS) {
                return true;
            }
        }

        return false;
    }

    public static boolean canTakeUnit(final Player player, final String id) {
        int unitCount = 0;
        for (final ItemStack stack : player.getInventory().getContents()) {
            if (stack != null) {
                final LoreMaterial material = LoreMaterial.getMaterial(stack);
                if (material != null && material instanceof UnitMaterial) {
                    if (((UnitMaterial) material).getUnit().id.equalsIgnoreCase(id)) {
                        return false;
                    }
                    if (UnitMaterial.validateUnitUse(player, stack)) {
                        unitCount += stack.getAmount();
                    }
                }
            }
        }
        return unitCount < 3;
    }

    public static String getUnitString(final Player player) {
        int unitCount = 0;
        String units = "";
        for (final ItemStack stack : player.getInventory().getContents()) {
            if (stack != null) {
                final LoreMaterial material = LoreMaterial.getMaterial(stack);
                if (material != null && material instanceof UnitMaterial) {
                    if (UnitMaterial.validateUnitUse(player, stack)) {
                        units += ((UnitMaterial) material).getUnit().name;
                        if (unitCount != 2) {
                            units += ", ";
                        }
                        ++unitCount;
                    }
                }
            }
        }
        return units;
    }

    public static String getUnitStringIds(final Player player) {
        int unitCount = 0;
        String units = "";
        for (final ItemStack stack : player.getInventory().getContents()) {
            if (stack != null) {
                final LoreMaterial material = LoreMaterial.getMaterial(stack);
                if (material != null && material instanceof UnitMaterial) {
                    if (UnitMaterial.validateUnitUse(player, stack)) {
                        units += ((UnitMaterial) material).getUnit().id;
                        if (unitCount != 2) {
                            units += ", ";
                        }
                        ++unitCount;
                    }
                }
            }
        }
        return units;
    }

    public static boolean canTakeUnitCloseEvent(final Player player) {
        int unitCount = 0;
        for (final ItemStack stack : player.getInventory().getContents()) {
            if (stack != null) {
                final LoreMaterial material = LoreMaterial.getMaterial(stack);
                if (material != null && material instanceof UnitMaterial) {
                    if (UnitMaterial.validateUnitUse(player, stack)) {
                        unitCount += stack.getAmount();
                    }
                }
            }
        }
        return unitCount < 4;
    }

    public static boolean isWearingFullHell(final Player player) {
        ItemStack[] armorContents;
        for (int length = (armorContents = player.getInventory().getArmorContents()).length, i = 0; i < length; ++i) {
            final ItemStack stack = armorContents[i];
            final LoreCraftableMaterial craftMat = LoreCraftableMaterial.getCraftMaterial(stack);
            if (craftMat == null) {
                return false;
            }
            if (!craftMat.getConfigId().contains("mat_hell_")) {
                return false;
            }
        }
        return true;
    }
}
