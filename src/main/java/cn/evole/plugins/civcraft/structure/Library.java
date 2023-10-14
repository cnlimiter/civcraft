/**
 * [2023] Cnlimiter LLC
 * All Rights Reserved.
 * Name: civcraft
 * Author: cnlimiter
 * UpdateTime: 2023/10/14 16:15
 * Description:
 * License: ARR
 */
package cn.evole.plugins.civcraft.structure;

import cn.evole.plugins.civcraft.components.AttributeBiome;
import cn.evole.plugins.civcraft.components.NonMemberFeeComponent;
import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.exception.CivException;
import cn.evole.plugins.civcraft.lorestorage.LoreMaterial;
import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.main.CivLog;
import cn.evole.plugins.civcraft.main.CivMessage;
import cn.evole.plugins.civcraft.object.LibraryEnchantment;
import cn.evole.plugins.civcraft.object.Resident;
import cn.evole.plugins.civcraft.object.StructureSign;
import cn.evole.plugins.civcraft.object.Town;
import cn.evole.plugins.civcraft.util.CivColor;
import org.bukkit.Location;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class Library extends Structure {

    public AttributeBiome cultureBeakers;
    ArrayList<LibraryEnchantment> enchantments = new ArrayList<LibraryEnchantment>();
    private int level;
    private NonMemberFeeComponent nonMemberFeeComponent;

    protected Library(Location center, String id, Town town)
            throws CivException {
        super(center, id, town);
        nonMemberFeeComponent = new NonMemberFeeComponent(this);
        nonMemberFeeComponent.onSave();
        setLevel(town.saved_library_level);
    }

    public Library(ResultSet rs) throws SQLException, CivException {
        super(rs);
        nonMemberFeeComponent = new NonMemberFeeComponent(this);
        nonMemberFeeComponent.onLoad();
    }

    public static Enchantment getEnchantFromString(String name) {

        // Armor Enchantments
        if (name.equalsIgnoreCase("protection")) {
            return Enchantment.PROTECTION_ENVIRONMENTAL;
        }
        if (name.equalsIgnoreCase("fire_protection")) {
            return Enchantment.PROTECTION_FIRE;
        }
        if (name.equalsIgnoreCase("feather_falling")) {
            return Enchantment.PROTECTION_FALL;
        }
        if (name.equalsIgnoreCase("blast_protection")) {
            return Enchantment.PROTECTION_EXPLOSIONS;
        }
        if (name.equalsIgnoreCase("projectile_protection")) {
            return Enchantment.PROTECTION_PROJECTILE;
        }
        if (name.equalsIgnoreCase("respiration")) {
            return Enchantment.OXYGEN;
        }
        if (name.equalsIgnoreCase("aqua_affinity")) {
            return Enchantment.WATER_WORKER;
        }

        // Sword Enchantments
        if (name.equalsIgnoreCase("sharpness")) {
            return Enchantment.DAMAGE_ALL;
        }
        if (name.equalsIgnoreCase("smite")) {
            return Enchantment.DAMAGE_UNDEAD;
        }
        if (name.equalsIgnoreCase("bane_of_arthropods")) {
            return Enchantment.DAMAGE_ARTHROPODS;
        }
        if (name.equalsIgnoreCase("knockback")) {
            return Enchantment.KNOCKBACK;
        }
        if (name.equalsIgnoreCase("fire_aspect")) {
            return Enchantment.FIRE_ASPECT;
        }
        if (name.equalsIgnoreCase("looting")) {
            return Enchantment.LOOT_BONUS_MOBS;
        }

        // Tool Enchantments
        if (name.equalsIgnoreCase("efficiency")) {
            return Enchantment.DIG_SPEED;
        }
        if (name.equalsIgnoreCase("silk_touch")) {
            return Enchantment.SILK_TOUCH;
        }
        if (name.equalsIgnoreCase("unbreaking")) {
            return Enchantment.DURABILITY;
        }
        if (name.equalsIgnoreCase("fortune")) {
            return Enchantment.LOOT_BONUS_BLOCKS;
        }

        // Bow Enchantments
        if (name.equalsIgnoreCase("power")) {
            return Enchantment.ARROW_DAMAGE;
        }
        if (name.equalsIgnoreCase("punch")) {
            return Enchantment.ARROW_KNOCKBACK;
        }
        if (name.equalsIgnoreCase("flame")) {
            return Enchantment.ARROW_FIRE;
        }
        if (name.equalsIgnoreCase("infinity")) {
            return Enchantment.ARROW_INFINITE;
        }

        return null;

    }

    public double getNonResidentFee() {
        return this.nonMemberFeeComponent.getFeeRate();
    }

    public void setNonResidentFee(double nonResidentFee) {
        this.nonMemberFeeComponent.setFeeRate(nonResidentFee);
    }

    private String getNonResidentFeeString() {
        return "Fee: " + ((int) (getNonResidentFee() * 100) + "%").toString();
    }

    @Override
    public void loadSettings() {
        super.loadSettings();
    }

    public int getLevel() {
        return level;
    }


    public void setLevel(int level) {
        this.level = level;
    }

    private StructureSign getSignFromSpecialId(int special_id) {
        for (StructureSign sign : getSigns()) {
            int id = Integer.valueOf(sign.getAction());
            if (id == special_id) {
                return sign;
            }
        }
        return null;
    }

    @Override
    public void updateSignText() {

        int count = 0;

        for (LibraryEnchantment enchant : this.enchantments) {
            StructureSign sign = getSignFromSpecialId(count);
            if (sign == null) {
                CivLog.error("sign from special id was null, id:" + count);
                return;
            }
            double price = enchant.price;

            if (this.getTown().hasStructure("s_shopingcenter")) {
                price /= 2.0;
            }
            sign.setText(enchant.displayName + "\n" +
                    "Level " + enchant.level + "\n" +
                    getNonResidentFeeString() + "\n" +
                    "For " + price);
            sign.update();
            count++;
        }

        for (; count < getSigns().size(); count++) {
            StructureSign sign = getSignFromSpecialId(count);
            sign.setText("Library Slot\nEmpty");
            sign.update();
        }
    }

    public void validateEnchantment(ItemStack item, LibraryEnchantment ench) throws CivException {
        if (ench.enchant != null) {

            if (!ench.enchant.canEnchantItem(item)) {
                throw new CivException(CivSettings.localize.localizedString("library_enchant_cannotEnchant"));
            }

            if (item.containsEnchantment(ench.enchant) && item.getEnchantmentLevel(ench.enchant) >= ench.level) {
                throw new CivException(CivSettings.localize.localizedString("library_enchant_hasEnchant"));
            }


        } else {
            if (!ench.enhancement.canEnchantItem(item)) {
                throw new CivException(CivSettings.localize.localizedString("library_enchant_cannotEnchant"));
            }

            if (ench.enhancement.hasEnchantment(item)) {
                throw new CivException(CivSettings.localize.localizedString("library_enchant_hasEnchantment"));
            }
        }
    }

    public ItemStack addEnchantment(ItemStack item, LibraryEnchantment ench) {
        if (ench.enchant != null) {
            item.addUnsafeEnchantment(ench.enchant, ench.level);
        } else {
            item = LoreMaterial.addEnhancement(item, ench.enhancement);
        }
        return item;
    }

    public void add_enchantment_to_tool(Player player, StructureSign sign, PlayerInteractEvent event) throws CivException {
        int special_id = Integer.valueOf(sign.getAction());

        if (!event.hasItem()) {
            CivMessage.send(player, CivColor.Rose + CivSettings.localize.localizedString("library_enchant_itemNotInHand"));
            return;
        }
        ItemStack item = event.getItem();

        if (special_id >= this.enchantments.size()) {
            throw new CivException(CivSettings.localize.localizedString("library_enchant_notReady"));
        }


        LibraryEnchantment ench = this.enchantments.get(special_id);
        this.validateEnchantment(item, ench);

        int payToTown = (int) Math.round(ench.price * getNonResidentFee());
        Resident resident;

        resident = CivGlobal.getResident(player.getName());
        Town t = resident.getTown();
        if (t == this.getTown()) {
            // Pay no taxes! You're a member.
            payToTown = 0;
        }

        // Determine if resident can pay.
        if (!resident.getTreasury().hasEnough(ench.price + payToTown)) {
            CivMessage.send(player, CivColor.Rose + CivSettings.localize.localizedString("var_library_enchant_cannotAfford", ench.price + payToTown, CivSettings.CURRENCY_NAME));
            return;
        }

        // Take money, give to server, TEH SERVER HUNGERS ohmnom nom
        resident.getTreasury().withdraw(ench.price);

        // Send money to town for non-resident fee
        if (payToTown != 0) {
            getTown().depositDirect(payToTown);
            CivMessage.send(player, CivColor.Yellow + " " + CivSettings.localize.localizedString("var_taxes_paid", payToTown, CivSettings.CURRENCY_NAME));
        }

        // Successful payment, process enchantment.
        ItemStack newStack = this.addEnchantment(item, ench);
        player.getInventory().setItemInMainHand(newStack);
        CivMessage.send(player, CivColor.LightGreen + CivSettings.localize.localizedString("var_library_enchantment_added", ench.displayName));
    }

    @Override
    public void processSignAction(Player player, StructureSign sign, PlayerInteractEvent event) {
        try {
            add_enchantment_to_tool(player, sign, event);
        } catch (CivException e) {
            CivMessage.send(player, CivColor.Rose + e.getMessage());
        }
    }

    @Override
    public String getDynmapDescription() {
        String out = "<u><b>" + this.getDisplayName() + "</u></b><br/>";

        if (this.enchantments.size() == 0) {
            out += CivSettings.localize.localizedString("library_dynmap_nothingStocked");
        } else {
            for (LibraryEnchantment mat : this.enchantments) {
                out += CivSettings.localize.localizedString("var_library_dynmap_item", mat.displayName, mat.price) + "<br/>";
            }
        }
        return out;
    }


    public ArrayList<LibraryEnchantment> getEnchants() {
        return enchantments;
    }


    public void addEnchant(LibraryEnchantment enchant) throws CivException {
        if (enchantments.size() >= 4) {
            throw new CivException(CivSettings.localize.localizedString("library_full"));
        }
        enchantments.add(enchant);
    }

    @Override
    public String getMarkerIconName() {
        return "bookshelf";
    }

    public void reset() {
        this.enchantments.clear();
        this.updateSignText();
    }

}
