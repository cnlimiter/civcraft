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

import cn.evole.plugins.civcraft.components.NonMemberFeeComponent;
import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.config.ConfigGrocerLevel;
import cn.evole.plugins.civcraft.exception.CivException;
import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.main.CivLog;
import cn.evole.plugins.civcraft.main.CivMessage;
import cn.evole.plugins.civcraft.object.Resident;
import cn.evole.plugins.civcraft.object.StructureSign;
import cn.evole.plugins.civcraft.object.Town;
import cn.evole.plugins.civcraft.util.CivColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Grocer extends Structure {

    private int level = 1;

    private NonMemberFeeComponent nonMemberFeeComponent;

    protected Grocer(Location center, String id, Town town) throws CivException {
        super(center, id, town);
        nonMemberFeeComponent = new NonMemberFeeComponent(this);
        nonMemberFeeComponent.onSave();
        setLevel(town.saved_grocer_levels);
    }

    public Grocer(ResultSet rs) throws SQLException, CivException {
        super(rs);
        nonMemberFeeComponent = new NonMemberFeeComponent(this);
        nonMemberFeeComponent.onLoad();
    }

    @Override
    public String getDynmapDescription() {
        String out = "<u><b>" + this.getDisplayName() + "</u></b><br/>";

        for (int i = 0; i < level; i++) {
            ConfigGrocerLevel grocerlevel = CivSettings.grocerLevels.get(i + 1);
            out += "<b>" + grocerlevel.itemName + "</b> " + CivSettings.localize.localizedString("Amount") + " " + grocerlevel.amount + " " + CivSettings.localize.localizedString("Price") + " " + grocerlevel.price + " " + CivSettings.CURRENCY_NAME + ".<br/>";
        }

        return out;
    }

    @Override
    public String getMarkerIconName() {
        return "cutlery";
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public double getNonResidentFee() {
        return nonMemberFeeComponent.getFeeRate();
    }

    public void setNonResidentFee(double nonResidentFee) {
        this.nonMemberFeeComponent.setFeeRate(nonResidentFee);
    }

    private String getNonResidentFeeString() {
        return "Fee: " + ((int) (getNonResidentFee() * 100) + "%").toString();
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

    public void sign_buy_material(Player player, String itemName, int id, byte data, int amount, double price) {
        Resident resident;
        int payToTown = (int) Math.round(price * this.getNonResidentFee());
        try {

            resident = CivGlobal.getResident(player.getName());
            Town t = resident.getTown();

            if (t == this.getTown()) {
                // Pay no taxes! You're a member.
                resident.buyItem(itemName, id, data, price, amount);
                CivMessage.send(player, CivColor.LightGreen + CivSettings.localize.localizedString("var_grocer_msgBought", amount, itemName, price + " " + CivSettings.CURRENCY_NAME));
                return;
            } else {
                // Pay non-resident taxes
                resident.buyItem(itemName, id, data, price + payToTown, amount);
                getTown().depositDirect(payToTown);
                CivMessage.send(player, CivColor.LightGreen + CivSettings.localize.localizedString("var_grocer_msgBought", amount, itemName, price, CivSettings.CURRENCY_NAME));
                CivMessage.send(player, CivColor.Yellow + CivSettings.localize.localizedString("var_grocer_msgPaidTaxes", this.getTown().getName(), payToTown + " " + CivSettings.CURRENCY_NAME));
            }

        } catch (CivException e) {
            CivMessage.send(player, CivColor.Rose + e.getMessage());
        }
        return;
    }


    @Override
    public void updateSignText() {
        int count = 0;

        for (count = 0; count < level; count++) {
            StructureSign sign = getSignFromSpecialId(count);
            if (sign == null) {
                CivLog.error("sign from special id was null, id:" + count);
                return;
            }
            ConfigGrocerLevel grocerlevel = CivSettings.grocerLevels.get(count + 1);

            sign.setText(CivSettings.localize.localizedString("grocer_sign_buy") + "\n" + grocerlevel.itemName + "\n" +
                    CivSettings.localize.localizedString("grocer_sign_for") + " " + grocerlevel.price + " " + CivSettings.CURRENCY_NAME + "\n" +
                    getNonResidentFeeString());

            sign.update();
        }

        for (; count < getSigns().size(); count++) {
            StructureSign sign = getSignFromSpecialId(count);
            if (sign == null) {
                CivLog.error("sign from special id was null, id:" + count);
                return;
            }
            sign.setText(CivSettings.localize.localizedString("grocer_sign_empty"));
            sign.update();
        }

    }

    @Override
    public void processSignAction(Player player, StructureSign sign, PlayerInteractEvent event) {
        int special_id = Integer.valueOf(sign.getAction());
        if (special_id < this.level) {
            ConfigGrocerLevel grocerlevel = CivSettings.grocerLevels.get(special_id + 1);
            sign_buy_material(player, grocerlevel.itemName, grocerlevel.itemId,
                    (byte) grocerlevel.itemData, grocerlevel.amount, grocerlevel.price);
        } else {
            CivMessage.send(player, CivColor.Rose + CivSettings.localize.localizedString("grocer_sign_needUpgrade"));
        }
    }


}
