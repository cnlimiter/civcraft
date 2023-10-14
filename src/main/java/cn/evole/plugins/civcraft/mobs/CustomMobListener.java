package cn.evole.plugins.civcraft.mobs;

import cn.evole.plugins.civcraft.config.ConfigMobs;
import net.minecraft.server.v1_12_R1.Entity;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public abstract class CustomMobListener {

    public static ConcurrentHashMap<UUID, Entity> customMobs = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<UUID, ConfigMobs> mobList = new ConcurrentHashMap<>();
}