package com.avrgaming.civcraft.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.kitteh.vanish.VanishPlugin;

public class VanishNoPacketUtil {

	public static boolean isVanished(Player player) {
		try {

			VanishPlugin vnp = (VanishPlugin) Bukkit.getPluginManager().getPlugin("VanishNoPacket");
			if (vnp != null) {
				return vnp.getManager().isVanished(player);
			} else {
				return false;
			}
		} catch (NoClassDefFoundError e ) {
			return false;
		}
	}
	
}
