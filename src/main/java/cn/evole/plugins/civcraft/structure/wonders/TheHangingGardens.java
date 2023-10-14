/**
 * [2023] Cnlimiter LLC
 * All Rights Reserved.
 * Name: civcraft
 * Author: cnlimiter
 * UpdateTime: 2023/10/14 16:15
 * Description:
 * License: ARR
 */
package cn.evole.plugins.civcraft.structure.wonders;

import cn.evole.plugins.civcraft.exception.CivException;
import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.object.Resident;
import cn.evole.plugins.civcraft.object.Town;
import cn.evole.plugins.civcraft.object.TownChunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.sql.ResultSet;
import java.sql.SQLException;

public class TheHangingGardens extends Wonder {

    public TheHangingGardens(ResultSet rs) throws SQLException, CivException {
        super(rs);
    }

    public TheHangingGardens(Location center, String id, Town town)
            throws CivException {
        super(center, id, town);
    }

    @Override
    protected void addBuffs() {
        addBuffToCiv(this.getCiv(), "buff_hanging_gardens_growth");
        addBuffToCiv(this.getCiv(), "buff_hanging_gardens_additional_growth");
        addBuffToTown(this.getTown(), "buff_hanging_gardens_regen");
    }

    @Override
    protected void removeBuffs() {
        removeBuffFromCiv(this.getCiv(), "buff_hanging_gardens_growth");
        removeBuffFromCiv(this.getCiv(), "buff_hanging_gardens_additional_growth");
        removeBuffFromTown(this.getTown(), "buff_hanging_gardens_regen");
    }

    @Override
    public void onLoad() {
        if (this.isActive()) {
            addBuffs();
        }
    }

    @Override
    public void onComplete() {
        addBuffs();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        removeBuffs();
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        for (Town t : this.getTown().getCiv().getTowns()) {
            for (Resident res : t.getResidents()) {
                try {
                    Player player = CivGlobal.getPlayer(res);

                    if (player.isDead() || !player.isValid()) {
                        continue;
                    }

                    if (player.getHealth() >= 20) {
                        continue;
                    }

                    TownChunk tc = CivGlobal.getTownChunk(player.getLocation());
                    if (tc == null || tc.getTown() != this.getTown()) {
                        continue;
                    }

                    if (player.getHealth() >= 19.0) {
                        player.setHealth(20);
                    } else {
                        player.setHealth(player.getHealth() + 1);
                    }
                } catch (CivException e) {
                    //Player not online;
                }

            }
        }
    }

}
