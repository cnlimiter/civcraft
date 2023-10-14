package cn.evole.plugins.civcraft.structurevalidation;

import cn.evole.plugins.civcraft.exception.CivException;
import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.main.CivLog;
import cn.evole.plugins.civcraft.structure.Structure;
import cn.evole.plugins.civcraft.util.BlockCoord;
import cn.evole.plugins.civcraft.war.War;

import java.util.Iterator;
import java.util.Map.Entry;

public class StructureValidationChecker implements Runnable {

    @Override
    public void run() {
        Iterator<Entry<BlockCoord, Structure>> structIter = CivGlobal.getStructureIterator();
        while (structIter.hasNext()) {
            Structure struct = structIter.next().getValue();

            if (War.isWarTime()) {
                /* Don't do any work once it's war time. */
                break;
            }

            if (!struct.isActive()) {
                continue;
            }

            if (struct.isIgnoreFloating()) {
                continue;
            }

            try {
                CivLog.warning("Doing a structure validate... " + struct.getDisplayName());
                struct.validate(null);
            } catch (CivException e) {
                e.printStackTrace();
            }

            synchronized (this) {
                try {
                    this.wait(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
