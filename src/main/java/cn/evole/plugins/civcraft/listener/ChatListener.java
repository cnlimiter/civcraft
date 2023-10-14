/**
 * [2023] Cnlimiter LLC
 * All Rights Reserved.
 * Name: civcraft
 * Author: cnlimiter
 * UpdateTime: 2023/10/14 16:15
 * Description:
 * License: ARR
 */
package cn.evole.plugins.civcraft.listener;

import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.main.CivMessage;
import cn.evole.plugins.civcraft.object.Civilization;
import cn.evole.plugins.civcraft.object.Resident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {

    @EventHandler(priority = EventPriority.LOW)
    void OnPlayerAsyncChatEvent(AsyncPlayerChatEvent event) {

        Resident resident = CivGlobal.getResident(event.getPlayer());
        if (resident == null) {
            /* resident not found, I guess just let the chat through. */
            return;
        }
        //yes部分优先拦截掉
        if (resident.isInteractiveMode()) {
            resident.getInteractiveResponse().respond(event.getMessage(), resident);
            event.setCancelled(true);
            return;
        }
        if (resident.isTownChat()) {
            event.setCancelled(true);
            if (resident.getTownChatOverride() == null) {
                CivMessage.sendTownChat(resident.getTown(), resident, event.getFormat(), event.getMessage());
            } else {
                CivMessage.sendTownChat(resident.getTownChatOverride(), resident, event.getFormat(), event.getMessage());
            }
            return;
        }

        if (resident.isCivChat()) {
            Civilization civ;
            event.setCancelled(true);
            if (resident.getTown() == null) {
                civ = null;
            } else {
                civ = resident.getTown().getCiv();
            }

            if (resident.getCivChatOverride() == null) {
                CivMessage.sendCivChat(civ, resident, event.getFormat(), event.getMessage());
            } else {
                CivMessage.sendCivChat(resident.getCivChatOverride(), resident, event.getFormat(), event.getMessage());
            }
        }


        //	CivLog.debug("Got message:"+event.getMessage());
        //event.setFormat("[[[%s %s]]]");
    }

}
