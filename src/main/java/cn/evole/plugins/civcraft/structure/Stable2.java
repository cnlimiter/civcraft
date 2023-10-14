package cn.evole.plugins.civcraft.structure;

import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.exception.CivException;
import cn.evole.plugins.civcraft.exception.InvalidConfiguration;
import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.main.CivLog;
import cn.evole.plugins.civcraft.main.CivMessage;
import cn.evole.plugins.civcraft.object.Civilization;
import cn.evole.plugins.civcraft.object.Resident;
import cn.evole.plugins.civcraft.object.StructureSign;
import cn.evole.plugins.civcraft.object.Town;
import cn.evole.plugins.civcraft.util.BlockCoord;
import cn.evole.plugins.civcraft.util.CivColor;
import cn.evole.plugins.civcraft.util.ItemManager;
import cn.evole.plugins.civcraft.util.SimpleBlock;
import cn.evole.plugins.civcraft.war.War;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class Stable2 extends Structure {
    private StructureSign respawnSign;
    private int index = 0;

    public Stable2(ResultSet rs) throws SQLException, CivException {
        super(rs);
    }

    protected Stable2(Location center, String id, Town town) throws CivException {
        super(center, id, town);
    }

    @Override
    public String getMarkerIconName() {
        return "pin";
    }

    private RespawnLocationHolder getSelectedHolder() {
        ArrayList<RespawnLocationHolder> respawnables = this.getTown().getCiv().getAvailableRespawnables();
        return respawnables.get(this.index);
    }

    private void changeIndex(int newIndex) {
        ArrayList<RespawnLocationHolder> respawnables = this.getTown().getCiv().getAvailableRespawnables();
        if (this.respawnSign != null) {
            block4:
            {
                try {
                    this.respawnSign.setText(CivSettings.localize.localizedString("stable_sign_respawnAt") + "\n" + CivColor.GreenBold + respawnables.get(newIndex).getRespawnName());
                    this.index = newIndex;
                } catch (IndexOutOfBoundsException e) {
                    if (respawnables.size() <= 0) break block4;
                    this.respawnSign.setText(CivSettings.localize.localizedString("stable_sign_respawnAt") + "\n" + CivColor.GreenBold + respawnables.get(0).getRespawnName());
                    this.index = 0;
                }
            }
            this.respawnSign.update();
        } else {
            CivLog.warning("Could not find civ spawn sign:" + this.getId() + " at " + this.getCorner());
        }
    }

    @Override
    public void processSignAction(Player player, StructureSign sign, PlayerInteractEvent event) {
        Resident resident = CivGlobal.getResident(player);
        if (resident == null) {
            return;
        }
        if (War.isWarTime()) {
            CivMessage.sendError(resident, CivSettings.localize.localizedString("stable_wartime_returned"));
            return;
        }
        Boolean hasPermission = false;
        Civilization civ = this.getTown().getCiv();
        if (civ.hasResident(resident)) {
            hasPermission = true;
        }
        if (!hasPermission) {
            CivMessage.sendError(resident, CivSettings.localize.localizedString("stable_Sign_noPermission"));
            return;
        }
        switch (sign.getAction()) {
            case "prev": {
                this.changeIndex(this.index - 1);
                break;

            }
            case "next": {
                this.changeIndex(this.index + 1);
                break;

            }
            case "respawn": {
                long timeNow;
                ArrayList<RespawnLocationHolder> respawnables = this.getTown().getCiv().getAvailableRespawnables();
                if (this.index >= respawnables.size()) {
                    this.index = 0;
                    this.changeIndex(this.index);
                    CivMessage.sendError(resident, CivSettings.localize.localizedString("stable_cannotRespawn"));
                    return;
                }
                respawnables.get(this.index).getRandomRevivePoint();
                long nextTeleport = resident.getNextTeleport();
                SimpleDateFormat sdf = new SimpleDateFormat("M/dd h:mm:ss a z");
                if (nextTeleport > (timeNow = Calendar.getInstance().getTimeInMillis())) {
                    CivMessage.sendError(resident, CivSettings.localize.localizedString("var_stable2_teleportNotAva", sdf.format(nextTeleport)));
                    return;
                }
                RespawnLocationHolder holder = this.getSelectedHolder();
                Town toTeleport = CivGlobal.getCultureChunk(holder.getRandomRevivePoint().getLocation()).getTown();
                boolean hasStable = false;
                Location placeToTeleport = null;
                for (Structure structure : toTeleport.getStructures()) {
                    if (!(structure instanceof Stable2)) continue;
                    hasStable = true;
                    placeToTeleport = ((Stable2) structure).respawnSign.getCoord().getLocation();
                    break;
                }
                if (!hasStable) {
                    CivMessage.sendError(resident, CivSettings.localize.localizedString("stable_Sign_teleport_noStable", toTeleport.getName()));
                    return;
                }
                if (!resident.getTreasury().hasEnough(2000.0)) {
                    CivMessage.sendError(resident, CivSettings.localize.localizedString("shipyard_Sign_noMoney", CivColor.Gold + (2000.0 - resident.getTreasury().getBalance()), CivColor.Green + CivSettings.CURRENCY_NAME + CivColor.Red));
                    return;
                }
                nextTeleport = timeNow + 120000L;
                CivMessage.send((Object) player, CivColor.Green + CivSettings.localize.localizedString("stable_respawningAlert"));
                player.teleport(placeToTeleport);
                resident.getTreasury().withdraw(2000.0);
                resident.setNextTeleport(nextTeleport);
                resident.save();
            }
        }
    }

    @Override
    public void onPostBuild(BlockCoord absCoord, SimpleBlock commandBlock) {
        switch (commandBlock.command) {
            case "/next": {
                ItemManager.setTypeId(absCoord.getBlock(), commandBlock.getType());
                ItemManager.setData(absCoord.getBlock(), commandBlock.getData());
                StructureSign structSign = new StructureSign(absCoord, this);
                structSign.setText("\n" + ChatColor.BOLD + ChatColor.UNDERLINE + CivSettings.localize.localizedString("stable_sign_nextLocation"));
                structSign.setDirection(commandBlock.getData());
                structSign.setAction("next");
                structSign.update();
                this.addStructureSign(structSign);
                CivGlobal.addStructureSign(structSign);
                break;
            }
            case "/prev": {
                ItemManager.setTypeId(absCoord.getBlock(), commandBlock.getType());
                ItemManager.setData(absCoord.getBlock(), commandBlock.getData());
                StructureSign structSign = new StructureSign(absCoord, this);
                structSign.setText("\n" + ChatColor.BOLD + ChatColor.UNDERLINE + CivSettings.localize.localizedString("stable_sign_previousLocation"));
                structSign.setDirection(commandBlock.getData());
                structSign.setAction("prev");
                structSign.update();
                this.addStructureSign(structSign);
                CivGlobal.addStructureSign(structSign);
                break;
            }
            case "/respawn": {
                ItemManager.setTypeId(absCoord.getBlock(), commandBlock.getType());
                ItemManager.setData(absCoord.getBlock(), commandBlock.getData());
                StructureSign structSign = new StructureSign(absCoord, this);
                structSign.setText(CivSettings.localize.localizedString("stable_sign_Stable"));
                structSign.setDirection(commandBlock.getData());
                structSign.setAction("respawn");
                structSign.update();
                this.addStructureSign(structSign);
                CivGlobal.addStructureSign(structSign);
                this.respawnSign = structSign;
                this.changeIndex(this.index);
            }
        }
    }

    @Override
    public void onInvalidPunish() {
        int invalid_respawn_penalty;
        try {
            invalid_respawn_penalty = CivSettings.getInteger(CivSettings.warConfig, "war.invalid_respawn_penalty");
        } catch (InvalidConfiguration e) {
            e.printStackTrace();
            return;
        }
        CivMessage.sendTown(this.getTown(), CivColor.RoseBold + CivSettings.localize.localizedString("stable_cannotSupport1") + " " + CivSettings.localize.localizedString("var_stable_cannotSupport2", invalid_respawn_penalty));
    }

    @Override
    public boolean isValid() {
        if (this.getCiv().isAdminCiv()) {
            return true;
        }
        for (Town town : this.getCiv().getTowns()) {
            TownHall townhall = town.getTownHall();
            if (townhall != null) continue;
            return false;
        }
        return super.isValid();
    }
}

