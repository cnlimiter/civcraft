package cn.evole.plugins.civcraft.structure;

import cn.evole.plugins.civcraft.exception.CivException;
import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.object.Resident;
import cn.evole.plugins.civcraft.object.Town;
import cn.evole.plugins.civcraft.object.TownChunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Hospital extends Structure {

    protected Hospital(Location center, String id, Town town)
            throws CivException {
        super(center, id, town);
    }

    public Hospital(ResultSet rs) throws SQLException, CivException {
        super(rs);
    }

    @Override
    public String getDynmapDescription() {
        return null;
    }

    @Override
    public String getMarkerIconName() {
        return "bed";
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        for (Town t : this.getTown().getCiv().getTowns()) {
            for (Resident res : t.getResidents()) {
                try {
                    Player player = CivGlobal.getPlayer(res);

                    if (player.isDead() || !player.isValid() || !player.isOnline()) {
                        continue;
                    }
                    if (player.getFoodLevel() >= 20) {
                        continue;
                    }

                    TownChunk tc = CivGlobal.getTownChunk(player.getLocation());
                    if (tc == null || tc.getTown() != this.getTown()) {
                        continue;
                    }

                    if (player.getFoodLevel() < 10.0) {
                        player.setFoodLevel(player.getFoodLevel() + 1);
                    }
                } catch (CivException e) {
                    //Player not online;
                }

            }
        }
    }
}
