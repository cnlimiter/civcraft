package cn.evole.plugins.civcraft.camp;

import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.config.ConfigBuildableInfo;
import cn.evole.plugins.civcraft.exception.CivException;
import cn.evole.plugins.civcraft.exception.InvalidConfiguration;
import cn.evole.plugins.civcraft.exception.InvalidNameException;
import cn.evole.plugins.civcraft.exception.InvalidObjectException;
import cn.evole.plugins.civcraft.lorestorage.LoreCraftableMaterial;
import cn.evole.plugins.civcraft.main.CivData;
import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.main.CivLog;
import cn.evole.plugins.civcraft.main.CivMessage;
import cn.evole.plugins.civcraft.object.*;
import cn.evole.plugins.civcraft.permission.PlotPermissions;
import cn.evole.plugins.civcraft.sessiondb.SessionEntry;
import cn.evole.plugins.civcraft.structure.Buildable;
import cn.evole.plugins.civcraft.structure.RespawnLocationHolder;
import cn.evole.plugins.civcraft.template.Template;
import cn.evole.plugins.civcraft.threading.TaskMaster;
import cn.evole.plugins.civcraft.util.*;
import cn.evole.plugins.civcraft.war.War;
import cn.evole.plugins.civcraft.war.WarRegen;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class WarCamp extends Buildable implements RespawnLocationHolder {

    public static final String RESTORE_NAME = "special:WarCamps";
    protected HashMap<BlockCoord, ControlPoint> controlPoints = new HashMap<BlockCoord, ControlPoint>();
    private ArrayList<BlockCoord> respawnPoints = new ArrayList<BlockCoord>();

    public WarCamp(Resident resident, Location loc, ConfigBuildableInfo info) {
        this.setCorner(new BlockCoord(loc));
        this.setTown(resident.getTown());
        this.info = info;
    }

    public static void newCamp(Resident resident, ConfigBuildableInfo info) {

        class SyncBuildWarCampTask implements Runnable {
            Resident resident;
            ConfigBuildableInfo info;

            public SyncBuildWarCampTask(Resident resident, ConfigBuildableInfo info) {
                this.resident = resident;
                this.info = info;
            }

            @Override
            public void run() {
                Player player;
                try {
                    player = CivGlobal.getPlayer(resident);
                } catch (CivException e) {
                    return;
                }

                try {
                    if (!resident.hasTown()) {
                        throw new CivException(CivSettings.localize.localizedString("warcamp_notInCiv"));
                    }

                    if (!resident.getCiv().getLeaderGroup().hasMember(resident) &&
                            !resident.getCiv().getAdviserGroup().hasMember(resident)) {
                        throw new CivException(CivSettings.localize.localizedString("warcamp_mustHaveRank"));
                    }

                    int warCampMax;
                    try {
                        warCampMax = CivSettings.getInteger(CivSettings.warConfig, "warcamp.max");
                    } catch (InvalidConfiguration e) {
                        e.printStackTrace();
                        return;
                    }

                    if (resident.getCiv().getWarCamps().size() >= warCampMax) {
                        throw new CivException(CivSettings.localize.localizedString("var_warcamp_maxReached", warCampMax));
                    }

                    ItemStack stack = player.getInventory().getItemInMainHand();
                    LoreCraftableMaterial craftMat = LoreCraftableMaterial.getCraftMaterial(stack);
                    if (craftMat == null || !craftMat.hasComponent("FoundWarCamp")) {
                        throw new CivException(CivSettings.localize.localizedString("warcamp_missingItem"));
                    }

                    WarCamp camp = new WarCamp(resident, player.getLocation(), info);
                    camp.buildCamp(player, player.getLocation());
                    resident.getCiv().addWarCamp(camp);

                    CivMessage.sendSuccess(player, CivSettings.localize.localizedString("warcamp_createSuccess"));
                    camp.setWarCampBuilt();
                    ItemStack newStack = new ItemStack(Material.AIR);
                    player.getInventory().setItemInMainHand(newStack);
                } catch (CivException e) {
                    CivMessage.sendError(player, e.getMessage());
                }
            }
        }

        TaskMaster.syncTask(new SyncBuildWarCampTask(resident, info));
    }

    public String getSessionKey() {
        return this.getCiv().getName() + ":warcamp:built";
    }

    public void setWarCampBuilt() {
        ArrayList<SessionEntry> entries = CivGlobal.getSessionDB().lookup(getSessionKey());
        Date now = new Date();
        if (entries.size() == 0) {
            CivGlobal.getSessionDB().add(getSessionKey(), now.getTime() + "", this.getCiv().getId(), this.getTown().getId(), 0);
        } else {
            CivGlobal.getSessionDB().update(entries.get(0).request_id, entries.get(0).key, now.getTime() + "");
        }
    }

    public int isWarCampCooldownLeft() {
        ArrayList<SessionEntry> entries = CivGlobal.getSessionDB().lookup(getSessionKey());
        Date now = new Date();
        long minsLeft = 0;
        if (entries.size() == 0) {
            return 0;
        } else {
            Date then = new Date(Long.parseLong(entries.get(0).value));
            int rebuild_timeout;
            try {
                rebuild_timeout = CivSettings.getInteger(CivSettings.warConfig, "warcamp.rebuild_timeout");
            } catch (InvalidConfiguration e) {
                e.printStackTrace();
                return 0;
            }

            minsLeft = (then.getTime() + (rebuild_timeout * 60 * 1000)) - now.getTime();
            minsLeft /= 1000;
            minsLeft /= 60;
            if (now.getTime() > (then.getTime() + (rebuild_timeout * 60 * 1000))) {
                return 0;
            }
            return (int) minsLeft;
        }
    }

    public void buildCamp(Player player, Location center) throws CivException {

        String templateFile;
        try {
            templateFile = CivSettings.getString(CivSettings.warConfig, "warcamp.template");
        } catch (InvalidConfiguration e) {
            e.printStackTrace();
            return;
        }
        Resident resident = CivGlobal.getResident(player);

        /* Load in the template. */
        Template tpl;
        try {
            String templatePath = Template.getTemplateFilePath(templateFile, Template.getDirection(center), Template.TemplateType.STRUCTURE, "default");
            this.setTemplateName(templatePath);
            tpl = Template.getTemplate(templatePath, center);
        } catch (IOException e) {
            e.printStackTrace();
            throw new CivException("Internal Error.");
        } catch (CivException e) {
            e.printStackTrace();
            throw new CivException("Internal Error.");
        }


        corner.setFromLocation(this.repositionCenter(center, tpl.dir(), tpl.size_x, tpl.size_z));
        checkBlockPermissionsAndRestrictions(player, corner.getBlock(), tpl.size_x, tpl.size_y, tpl.size_z);
        buildWarCampFromTemplate(tpl, corner);
        processCommandSigns(tpl, corner);
        try {
            this.saveNow();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new CivException("Internal SQL Error.");
        }

        resident.save();

    }

    private void processCommandSigns(Template tpl, BlockCoord corner) {
        for (BlockCoord relativeCoord : tpl.commandBlockRelativeLocations) {
            SimpleBlock sb = tpl.blocks[relativeCoord.getX()][relativeCoord.getY()][relativeCoord.getZ()];
            BlockCoord absCoord = new BlockCoord(corner.getBlock().getRelative(relativeCoord.getX(), relativeCoord.getY(), relativeCoord.getZ()));

            switch (sb.command) {
                case "/respawn":
                    this.respawnPoints.add(absCoord);
                    BlockCoord coord = new BlockCoord(absCoord);
                    ItemManager.setTypeId(coord.getBlock(), CivData.AIR);
                    this.addStructureBlock(new BlockCoord(absCoord), false);

                    coord = new BlockCoord(absCoord);
                    coord.setY(absCoord.getY() + 1);
                    ItemManager.setTypeId(coord.getBlock(), CivData.AIR);
                    this.addStructureBlock(coord, false);

                    break;
                case "/control":
                    this.createControlPoint(absCoord, "");
                    break;
            }
        }
    }

    protected void checkBlockPermissionsAndRestrictions(Player player, Block centerBlock, int regionX, int regionY, int regionZ) throws CivException {

        if (!War.isWarTime()) {
            throw new CivException(CivSettings.localize.localizedString("warcamp_notWarTime"));
        }

        if (player.getLocation().getY() >= 200) {
            throw new CivException(CivSettings.localize.localizedString("camp_checkTooHigh"));
        }

        if ((regionY + centerBlock.getLocation().getBlockY()) >= 255) {
            throw new CivException(CivSettings.localize.localizedString("camp_checkWayTooHigh"));
        }

        if (player.getLocation().getY() < CivGlobal.minBuildHeight) {
            throw new CivException(CivSettings.localize.localizedString("cannotBuild_toofarUnderground"));
        }

        int minsLeft = this.isWarCampCooldownLeft();
        if (minsLeft > 0) {
            throw new CivException(CivSettings.localize.localizedString("var_warcamp_oncooldown", minsLeft));
        }

        if (!player.isOp()) {
            Buildable.validateDistanceFromSpawn(centerBlock.getLocation());
        }

        int yTotal = 0;
        int yCount = 0;

        for (int x = 0; x < regionX; x++) {
            for (int y = 0; y < regionY; y++) {
                for (int z = 0; z < regionZ; z++) {
                    Block b = centerBlock.getRelative(x, y, z);

                    if (ItemManager.getId(b) == CivData.CHEST) {
                        throw new CivException(CivSettings.localize.localizedString("cannotBuild_chestInWay"));
                    }

                    BlockCoord coord = new BlockCoord(b);
                    ChunkCoord chunkCoord = new ChunkCoord(coord.getLocation());

                    TownChunk tc = CivGlobal.getTownChunk(chunkCoord);
                    if (tc != null && !tc.perms.hasPermission(PlotPermissions.Type.DESTROY, CivGlobal.getResident(player))) {
                        // Make sure we have permission to destroy any block in this area.
                        throw new CivException(CivSettings.localize.localizedString("cannotBuild_needPermissions") + " " + b.getX() + "," + b.getY() + "," + b.getZ());
                    }

                    if (CivGlobal.getProtectedBlock(coord) != null) {
                        throw new CivException(CivSettings.localize.localizedString("cannotBuild_protectedInWay"));
                    }

                    if (CivGlobal.getStructureBlock(coord) != null) {
                        throw new CivException(CivSettings.localize.localizedString("cannotBuild_structureInWay"));
                    }

                    if (CivGlobal.getFarmChunk(chunkCoord) != null) {
                        throw new CivException(CivSettings.localize.localizedString("cannotBuild_farmInWay"));
                    }

                    if (CivGlobal.getWallChunk(chunkCoord) != null) {
                        throw new CivException(CivSettings.localize.localizedString("cannotBuild_wallInWay"));
                    }

                    if (CivGlobal.getCampBlock(coord) != null) {
                        throw new CivException(CivSettings.localize.localizedString("cannotBuild_campinWay"));
                    }

                    yTotal += b.getWorld().getHighestBlockYAt(centerBlock.getX() + x, centerBlock.getZ() + z);
                    yCount++;

                    if (CivGlobal.getRoadBlock(coord) != null) {
                        throw new CivException(CivSettings.localize.localizedString("warcamp_cannotBuildOnRoad"));
                    }
                }
            }
        }

        double highestAverageBlock = (double) yTotal / (double) yCount;

        if (((centerBlock.getY() > (highestAverageBlock + 10)) ||
                (centerBlock.getY() < (highestAverageBlock - 10)))) {
            throw new CivException(CivSettings.localize.localizedString("cannotBuild_toofarUnderground"));
        }
    }

    private void buildWarCampFromTemplate(Template tpl, BlockCoord corner) {
        Block cornerBlock = corner.getBlock();
        for (int x = 0; x < tpl.size_x; x++) {
            for (int y = 0; y < tpl.size_y; y++) {
                for (int z = 0; z < tpl.size_z; z++) {
                    Block nextBlock = cornerBlock.getRelative(x, y, z);

                    if (tpl.blocks[x][y][z].specialType == SimpleBlock.Type.COMMAND) {
                        continue;
                    }

                    if (tpl.blocks[x][y][z].specialType == SimpleBlock.Type.LITERAL) {
                        // Adding a command block for literal sign placement
                        tpl.blocks[x][y][z].command = "/literal";
                        tpl.commandBlockRelativeLocations.add(new BlockCoord(cornerBlock.getWorld().getName(), x, y, z));
                        continue;
                    }

                    try {
                        if (ItemManager.getId(nextBlock) != tpl.blocks[x][y][z].getType()) {
                            /* XXX Save it as a war block so it's automatically removed when war time ends. */
                            WarRegen.saveBlock(nextBlock, WarCamp.RESTORE_NAME, false);
                            ItemManager.setTypeId(nextBlock, tpl.blocks[x][y][z].getType());
                            ItemManager.setData(nextBlock, tpl.blocks[x][y][z].getData());
                        }

                        if (ItemManager.getId(nextBlock) != CivData.AIR) {
                            this.addStructureBlock(new BlockCoord(nextBlock.getLocation()), true);
                        }
                    } catch (Exception e) {
                        CivLog.error(e.getMessage());
                    }
                }
            }
        }
    }

    @Override
    public void processUndo() throws CivException {
    }

    @Override
    public void updateBuildProgess() {

    }

    @Override
    public void build(Player player, Location centerLoc, Template tpl) throws Exception {

    }

    @Override
    protected void runOnBuild(Location centerLoc, Template tpl) throws CivException {

    }

    @Override
    public String getDynmapDescription() {
        return null;
    }

    @Override
    public String getMarkerIconName() {
        return null;
    }

    @Override
    public void onComplete() {

    }

    @Override
    public void onLoad() throws CivException {

    }

    @Override
    public void onUnload() {

    }

    @Override
    public void load(ResultSet rs) throws SQLException, InvalidNameException, InvalidObjectException, CivException {

    }

    @Override
    public void save() {

    }

    @Override
    public void saveNow() throws SQLException {

    }

    public void createControlPoint(BlockCoord absCoord, String info) {

        Location centerLoc = absCoord.getLocation();

        /* Build the bedrock tower. */
        //for (int i = 0; i < 1; i++) {
        Block b = centerLoc.getBlock();
        WarRegen.saveBlock(b, WarCamp.RESTORE_NAME, false);
        ItemManager.setTypeId(b, CivData.FENCE);
        ItemManager.setData(b, 0);

        StructureBlock sb = new StructureBlock(new BlockCoord(b), this);
        this.addStructureBlock(sb.getCoord(), true);
        //}

        /* Build the control block. */
        b = centerLoc.getBlock().getRelative(0, 1, 0);
        WarRegen.saveBlock(b, WarCamp.RESTORE_NAME, false);
        ItemManager.setTypeId(b, CivData.OBSIDIAN);

        sb = new StructureBlock(new BlockCoord(b), this);
        this.addStructureBlock(sb.getCoord(), true);

        int townhallControlHitpoints;
        try {
            townhallControlHitpoints = CivSettings.getInteger(CivSettings.warConfig, "warcamp.control_block_hitpoints");
        } catch (InvalidConfiguration e) {
            e.printStackTrace();
            return;
        }
        if (this.getTown().getBuffManager().hasBuff("buff_chichen_itza_tower_hp") && this.getTown().getBuffManager().hasBuff("buff_greatlibrary_extra_beakers")) {
            townhallControlHitpoints += 10;
        }
        int additionally = 0;
        for (final Town town : this.getCiv().getTowns()) {
            if (town.hasStructure("s_castle")) {
                additionally += 3;
            }
        }
        townhallControlHitpoints += additionally;
        if (this.getCiv().getCapitol() != null && this.getCiv().getCapitol().getBuffManager().hasBuff("level9_wcHPTown") && this.getCiv().getCapitol() != null) {
            townhallControlHitpoints *= 2;
        }
        if (this.getCiv().getCapitol() != null && this.getCiv().getCapitol().getBuffManager().hasBuff("level6_extraHPcpTown") && this.getCiv().getCapitol() != null) {
            townhallControlHitpoints *= (int) 1.2;
        }
        if (this.getCiv().getCapitol() != null && this.getCiv().getCapitol().getBuffManager().hasBuff("level10_dominatorTown") && this.getCiv().getCapitol() != null) {
            townhallControlHitpoints *= 2;
        }

        BlockCoord coord = new BlockCoord(b);
        this.controlPoints.put(coord, new ControlPoint(coord, this, townhallControlHitpoints, info));
    }

    @Override
    public void onDamage(int amount, World world, Player player, BlockCoord coord, BuildableDamageBlock hit) {
        ControlPoint cp = this.controlPoints.get(coord);
        Resident resident = CivGlobal.getResident(player);

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
                CivMessage.send(player, CivColor.Rose + CivSettings.localize.localizedString("camp_controlBlockAlreadyDestroyed"));
            }

        } else {
            CivMessage.send(player, CivColor.Rose + CivSettings.localize.localizedString("structure_cannotDamage") + " " + this.getDisplayName() + ", " + CivSettings.localize.localizedString("structure_targetControlBlocks"));
        }
    }

    public void onControlBlockDestroy(ControlPoint cp, World world, Player player, StructureBlock hit) {
        //Should always have a resident and a town at this point.
        Resident attacker = CivGlobal.getResident(player);

        ItemManager.setTypeId(hit.getCoord().getLocation().getBlock(), CivData.AIR);
        world.playSound(hit.getCoord().getLocation(), Sound.BLOCK_ANVIL_BREAK, 1.0f, -1.0f);
        world.playSound(hit.getCoord().getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);

        FireworkEffect effect = FireworkEffect.builder().with(org.bukkit.FireworkEffect.Type.BURST).withColor(Color.OLIVE).withColor(Color.RED).withTrail().withFlicker().build();
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
            if (!c.isDestroyed() && c.getBuildable() == this) {
                allDestroyed = false;
                break;
            }
        }

        if (allDestroyed) {
            this.onWarCampDestroy();
        } else {
            CivMessage.sendCiv(attacker.getTown().getCiv(), CivColor.LightGreen + CivSettings.localize.localizedString("warcamp_enemyControlBlockDestroyed") + " " + getCiv().getName() + CivSettings.localize.localizedString("warcamp_name"));
            CivMessage.sendCiv(getCiv(), CivColor.Rose + CivSettings.localize.localizedString("warcamp_ownControlBlockDestroyed"));
        }

    }

    private void onWarCampDestroy() {
        CivMessage.sendCiv(this.getCiv(), CivColor.Rose + CivSettings.localize.localizedString("warcamp_ownDestroyed"));
        this.getCiv().getWarCamps().remove(this);

        for (BlockCoord coord : this.structureBlocks.keySet()) {
            CivGlobal.removeStructureBlock(coord);
        }
        this.structureBlocks.clear();

        this.fancyDestroyStructureBlocks();
        setWarCampBuilt();
    }

    public void onControlBlockHit(ControlPoint cp, World world, Player player, StructureBlock hit) {
        world.playSound(hit.getCoord().getLocation(), Sound.BLOCK_ANVIL_USE, 0.2f, 1);
        world.playEffect(hit.getCoord().getLocation(), Effect.MOBSPAWNER_FLAMES, 0);

        CivMessage.send(player, CivColor.LightGray + CivSettings.localize.localizedString("warcamp_hitControlBlock") + " (" + cp.getHitpoints() + " / " + cp.getMaxHitpoints() + ")");
        CivMessage.sendCiv(getCiv(), CivColor.Yellow + CivSettings.localize.localizedString("warcamp_controlBlockUnderAttack"));
    }

    @Override
    public String getRespawnName() {
        return "WarCamp\n(" + this.corner.getX() + "," + this.corner.getY() + "," + this.corner.getZ() + ")";
    }

    @Override
    public List<BlockCoord> getRespawnPoints() {
        return this.getRespawnPoints();
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

    @Override
    public int getRegenRate() {

        if (this.getCiv().getCapitol().getBuffManager().hasBuff("level9_wcHPTown")) {
            return 1;
        }
        if (this.info.regenRate == null) {
            return 0;
        }

        return info.regenRate;
    }


    @Override
    public BlockCoord getRandomRevivePoint() {
        if (this.respawnPoints.size() == 0) {
            return new BlockCoord(this.getCorner());
        }
        Random rand = new Random();
        int index = rand.nextInt(this.respawnPoints.size());
        return this.respawnPoints.get(index);
    }

    public void onWarEnd() {

        /* blocks are cleared by war regen, but structure blocks need to be cleared. */
        for (BlockCoord coord : this.structureBlocks.keySet()) {
            CivGlobal.removeStructureBlock(coord);
        }

        this.structureBlocks.clear();
    }
}
