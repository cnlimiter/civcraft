/**
 * [2023] Cnlimiter LLC
 * All Rights Reserved.
 * Name: civcraft
 * Author: cnlimiter
 * UpdateTime: 2023/10/14 16:15
 * Description:
 * License: ARR
 */
package cn.evole.plugins.civcraft.interactive;

import cn.evole.plugins.civcraft.camp.Camp;
import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.exception.CivException;
import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.main.CivMessage;
import cn.evole.plugins.civcraft.object.Resident;
import cn.evole.plugins.civcraft.util.CivColor;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class InteractiveCampName implements InteractiveResponse {

    @Override
    public void respond(String message, Resident resident) {
        Player player;
        try {
            player = CivGlobal.getPlayer(resident);
        } catch (CivException e) {
            return;
        }
        if ("cancel".equalsIgnoreCase(message)) {
            CivMessage.send(player, CivSettings.localize.localizedString("interactive_camp_cancel"));
            resident.clearInteractiveMode();
            return;
        }
        //|| !StringUtils.isAsciiPrintable(message)
        // 检查是否只包含unicode字母
        if (!valid(message)) {
            CivMessage.send(player, CivColor.Rose + ChatColor.BOLD + CivSettings.localize.localizedString("interactive_camp_invalid"));
            return;
        }
        message = message.replace(" ", "_");
        message = message.replace("\"", "");
        message = message.replace("\'", "");

        Camp.newCamp(resident, player, message);
    }

}
