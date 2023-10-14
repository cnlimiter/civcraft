package cn.evole.plugins.civcraft.structure;

import cn.evole.plugins.civcraft.components.AttributeBiomeRadiusPerLevel;
import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.exception.CivException;
import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.main.CivLog;
import cn.evole.plugins.civcraft.main.CivMessage;
import cn.evole.plugins.civcraft.object.*;
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

public class Shipyard extends WaterStructure {
    private StructureSign respawnSign;
    private int index = 0;

    protected Shipyard(Location center, String id, Town town) throws CivException {
        super(center, id, town);
    }

    public Shipyard(ResultSet rs) throws SQLException, CivException {
        super(rs);
    }

    @Override
    public void loadSettings() {
        super.loadSettings();
    }

    public String getkey() {
        return getTown().getName() + "_" + this.getConfigId() + "_" + this.getCorner().toString();
    }

    @Override
    public String getDynmapDescription() {
        return null;
    }

    @Override
    public String getMarkerIconName() {
        return "anchor";
    }

    public double getHammersPerTile() {
        AttributeBiomeRadiusPerLevel attrBiome = (AttributeBiomeRadiusPerLevel) this.getComponent("AttributeBiomeBase");
        double base = attrBiome.getBaseValue();

        double rate = 1;
        rate += this.getTown().getBuffManager().getEffectiveDouble(Buff.ADVANCED_TOOLING);
        return (rate * base);
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
        switch (sign.getAction()) {
            case "prev": {
                if (hasPermission.booleanValue()) {
                    this.changeIndex(this.index - 1);
                    break;
                }
                CivMessage.sendError(resident, CivSettings.localize.localizedString("stable_Sign_noPermission"));
                break;
            }
            case "next": {
                if (hasPermission.booleanValue()) {
                    this.changeIndex(this.index + 1);
                    break;
                }
                CivMessage.sendError(resident, CivSettings.localize.localizedString("stable_Sign_noPermission"));
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
                if (!hasPermission.booleanValue()) {
                    CivMessage.sendError(resident, CivSettings.localize.localizedString("stable_Sign_noPermission"));
                    return;
                }
                long nextTeleport = resident.getNextTeleport();
                if (nextTeleport > (timeNow = Calendar.getInstance().getTimeInMillis())) {
                    SimpleDateFormat sdf = new SimpleDateFormat("M/dd h:mm:ss a z");
                    CivMessage.sendError(resident, CivSettings.localize.localizedString("var_stable2_teleportNotAva", sdf.format(nextTeleport)));
                    return;
                }
                RespawnLocationHolder holder = this.getSelectedHolder();
                Town toTeleport = CivGlobal.getCultureChunk(holder.getRandomRevivePoint().getLocation()).getTown();
                boolean hasShipyard = false;
                Location placeToTeleport = null;
                for (Structure structure : toTeleport.getStructures()) {
                    if (!(structure instanceof Shipyard)) continue;
                    hasShipyard = true;
                    placeToTeleport = ((Shipyard) structure).respawnSign.getCoord().getLocation();
                    break;
                }
                if (!hasShipyard) {
                    CivMessage.sendError(resident, CivSettings.localize.localizedString("shipyard_Sign_teleport_noShupyard", toTeleport.getName()));
                    return;
                }
                if (!resident.getTreasury().hasEnough(1000.0)) {
                    CivMessage.sendError(resident, CivSettings.localize.localizedString("shipyard_Sign_noMoney", CivColor.Gold + (2000.0 - resident.getTreasury().getBalance()), CivColor.Green + CivSettings.CURRENCY_NAME + CivColor.Red));
                    return;
                }
                nextTeleport = timeNow + 60000L;
                CivMessage.send((Object) player, "§a" + CivSettings.localize.localizedString("stable_respawningAlert"));
                player.teleport(placeToTeleport);
                resident.getTreasury().withdraw(1000.0);
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
                structSign.setText("\n" + (Object) ChatColor.BOLD + (Object) ChatColor.UNDERLINE + CivSettings.localize.localizedString("stable_sign_nextLocation"));
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
                structSign.setText("\n" + (Object) ChatColor.BOLD + (Object) ChatColor.UNDERLINE + CivSettings.localize.localizedString("stable_sign_previousLocation"));
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
                structSign.setText(CivSettings.localize.localizedString("shipyard_sign"));
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
}
