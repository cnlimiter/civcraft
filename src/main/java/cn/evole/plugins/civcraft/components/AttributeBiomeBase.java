package cn.evole.plugins.civcraft.components;

import cn.evole.plugins.civcraft.object.CultureChunk;

public abstract class AttributeBiomeBase extends Component {

    public AttributeBiomeBase() {
        this.typeName = "AttributeBiomeBase";
    }

    public abstract double getGenerated(CultureChunk cultureChunk);

    public abstract String getAttribute();
}
