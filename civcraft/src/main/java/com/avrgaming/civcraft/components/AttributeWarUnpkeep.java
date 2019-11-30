
package com.avrgaming.civcraft.components;

import com.avrgaming.civcraft.structure.Buildable;

public class AttributeWarUnpkeep extends Component {
    public double value;

    @Override
    public void createComponent(Buildable buildable, boolean async) {
        super.createComponent(buildable, async);
        this.value = this.getDouble("value");
    }
}

