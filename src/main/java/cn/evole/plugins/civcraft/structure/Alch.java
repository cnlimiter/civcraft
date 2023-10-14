package cn.evole.plugins.civcraft.structure;

import cn.evole.plugins.civcraft.components.NonMemberFeeComponent;
import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.config.ConfigAlchLevel;
import cn.evole.plugins.civcraft.exception.CivException;
import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.main.CivLog;
import cn.evole.plugins.civcraft.main.CivMessage;
import cn.evole.plugins.civcraft.object.Civilization;
import cn.evole.plugins.civcraft.object.Resident;
import cn.evole.plugins.civcraft.object.StructureSign;
import cn.evole.plugins.civcraft.object.Town;
import cn.evole.plugins.civcraft.util.CivColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Alch extends Structure {
    private int level = 1;
    private NonMemberFeeComponent nonMemberFeeComponent;

    protected Alch(Location center, String id, Town town) throws CivException {
        super(center, id, town);
        this.nonMemberFeeComponent = new NonMemberFeeComponent(this);
        this.nonMemberFeeComponent.onSave();
        this.setLevel(town.saved_alch_levels);
    }

    public Alch(ResultSet rs) throws SQLException, CivException {
        super(rs);
        this.nonMemberFeeComponent = new NonMemberFeeComponent(this);
        this.nonMemberFeeComponent.onLoad();
    }

    @Override
    public String getDynmapDescription() {
        StringBuilder out = new StringBuilder("<u><b>" + this.getDisplayName() + "</u></b><br/>");
        for (int i = 0; i < this.level; ++i) {
            ConfigAlchLevel alchlevel = CivSettings.alchLevels.get(i + 1);
            out.append("<b>").append(alchlevel.itemName).append("</b> ").append(CivSettings.localize.localizedString("Amount")).append(" ").append(alchlevel.amount).append(" ").append(CivSettings.localize.localizedString("Price")).append(" ").append(alchlevel.price).append(" ").append(CivSettings.CURRENCY_NAME).append(".<br/>");
        }
        return out.toString();
    }

    @Override
    public String getMarkerIconName() {
        return "drink";
    }

    public int getLevel() {
        return this.level;
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

    private String getNonResidentFeeString() {
        return "Fee: " + this.getNonResidentFee() * 100.0 + "%";
    }

    private StructureSign getSignFromSpecialId(int special_id) {
        for (StructureSign sign : this.getSigns()) {
            int id = Integer.parseInt(sign.getAction());
            if (id != special_id) continue;
            return sign;
        }
        return null;
    }

    public void sign_buy_material(Player player, String itemName, int id, byte data, int amount, double price) {
        int payToTown = (int) Math.round(price * this.getNonResidentFee());
        try {
            Resident resident = CivGlobal.getResident(player.getName());
            Civilization c = resident.getCiv();
            if (c == this.getCiv()) {
                resident.buyItem(itemName, id, data, price, amount);
                CivMessage.send(player, CivColor.Green + CivSettings.localize.localizedString("var_alch_msgBought", amount, itemName, new StringBuilder().append(price).append(" ").append(CivSettings.CURRENCY_NAME).toString()));
                return;
            }
            resident.buyItem(itemName, id, data, price + (double) payToTown, amount);
            this.getTown().depositDirect(payToTown);
            CivMessage.send(player, CivColor.Green + CivSettings.localize.localizedString("var_alch_msgBought", amount, itemName, price, CivSettings.CURRENCY_NAME));
            CivMessage.send(player, CivColor.Yellow + CivSettings.localize.localizedString("var_taxes_paid", this.getTown().getName(), new StringBuilder().append(payToTown).append(" ").append(CivSettings.CURRENCY_NAME).toString()));
        } catch (CivException e) {
            CivMessage.send(player, CivColor.Red + e.getMessage());
        }
    }

    @Override
    public void updateSignText() {
        StructureSign sign;
        int count = 0;
        for (count = 0; count < this.level; ++count) {
            sign = this.getSignFromSpecialId(count);
            if (sign == null) {
                CivLog.error("sign from special id was null, id:" + count);
                return;
            }
            ConfigAlchLevel alchlevel = CivSettings.alchLevels.get(count + 1);
            sign.setText(CivSettings.localize.localizedString("alch_sign_buy") + "\n" + alchlevel.itemName + "\n" + CivSettings.localize.localizedString("alch_sign_for") + " " + alchlevel.price + " " + CivSettings.CURRENCY_NAME + "\n" + this.getNonResidentFeeString());
            sign.update();
        }
        while (count < this.getSigns().size()) {
            sign = this.getSignFromSpecialId(count);
            if (sign == null) {
                CivLog.error("sign from special id was null, id:" + count);
                return;
            }
            sign.setText(CivSettings.localize.localizedString("alch_sign_empty"));
            sign.update();
            ++count;
        }
    }

    @Override
    public void processSignAction(Player player, StructureSign sign, PlayerInteractEvent event) {
        int special_id = Integer.parseInt(sign.getAction());
        if (special_id < this.level) {
            ConfigAlchLevel alchlevel = CivSettings.alchLevels.get(special_id + 1);
            this.sign_buy_material(player, alchlevel.itemName, alchlevel.itemId, (byte) alchlevel.itemData, alchlevel.amount, alchlevel.price);
        } else {
            CivMessage.send(player, CivColor.Red + CivSettings.localize.localizedString("alch_sign_needUpgrade"));
        }
    }
}

