
package com.avrgaming.civcraft.command.civ;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import com.avrgaming.civcraft.command.CommandBase;
import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.config.ConfigBuff;
import com.avrgaming.civcraft.config.ConfigTradeGood;
import com.avrgaming.civcraft.lorestorage.LoreGuiItem;

import com.avrgaming.civcraft.exception.CivException;
import com.avrgaming.civcraft.object.Civilization;
import com.avrgaming.civcraft.object.Resident;
import com.avrgaming.civcraft.object.Town;
import com.avrgaming.civcraft.util.CivColor;

public class CivTradeCommand
        extends CommandBase {
    @Override
    public void init() {
        this.command = "/civ trade";
        this.displayName = CivSettings.localize.localizedString("cmd_civ_trade_name");
        this.commands.put("listciv", CivSettings.localize.localizedString("cmd_civ_trade_listciv"));
        this.commands.put("listtown", CivSettings.localize.localizedString("cmd_civ_trade_listtown"));
        this.commands.put("deposit", CivSettings.localize.localizedString("cmd_civ_trade_deposit"));
        this.commands.put("withdraw", CivSettings.localize.localizedString("cmd_civ_trade_withdraw"));
        this.commands.put("gift", CivSettings.localize.localizedString("cmd_civ_trade_gift"));
    }

    public void withdraw_cmd() throws CivException {
        this.validLeaderMayor();
        Town from = this.getSelectedTown();
        Civilization to = this.getSenderCiv();
        if (StringUtils.isBlank((String) from.tradeGoods)) {
            throw new CivException(CivSettings.localize.localizedString("cmd_civ_trade_listtown_noGoods", CivColor.Gold + from.getName() + CivColor.Red));
        }
        Inventory withdrawInventory = this.genInventory(this.getPlayer(), 9, CivColor.GoldBold + CivSettings.localize.localizedString("cmd_civ_trade_gift_giftInvName"), CivColor.RoseBold + from.getName());
        int i = 0;
        for (String goodID : from.tradeGoods.split(", ")) {
            ConfigTradeGood configTradeGood = CivSettings.goods.get(goodID);
            if (configTradeGood == null) continue;
            String[] split = this.getBonusDisplayString(configTradeGood, CivColor.Green + CivSettings.localize.localizedString("cmd_civ_trade_withdraw_clickToWithdraw", new StringBuilder().append(CivColor.Green).append(to.getName()).append(CivColor.Green).toString())).split(";");
            ItemStack tradeGood = LoreGuiItem.build(configTradeGood.name, configTradeGood.material, configTradeGood.material_data, split);
            tradeGood = LoreGuiItem.setAction(tradeGood, "Confirmation");
            tradeGood = LoreGuiItem.setActionData(tradeGood, "civilizationName", to.getName());
            tradeGood = LoreGuiItem.setActionData(tradeGood, "townName", from.getName());
            tradeGood = LoreGuiItem.setActionData(tradeGood, "tradeGoodID", goodID);
            tradeGood = LoreGuiItem.setActionData(tradeGood, "passFields", "civilizationName,townName,tradeGoodID");
            tradeGood = LoreGuiItem.setActionData(tradeGood, "passAction", "WithdrawTradeGood");
            tradeGood = LoreGuiItem.setActionData(tradeGood, "confirmText", CivSettings.localize.localizedString("cmd_civ_trade_withdraw_confirmText", CivColor.Green + to.getName() + CivColor.Green));
            withdrawInventory.setItem(i, tradeGood);
            ++i;
        }
        this.getPlayer().openInventory(withdrawInventory);
    }

    public void deposit_cmd() throws CivException {
        this.validLeaderCiv();
        Civilization from = this.getSenderCiv();
        Town to = this.getSelectedTown();
        if (StringUtils.isBlank((String) from.tradeGoods)) {
            throw new CivException(CivSettings.localize.localizedString("cmd_civ_trade_gift_noGoods"));
        }
        Inventory gepositInventory = this.genInventory(this.getPlayer(), 54, CivColor.GoldBold + CivSettings.localize.localizedString("cmd_civ_trade_gift_giftInvName"), CivColor.RoseBold + from.getName());
        int i = 0;
        for (String goodID : from.tradeGoods.split(", ")) {
            ConfigTradeGood configTradeGood = CivSettings.goods.get(goodID);
            if (configTradeGood == null) continue;
            String[] split = this.getBonusDisplayString(configTradeGood, CivColor.Green + CivSettings.localize.localizedString("cmd_civ_trade_deposit_clickToDeposit", new StringBuilder().append(CivColor.Green).append(to.getName()).append(CivColor.Green).toString())).split(";");
            ItemStack tradeGood = LoreGuiItem.build(configTradeGood.name, configTradeGood.material, configTradeGood.material_data, split);
            tradeGood = LoreGuiItem.setAction(tradeGood, "Confirmation");
            tradeGood = LoreGuiItem.setActionData(tradeGood, "townName", to.getName());
            tradeGood = LoreGuiItem.setActionData(tradeGood, "tradeGoodID", goodID);
            tradeGood = LoreGuiItem.setActionData(tradeGood, "passFields", "townName,tradeGoodID");
            tradeGood = LoreGuiItem.setActionData(tradeGood, "passAction", "DepositTradeGood");
            tradeGood = LoreGuiItem.setActionData(tradeGood, "confirmText", CivSettings.localize.localizedString("cmd_civ_trade_deposit_confirmText", CivColor.Green + to.getName() + CivColor.Green));
            gepositInventory.setItem(i, tradeGood);
            ++i;
        }
        this.getPlayer().openInventory(gepositInventory);
    }

    public void listtown_cmd() throws CivException {
        this.validLeaderMayor();
        Town town = this.getSelectedTown();
        if (StringUtils.isBlank((String) town.tradeGoods)) {
            throw new CivException(CivSettings.localize.localizedString("cmd_civ_trade_listtown_noGoods", CivColor.Gold + town.getName() + CivColor.Red));
        }
        Inventory listInventory = this.genInventory(this.getPlayer(), 9, CivColor.GoldBold + CivSettings.localize.localizedString("cmd_civ_trade_listtown_listInvName"), CivColor.RoseBold + town.getName());
        int i = 0;
        for (String goodID : town.tradeGoods.split(", ")) {
            ConfigTradeGood configTradeGood = CivSettings.goods.get(goodID);
            if (configTradeGood == null) continue;
            String[] split = this.getBonusDisplayString(configTradeGood, null).split(";");
            ItemStack tradeGood = LoreGuiItem.build(configTradeGood.name, configTradeGood.material, configTradeGood.material_data, split);
            tradeGood = LoreGuiItem.setAction(tradeGood, "ShowTradeGoodInfo");
            listInventory.setItem(i, tradeGood);
            ++i;
        }
        this.getPlayer().openInventory(listInventory);
    }

    public void listciv_cmd() throws CivException {
        this.validLeaderCiv();
        Civilization civ = this.getSenderCiv();
        if (StringUtils.isBlank((String) civ.tradeGoods)) {
            throw new CivException(CivSettings.localize.localizedString("cmd_civ_trade_gift_noGoods"));
        }
        Inventory listInventory = this.genInventory(this.getPlayer(), 54, CivColor.GoldBold + CivSettings.localize.localizedString("cmd_civ_trade_listciv_listInvName"), CivColor.RoseBold + civ.getName());
        int i = 0;
        for (String goodID : civ.tradeGoods.split(", ")) {
            ConfigTradeGood configTradeGood = CivSettings.goods.get(goodID);
            if (configTradeGood == null) continue;
            String[] split = this.getBonusDisplayString(configTradeGood, null).split(";");
            ItemStack tradeGood = LoreGuiItem.build(configTradeGood.name, configTradeGood.material, configTradeGood.material_data, split);
            tradeGood = LoreGuiItem.setAction(tradeGood, "ShowTradeGoodInfo");
            listInventory.setItem(i, tradeGood);
            ++i;
        }
        this.getPlayer().openInventory(listInventory);
    }

    public void gift_cmd() throws CivException {
        this.validLeaderCiv();
        Civilization from = this.getSenderCiv();
        if (StringUtils.isBlank((String) from.tradeGoods)) {
            throw new CivException(CivSettings.localize.localizedString("cmd_civ_trade_gift_noGoods"));
        }
        Civilization to = this.getNamedCiv(1);
        if (to == from) {
            throw new CivException(CivSettings.localize.localizedString("cmd_civ_trade_gift_yourSelf"));
        }
        Inventory giftInventory = this.genInventory(this.getPlayer(), 54, CivColor.GoldBold + CivSettings.localize.localizedString("cmd_civ_trade_gift_giftInvName"), CivColor.RoseBold + from.getName());
        int i = 0;
        for (String goodID : from.tradeGoods.split(", ")) {
            ConfigTradeGood configTradeGood = CivSettings.goods.get(goodID);
            if (configTradeGood == null) continue;
            String[] split = this.getBonusDisplayString(configTradeGood, CivColor.Gold + CivSettings.localize.localizedString("cmd_civ_trade_gift_clickToGift")).split(";");
            ItemStack tradeGood = LoreGuiItem.build(configTradeGood.name, configTradeGood.material, configTradeGood.material_data, split);
            tradeGood = LoreGuiItem.setAction(tradeGood, "Confirmation");
            tradeGood = LoreGuiItem.setActionData(tradeGood, "civilizationName", to.getName());
            tradeGood = LoreGuiItem.setActionData(tradeGood, "tradeGoodID", goodID);
            tradeGood = LoreGuiItem.setActionData(tradeGood, "passFields", "civilizationName,tradeGoodID");
            tradeGood = LoreGuiItem.setActionData(tradeGood, "passAction", "GiftTradeGood");
            tradeGood = LoreGuiItem.setActionData(tradeGood, "confirmText", CivSettings.localize.localizedString("cmd_civ_trade_gift_confirmText"));
            giftInventory.setItem(i, tradeGood);
            ++i;
        }
        this.getPlayer().openInventory(giftInventory);
    }

    @Override
    public void doDefaultAction() throws CivException {
        this.showHelp();
    }

    @Override
    public void showHelp() {
        this.showBasicHelp();
    }

    @Override
    public void permissionCheck() throws CivException {
    }

    public void validLeaderCiv() throws CivException {
        Resident res = this.getResident();
        Civilization civ = this.getSenderCiv();
        if (civ == null) {
            throw new CivException(CivSettings.localize.localizedString("cmd_civ_trade_noCiv"));
        }
        if (!civ.getLeaderGroup().hasMember(res)) {
            throw new CivException(CivSettings.localize.localizedString("cmd_civ_trade_higherRank"));
        }
    }

    public void validLeaderMayor() throws CivException {
        Resident res = this.getResident();
        Civilization civ = this.getSenderCiv();
        if (civ == null) {
            throw new CivException(CivSettings.localize.localizedString("cmd_civ_trade_noCiv"));
        }
        if (!civ.getLeaderGroup().hasMember(res) && !res.getSelectedTown().getMayorGroup().hasMember(res)) {
            throw new CivException(CivSettings.localize.localizedString("cmd_civ_tradelist_higherRank"));
        }
    }

    private Inventory genInventory(Player player, int size, String text, String more) {
        return Bukkit.getServer().createInventory((InventoryHolder) player, size, this.formatTextForGui(text + " " + more));
    }

    private String formatTextForGui(String text) {
        return StringUtils.left((String) text, (int) 32);
    }

    public String getBonusDisplayString(ConfigTradeGood configTradeGood, String addText) {
        StringBuilder out = new StringBuilder();
        out.append(CivColor.PurpleItalic + CivSettings.localize.localizedString("var_tradeGood_heading"));
        out.append(";");
        for (ConfigBuff cBuff : configTradeGood.buffs.values()) {
            out.append((Object) ChatColor.UNDERLINE).append(cBuff.name);
            out.append(";");
            out.append(CivColor.RESET + (Object) ChatColor.ITALIC).append(cBuff.description);
            out.append(";");
        }
        if (configTradeGood.water) {
            out.append(CivColor.LightBlue + CivSettings.localize.localizedString("var_tradegood_water"));
        } else {
            out.append(CivColor.Green + CivSettings.localize.localizedString("var_tradegood_earth"));
        }
        out.append(";");
        if (!StringUtils.isBlank((String) addText)) {
            out.append(addText);
            out.append(";");
        }
        return out.toString();
    }
}

