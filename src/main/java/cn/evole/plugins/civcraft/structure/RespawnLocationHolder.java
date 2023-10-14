package cn.evole.plugins.civcraft.structure;

import cn.evole.plugins.civcraft.util.BlockCoord;

import java.util.List;

public interface RespawnLocationHolder {

    public String getRespawnName();

    public List<BlockCoord> getRespawnPoints();

    public BlockCoord getRandomRevivePoint();

    public boolean isTeleportReal();
}
