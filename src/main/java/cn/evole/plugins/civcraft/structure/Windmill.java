/**
 * [2023] Cnlimiter LLC
 * All Rights Reserved.
 * Name: civcraft
 * Author: cnlimiter
 * UpdateTime: 2023/10/14 16:15
 * Description:
 * License: ARR
 */
package cn.evole.plugins.civcraft.structure;

import cn.evole.plugins.civcraft.exception.CivException;
import cn.evole.plugins.civcraft.object.Town;
import cn.evole.plugins.civcraft.threading.TaskMaster;
import cn.evole.plugins.civcraft.threading.tasks.WindmillStartSyncTask;
import org.bukkit.Location;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Windmill extends Structure {

    public Windmill(ResultSet rs) throws SQLException, CivException {
        super(rs);
    }

    public Windmill(Location center, String id, Town town)
            throws CivException {
        super(center, id, town);
    }

    @Override
    public void onEffectEvent() {


    }

    public void processWindmill() {
        /* Fire a sync task to perform this. */
        TaskMaster.syncTask(new WindmillStartSyncTask(this), 0);
    }

    public enum CropType {
        WHEAT,
        CARROTS,
        POTATOES
    }

}
