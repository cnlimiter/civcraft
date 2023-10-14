package cn.evole.plugins.civcraft.structure.farm;

import cn.evole.plugins.civcraft.util.BlockCoord;

public class GrowBlock {

    public BlockCoord bcoord;
    public int typeId;
    public int data;
    public boolean spawn;
    public GrowBlock(String world, int x, int y, int z, int typeid2, int data2, boolean spawn2) {
        this.bcoord = new BlockCoord(world, x, y, z);
        this.typeId = typeid2;
        this.data = data2;
        this.spawn = spawn2;
    }
}
