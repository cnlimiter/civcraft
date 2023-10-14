/**
 * [2023] Cnlimiter LLC
 * All Rights Reserved.
 * Name: civcraft
 * Author: cnlimiter
 * UpdateTime: 2023/10/14 16:15
 * Description:
 * License: ARR
 */
package cn.evole.plugins.civcraft.recover;

import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.exception.CivException;
import cn.evole.plugins.civcraft.main.CivData;
import cn.evole.plugins.civcraft.main.CivLog;
import cn.evole.plugins.civcraft.main.CivMessage;
import cn.evole.plugins.civcraft.structure.Structure;
import cn.evole.plugins.civcraft.template.Template;
import cn.evole.plugins.civcraft.util.CivColor;
import cn.evole.plugins.civcraft.util.ItemManager;
import cn.evole.plugins.civcraft.util.SimpleBlock.Type;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;

import java.io.IOException;
import java.util.ArrayList;

public class RecoverStructureSyncTask implements Runnable {

    ArrayList<Structure> structures;
    CommandSender sender;


    public RecoverStructureSyncTask(CommandSender sender, ArrayList<Structure> structs) {
        this.structures = structs;
        this.sender = sender;
    }

    public void repairStructure(Structure struct) {
        // Repairs a structure, one block at a time. Does not bother repairing
        // command blocks since they will be re-populated in onLoad() anyway.

        // Template is already loaded.
        Template tpl;
        try {
            //tpl.load_template(struct.getSavedTemplatePath());
            tpl = Template.getTemplate(struct.getSavedTemplatePath(), null);
        } catch (IOException | CivException e) {
            e.printStackTrace();
            return;
        }

        Block cornerBlock = struct.getCorner().getBlock();
        for (int x = 0; x < tpl.size_x; x++) {
            for (int y = 0; y < tpl.size_y; y++) {
                for (int z = 0; z < tpl.size_z; z++) {
                    Block nextBlock = cornerBlock.getRelative(x, y, z);

//					if (RecoverStructuresAsyncTask.ignoreBlocks.contains(nextBlock.getTypeId())) {
//						continue;
//					}
//					
//					if (RecoverStructuresAsyncTask.ignoreBlocks.contains(tpl.blocks[x][y][z].getType())) {
//						continue;
//					}

                    if (tpl.blocks[x][y][z].specialType != Type.NORMAL) {
                        continue;
                    }

                    if (ItemManager.getId(nextBlock) != CivData.BEDROCK) {
                        if (tpl.blocks[x][y][z].isAir()) {
                            continue;
                        }
                    }

                    try {
                        if (ItemManager.getId(nextBlock) != tpl.blocks[x][y][z].getType()) {
                            ItemManager.setTypeId(nextBlock, tpl.blocks[x][y][z].getType());
                            ItemManager.setData(nextBlock, tpl.blocks[x][y][z].getData());
                        }
                    } catch (Exception e) {
                        CivLog.error(e.getMessage());
                    }
                }
            }
        }

    }

    @Override
    public void run() {
        for (Structure struct : this.structures) {
            CivMessage.send(sender, CivSettings.localize.localizedString("structureRepairStart") + " " + struct.getDisplayName() + " @ " + CivColor.Yellow + struct.getCorner());
            repairStructure(struct);
        }

        CivMessage.send(sender, CivSettings.localize.localizedString("structureRepairComplete"));
    }


}
