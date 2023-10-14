//package cn.evole.plugins.civcraft.listener;
//
//import cn.evole.plugins.civcraft.config.CivSettings;
//import cn.evole.plugins.civcraft.exception.CivException;
//import cn.evole.plugins.civcraft.main.CivGlobal;
//import cn.evole.plugins.civcraft.object.Resident;
//import cn.evole.plugins.civcraft.util.CivColor;
//import com.dthielke.herochat.ChannelChatEvent;
//import com.dthielke.herochat.Chatter;
//import com.dthielke.herochat.Chatter.Result;
//import com.dthielke.herochat.Herochat;
//import org.bukkit.entity.Player;
//import org.bukkit.event.EventHandler;
//import org.bukkit.event.EventPriority;
//import org.bukkit.event.Listener;
//
//public class HeroChatListener implements Listener {
//
//    @EventHandler(priority = EventPriority.MONITOR)
//    public void onChannelChatEvent(TrChatEvent event) {
//        Resident resident = CivGlobal.getResident(event.getSender().getName());
//        if (resident == null) {
//            event.setResult(Result.FAIL);
//            return;
//        }
//
//        if (!resident.isInteractiveMode()) {
//            if (resident.isMuted()) {
//                event.setResult(Result.MUTED);
//                return;
//            }
//        }
//
//        if (event.getChannel().getDistance() > 0) {
//            for (String name : Resident.allchatters) {
//                Player player;
//                try {
//                    player = CivGlobal.getPlayer(name);
//                } catch (CivException e) {
//                    continue;
//                }
//
//                Chatter you = Herochat.getChatterManager().getChatter(player);
//                if (!event.getSender().isInRange(you, event.getChannel().getDistance())) {
//                    player.sendMessage(CivColor.White + event.getSender().getName() + CivSettings.localize.localizedString("hc_prefix_far") + " " + event.getMessage());
//                }
//            }
//        }
//    }
//}
