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

import cn.evole.plugins.civcraft.components.ProjectileArrowComponent;
import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.exception.CivException;
import cn.evole.plugins.civcraft.exception.InvalidConfiguration;
import cn.evole.plugins.civcraft.main.CivData;
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
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class Capitol extends TownHall {

    private HashMap<Integer, ProjectileArrowComponent> arrowTowers = new HashMap<Integer, ProjectileArrowComponent>();
    private StructureSign respawnSign;
    private int index = 0;

    public Capitol(ResultSet rs) throws SQLException, CivException {
        super(rs);
    }


    protected Capitol(Location center, String id, Town town)
            throws CivException {
        super(center, id, town);
    }

    private RespawnLocationHolder getSelectedHolder() {
        ArrayList<RespawnLocationHolder> respawnables = this.getTown().getCiv().getAvailableRespawnables();
        return respawnables.get(index);
    }

    private void changeIndex(int newIndex) {
        ArrayList<RespawnLocationHolder> respawnables = this.getTown().getCiv().getAvailableRespawnables();

        if (this.respawnSign != null) {
            try {
                this.respawnSign.setText(CivSettings.localize.localizedString("capitol_sign_respawnAt") + "\n" + CivColor.Green + CivColor.BOLD + respawnables.get(newIndex).getRespawnName());
                index = newIndex;
            } catch (IndexOutOfBoundsException e) {
                if (respawnables.size() > 0) {
                    this.respawnSign.setText(CivSettings.localize.localizedString("capitol_sign_respawnAt") + "\n" + CivColor.Green + CivColor.BOLD + respawnables.get(0).getRespawnName());
                    index = 0;
                }
                //this.unitNameSign.setText(getUnitSignText(index));
            }
            this.respawnSign.update();
        } else {
            CivLog.warning("Could not find civ spawn sign:" + this.getId() + " at " + this.getCorner());
        }
    }

    @Override
    public void processSignAction(Player player, StructureSign sign, PlayerInteractEvent event) {
        //int special_id = Integer.valueOf(sign.getAction());
        Resident resident = CivGlobal.getResident(player);

        if (resident == null) {
            return;
        }

        if (!War.isWarTime()) {
            return;
        }
        boolean hasPermission = false;
        if (resident.getTown().isMayor(resident)
                || resident.getTown().getAssistantGroup().hasMember(resident)
                || resident.getCiv().getLeaderGroup().hasMember(resident)
                || resident.getCiv().getAdviserGroup().hasMember(resident)) {
            hasPermission = true;
        }

        switch (sign.getAction()) {
            case "prev":
                if (hasPermission) {
                    changeIndex((index - 1));
                } else {
                    CivMessage.sendError(resident, CivSettings.localize.localizedString("capitol_Sign_noPermission"));
                }
                break;
            case "next":
                if (hasPermission) {
                    changeIndex((index + 1));
                } else {
                    CivMessage.sendError(resident, CivSettings.localize.localizedString("capitol_Sign_noPermission"));
                }
                break;
            case "respawn":
                ArrayList<RespawnLocationHolder> respawnables = this.getTown().getCiv().getAvailableRespawnables();
                if (index >= respawnables.size()) {
                    index = 0;
                    changeIndex(index);
                    CivMessage.sendError(resident, CivSettings.localize.localizedString("capitol_cannotRespawn"));
                    return;
                }

                RespawnLocationHolder holder = getSelectedHolder();
                int respawnTimeSeconds = this.getRespawnTime();
                Date now = new Date();

                if (resident.getLastKilledTime() != null) {
                    long secondsLeft = (resident.getLastKilledTime().getTime() + (respawnTimeSeconds * 1000)) - now.getTime();
                    if (secondsLeft > 0) {
                        secondsLeft /= 1000;
                        CivMessage.sendError(resident, CivColor.Rose + CivSettings.localize.localizedString("var_capitol_secondsLeftTillRespawn", secondsLeft));
                        return;
                    }
                }

                BlockCoord revive = holder.getRandomRevivePoint();
                Location loc;
                if (revive == null) {
                    loc = player.getBedSpawnLocation();
                } else {
                    loc = revive.getLocation();
                }

                CivMessage.send(player, CivColor.LightGreen + CivSettings.localize.localizedString("capitol_respawningAlert"));
                player.teleport(loc);
                break;
        }
    }


    @Override
    public void onPostBuild(BlockCoord absCoord, SimpleBlock commandBlock) {
        StructureSign structSign;

        if (commandBlock.command.equals("/towerfire")) {
            String id = commandBlock.keyvalues.get("id");
            Integer towerID = Integer.valueOf(id);

            if (!arrowTowers.containsKey(towerID)) {

                ProjectileArrowComponent arrowTower = new ProjectileArrowComponent(this, absCoord.getLocation());
                arrowTower.createComponent(this);
                arrowTower.setTurretLocation(absCoord);

                arrowTowers.put(towerID, arrowTower);
            }
        } else if (commandBlock.command.equals("/next")) {
            ItemManager.setTypeId(absCoord.getBlock(), commandBlock.getType());
            ItemManager.setData(absCoord.getBlock(), commandBlock.getData());

            structSign = new StructureSign(absCoord, this);
            structSign.setText("\n" + ChatColor.BOLD + ChatColor.UNDERLINE + CivSettings.localize.localizedString("capitol_sign_nextLocation"));
            structSign.setDirection(commandBlock.getData());
            structSign.setAction("next");
            structSign.update();
            this.addStructureSign(structSign);
            CivGlobal.addStructureSign(structSign);

        } else if (commandBlock.command.equals("/prev")) {
            ItemManager.setTypeId(absCoord.getBlock(), commandBlock.getType());
            ItemManager.setData(absCoord.getBlock(), commandBlock.getData());
            structSign = new StructureSign(absCoord, this);
            structSign.setText("\n" + ChatColor.BOLD + ChatColor.UNDERLINE + CivSettings.localize.localizedString("capitol_sign_previousLocation"));
            structSign.setDirection(commandBlock.getData());
            structSign.setAction("prev");
            structSign.update();
            this.addStructureSign(structSign);
            CivGlobal.addStructureSign(structSign);

        } else if (commandBlock.command.equals("/respawndata")) {
            ItemManager.setTypeId(absCoord.getBlock(), commandBlock.getType());
            ItemManager.setData(absCoord.getBlock(), commandBlock.getData());
            structSign = new StructureSign(absCoord, this);
            structSign.setText(CivSettings.localize.localizedString("capitol_sign_Capitol"));
            structSign.setDirection(commandBlock.getData());
            structSign.setAction("respawn");
            structSign.update();
            this.addStructureSign(structSign);
            CivGlobal.addStructureSign(structSign);

            this.respawnSign = structSign;
            changeIndex(index);
        }

    }

    @Override
    public void createControlPoint(BlockCoord absCoord, String info) {

        Location centerLoc = absCoord.getLocation();

        /* Build the bedrock tower. */
        //for (int i = 0; i < 1; i++) {
        Block b = centerLoc.getBlock();
        ItemManager.setTypeId(b, ItemManager.getId(Material.SANDSTONE));
        ItemManager.setData(b, 0);

        StructureBlock sb = new StructureBlock(new BlockCoord(b), this);
        this.addStructureBlock(sb.getCoord(), true);
        //}

        /* Build the control block. */
        b = centerLoc.getBlock().getRelative(0, 1, 0);
        ItemManager.setTypeId(b, CivData.OBSIDIAN);
        sb = new StructureBlock(new BlockCoord(b), this);
        this.addStructureBlock(sb.getCoord(), true);

        int capitolControlHitpoints = this.getTown().getBuffManager().hasBuff("buff_chichen_itza_tower_hp") && this.getTown().getBuffManager().hasBuff("buff_greatlibrary_extra_beakers") ? 150 : 100;
        if (this.getTown().hasStructure("s_castle")) {
            capitolControlHitpoints += 3;
        }
        if (this.getCiv().getCapitol() != null && this.getCiv().getCapitol().getBuffManager().hasBuff("level6_extraHPcpTown")) {
            capitolControlHitpoints = (int) ((double) capitolControlHitpoints * 1.2);
        }
        if (this.getCiv().getCapitol() != null && this.getCiv().getCapitol().getBuffManager().hasBuff("level10_dominatorTown")) {
            capitolControlHitpoints *= 2;
        }

        BlockCoord coord = new BlockCoord(b);
        this.controlPoints.put(coord, new ControlPoint(coord, this, capitolControlHitpoints, info));
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

        CivMessage.sendTown(this.getTown(), CivColor.Rose + CivColor.BOLD + CivSettings.localize.localizedString("capitol_cannotSupport1") +
                " " + CivSettings.localize.localizedString("var_capitol_cannotSupport2", invalid_respawn_penalty));
    }

    @Override
    public boolean isValid() {
        if (this.getCiv().isAdminCiv()) {
            return true;
        }

        /*
         * Validate that all of the towns in our civ have town halls. If not, then
         * we need to punish by increasing respawn times.
         */
        for (Town town : this.getCiv().getTowns()) {
            TownHall townhall = town.getTownHall();
            if (townhall == null) {
                return false;
            }
        }

        return super.isValid();
    }

    @Override
    public String getRespawnName() {
        return "Capitol\n" + this.getTown().getName();
    }


    public void updateRespawnSigns() {
        // TODO Auto-generated method stub

    }
}
