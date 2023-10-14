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

import cn.evole.plugins.civcraft.cache.PlayerLocationCache;
import cn.evole.plugins.civcraft.components.PlayerProximityComponent;
import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.exception.CivException;
import cn.evole.plugins.civcraft.exception.InvalidConfiguration;
import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.main.CivMessage;
import cn.evole.plugins.civcraft.object.Relation;
import cn.evole.plugins.civcraft.object.Resident;
import cn.evole.plugins.civcraft.object.Town;
import cn.evole.plugins.civcraft.util.CivColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;

public class ScoutTower extends Structure {

    double range;
    private PlayerProximityComponent proximityComponent;

    private int reportSeconds = 60;
    private int count = 0;

    public ScoutTower(ResultSet rs) throws SQLException, CivException {
        super(rs);
    }

    protected ScoutTower(Location center, String id, Town town)
            throws CivException {
        super(center, id, town);
        this.hitpoints = this.getMaxHitPoints();
    }

    @Override
    public void loadSettings() {
        super.loadSettings();

        try {
            range = CivSettings.getDouble(CivSettings.warConfig, "scout_tower.range");
            proximityComponent = new PlayerProximityComponent();
            proximityComponent.createComponent(this);

            proximityComponent.setBuildable(this);
            proximityComponent.setCenter(this.getCenterLocation());
            int reportrate = (int) CivSettings.getDouble(CivSettings.warConfig, "scout_tower.update");
            if (this.getTown().getBuffManager().hasBuff("buff_colossus_coins_from_culture") && this.getTown().getBuffManager().hasBuff("buff_great_lighthouse_tower_range")) {
                range = 600.0;
                reportrate = 60;
            } else {
                range = 400.0;
                reportrate = 120;
            }
            if (this.getCiv().getCapitol() != null && this.getCiv().getCapitol().getBuffManager().hasBuff("level6_extraRangeTown")) {
                range += this.getTown().getCiv().getCapitol().getBuffManager().getEffectiveDouble("level6_extraRangeTown");
            }

            proximityComponent.setRadius(range);

            reportSeconds = reportrate;

        } catch (InvalidConfiguration e) {
            e.printStackTrace();
        }
    }

    private void scoutDebug(String str) {
        if (this.getCiv().scoutDebug && this.getCiv().scoutDebugPlayer != null) {
            Player player;
            try {
                player = CivGlobal.getPlayer(this.getCiv().scoutDebugPlayer);
            } catch (CivException e) {
                return;
            }
            CivMessage.send(player, CivColor.Yellow + "[ScoutDebug] " + str);
        }
    }

    @Override
    public int getMaxHitPoints() {
        double rate = 1.0;
        if (this.getTown().getBuffManager().hasBuff("buff_chichen_itza_tower_hp")) {
            rate += this.getTown().getBuffManager().getEffectiveDouble("buff_chichen_itza_tower_hp");
        }
        if (this.getTown().getBuffManager().hasBuff("buff_barricade")) {
            rate += this.getTown().getBuffManager().getEffectiveDouble("buff_barricade");
        }
        if (this.getCiv().getCapitol() != null && this.getCiv().getCapitol().getBuffManager().hasBuff("level6_extraTowerHPTown")) {
            rate *= this.getCiv().getCapitol().getBuffManager().getEffectiveDouble("level6_extraTowerHPTown");
        }
        return (int) ((double) this.info.max_hitpoints * rate);
    }

    /*
     * Asynchronously sweeps for players within the scout tower's radius. If
     * it finds a player that is not in the civ, then it informs the town.
     * If the town is the capitol, it informs the civ.
     */
    public void process(HashSet<String> alreadyAnnounced) {
        count++;
        if (count < reportSeconds) {
            return;
        }

        count = 0;
        boolean empty = true;

        for (PlayerLocationCache pc : proximityComponent.tryGetNearbyPlayers(true)) {
            empty = false;
            scoutDebug(CivSettings.localize.localizedString("scoutTower_debug_inspectingPlayer") + pc.getName());
            Player player;
            try {
                player = CivGlobal.getPlayer(pc.getName());
            } catch (CivException e) {
                scoutDebug(CivSettings.localize.localizedString("scoutTower_debug_notOnline"));
                return;
            }

            if (player.isOp() || player.getGameMode() != GameMode.SURVIVAL) {
                scoutDebug(CivSettings.localize.localizedString("scoutTower_debug_isOP"));
                continue;
            }

            Location center = this.getCenterLocation().getLocation();

            /* Do not re-announce players announced by other scout towers */
            if (alreadyAnnounced.contains(this.getCiv().getName() + ":" + player.getName())) {
                scoutDebug(CivSettings.localize.localizedString("scoutTower_debug_alreadyAnnounced") + pc.getName());
                continue;
            }

            /* Always announce outlaws, so skip down to bottom. */
            String relationName = "";
            String relationColor = "";
            if (!this.getTown().isOutlaw(player.getName())) {
                /* do not announce residents in this civ */
                Resident resident = CivGlobal.getResident(player);
                if (resident != null && resident.hasTown() && resident.getCiv() == this.getCiv()) {
                    scoutDebug(CivSettings.localize.localizedString("scoutTower_debug_sameCiv"));
                    continue;
                }

                /* Only announce hostile, war, and neutral players */
                Relation.Status relation = this.getCiv().getDiplomacyManager().getRelationStatus(player);
                switch (relation) {
                    case PEACE:
                    case ALLY:
//				case VASSAL:
//				case MASTER:
                        scoutDebug(CivSettings.localize.localizedString("acoutTower_debug_ally"));
                        continue;
                    default:
                        break;
                }

                relationName = relation.name();
                relationColor = Relation.getRelationColor(relation);
            } else {
                relationName = CivSettings.localize.localizedString("scoutTower_isOutlaw");
                relationColor = CivColor.Yellow;
            }


            if (center.getWorld() != this.getCorner().getLocation().getWorld()) {
                scoutDebug(CivSettings.localize.localizedString("scoutTower_debug_wrongWorld"));
                continue;
            }

            if (center.distance(player.getLocation()) < range) {
                /* Notify the town or civ. */
                CivMessage.sendScout(this.getCiv(), CivSettings.localize.localizedString("var_scoutTower_detection",
                        (relationColor + player.getName() + "(" + relationName + ")" + CivColor.White), (player.getLocation().getBlockX() + "," + player.getLocation().getBlockY() + "," + player.getLocation().getBlockZ()),
                        this.getTown().getName()));
                alreadyAnnounced.add(this.getCiv().getName() + ":" + player.getName());
                CivMessage.send(player, CivColor.RoseItalic + CivSettings.localize.localizedString("event_found_by_scoutTower", this.getTown().getName()));


            }
        }

        if (empty) {
            scoutDebug(CivSettings.localize.localizedString("scoutTower_debug_emptyCache"));
        }
    }

    @Override
    public String getMarkerIconName() {
        return "tower";
    }

    public int getReportSeconds() {
        return reportSeconds;
    }

    public void setReportSeconds(int reportSeconds) {
        this.reportSeconds = reportSeconds;
    }
}
