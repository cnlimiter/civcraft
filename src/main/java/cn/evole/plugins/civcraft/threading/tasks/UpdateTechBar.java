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
import cn.evole.plugins.civcraft.main.CivData;
import cn.evole.plugins.civcraft.object.Civilization;
import cn.evole.plugins.civcraft.object.Town;
import cn.evole.plugins.civcraft.structure.TownHall;
import cn.evole.plugins.civcraft.threading.CivAsyncTask;
import cn.evole.plugins.civcraft.util.BlockCoord;
import cn.evole.plugins.civcraft.util.SimpleBlock;

import java.util.LinkedList;
import java.util.Queue;

public class UpdateTechBar extends CivAsyncTask {

    private Civilization civ;

    public UpdateTechBar(Civilization civ) {
        this.civ = civ;
    }

    @Override
    public void run() {

        Queue<SimpleBlock> sbs = new LinkedList<SimpleBlock>();

        for (Town town : civ.getTowns()) {
            double percentageDone = 0.0;
            TownHall townhall = town.getTownHall();

            if (townhall == null) {
                return;
            }

            if (!townhall.isActive()) {
                return;
            }

            SimpleBlock sb;
            if (civ.getResearchTech() != null) {
                percentageDone = (civ.getResearchProgress() / civ.getResearchTech().getAdjustedBeakerCost(civ));
                /* Get the number of blocks to light up. */
                int size = townhall.getTechBarSize();
                int blockCount = (int) (percentageDone * townhall.getTechBarSize());

                for (int i = 0; i < size; i++) {
                    BlockCoord bcoord = townhall.getTechBarBlockCoord(i);
                    if (bcoord == null) {
                        /* tech bar DNE, might not be finished yet. */
                        continue;
                    }

                    if (i <= blockCount) {
                        sb = new SimpleBlock(CivData.WOOL, CivData.DATA_WOOL_GREEN);
                        sb.x = bcoord.getX();
                        sb.y = bcoord.getY();
                        sb.z = bcoord.getZ();
                        sb.worldname = bcoord.getWorldname();
                        sbs.add(sb);
                    } else {
                        sb = new SimpleBlock(CivData.WOOL, CivData.DATA_WOOL_BLACK);
                        sb.x = bcoord.getX();
                        sb.y = bcoord.getY();
                        sb.z = bcoord.getZ();
                        sb.worldname = bcoord.getWorldname();
                        sbs.add(sb);
                    }

                    townhall.addStructureBlock(townhall.getTechBar(i), false);
                }
            } else {
                /* Resets the bar after a tech is finished. */
                int size = townhall.getTechBarSize();
                for (int i = 0; i < size; i++) {
                    BlockCoord bcoord = townhall.getTechBarBlockCoord(i);
                    if (bcoord == null) {
                        /* tech bar DNE, might not be finished yet. */
                        continue;
                    }

                    sb = new SimpleBlock(CivData.WOOL, CivData.DATA_WOOL_BLACK);
                    sb.x = bcoord.getX();
                    sb.y = bcoord.getY();
                    sb.z = bcoord.getZ();
                    sb.worldname = bcoord.getWorldname();
                    sbs.add(sb);
                    townhall.addStructureBlock(townhall.getTechBar(i), false);
                }
            }

            if (townhall.getTechnameSign() != null) {
                BlockCoord bcoord = townhall.getTechnameSign();
                sb = new SimpleBlock(CivData.WALL_SIGN, townhall.getTechnameSignData());
                sb.x = bcoord.getX();
                sb.y = bcoord.getY();
                sb.z = bcoord.getZ();
                sb.worldname = bcoord.getWorldname();
                sb.specialType = SimpleBlock.Type.LITERAL;

                if (civ.getResearchTech() != null) {
                    sb.message[0] = CivSettings.localize.localizedString("Researching");
                    sb.message[1] = "";
                    sb.message[2] = civ.getResearchTech().name;
                    sb.message[3] = "";
                } else {
                    sb.message[0] = CivSettings.localize.localizedString("Researching");
                    sb.message[1] = "";
                    sb.message[2] = CivSettings.localize.localizedString("Nothing");
                    sb.message[3] = "";
                }
                sbs.add(sb);

                townhall.addStructureBlock(townhall.getTechnameSign(), false);

            }

            if (townhall.getTechdataSign() != null) {
                BlockCoord bcoord = townhall.getTechdataSign();
                sb = new SimpleBlock(CivData.WALL_SIGN, townhall.getTechdataSignData());
                sb.x = bcoord.getX();
                sb.y = bcoord.getY();
                sb.z = bcoord.getZ();
                sb.worldname = bcoord.getWorldname();
                sb.specialType = SimpleBlock.Type.LITERAL;

                if (civ.getResearchTech() != null) {
                    percentageDone = Math.round(percentageDone * 100);

                    sb.message[0] = CivSettings.localize.localizedString("UpdateTechBar_sign_Percent");
                    sb.message[1] = CivSettings.localize.localizedString("UpdateTechBar_sign_Complete");
                    sb.message[2] = "" + percentageDone + "%";
                    sb.message[3] = "";

                } else {
                    sb.message[0] = CivSettings.localize.localizedString("UpdateTechBar_sign_Use");
                    sb.message[1] = "/civ research";
                    sb.message[2] = CivSettings.localize.localizedString("UpdateTechBar_sign_toStart");
                    sb.message[3] = CivSettings.localize.localizedString("UpdateTechBar_sign_Researching");
                }
                sbs.add(sb);


                townhall.addStructureBlock(townhall.getTechdataSign(), false);
            }

        }

        this.updateBlocksQueue(sbs);
    }

}
