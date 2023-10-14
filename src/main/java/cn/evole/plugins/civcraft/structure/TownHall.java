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

import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.config.ConfigCultureLevel;
import cn.evole.plugins.civcraft.config.ConfigTech;
import cn.evole.plugins.civcraft.exception.CivException;
import cn.evole.plugins.civcraft.exception.InvalidConfiguration;
import cn.evole.plugins.civcraft.items.BonusGoodie;
import cn.evole.plugins.civcraft.main.CivData;
import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.main.CivLog;
import cn.evole.plugins.civcraft.main.CivMessage;
import cn.evole.plugins.civcraft.object.*;
import cn.evole.plugins.civcraft.siege.CannonProjectile;
import cn.evole.plugins.civcraft.util.*;
import cn.evole.plugins.civcraft.war.War;
import cn.evole.plugins.civcraft.war.WarStats;
import org.bukkit.*;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class TownHall extends Structure implements RespawnLocationHolder {

    //TODO make this configurable.
    public static int MAX_GOODIE_FRAMES = 8;
    public ArrayList<BlockCoord> nextGoodieFramePoint = new ArrayList<BlockCoord>();
    public ArrayList<Integer> nextGoodieFrameDirection = new ArrayList<Integer>();
    protected HashMap<BlockCoord, ControlPoint> controlPoints = new HashMap<BlockCoord, ControlPoint>();
    private BlockCoord[] techbar = new BlockCoord[10];
    private BlockCoord technameSign;
    private byte technameSignData; //Hold the sign's orientation
    private BlockCoord techdataSign;
    private byte techdataSignData; //Hold the sign's orientation
    private ArrayList<ItemFrameStorage> goodieFrames = new ArrayList<ItemFrameStorage>();
    private ArrayList<BlockCoord> respawnPoints = new ArrayList<BlockCoord>();
    private ArrayList<BlockCoord> revivePoints = new ArrayList<BlockCoord>();

    protected TownHall(Location center, String id, Town town)
            throws CivException {
        super(center, id, town);
    }

    public TownHall(ResultSet rs) throws SQLException, CivException {
        super(rs);
    }

    @Override
    public void delete() throws SQLException {
        if (this.getTown() != null) {
            /* Remove any protected item frames. */
            for (ItemFrameStorage framestore : goodieFrames) {
                BonusGoodie goodie = CivGlobal.getBonusGoodie(framestore.getItem());
                if (goodie != null) {
                    goodie.replenish();
                }

                CivGlobal.removeProtectedItemFrame(framestore.getFrameID());
            }
        }

        super.delete();
    }

    @Override
    public String getDynmapDescription() {
        String out = "";
        out += "<b>" + CivSettings.localize.localizedString("var_townHall_dynmap_heading", this.getTown().getName()) + "</b>";
        ConfigCultureLevel culturelevel = CivSettings.cultureLevels.get(this.getTown().getCultureLevel());
        out += "<br/>" + CivSettings.localize.localizedString("townHall_dynmap_cultureLevel") + " " + culturelevel.level + " (" + this.getTown().getAccumulatedCulture() + "/" + culturelevel.amount + ")";
        out += "<br/>" + CivSettings.localize.localizedString("townHall_dynmap_flatTax") + " " + this.getTown().getFlatTax() * 100 + "%";
        out += "<br/>" + CivSettings.localize.localizedString("townHall_dynmap_propertyTax") + " " + this.getTown().getTaxRate() * 100 + "%";
        return out;
    }

    public void addTechBarBlock(BlockCoord coord, int index) {
        techbar[index] = coord;
    }

    public BlockCoord getTechBarBlockCoord(int i) {
        if (techbar[i] == null)
            return null;

        return techbar[i];
    }

    public BlockCoord getTechnameSign() {
        return technameSign;
    }

    public void setTechnameSign(BlockCoord technameSign) {
        this.technameSign = technameSign;
    }

    public BlockCoord getTechdataSign() {
        return techdataSign;
    }

    public void setTechdataSign(BlockCoord techdataSign) {
        this.techdataSign = techdataSign;
    }

    public byte getTechdataSignData() {
        return techdataSignData;
    }

    public void setTechdataSignData(byte techdataSignData) {
        this.techdataSignData = techdataSignData;
    }

    public byte getTechnameSignData() {
        return technameSignData;
    }

    public void setTechnameSignData(byte technameSignData) {
        this.technameSignData = technameSignData;
    }

    public BlockCoord getTechBar(int i) {
        return techbar[i];
    }

    public void createGoodieItemFrame(BlockCoord absCoord, int slotId, int direction) {
        if (slotId >= MAX_GOODIE_FRAMES) {
            return;
        }

        /*
         * Make sure there isn't another frame here. We have the position of the sign, but the entity's
         * position is the block it's attached to. We'll use the direction from the sign data to determine
         * which direction to look for the entity.
         */
        Block attachedBlock;
        BlockFace facingDirection;

        switch (direction) {
            case CivData.DATA_SIGN_EAST:
                attachedBlock = absCoord.getBlock();
                facingDirection = BlockFace.EAST;
                break;
            case CivData.DATA_SIGN_WEST:
                attachedBlock = absCoord.getBlock();
                facingDirection = BlockFace.WEST;
                break;
            case CivData.DATA_SIGN_NORTH:
                attachedBlock = absCoord.getBlock();
                facingDirection = BlockFace.NORTH;
                break;
            case CivData.DATA_SIGN_SOUTH:
                attachedBlock = absCoord.getBlock();
                facingDirection = BlockFace.SOUTH;
                break;
            default:
                CivLog.error("Bad sign data for /itemframe sign in town hall.");
                return;
        }

        Block itemFrameBlock = absCoord.getBlock();
        if (ItemManager.getId(itemFrameBlock) != CivData.AIR) {
            ItemManager.setTypeId(itemFrameBlock, CivData.AIR);
        }

        ItemFrameStorage itemStore;
        ItemFrame frame = null;
        Entity entity = CivGlobal.getEntityAtLocation(absCoord.getBlock().getLocation());
        if (entity == null || (!(entity instanceof ItemFrame))) {
            itemStore = new ItemFrameStorage(attachedBlock.getLocation(), facingDirection);
        } else {
            try {
                frame = (ItemFrame) entity;
                itemStore = new ItemFrameStorage(frame, attachedBlock.getLocation());
            } catch (CivException e) {
                e.printStackTrace();
                return;
            }
            if (facingDirection != BlockFace.EAST) {
                itemStore.setFacingDirection(facingDirection);
            }
        }

        itemStore.setBuildable(this);
        goodieFrames.add(itemStore);

    }

    public ArrayList<ItemFrameStorage> getGoodieFrames() {
        return this.goodieFrames;
    }

    public void setRespawnPoint(BlockCoord absCoord) {
        this.respawnPoints.add(absCoord);
    }

    public BlockCoord getRandomRespawnPoint() {
        if (this.respawnPoints.size() == 0) {
            return null;
        }

        Random rand = new Random();
        return this.respawnPoints.get(rand.nextInt(this.respawnPoints.size()));

    }

    @Override
    public boolean isTeleportReal() {
        if (this.getTown().isCapitol()) {
            return true;
        }
        for (final ControlPoint c : this.controlPoints.values()) {
            if (c.isDestroyed()) {
                return false;
            }
        }
        return true;
    }


    public int getRespawnTime() {
        try {
            int baseRespawn = CivSettings.getInteger(CivSettings.warConfig, "war.respawn_time");
            int controlRespawn = CivSettings.getInteger(CivSettings.warConfig, "war.control_block_respawn_time");
            int invalidRespawnPenalty = CivSettings.getInteger(CivSettings.warConfig, "war.invalid_respawn_penalty");

            int totalRespawn = baseRespawn;
            for (ControlPoint cp : this.controlPoints.values()) {
                if (cp.isDestroyed()) {
                    totalRespawn += controlRespawn;
                }
            }

            if (this.validated && !this.isValid()) {
                totalRespawn += invalidRespawnPenalty * 60;
            }

            // Search for any town in our civ with the medicine goodie.
            for (Town t : this.getCiv().getTowns()) {
                if (t.getBuffManager().hasBuff(Buff.MEDICINE)) {
                    int respawnTimeBonus = t.getBuffManager().getEffectiveInt(Buff.MEDICINE);
                    totalRespawn = Math.max(1, (totalRespawn - respawnTimeBonus));
                    break;
                }
            }


            return totalRespawn;
        } catch (InvalidConfiguration e) {
            e.printStackTrace();
        }
        return 60;
    }

    public void setRevivePoint(BlockCoord absCoord) {
        this.revivePoints.add(absCoord);
    }

    public BlockCoord getRandomRevivePoint() {
        if (this.revivePoints.size() == 0 || !this.isComplete()) {
            return new BlockCoord(this.getCorner());
        }
        Random rand = new Random();
        int index = rand.nextInt(this.revivePoints.size());
        return this.revivePoints.get(index);

    }

    public void createControlPoint(BlockCoord absCoord, String info) {

        Location centerLoc = absCoord.getLocation();

        /* Build the fence block. */
        //for (int i = 0; i < 1; i++) {
        Block b = centerLoc.getBlock();
        ItemManager.setTypeId(b, CivData.FENCE);
        ItemManager.setData(b, 0);

        StructureBlock sb = new StructureBlock(new BlockCoord(b), this);
        this.addStructureBlock(sb.getCoord(), true);
        //}

        /* Build the control block. */
        b = centerLoc.getBlock().getRelative(0, 1, 0);
        ItemManager.setTypeId(b, CivData.OBSIDIAN);
        sb = new StructureBlock(new BlockCoord(b), this);
        this.addStructureBlock(sb.getCoord(), true);

        int townhallControlHitpoints;
        if (this.getTown().getBuffManager().hasBuff("buff_chichen_itza_tower_hp") && this.getTown().getBuffManager().hasBuff("buff_greatlibrary_extra_beakers")) {
            townhallControlHitpoints = 30;
        } else {
            townhallControlHitpoints = 20;
        }
        if (this.getTown().hasStructure("s_castle")) {
            townhallControlHitpoints += 3;
        }
        if (this.getCiv().getCapitol() != null && this.getCiv().getCapitol().getBuffManager().hasBuff("level6_extraHPcpTown")) {
            townhallControlHitpoints *= (int) 1.2;
        }
        if (this.getCiv().getCapitol() != null && this.getCiv().getCapitol().getBuffManager().hasBuff("level10_dominatorTown")) {
            townhallControlHitpoints *= 2;
        }
        BlockCoord coord = new BlockCoord(b);
        this.controlPoints.put(coord, new ControlPoint(coord, this, townhallControlHitpoints, info));
    }

    public void onControlBlockDestroy(ControlPoint cp, World world, Player player, StructureBlock hit) {
        //Should always have a resident and a town at this point.
        Resident attacker = CivGlobal.getResident(player);

        ItemManager.setTypeId(hit.getCoord().getLocation().getBlock(), CivData.AIR);
        world.playSound(hit.getCoord().getLocation(), Sound.BLOCK_ANVIL_BREAK, 1.0f, -1.0f);
        world.playSound(hit.getCoord().getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);

        FireworkEffect effect = FireworkEffect.builder().with(Type.BURST).withColor(Color.YELLOW).withColor(Color.RED).withTrail().withFlicker().build();
        FireworkEffectPlayer fePlayer = new FireworkEffectPlayer();
        for (int i = 0; i < 3; i++) {
            try {
                fePlayer.playFirework(world, hit.getCoord().getLocation(), effect);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        boolean allDestroyed = true;
        for (ControlPoint c : this.controlPoints.values()) {
            if (!c.isDestroyed()) {
                allDestroyed = false;
                break;
            }
        }
        CivMessage.sendTownSound(hit.getTown(), Sound.AMBIENT_CAVE, 1.0f, 0.5f);

        if (allDestroyed) {
            Civilization civ = getTown().getCiv();
            //r如果是主城被打了
            if (civ.getCapitolName().equals(this.getTown().getName())) {
                CivMessage.globalTitle(CivColor.LightBlue + CivSettings.localize.localizedString("var_townHall_destroyed_isCap", this.getTown().getCiv().getName()), CivSettings.localize.localizedString("var_townHall_destroyed_isCap2", attacker.getCiv().getName()));
                for (Town town : civ.getTowns()) {
                    town.defeated = true;
                }

                if (this instanceof Capitol) {
                    civ.updateReviveSigns();
                }
                //q启蒙时代归属权以交
                if (civ.hasTechnology("tech_enlightenment")) {
                    civ.removeTech("tech_enlightenment");
                    final ConfigTech tech = CivSettings.techs.get("tech_enlightenment");
                    attacker.getCiv().addTech(tech);
                    CivMessage.global(CivSettings.localize.localizedString("war_defeat_loseEnlightenment", this.getTown().getCiv().getName(), attacker.getCiv().getName()));
                }
                // 当前任务？ 所处的era（我猜的  -----其实感觉是飞机的哪个
                if (civ.getCurrentMission() >= 2) {
                    try {
                        civ.setCurrentMission(this.getCiv().getCurrentMission() - 1);
                        civ.setMissionActive(false);
                        civ.updateMissionProgress(0.0, 0.0);
                        civ.saveNow();
                    } catch (SQLException e2) {
                        e2.printStackTrace();
                    }
                    CivMessage.global(CivSettings.localize.localizedString("war_defeat_loseMission", civ.getName(), civ.getCurrentMission()));
                }

                War.transferDefeated(this.getTown().getCiv(), attacker.getTown().getCiv());
                WarStats.logCapturedCiv(attacker.getTown().getCiv(), this.getTown().getCiv());
                War.saveDefeatedCiv(this.getCiv(), attacker.getTown().getCiv());

                if (CivGlobal.isCasualMode()) {
                    HashMap<Integer, ItemStack> leftovers = player.getInventory().addItem(this.getCiv().getRandomLeaderSkull(CivSettings.localize.localizedString("var_townHall_victoryOverItem", this.getCiv().getName())));
                    for (ItemStack stack : leftovers.values()) {
                        player.getWorld().dropItem(player.getLocation(), stack);
                    }
                }

            } else {
                CivMessage.global(CivColor.Yellow + ChatColor.BOLD + CivSettings.localize.localizedString("var_townHall_destroyed", getTown().getName(), this.getCiv().getName(), attacker.getCiv().getName()));
                //this.getTown().onDefeat(attacker.getTown().getCiv());
                this.getTown().defeated = true;
                //War.defeatedTowns.put(this.getTown().getName(), attacker.getTown().getCiv());
                WarStats.logCapturedTown(attacker.getTown().getCiv(), this.getTown());
                War.saveDefeatedTown(this.getTown().getName(), attacker.getTown().getCiv());
            }

        } else {
            CivMessage.sendTown(hit.getTown(), CivColor.Rose + CivSettings.localize.localizedString("townHall_controlBlockDestroyed"));
            if (hit.getTown().hasWonder("w_neuschwanstein")) {
                CivMessage.sendCiv(attacker.getCiv(), CivSettings.localize.localizedString("var_townHall_didDestroyNeus", hit.getTown().getName()));
            }
            CivMessage.sendCiv(attacker.getTown().getCiv(), CivColor.LightGreen + CivSettings.localize.localizedString("var_townHall_didDestroyCB", hit.getTown().getName()));
            CivMessage.sendCiv(hit.getTown().getCiv(), CivColor.Rose + CivSettings.localize.localizedString("var_townHall_civMsg_controlBlockDestroyed", hit.getTown().getName()));
        }
    }

    public void onControlBlockCannonDestroy(ControlPoint cp, Player player, StructureBlock hit) {
        //Should always have a resident and a town at this point.
        Resident attacker = CivGlobal.getResident(player);

        ItemManager.setTypeId(hit.getCoord().getLocation().getBlock(), CivData.AIR);

        boolean allDestroyed = true;
        for (ControlPoint c : this.controlPoints.values()) {
            if (c.isDestroyed() == false) {
                allDestroyed = false;
                break;
            }
        }
        CivMessage.sendTownSound(hit.getTown(), Sound.AMBIENT_CAVE, 1.0f, 0.5f);

        if (allDestroyed) {

            if (this.getTown().getCiv().getCapitolName().equals(this.getTown().getName())) {
                CivMessage.globalTitle(CivColor.LightBlue + CivSettings.localize.localizedString("var_townHall_destroyed_isCap", this.getTown().getCiv().getName()), CivSettings.localize.localizedString("var_townHall_destroyed_isCap2", attacker.getCiv().getName()));
                for (Town town : this.getTown().getCiv().getTowns()) {
                    town.defeated = true;
                }

                War.transferDefeated(this.getTown().getCiv(), attacker.getTown().getCiv());
                WarStats.logCapturedCiv(attacker.getTown().getCiv(), this.getTown().getCiv());
                War.saveDefeatedCiv(this.getCiv(), attacker.getTown().getCiv());

                if (CivGlobal.isCasualMode()) {
                    HashMap<Integer, ItemStack> leftovers = player.getInventory().addItem(this.getCiv().getRandomLeaderSkull(CivSettings.localize.localizedString("var_townHall_victoryOverItem_withCannon", this.getCiv().getName())));
                    for (ItemStack stack : leftovers.values()) {
                        player.getWorld().dropItem(player.getLocation(), stack);
                    }
                }

            } else {
                CivMessage.global(CivColor.Yellow + ChatColor.BOLD + CivSettings.localize.localizedString("var_townHall_destroyed", getTown().getName(), this.getCiv().getName(), attacker.getCiv().getName()));
                //this.getTown().onDefeat(attacker.getTown().getCiv());
                this.getTown().defeated = true;
                //War.defeatedTowns.put(this.getTown().getName(), attacker.getTown().getCiv());
                WarStats.logCapturedTown(attacker.getTown().getCiv(), this.getTown());
                War.saveDefeatedTown(this.getTown().getName(), attacker.getTown().getCiv());
            }

        } else {
            CivMessage.sendTown(hit.getTown(), CivColor.Rose + CivSettings.localize.localizedString("townHall_controlBlockDestroyed"));
            CivMessage.sendCiv(attacker.getTown().getCiv(), CivColor.LightGreen + CivSettings.localize.localizedString("var_townHall_didDestroyCB", hit.getTown().getName()));
            CivMessage.sendCiv(hit.getTown().getCiv(), CivColor.Rose + CivSettings.localize.localizedString("var_townHall_civMsg_controlBlockDestroyed", hit.getTown().getName()));
        }
    }

    public void onControlBlockHit(ControlPoint cp, World world, Player player, StructureBlock hit) {
        world.playSound(hit.getCoord().getLocation(), Sound.BLOCK_ANVIL_USE, 0.2f, 1);
        world.playEffect(hit.getCoord().getLocation(), Effect.MOBSPAWNER_FLAMES, 0);

        CivMessage.send(player, CivColor.LightGray + CivSettings.localize.localizedString("var_townHall_damagedControlBlock", ("(" + cp.getHitpoints() + " / " + cp.getMaxHitpoints() + ")")));
        CivMessage.sendTown(hit.getTown(), CivColor.Yellow + CivSettings.localize.localizedString("townHall_cbUnderAttack"));
    }

    @Override
    public void onDamage(int amount, World world, Player player, BlockCoord coord, BuildableDamageBlock hit) {

        ControlPoint cp = this.controlPoints.get(coord);
        Resident resident = CivGlobal.getResident(player);

        if (!resident.canDamageControlBlock()) {
            CivMessage.send(player, CivColor.Rose + CivSettings.localize.localizedString("townHall_damageCB_invalid"));
            return;
        }

        if (cp != null) {
            if (!cp.isDestroyed()) {

                if (resident.isControlBlockInstantBreak()) {
                    cp.damage(cp.getHitpoints());
                } else {
                    cp.damage(amount);
                }

                if (cp.isDestroyed()) {
                    onControlBlockDestroy(cp, world, player, (StructureBlock) hit);
                } else {
                    onControlBlockHit(cp, world, player, (StructureBlock) hit);
                }
            } else {
                CivMessage.send(player, CivColor.Rose + CivSettings.localize.localizedString("townHall_damageCB_destroyed"));
            }

        } else {
            CivMessage.send(player, CivColor.Rose + CivSettings.localize.localizedString("var_townHall_damage_notCB", this.getDisplayName()));
        }
    }

    public void regenControlBlocks() {
        for (BlockCoord coord : this.controlPoints.keySet()) {
            ItemManager.setTypeId(coord.getBlock(), CivData.OBSIDIAN);

            ControlPoint cp = this.controlPoints.get(coord);
            cp.setHitpoints(cp.getMaxHitpoints());
        }
    }

    public int getTechBarSize() {
        return techbar.length;
    }

    @Override
    public void onLoad() {
        // We must load goodies into the frame as we find them from the trade outpost's
        // onLoad() function, otherwise we run into timing issues over which loads first.
    }

    @Override
    public void onPreBuild(Location loc) throws CivException {
        TownHall oldTownHall = this.getTown().getTownHall();
        if (oldTownHall != null) {
            ChunkCoord coord = new ChunkCoord(loc);
            TownChunk tc = CivGlobal.getTownChunk(coord);
            if (tc == null || tc.getTown() != this.getTown()) {
                throw new CivException(CivSettings.localize.localizedString("townHall_preBuild_outsideBorder"));
            }

            if (War.isWarTime()) {
                throw new CivException(CivSettings.localize.localizedString("townHall_preBuild_duringWar"));
            }

            this.getTown().clearBonusGoods();

            try {
                this.getTown().demolish(oldTownHall, true);
            } catch (CivException e) {
                e.printStackTrace();
            }
            CivMessage.sendTown(this.getTown(), CivSettings.localize.localizedString("var_townHall_preBuild_Success", this.getDisplayName()));
            this.autoClaim = false;
        } else {
            this.autoClaim = true;
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

        CivMessage.sendTown(this.getTown(), CivColor.Rose + CivColor.BOLD + CivSettings.localize.localizedString("var_townHall_invalidPunish", invalid_respawn_penalty));
    }

    @Override
    public List<BlockCoord> getRespawnPoints() {
        return this.revivePoints;
    }

    @Override
    public String getRespawnName() {
        return this.getDisplayName() + "\n" + this.getTown().getName();
    }

    public HashMap<BlockCoord, ControlPoint> getControlPoints() {
        return this.controlPoints;
    }

    /**
     * da大炮对建筑伤害 然后核心扣血
     */
    public void onCannonDamage(int damage, CannonProjectile projectile) throws CivException {
        if (!this.getCiv().getDiplomacyManager().isAtWar()) {
            return;
        }
        this.hitpoints -= damage;

//		Resident resident = projectile.whoFired;
        if (hitpoints <= 0) {
            for (BlockCoord coord : this.controlPoints.keySet()) {
                ControlPoint cp = this.controlPoints.get(coord);
                if (cp != null) {
                    if (cp.getHitpoints() > CannonProjectile.controlBlockHP) {
                        cp.damage(cp.getHitpoints() - 1);
                        this.hitpoints = this.getMaxHitPoints() / 2;
//						StructureBlock hit = CivGlobal.getStructureBlock(coord);
//						onControlBlockCannonDestroy(cp, CivGlobal.getPlayer(resident), hit);
                        CivMessage.sendCiv(getCiv(), CivSettings.localize.localizedString("var_townHall_cannonHit_destroyCB", this.getDisplayName(), CannonProjectile.controlBlockHP));
                        CivMessage.sendCiv(getCiv(), CivSettings.localize.localizedString("var_townHall_cannonHit_regen", this.getDisplayName(), this.getMaxHitPoints() / 2));
                        return;
                    }
                }
            }
            CivMessage.sendCiv(getCiv(), CivSettings.localize.localizedString("var_townHall_cannonHit_destroyed", this.getDisplayName()));
            hitpoints = 0;
        }

        CivMessage.sendCiv(getCiv(), CivSettings.localize.localizedString("var_townHall_cannonHit", this.getDisplayName(), ("(" + this.hitpoints + "/" + this.getMaxHitPoints() + ")")));
    }

    public void onTNTDamage(int damage) {
        if (!this.getCiv().getDiplomacyManager().isAtWar()) {
            return;
        }
        if (hitpoints >= damage + 1) {
            this.hitpoints -= damage;
            CivMessage.sendCiv(getCiv(), CivSettings.localize.localizedString("var_townHall_tntHit",
                    this.getDisplayName(), ("(" + this.hitpoints + "/" + this.getMaxHitPoints() + ")")));
        }

    }
}
