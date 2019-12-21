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
package com.avrgaming.civcraft.structure;

import com.avrgaming.civcraft.components.NonMemberFeeComponent;
import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.config.ConfigBankLevel;
import com.avrgaming.civcraft.exception.CivException;
import com.avrgaming.civcraft.lorestorage.LoreMaterial;
import com.avrgaming.civcraft.main.CivData;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.object.Buff;
import com.avrgaming.civcraft.object.Resident;
import com.avrgaming.civcraft.object.StructureSign;
import com.avrgaming.civcraft.object.Town;
import com.avrgaming.civcraft.util.BlockCoord;
import com.avrgaming.civcraft.util.CivColor;
import com.avrgaming.civcraft.util.SimpleBlock;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;

public class Bank extends Structure {

    private int level = 1;
    private double interestRate = 0;

    private NonMemberFeeComponent nonMemberFeeComponent;

    //	private static final int EMERALD_SIGN = 3;
    private static final int IRON_SIGN = 0;
    private static final int GOLD_SIGN = 1;
    private static final int DIAMOND_SIGN = 2;
    private static final int EMERALD_SIGN = 3;
    private static final int IRON_BLOCK_SIGN = 4;
    private static final int GOLD_BLOCK_SIGN = 5;
    private static final int DIAMOND_BLOCK_SIGN = 6;
    private static final int EMERALD_BLOCK_SIGN = 7;

    protected Bank(Location center, String id, Town town) throws CivException {
        super(center, id, town);
        nonMemberFeeComponent = new NonMemberFeeComponent(this);
        nonMemberFeeComponent.onSave();
        setLevel(town.saved_bank_level);
        setInterestRate(town.saved_bank_interest_amount);
    }

    public Bank(ResultSet rs) throws SQLException, CivException {
        super(rs);
        nonMemberFeeComponent = new NonMemberFeeComponent(this);
        nonMemberFeeComponent.onLoad();
    }

    public double getBankExchangeRate() {
        double exchange_rate = 0.4;
        ConfigBankLevel cbl = CivSettings.bankLevels.get(this.level);
        if (cbl != null) {
            exchange_rate = cbl.exchange_rate;
        }
        double rate = 1;
        double addtional = rate * this.getTown().getBuffManager().getEffectiveDouble(Buff.BARTER);
        rate += addtional;
        if (rate > 1) {
            exchange_rate *= rate;
        }
        if (this.getCiv().getCapitol() != null && this.getCiv().getCapitol().getBuffManager().hasBuff("level4_extraBankTown")) {
            exchange_rate *= this.getCiv().getCapitol().getBuffManager().getEffectiveDouble("level4_extraBankTown");
        }
        if (this.getCiv().getStockExchangeLevel() >= 3) {
            exchange_rate *= 1.25;
        }
        return exchange_rate;
    }

    @Override
    public void onBonusGoodieUpdate() {
        this.updateSignText();
    }

    private String getExchangeRateString() {
        return ((int) (getBankExchangeRate() * 100) + "%");
    }

    private String getNonResidentFeeString() {
        return CivSettings.localize.localizedString("bank_sign_fee") + " " + ((int) (this.nonMemberFeeComponent.getFeeRate() * 100) + "%").toString();
    }

    private String getSignItemPrice(int signId) {
        double itemPrice;
        if (signId == IRON_SIGN) {
            itemPrice = CivSettings.iron_rate;
        } else if (signId == IRON_BLOCK_SIGN) {
            itemPrice = CivSettings.iron_rate * 9;
        } else if (signId == GOLD_SIGN) {
            itemPrice = CivSettings.gold_rate;
        } else if (signId == GOLD_BLOCK_SIGN) {
            itemPrice = CivSettings.gold_rate * 9;
        } else if (signId == DIAMOND_SIGN) {
            itemPrice = CivSettings.diamond_rate;
        } else if (signId == DIAMOND_BLOCK_SIGN) {
            itemPrice = CivSettings.diamond_rate * 9;
        } else if (signId == EMERALD_SIGN) {
            itemPrice = CivSettings.emerald_rate;
        } else {
            itemPrice = CivSettings.emerald_rate * 9;
        }


        String out = "1 = ";
        out += (int) (itemPrice * getBankExchangeRate());
        out += " Coins";
        return out;
    }

    public void exchange_for_coins(Resident resident, int itemId, double coins) throws CivException {
        double exchange_rate;
        String itemName;
        Player player = CivGlobal.getPlayer(resident);

        Inventory inv = player.getInventory();

        ItemStack stack = player.getInventory().getItemInMainHand();
        int count = 0;

        if (itemId == CivData.IRON_INGOT) {
            itemName = CivSettings.localize.localizedString("bank_itemName_iron");
            if (stack.getType().equals(Material.IRON_INGOT) || stack.getType().equals(Material.IRON_BLOCK)) {
                count = stack.getAmount();
                inv.removeItem(stack);
            }
        } else if (itemId == CivData.GOLD_INGOT) {
            itemName = CivSettings.localize.localizedString("bank_itemName_gold");
            if (stack.getType().equals(Material.GOLD_INGOT) || stack.getType().equals(Material.GOLD_BLOCK)) {
                count = stack.getAmount();
                inv.removeItem(stack);
            }
        } else if (itemId == CivData.DIAMOND) {
            itemName = CivSettings.localize.localizedString("bank_itemName_diamond");
            if (stack.getType().equals(Material.DIAMOND) || stack.getType().equals(Material.DIAMOND_BLOCK)) {
                count = stack.getAmount();
                inv.removeItem(stack);
            }
        } else if (itemId == CivData.EMERALD) {
            itemName = CivSettings.localize.localizedString("bank_itemName_emerald");
            if (stack.getType().equals(Material.EMERALD) || stack.getType().equals(Material.EMERALD_BLOCK)) {
                count = stack.getAmount();
                inv.removeItem(stack);
            }
        } else {
            itemName = CivSettings.localize.localizedString("bank_itemName_stuff");
        }
        exchange_rate = getBankExchangeRate();
        if (count == 0) {
            throw new CivException(CivSettings.localize.localizedString("var_bank_notEnoughInHand", itemName));
        }

        Town usersTown = resident.getTown();

        // Resident is in his own town.
        if (usersTown == this.getTown()) {
            DecimalFormat df = new DecimalFormat();
            resident.getTreasury().deposit(((int) ((coins * count) * exchange_rate)));
            CivMessage.send(player, CivColor.LightGreen +
                    CivSettings.localize.localizedString("var_bank_exchanged", count, itemName, (df.format((coins * count) * exchange_rate)), CivSettings.CURRENCY_NAME));
            return;
        }

        // non-resident must pay the town's non-resident tax
        double giveToPlayer = (int) ((coins * count) * exchange_rate);
        double giveToTown = (int) giveToPlayer * this.getNonResidentFee();
        giveToPlayer -= giveToTown;

        giveToTown = Math.round(giveToTown);
        giveToPlayer = Math.round(giveToPlayer);

        this.getTown().depositDirect(giveToTown);
        resident.getTreasury().deposit(giveToPlayer);

        CivMessage.send(player, CivColor.LightGreen + CivSettings.localize.localizedString("var_bank_exchanged", count, itemName, giveToPlayer, CivSettings.CURRENCY_NAME));
        CivMessage.send(player, CivColor.Yellow + " " + CivSettings.localize.localizedString("var_taxes_paid", giveToTown, CivSettings.CURRENCY_NAME));

    }

    @Override
    public void processSignAction(Player player, StructureSign sign, PlayerInteractEvent event) {
        //int special_id = Integer.valueOf(sign.getAction());
        Resident resident = CivGlobal.getResident(player);

        if (resident == null) {
            return;
        }

        try {
            ItemStack itemStack = player.getInventory().getItemInMainHand();
            if (LoreMaterial.isCustom(itemStack) || CivGlobal.isBonusGoodie(itemStack))
                throw new CivException(CivSettings.localize.localizedString("bank_invalidItem"));

            switch (sign.getAction()) {
                case "iron":
                    exchange_for_coins(resident, CivData.IRON_INGOT, CivSettings.iron_rate);
                    break;
                case "gold":
                    exchange_for_coins(resident, CivData.GOLD_INGOT, CivSettings.gold_rate);
                    break;
                case "diamond":
                    exchange_for_coins(resident, CivData.DIAMOND, CivSettings.diamond_rate);
                    break;
                case "emerald":
                    exchange_for_coins(resident, CivData.EMERALD, CivSettings.emerald_rate);
                    break;
            }
        } catch (CivException e) {
            CivMessage.send(player, CivColor.Rose + e.getMessage());
        }
    }

    @Override
    public void updateSignText() {
        for (StructureSign sign : getSigns()) {

            switch (sign.getAction().toLowerCase()) {
                case "iron":
                    sign.setText(CivSettings.localize.localizedString("bank_itemName_iron") + "\n" +
                            "At " + getExchangeRateString() + "\n" +
                            getSignItemPrice(IRON_SIGN) + "\n" +
                            getNonResidentFeeString());
                    break;
                case "gold":
                    sign.setText(CivSettings.localize.localizedString("bank_itemName_gold") + "\n" +
                            "At " + getExchangeRateString() + "\n" +
                            getSignItemPrice(GOLD_SIGN) + "\n" +
                            getNonResidentFeeString());
                    break;
                case "diamond":
                    sign.setText(CivSettings.localize.localizedString("bank_itemName_diamond") + "\n" +
                            "At " + getExchangeRateString() + "\n" +
                            getSignItemPrice(DIAMOND_SIGN) + "\n" +
                            getNonResidentFeeString());
                    break;
                case "emerald":
                    sign.setText(CivSettings.localize.localizedString("bank_itemName_emerald") + "\n" +
                            "At " + getExchangeRateString() + "\n" +
                            getSignItemPrice(EMERALD_SIGN) + "\n" +
                            getNonResidentFeeString());
                    break;
            }


            sign.update();
        }
    }

    @Override
    public String getDynmapDescription() {
        String out = "<u><b>" + CivSettings.localize.localizedString("bank_dynmapName") + "</u></b><br/>";
        out += CivSettings.localize.localizedString("Level") + " " + this.level;
        return out;
    }

    @Override
    public String getMarkerIconName() {
        return "bank";
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public double getNonResidentFee() {
        return this.nonMemberFeeComponent.getFeeRate();
    }

    public void setNonResidentFee(double nonResidentFee) {
        this.nonMemberFeeComponent.setFeeRate(nonResidentFee);
    }

    public double getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(double interestRate) {
        this.interestRate = interestRate;
    }

    @Override
    public void onLoad() {
        /* Process the interest rate. */
        if (interestRate == 0.0) {
            this.getTown().getTreasury().setPrincipalAmount(0);
            return;
        }

        /* Update the principal with the new value. */
        this.getTown().getTreasury().setPrincipalAmount(this.getTown().getTreasury().getBalance());
    }

    @Override
    public void onDailyEvent() {

        /* Process the interest rate. */
        double effectiveInterestRate = interestRate;
        if (effectiveInterestRate == 0.0) {
            this.getTown().getTreasury().setPrincipalAmount(0);
            return;
        }

        double principal = this.getTown().getTreasury().getPrincipalAmount();

        if (this.getTown().getBuffManager().hasBuff("buff_greed")) {
            double increase = this.getTown().getBuffManager().getEffectiveDouble("buff_greed");
            effectiveInterestRate += increase;
            CivMessage.sendTown(this.getTown(), CivColor.LightGray + CivSettings.localize.localizedString("bank_greed"));
        }

        if (this.getCiv().getCapitol() != null && this.getCiv().getCapitol().getBuffManager().hasBuff("level4_extraBankTown")) {
            effectiveInterestRate *= 2.0;
        }
        double newCoins = principal * effectiveInterestRate;

        //Dont allow fractional coins.
        newCoins = Math.floor(newCoins);

        if (newCoins != 0) {
            CivMessage.sendTown(this.getTown(), CivColor.LightGreen + CivSettings.localize.localizedString("var_bank_interestMsg1", newCoins, CivSettings.CURRENCY_NAME, principal));
            this.getTown().getTreasury().deposit(newCoins);

        }

        /* Update the principal with the new value. */
        this.getTown().getTreasury().setPrincipalAmount(this.getTown().getTreasury().getBalance());

    }

    @Override
    public void onPostBuild(BlockCoord absCoord, SimpleBlock commandBlock) {
        this.level = getTown().saved_bank_level;
        this.interestRate = getTown().saved_bank_interest_amount;
    }

    public NonMemberFeeComponent getNonMemberFeeComponent() {
        return nonMemberFeeComponent;
    }

    public void setNonMemberFeeComponent(NonMemberFeeComponent nonMemberFeeComponent) {
        this.nonMemberFeeComponent = nonMemberFeeComponent;
    }

    public void onGoodieFromFrame() {
        this.updateSignText();
    }

    public void onGoodieToFrame() {
        this.updateSignText();
    }

}
