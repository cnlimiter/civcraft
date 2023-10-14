package cn.evole.plugins.civcraft.components;

import cn.evole.plugins.civcraft.structure.Buildable;

public class AttributeWarUnpkeep extends Component {
    public double value;

    @Override
    public void createComponent(Buildable buildable, boolean async) {
        super.createComponent(buildable, async);
        this.value = this.getDouble("value");
    }
}

