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
import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.main.CivLog;
import cn.evole.plugins.civcraft.structure.Structure;
import cn.evole.plugins.civcraft.structure.wonders.Wonder;
import cn.evole.plugins.civcraft.template.Template;
import cn.evole.plugins.civcraft.util.BlockCoord;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map.Entry;

public class onLoadTask implements Runnable {
    /*
     * After all the trade outposts, town halls, and bonus goodies are loaded
     * we'll call an event on all structures to load them.
     */

    @Override
    public void run() {

        /* Run all post-build sync tasks first. */
        Iterator<Entry<BlockCoord, Structure>> iter = CivGlobal.getStructureIterator();
        while (iter.hasNext()) {
            Structure struct = iter.next().getValue();
            try {
                Template tpl;
                try {
                    if (struct.getSavedTemplatePath() == null && struct.hasTemplate()) {
                        CivLog.warning("structure:" + struct.getDisplayName() + " did not have a template name set but says it needs one!");
                        continue;
                    }

                    if (!struct.hasTemplate()) {
                        continue;
                    }

                    //tpl.load_template(struct.getSavedTemplatePath());
                    tpl = Template.getTemplate(struct.getSavedTemplatePath(), null);
                } catch (CivException | IOException e) {
                    e.printStackTrace();
                    return;
                }

                /* Re-run the post build on the command blocks we found. */
                if (struct.isActive() && !struct.isPartOfAdminCiv()) {
                    PostBuildSyncTask.start(tpl, struct);
                } else if (struct.isPartOfAdminCiv()) {
                    PostBuildSyncTask.validate(tpl, struct);
                }
            } catch (Exception e) {
                CivLog.error("-----ON LOAD EXCEPTION-----");
                if (struct != null) {
                    CivLog.error("Structure:" + struct.getDisplayName());
                    if (struct.getTown() != null) {
                        CivLog.error("Town:" + struct.getTown().getName());
                    }
                }

                CivLog.error(e.getMessage());
                e.printStackTrace();
            }
        }

        for (Wonder wonder : CivGlobal.getWonders()) {
            Template tpl;
            try {
                //Template tpl = new Template();
                try {
                    //tpl.load_template(wonder.getSavedTemplatePath());
                    tpl = Template.getTemplate(wonder.getSavedTemplatePath(), null);
                } catch (CivException | IOException e) {
                    e.printStackTrace();
                    return;
                }

                if (wonder.isActive()) {
                    /* Re-run the post build on the command blocks we found. */
                    PostBuildSyncTask.start(tpl, wonder);
                }
            } catch (Exception e) {
                CivLog.error("-----ON LOAD EXCEPTION-----");
                if (wonder != null) {
                    CivLog.error("Structure:" + wonder.getDisplayName());
                    if (wonder.getTown() != null) {
                        CivLog.error("Town:" + wonder.getTown().getName());
                    }
                }
                CivLog.error(e.getMessage());
                e.printStackTrace();

            }
        }


        /* Now everything should be loaded and ready to go. */
        iter = CivGlobal.getStructureIterator();
        while (iter.hasNext()) {
            Structure struct = iter.next().getValue();
            try {
                struct.onLoad();
            } catch (Exception e) {
                CivLog.error("-----ON LOAD EXCEPTION-----");
                if (struct != null) {
                    CivLog.error("Structure:" + struct.getDisplayName());
                    if (struct.getTown() != null) {
                        CivLog.error("Town:" + struct.getTown().getName());
                    }
                }
                CivLog.error(e.getMessage());
                e.printStackTrace();

            }
        }

        for (Wonder wonder : CivGlobal.getWonders()) {
            try {
                wonder.onLoad();
            } catch (Exception e) {
                CivLog.error("-----ON LOAD EXCEPTION-----");
                if (wonder != null) {
                    CivLog.error("Structure:" + wonder.getDisplayName());
                    if (wonder.getTown() != null) {
                        CivLog.error("Town:" + wonder.getTown().getName());
                    }
                }
                CivLog.error(e.getMessage());
                e.printStackTrace();

            }
        }

    }


}
