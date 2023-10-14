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
import cn.evole.plugins.civcraft.exception.CivException;
import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.main.CivLog;
import cn.evole.plugins.civcraft.main.CivMessage;
import cn.evole.plugins.civcraft.object.Resident;
import cn.evole.plugins.civcraft.object.StoreMaterial;
import cn.evole.plugins.civcraft.object.StructureSign;
import cn.evole.plugins.civcraft.object.Town;
import cn.evole.plugins.civcraft.util.CivColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class Store extends Structure {

    ArrayList<StoreMaterial> materials = new ArrayList<StoreMaterial>();
    private int level = 1;
    private NonMemberFeeComponent nonMemberFeeComponent;

    protected Store(Location center, String id, Town town) throws CivException {
        super(center, id, town);
        nonMemberFeeComponent = new NonMemberFeeComponent(this);
        nonMemberFeeComponent.onSave();
        setLevel(town.saved_store_level);
    }

    protected Store(ResultSet rs) throws SQLException, CivException {
        super(rs);
        nonMemberFeeComponent = new NonMemberFeeComponent(this);
        nonMemberFeeComponent.onLoad();
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
        nonMemberFeeComponent.setFeeRate(nonResidentFee);
    }

    private String getNonResidentFeeString() {
        return "Fee: " + ((int) (nonMemberFeeComponent.getFeeRate() * 100) + "%").toString();
    }

    public void addStoreMaterial(StoreMaterial mat) throws CivException {
        if (materials.size() >= 4) {
            throw new CivException(CivSettings.localize.localizedString("store_isFull"));
        }
        materials.add(mat);
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


        // iterate through materials, set signs using array...

        for (StoreMaterial mat : this.materials) {
            StructureSign sign = getSignFromSpecialId(count);
            if (sign == null) {
                CivLog.error("sign from special id was null, id:" + count);
                return;
            }

            sign.setText(CivSettings.localize.localizedString("var_store_sign_buy", mat.name, ((int) mat.price + " " + CivSettings.CURRENCY_NAME), getNonResidentFeeString()));
            sign.update();
            count++;
        }

        // We've finished with all of the materials, update the empty signs to show correct text.
        for (; count < getSigns().size(); count++) {
            StructureSign sign = getSignFromSpecialId(count);
            sign.setText(CivSettings.localize.localizedString("store_sign_empty"));
            sign.update();
        }
    }

    @Override
    public void processSignAction(Player player, StructureSign sign, PlayerInteractEvent event) {
        int special_id = Integer.valueOf(sign.getAction());
        if (special_id < this.materials.size()) {
            StoreMaterial mat = this.materials.get(special_id);
            sign_buy_material(player, mat.name, mat.type, mat.data, 64, mat.price);
        } else {
            CivMessage.send(player, CivColor.Rose + CivSettings.localize.localizedString("store_buy_empty"));
        }
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
                CivMessage.send(player, CivColor.LightGreen + CivSettings.localize.localizedString("var_market_buy", amount, itemName, price, CivSettings.CURRENCY_NAME));
                return;
            } else {
                // Pay non-resident taxes
                resident.buyItem(itemName, id, data, price + payToTown, amount);
                getTown().depositDirect(payToTown);
                CivMessage.send(player, CivColor.Yellow + CivSettings.localize.localizedString("var_taxes_paid", payToTown, CivSettings.CURRENCY_NAME));
            }

        } catch (CivException e) {
            CivMessage.send(player, CivColor.Rose + e.getMessage());
        }
        return;
    }

    @Override
    public String getDynmapDescription() {
        String out = "<u><b>" + this.getDisplayName() + "</u></b><br/>";
        if (this.materials.size() == 0) {
            out += CivSettings.localize.localizedString("store_dynmap_nothingStocked");
        } else {
            for (StoreMaterial mat : this.materials) {
                out += CivSettings.localize.localizedString("var_store_dynmap_item", mat.name, mat.price) + "<br/>";
            }
        }
        return out;
    }

    @Override
    public String getMarkerIconName() {
        return "bricks";
    }

    public void reset() {
        this.materials.clear();
        this.updateSignText();
    }

}
