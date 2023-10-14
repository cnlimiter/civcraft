package cn.evole.plugins.civcraft.structurevalidation;

import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.structure.Structure;
import cn.evole.plugins.civcraft.util.BlockCoord;

import java.util.Iterator;
import java.util.Map.Entry;

public class StructureValidationPunisher implements Runnable {

    @Override
    public void run() {
        if (!StructureValidator.isEnabled()) {
            return;
        }

        Iterator<Entry<BlockCoord, Structure>> structIter = CivGlobal.getStructureIterator();
        while (structIter.hasNext()) {
            Structure struct = structIter.next().getValue();
            if (struct.getCiv().isAdminCiv()) {
                continue;
            }

            if (struct.validated && struct.isActive()) {
                if (!struct.isValid()) {
                    struct.onInvalidPunish();
                }
            }
        }
    }

}
