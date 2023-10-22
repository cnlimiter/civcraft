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


import cn.evole.plugins.civcraft.exception.CivException;
import cn.evole.plugins.civcraft.main.CivData;
import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.object.StructureChest;
import cn.evole.plugins.civcraft.object.StructureSign;
import cn.evole.plugins.civcraft.structure.*;
import cn.evole.plugins.civcraft.structure.wonders.GrandShipIngermanland;
import cn.evole.plugins.civcraft.structure.wonders.Neuschwanstein;
import cn.evole.plugins.civcraft.template.Template;
import cn.evole.plugins.civcraft.util.BlockCoord;
import cn.evole.plugins.civcraft.util.ItemManager;
import cn.evole.plugins.civcraft.util.SimpleBlock;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.material.MaterialData;

public class PostBuildSyncTask implements Runnable {

    /*
     * Search the template for special command blocks and handle them *after* the structure
     * has finished building.
     */

    Template tpl;
    Buildable buildable;

    public PostBuildSyncTask(Template tpl, Buildable buildable) {
        this.tpl = tpl;
        this.buildable = buildable;
    }

    public static void validate(Template tpl, Buildable buildable) {

        /*
         * Use the location's of the command blocks in the template and the buildable's corner
         * to find their real positions. Then perform any special building we may want to do
         * at those locations.
         */
        // 使用模板中命令块的位置和可构建对象的角来查找其实际位置。 然后执行我们可能想在这些位置做的任何特殊建筑。
        /* These block coords do not point to a location in the world, just a location in the template. */
        //这些块坐标并不指向世界上的某个位置，而只是指向模板中的一个位置。
        for (BlockCoord relativeCoord : tpl.commandBlockRelativeLocations) {
            SimpleBlock sb = tpl.blocks[relativeCoord.getX()][relativeCoord.getY()][relativeCoord.getZ()];
            StructureSign structSign;
            Block block;
            BlockCoord absCoord = new BlockCoord(buildable.getCorner().getBlock().getRelative(relativeCoord.getX(), relativeCoord.getY(), relativeCoord.getZ()));

            /* Signs and chests should already be handled, look for more exotic things. */
            switch (sb.command) {
                case "/tradeoutpost":
                    /* Builds the trade outpost tower at this location. */
                    if (buildable instanceof TradeOutpost) {
                        TradeOutpost outpost = (TradeOutpost) buildable;
                        outpost.setTradeOutpostTower(absCoord);
                        try {
                            outpost.build_trade_outpost_tower();
                        } catch (CivException e) {
                            e.printStackTrace();
                        }

                    }
                    break;
                case "/techbar":
                    if (buildable instanceof TownHall) {
                        TownHall townhall = (TownHall) buildable;

                        int index = Integer.valueOf(sb.keyvalues.get("id"));
                        townhall.addTechBarBlock(absCoord, index);

                    }
                    break;
                case "/techname":
                    if (buildable instanceof TownHall) {
                        TownHall townhall = (TownHall) buildable;

                        townhall.setTechnameSign(absCoord);
                        townhall.setTechnameSignData((byte) sb.getData());

                    }
                    break;
                case "/techdata":
                    if (buildable instanceof TownHall) {
                        TownHall townhall = (TownHall) buildable;

                        townhall.setTechdataSign(absCoord);
                        townhall.setTechdataSignData((byte) sb.getData());

                    }
                    break;
                case "/itemframe":
                    String strvalue = sb.keyvalues.get("id");
                    if (strvalue != null) {
                        int index = Integer.valueOf(strvalue);

                        if (buildable instanceof TownHall) {
                            TownHall townhall = (TownHall) buildable;
                            townhall.createGoodieItemFrame(absCoord, index, sb.getData());
                            townhall.addStructureBlock(absCoord, false);
                        }
                    }
                    break;
                case "/respawn":
                    if (buildable instanceof TownHall) {
                        TownHall townhall = (TownHall) buildable;

                        townhall.setRespawnPoint(absCoord);
                    }
                    break;
                case "/revive":
                    if (buildable instanceof TownHall) {
                        TownHall townhall = (TownHall) buildable;

                        townhall.setRevivePoint(absCoord);
                    }
                    if (buildable instanceof Neuschwanstein) {
                        final Neuschwanstein neuschwanstein = (Neuschwanstein) buildable;
                        neuschwanstein.setRevivePoint(absCoord);
                        break;
                    }
                    break;
                case "/towerfire":
                    if (buildable instanceof ArrowShip) {
                        ArrowShip arrowship = (ArrowShip) buildable;
                        arrowship.setTurretLocation(absCoord);
                    } else if (buildable instanceof ArrowTower) {
                        ArrowTower arrowtower = (ArrowTower) buildable;
                        arrowtower.setTurretLocation(absCoord);
                    } else if (buildable instanceof CannonShip) {
                        CannonShip cannonship = (CannonShip) buildable;
                        cannonship.setTurretLocation(absCoord);
                    } else if (buildable instanceof CannonTower) {
                        CannonTower cannontower = (CannonTower) buildable;
                        cannontower.setTurretLocation(absCoord);
                    } else if (buildable instanceof TeslaTower) {
                        TeslaTower teslaTower = (TeslaTower) buildable;
                        teslaTower.setTurretLocation(absCoord);
                    }

                    break;
                case "/arrowfire":
                    if (buildable instanceof GrandShipIngermanland) {
                        GrandShipIngermanland arrowtower = (GrandShipIngermanland) buildable;
                        arrowtower.setArrowLocation(absCoord);
                    }
                    break;
                case "/cannonfire":
                    if (buildable instanceof GrandShipIngermanland) {
                        GrandShipIngermanland cannontower = (GrandShipIngermanland) buildable;
                        cannontower.setCannonLocation(absCoord);
                    }
                    break;
                case "/sign":
                    structSign = CivGlobal.getStructureSign(absCoord);
                    if (structSign == null) {
                        structSign = new StructureSign(absCoord, buildable);
                    }
                    block = absCoord.getBlock();
                    ItemManager.setTypeId(block, sb.getType());
                    ItemManager.setData(block, sb.getData());

                    structSign.setDirection(ItemManager.getData(block.getState()));
                    for (String key : sb.keyvalues.keySet()) {
                        structSign.setType(key);
                        structSign.setAction(sb.keyvalues.get(key));
                        break;
                    }

                    structSign.setOwner(buildable);
                    buildable.addStructureSign(structSign);
                    CivGlobal.addStructureSign(structSign);

                    break;
                case "/chest":
                    StructureChest structChest = CivGlobal.getStructureChest(absCoord);
                    if (structChest == null) {
                        structChest = new StructureChest(absCoord, buildable);
                    }
                    structChest.setChestId(Integer.valueOf(sb.keyvalues.get("id")));
                    buildable.addStructureChest(structChest);
                    CivGlobal.addStructureChest(structChest);

                    /* Convert sign data to chest data.*/
                    block = absCoord.getBlock();
                    if (ItemManager.getId(block) != CivData.CHEST) {
                        byte chestData = CivData.convertSignDataToChestData((byte) sb.getData());
                        ItemManager.setTypeId(block, CivData.CHEST);
                        ItemManager.setData(block, chestData, true);
                    }

                    Chest chest = (Chest) block.getState();
                    MaterialData data = chest.getData();
//					ItemManager.setData(data, chestData);
                    chest.setData(data);
                    chest.update();
//				}

                    break;
            }

            buildable.onPostBuild(absCoord, sb);
        }

        //	if (buildable instanceof Structure) {
        buildable.updateSignText();
        //}
    }

    public static void start(Template tpl, Buildable buildable) {
        for (BlockCoord relativeCoord : tpl.doorRelativeLocations) {
            SimpleBlock sb = tpl.blocks[relativeCoord.getX()][relativeCoord.getY()][relativeCoord.getZ()];
            BlockCoord absCoord = new BlockCoord(buildable.getCorner().getBlock().getRelative(relativeCoord.getX(), relativeCoord.getY(), relativeCoord.getZ()));

            Block block = absCoord.getBlock();
            if (ItemManager.getId(block) != sb.getType()) {
                if (buildable.getCiv().isAdminCiv()) {
                    ItemManager.setTypeIdAndData(block, CivData.AIR, (byte) 0, false);
                } else {
                    ItemManager.setTypeIdAndData(block, sb.getType(), (byte) sb.getData(), false);
                }
            }
        }

        for (BlockCoord relativeCoord : tpl.attachableLocations) {
            SimpleBlock sb = tpl.blocks[relativeCoord.getX()][relativeCoord.getY()][relativeCoord.getZ()];
            BlockCoord absCoord = new BlockCoord(buildable.getCorner().getBlock().getRelative(relativeCoord.getX(), relativeCoord.getY(), relativeCoord.getZ()));

            Block block = absCoord.getBlock();
            if (ItemManager.getId(block) != sb.getType()) {
                ItemManager.setTypeIdAndData(block, sb.getType(), (byte) sb.getData(), false);
            }
        }

        /*
         * Use the location's of the command blocks in the template and the buildable's corner
         * to find their real positions. Then perform any special building we may want to do
         * at those locations.
         */
        /* These block coords do not point to a location in the world, just a location in the template. */
        for (BlockCoord relativeCoord : tpl.commandBlockRelativeLocations) {
            SimpleBlock sb = tpl.blocks[relativeCoord.getX()][relativeCoord.getY()][relativeCoord.getZ()];
            StructureSign structSign;
            Block block;
            BlockCoord absCoord = new BlockCoord(buildable.getCorner().getBlock().getRelative(relativeCoord.getX(), relativeCoord.getY(), relativeCoord.getZ()));

            /* Signs and chests should already be handled, look for more exotic things. */
            switch (sb.command) {
                case "/tradeoutpost":
                    /* Builds the trade outpost tower at this location. */
                    if (buildable instanceof TradeOutpost) {
                        TradeOutpost outpost = (TradeOutpost) buildable;
                        outpost.setTradeOutpostTower(absCoord);
                        try {
                            outpost.build_trade_outpost_tower();
                        } catch (CivException e) {
                            e.printStackTrace();
                        }

                    }
                    break;

                case "/techbar":
                    if (buildable instanceof TownHall) {
                        TownHall townhall = (TownHall) buildable;

                        int index = Integer.valueOf(sb.keyvalues.get("id"));
                        townhall.addTechBarBlock(absCoord, index);

                    }
                    break;
                case "/techname":
                    if (buildable instanceof TownHall) {
                        TownHall townhall = (TownHall) buildable;

                        townhall.setTechnameSign(absCoord);
                        townhall.setTechnameSignData((byte) sb.getData());

                    }
                    break;
                case "/techdata":
                    if (buildable instanceof TownHall) {
                        TownHall townhall = (TownHall) buildable;

                        townhall.setTechdataSign(absCoord);
                        townhall.setTechdataSignData((byte) sb.getData());

                    }
                    break;
                case "/itemframe":
                    String strvalue = sb.keyvalues.get("id");
                    if (strvalue != null) {
                        int index = Integer.parseInt(strvalue);

                        if (buildable instanceof TownHall) {
                            TownHall townhall = (TownHall) buildable;
                            townhall.createGoodieItemFrame(absCoord, index, sb.getData());
                            townhall.addStructureBlock(absCoord, false);
                        }
                    }
                    break;
                case "/respawn":
                    if (buildable instanceof TownHall) {
                        TownHall townhall = (TownHall) buildable;
                        townhall.setRespawnPoint(absCoord);
                    }
                    break;
                case "/revive":
                    if (buildable instanceof TownHall) {
                        TownHall townhall = (TownHall) buildable;

                        townhall.setRevivePoint(absCoord);
                    }
                    break;
                case "/control":
                    if (buildable instanceof TownHall) {
                        TownHall townhall = (TownHall) buildable;
                        townhall.createControlPoint(absCoord, "");
                    }
                    if (!(buildable instanceof Neuschwanstein)) {
                        break;
                    }
                    if (buildable.getTown().hasStructure("s_capitol")) {
                        final Capitol capitol = (Capitol) buildable.getTown().getStructureByType("s_capitol");
                        capitol.createControlPoint(absCoord, "Neuschwanstein");
                        break;
                    }
                    if (buildable.getTown().hasStructure("s_townhall")) {
                        final TownHall townHall = (TownHall) buildable.getTown().getStructureByType("s_townhall");
                        townHall.createControlPoint(absCoord, "Neuschwanstein");
                        break;
                    }
                    break;
                case "/towerfire":
                    if (buildable instanceof ArrowShip) {
                        ArrowShip arrowship = (ArrowShip) buildable;
                        arrowship.setTurretLocation(absCoord);
                    } else if (buildable instanceof ArrowTower) {
                        ArrowTower arrowtower = (ArrowTower) buildable;
                        arrowtower.setTurretLocation(absCoord);
                    } else if (buildable instanceof CannonShip) {
                        CannonShip cannonship = (CannonShip) buildable;
                        cannonship.setTurretLocation(absCoord);
                    } else if (buildable instanceof CannonTower) {
                        CannonTower cannontower = (CannonTower) buildable;
                        cannontower.setTurretLocation(absCoord);
                    } else if (buildable instanceof TeslaTower) {
                        TeslaTower teslaTower = (TeslaTower) buildable;
                        teslaTower.setTurretLocation(absCoord);
                    }
                    break;
                case "/arrowfire":
                    if (buildable instanceof GrandShipIngermanland) {
                        GrandShipIngermanland arrowtower = (GrandShipIngermanland) buildable;
                        arrowtower.setArrowLocation(absCoord);
                    }
                    break;
                case "/cannonfire":
                    if (buildable instanceof GrandShipIngermanland) {
                        GrandShipIngermanland cannontower = (GrandShipIngermanland) buildable;
                        cannontower.setCannonLocation(absCoord);
                    }
                    break;
                case "/sign":
                    structSign = CivGlobal.getStructureSign(absCoord);
                    if (structSign == null) {
                        structSign = new StructureSign(absCoord, buildable);
                    }
                    block = absCoord.getBlock();
                    ItemManager.setTypeId(block, sb.getType());
                    ItemManager.setData(block, sb.getData());

                    structSign.setDirection(ItemManager.getData(block.getState()));
                    for (String key : sb.keyvalues.keySet()) {
                        structSign.setType(key);
                        structSign.setAction(sb.keyvalues.get(key));
                        break;
                    }

                    structSign.setOwner(buildable);
                    buildable.addStructureSign(structSign);
                    CivGlobal.addStructureSign(structSign);

                    break;
                case "/chest":
                    StructureChest structChest = CivGlobal.getStructureChest(absCoord);
                    if (structChest == null) {
                        structChest = new StructureChest(absCoord, buildable);
                    }
                    structChest.setChestId(Integer.parseInt(sb.keyvalues.get("id")));
                    buildable.addStructureChest(structChest);
                    CivGlobal.addStructureChest(structChest);

                    /* Convert sign data to chest data.*/
                    block = absCoord.getBlock();
                    if (ItemManager.getId(block) != CivData.CHEST) {
                        byte chestData = CivData.convertSignDataToChestData((byte) sb.getData());
                        ItemManager.setTypeId(block, CivData.CHEST);
                        ItemManager.setData(block, chestData, true);
                    }

                    Chest chest = (Chest) block.getState();
                    MaterialData data = chest.getData();
//					ItemManager.setData(data, chestData);
                    chest.setData(data);
                    chest.update();
//				}

                    break;
            }

            buildable.onPostBuild(absCoord, sb);
        }
        /* Run the tech bar task now in order to protect the blocks */
        if (buildable instanceof TownHall) {
            UpdateTechBar techbartask = new UpdateTechBar(buildable.getCiv());
            techbartask.run();
        }

        //	if (buildable instanceof Structure) {
        buildable.updateSignText();
        //}
    }

    @Override
    public void run() {
        PostBuildSyncTask.start(tpl, buildable);
    }

}
