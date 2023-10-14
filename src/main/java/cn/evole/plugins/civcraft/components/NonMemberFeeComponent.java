/**
 * Name: civcraft
 * Author: cnlimiter
 * UpdateTime: 2023/10/14 16:15
 * Description:
 */
package cn.evole.plugins.civcraft.components;

import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.sessiondb.SessionEntry;
import cn.evole.plugins.civcraft.structure.Buildable;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class NonMemberFeeComponent extends Component {

    private Buildable buildable;
    private double feeRate = 0.05;

    public NonMemberFeeComponent(Buildable buildable) {
        this.buildable = buildable;
    }


    private String getKey() {
        return buildable.getDisplayName() + ":" + buildable.getId() + ":" + "fee";
    }

    @Override
    public void onLoad() {
        ArrayList<SessionEntry> entries = CivGlobal.getSessionDB().lookup(getKey());

        if (entries.size() == 0) {
            buildable.sessionAdd(getKey(), "" + feeRate);
            return;
        }

        feeRate = Double.parseDouble(entries.get(0).value);

    }

    @Override
    public void onSave() {

        ArrayList<SessionEntry> entries = CivGlobal.getSessionDB().lookup(getKey());

        if (entries.size() == 0) {
            buildable.sessionAdd(getKey(), "" + feeRate);
            return;
        }
        CivGlobal.getSessionDB().update(entries.get(0).request_id, getKey(), "" + feeRate);
    }


    public double getFeeRate() {
        return feeRate;
    }


    public void setFeeRate(double feeRate) {
        this.feeRate = feeRate;
        onSave();
    }


    public Buildable getBuildable() {
        return buildable;
    }

    public String getFeeString() {
        DecimalFormat df = new DecimalFormat();
        return "" + df.format(this.getFeeRate() * 100) + "%";
    }

}
