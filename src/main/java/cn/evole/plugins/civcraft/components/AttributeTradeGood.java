package cn.evole.plugins.civcraft.components;

import cn.evole.plugins.civcraft.items.BonusGoodie;
import cn.evole.plugins.civcraft.object.Town;
import cn.evole.plugins.civcraft.structure.Buildable;

import java.util.HashSet;

public class AttributeTradeGood extends AttributeBase {

    HashSet<String> goods = new HashSet<String>();
    double value;
    String attribute;

    @Override
    public void createComponent(Buildable buildable, boolean async) {
        super.createComponent(buildable, async);

        String[] good_ids = this.getString("goods").split(",");
        for (String id : good_ids) {
            goods.add(id.toLowerCase().trim());
        }

        attribute = this.getString("attribute");
        value = this.getDouble("value");
    }


    @Override
    public double getGenerated() {
        if (!this.getBuildable().isActive()) {
            return 0.0;
        }

        Town town = this.getBuildable().getTown();
        double generated = 0.0;

        for (BonusGoodie goodie : town.getBonusGoodies()) {
            if (goods.contains(goodie.getConfigTradeGood().id)) {
                generated += value;
            }
        }


        return generated;
    }

}
