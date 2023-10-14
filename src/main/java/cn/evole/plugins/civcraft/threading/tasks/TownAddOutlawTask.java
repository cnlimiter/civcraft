/**
 * [2023] Cnlimiter LLC
 * All Rights Reserved.
 * Name: civcraft
 * Author: cnlimiter
 * UpdateTime: 2023/10/14 16:15
 * Description:
 * License: ARR
 */
package cn.evole.plugins.civcraft.threading.tasks;

import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.exception.CivException;
import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.main.CivMessage;
import cn.evole.plugins.civcraft.object.Town;
import cn.evole.plugins.civcraft.util.CivColor;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class TownAddOutlawTask implements Runnable {

    String name;
    Town town;


    public TownAddOutlawTask(String name, Town town) {
        this.name = name;
        this.town = town;
    }

    @Override
    public void run() {

        try {
            Player player = CivGlobal.getPlayer(name);
            CivMessage.send(player, CivColor.Yellow + ChatColor.BOLD + CivSettings.localize.localizedString("var_TownAddOutlawTask_Notify", town.getName()));
        } catch (CivException e) {
        }

        town.addOutlaw(name);
        town.save();
        CivMessage.sendTown(town, CivColor.Yellow + CivSettings.localize.localizedString("var_TownAddOutlawTask_Message", name));

    }

}
