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
import cn.evole.plugins.civcraft.endgame.EndConditionDiplomacy;
import cn.evole.plugins.civcraft.exception.CivException;
import cn.evole.plugins.civcraft.exception.InvalidConfiguration;
import cn.evole.plugins.civcraft.exception.InvalidNameException;
import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.main.CivLog;
import cn.evole.plugins.civcraft.main.CivMessage;
import cn.evole.plugins.civcraft.object.Civilization;
import cn.evole.plugins.civcraft.object.CultureChunk;
import cn.evole.plugins.civcraft.object.Relation;
import cn.evole.plugins.civcraft.object.Resident;
import cn.evole.plugins.civcraft.sessiondb.SessionEntry;
import cn.evole.plugins.civcraft.threading.TaskMaster;
import cn.evole.plugins.civcraft.util.BlockCoord;
import cn.evole.plugins.civcraft.util.ChunkCoord;
import cn.evole.plugins.civcraft.util.CivColor;
import cn.evole.plugins.civcraft.war.War;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.UUID;

public class PlayerLoginAsyncTask implements Runnable {

    UUID playerUUID;

    public PlayerLoginAsyncTask(UUID playerUUID) {
        this.playerUUID = playerUUID;
    }

    public Player getPlayer() throws CivException {
        return Bukkit.getPlayer(playerUUID);
    }

    @Override
    public void run() {
        try {
            if (playerUUID == null || getPlayer() == null) {
                CivLog.info("Skipping PlayerLoginAsyncTask, otherwise NPE.");
                return;
            }
            CivLog.info("Running PlayerLoginAsyncTask for " + getPlayer().getName() + " UUID(" + playerUUID + ")");
            //Resident resident = CivGlobal.getResident(getPlayer().getName());
            Resident resident = CivGlobal.getResidentViaUUID(playerUUID);
            if (resident != null && !resident.getName().equals(getPlayer().getName())) {
                CivGlobal.removeResident(resident);
                resident.setName(getPlayer().getName());
                resident.save();
                CivGlobal.addResident(resident);
            }

            /*
              测试看看玩家是否改变了他们的名字。如果他们有，这些居民将不会匹配。不允许玩家在未经管理员批准的情况下更改姓名。
             */
            if (CivGlobal.getResidentViaUUID(getPlayer().getUniqueId()) != resident) {
                TaskMaster.syncTask(new PlayerKickBan(getPlayer().getName(), true, false,
                        CivSettings.localize.localizedString("PlayerLoginAsync_usernameChange1") +
                                CivSettings.localize.localizedString("PlayerLoginAsync_usernameChange2")));
                return;
            }

            if (resident == null) {
                CivLog.info("No resident found. Creating for " + getPlayer().getName());
                try {
                    resident = new Resident(getPlayer().getUniqueId(), getPlayer().getName());
                } catch (InvalidNameException e) {
                    TaskMaster.syncTask(new PlayerKickBan(getPlayer().getName(), true, false, CivSettings.localize.localizedString("PlayerLoginAsync_usernameInvalid")));
                    return;
                }

                CivGlobal.addResident(resident);
                CivLog.info("Added resident:" + resident.getName());
                resident.setRegistered(System.currentTimeMillis());
//				CivTutorial.showTutorialInventory(getPlayer());
                resident.setisProtected(true);
                int mins;
                try {
                    mins = CivSettings.getInteger(CivSettings.civConfig, "global.pvp_timer");
                } catch (InvalidConfiguration e1) {
                    e1.printStackTrace();
                    return;
                }
                CivMessage.send(resident, CivColor.LightGray + CivSettings.localize.localizedString("var_PlayerLoginAsync_pvpTimerPropmt1", mins));
                CivMessage.send(resident, CivColor.LightGray + CivSettings.localize.localizedString("PlayerLoginAsync_pvpTimerPropmt2"));

            }

            /*
             * Resident is present. Lets check the UUID against the stored UUID.
             * We are not going to allow residents to change names without admin permission.
             * If someone logs in with a name that does not match the stored UUID, we'll kick them.
             */
            if (resident.getUUID() == null) {
                /* This resident does not yet have a UUID stored. Free lunch. */
                resident.setUUID(getPlayer().getUniqueId());
                CivLog.info("Resident named:" + resident.getName() + " was acquired by UUID:" + resident.getUUIDString());
            } else if (!resident.getUUID().equals(getPlayer().getUniqueId())) {
                TaskMaster.syncTask(new PlayerKickBan(getPlayer().getName(), true, false, CivSettings.localize.localizedString("PlayerLoginAsync_usernameInUse")));
                return;
            }

            if (!resident.isGivenKit()) {
                TaskMaster.syncTask(new GivePlayerStartingKit(resident.getName()));
            }

            if (War.isWarTime() && War.isOnlyWarriors()) {
                if (getPlayer().isOp() || getPlayer().hasPermission(CivSettings.MINI_ADMIN)) {
                    //Allowed to connect since player is OP or mini admin.
                } else if (!resident.hasTown() || !resident.getTown().getCiv().getDiplomacyManager().isAtWar()) {
                    TaskMaster.syncTask(new PlayerKickBan(getPlayer().getName(), true, false, CivSettings.localize.localizedString("PlayerLoginAsync_onlyWarriorsAllowed")));
                    return;
                }
            }

            /* turn on allchat by default for admins and moderators. */
            if (getPlayer().hasPermission(CivSettings.MODERATOR) || getPlayer().hasPermission(CivSettings.MINI_ADMIN)) {
                resident.allchat = true;
                Resident.allchatters.add(resident.getName());
            }

            if (resident.getTreasury().inDebt()) {
                TaskMaster.asyncTask("", new PlayerDelayedDebtWarning(resident), 1000);
            }

            if (!getPlayer().isOp()) {
                CultureChunk cc = CivGlobal.getCultureChunk(new ChunkCoord(getPlayer().getLocation()));
                if (cc != null && cc.getCiv() != resident.getCiv()) {
                    Relation.Status status = cc.getCiv().getDiplomacyManager().getRelationStatus(getPlayer());
                    String color = PlayerChunkNotifyAsyncTask.getNotifyColor(cc, status, getPlayer());
                    String relationName = status.name();

                    if (War.isWarTime() && status.equals(Relation.Status.WAR) && !cc.getCiv().isAdminCiv()) {
                        /*
                         * Test for players who were not logged in when war time started.
                         * If they were not logged in, they are enemies, and are inside our borders
                         * they need to be teleported back to their own town hall.
                         */

                        if (resident.getLastOnline() < War.getStart().getTime()) {
                            resident.teleportHome();
                            CivMessage.send(resident, CivColor.LightGray + CivSettings.localize.localizedString("PlayerLoginAsync_loginDuringWar"));
                        }
                    } else if (!status.equals(Relation.Status.ALLY) && !status.equals(Relation.Status.PEACE) && !cc.getCiv().isAdminCiv()) {
                        resident.teleportHome();
                        CivMessage.send(resident, CivColor.LightGray + CivSettings.localize.localizedString("PlayerLoginAsync_loginNotAllies"));
                    }

                    CivMessage.sendCiv(cc.getCiv(), CivSettings.localize.localizedString("var_PlayerLoginAsync_inBorderAlert", (color + getPlayer().getDisplayName() + "(" + relationName + ")")));
                }
            }

            resident.setLastOnline(System.currentTimeMillis());
            resident.setLastIP(getPlayer().getAddress().getAddress().getHostAddress());
            resident.setSpyExposure(resident.getSpyExposure());
            resident.save();

            //TODO send town board messages?
            //TODO set default modes?
            resident.showWarnings(getPlayer());
            resident.loadPerks(getPlayer());
            resident.calculateWalkingModifier(getPlayer());

            // Check for pending respawns.
            ArrayList<SessionEntry> entries = CivGlobal.getSessionDB().lookup("global:respawnPlayer");
            ArrayList<SessionEntry> deleted = new ArrayList<SessionEntry>();

            for (SessionEntry e : entries) {
                String[] split = e.value.split(":");

                BlockCoord coord = new BlockCoord(split[1]);
                getPlayer().teleport(coord.getLocation());
                deleted.add(e);
            }

            for (SessionEntry e : deleted) {
                CivGlobal.getSessionDB().delete(e.request_id, "global:respawnPlayer");
            }

            try {
                Player p = CivGlobal.getPlayer(resident);

                ArrayList<SessionEntry> deathEvents = CivGlobal.getSessionDB().lookup("pvplogger:death:" + resident.getName());
                if (deathEvents.size() != 0) {
                    CivMessage.send(resident, CivColor.Rose + CivColor.BOLD + CivSettings.localize.localizedString("PlayerLoginAsync_killedWhilePVPLogged"));
                    class SyncTask implements Runnable {
                        String playerName;

                        public SyncTask(String playerName) {
                            this.playerName = playerName;
                        }

                        @Override
                        public void run() {
                            Player p;
                            try {
                                p = CivGlobal.getPlayer(playerName);
                                p.setHealth(0);
                                CivGlobal.getSessionDB().delete_all("pvplogger:death:" + p.getName());
                            } catch (CivException e) {
                                // You cant excape death that easily!
                            }
                        }
                    }

                    TaskMaster.syncTask(new SyncTask(p.getName()));
                }
            } catch (CivException e1) {
            }

            if (EndConditionDiplomacy.canPeopleVote()) {
                CivMessage.send(resident, CivColor.LightGreen + CivSettings.localize.localizedString("PlayerLoginAsync_councilOf8"));
            }
            Civilization civ = resident.getCiv();
            if (civ != null) {
                if (civ.MOTD() != null) {
                    CivMessage.send(resident, CivColor.LightPurple + "[Civ MOTD] " + CivColor.White + resident.getCiv().MOTD());
                }
            }

        } catch (CivException playerNotFound) {
            // Player logged out while async task was running.
        } catch (InvalidNameException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }


}
