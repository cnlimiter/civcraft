package cn.evole.plugins.civ_dynmap;

import cn.evole.plugins.civcraft.main.CivLog;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerSet;

import java.util.logging.Logger;


public class CivDynmap {
    public static final Logger log = CivLog.plugin.getLogger();
    public static CivDynmap INSTANCE = new CivDynmap();
    DynmapAPI api;
    Plugin dynmap;
    MarkerSet townBorderSet;
    MarkerSet cultureSet;
    MarkerSet structureSet;
    MarkerAPI markerapi;

    public void init(Plugin plugin) {
        PluginManager pm = plugin.getServer().getPluginManager();

        dynmap = pm.getPlugin("dynmap");
        api = (DynmapAPI) dynmap;


        markerapi = api.getMarkerAPI();

        townBorderSet = markerapi.createMarkerSet("townborder.markerset", "城镇的边界", null, false);
        townBorderSet.setLayerPriority(10);
        townBorderSet.setHideByDefault(false);

        cultureSet = markerapi.createMarkerSet("townculture.markerset", "文化", null, false);
        cultureSet.setLayerPriority(15);
        cultureSet.setHideByDefault(false);

        structureSet = markerapi.createMarkerSet("structures.markerset", "建筑", null, false);
        structureSet.setLayerPriority(20);
        structureSet.setHideByDefault(false);

        plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin,
                new DynmapUpdateTask(this.api, this.markerapi, this.townBorderSet, this.cultureSet, this.structureSet), 40, 40);
    }

}
