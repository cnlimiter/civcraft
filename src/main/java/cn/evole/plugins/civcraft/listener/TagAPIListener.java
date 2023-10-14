//package cn.evole.plugins.civcraft.listener;
//
//import cn.evole.plugins.civcraft.main.CivGlobal;
//import org.bukkit.event.EventHandler;
//import org.bukkit.event.EventPriority;
//import org.bukkit.event.Listener;
//import org.kitteh.tag.AsyncPlayerReceiveNameTagEvent;
//
//public class TagAPIListener implements Listener {
//    @EventHandler(priority = EventPriority.HIGHEST)
//    public void onNameTag(AsyncPlayerReceiveNameTagEvent event) {
//        event.setTag(CivGlobal.updateTag(event.getNamedPlayer(), event.getPlayer()));
//    }
//}