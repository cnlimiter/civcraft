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
import cn.evole.plugins.civcraft.structure.Structure;
import cn.evole.plugins.civcraft.template.Template;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.sql.SQLException;

public class StartStructureBuild implements Runnable {

    public String playerName;
    public Structure struct;
    public Template tpl;
    public Location centerLoc;

    @Override
    public void run() {
        Player player;
        try {
            player = CivGlobal.getPlayer(playerName);
        } catch (CivException e1) {
            e1.printStackTrace();
            return;
        }

        try {
            struct.doBuild(player, centerLoc, tpl);
            struct.save();
        } catch (CivException e) {
            CivMessage.sendError(player, CivSettings.localize.localizedString("internalCommandException") + " " + e.getMessage());
        } catch (IOException e) {
            CivMessage.sendError(player, CivSettings.localize.localizedString("internalIOException"));
            e.printStackTrace();
        } catch (SQLException e) {
            CivMessage.sendError(player, CivSettings.localize.localizedString("internalDatabaseException"));
            e.printStackTrace();
        }
    }

}
