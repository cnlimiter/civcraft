/**
 * [2023] Cnlimiter LLC
 * All Rights Reserved.
 * Name: civcraft
 * Author: cnlimiter
 * UpdateTime: 2023/10/14 16:15
 * Description:
 * License: ARR
 */
package cn.evole.plugins.civcraft.threading.timers;

import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.exception.InvalidConfiguration;
import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.main.CivLog;
import cn.evole.plugins.civcraft.main.CivMessage;
import cn.evole.plugins.civcraft.object.AttrSource;
import cn.evole.plugins.civcraft.object.Civilization;
import cn.evole.plugins.civcraft.object.Town;
import cn.evole.plugins.civcraft.structure.*;
import cn.evole.plugins.civcraft.threading.CivAsyncTask;
import cn.evole.plugins.civcraft.util.BlockCoord;
import cn.evole.plugins.civcraft.util.CivColor;

import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReentrantLock;

public class HourlyEventTimer extends CivAsyncTask {

    //public static Boolean running = false;

    public static ReentrantLock runningLock = new ReentrantLock();

    public HourlyEventTimer() {
    }

    private void processTick() {
        /* Clear the last taxes so they don't accumulate. */
        for (Civilization civ : CivGlobal.getCivs()) {
            civ.lastTaxesPaidMap.clear();
        }

        //HashMap<Town, Integer> cultureGenerated = new HashMap<Town, Integer>();

        // Loop through each structure, if it has an update function call it in another async process
        Iterator<Entry<BlockCoord, Structure>> iter = CivGlobal.getStructureIterator();

        while (iter.hasNext()) {
            Structure struct = iter.next().getValue();
            TownHall townhall = struct.getTown().getTownHall();

            if (townhall == null) {
                continue;
            }

            if (!struct.isActive())
                continue;

            struct.onEffectEvent();

            if (struct.getEffectEvent() == null || struct.getEffectEvent().equals(""))
                continue;

            String[] split = struct.getEffectEvent().toLowerCase().split(":");
            switch (split[0]) {
                case "generate_coins":
                    if (struct instanceof Cottage) {
                        Cottage cottage = (Cottage) struct;
                        //cottage.generate_coins(this);
                        cottage.generateCoins(this);
                    }
                    break;
                case "process_mine":
                    if (struct instanceof Mine) {
                        Mine mine = (Mine) struct;
                        try {
                            mine.process_mine(this);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case "temple_culture":
                    if (struct instanceof Temple) {
                        Temple temple = (Temple) struct;
                        try {
                            temple.templeCulture(this);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case "process_trade_ship":
                    if (struct instanceof TradeShip) {
                        TradeShip tradeShip = (TradeShip) struct;
                        try {
                            tradeShip.process_trade_ship(this);
                        } catch (InterruptedException | InvalidConfiguration e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case "process_lab": {
                    if (struct instanceof Lab) {
                        Lab lab = (Lab) struct;
                        try {
                            lab.process_lab(this);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                }
            }

        }

        /*
         * Process any hourly attributes for this town.
         *  - Culture
         *
         */
        for (Town town : CivGlobal.getTowns()) {
            double cultureGenerated;

            // highjack this loop to display town hall warning.
            TownHall townhall = town.getTownHall();
            if (townhall == null) {
                CivMessage.sendTown(town, CivColor.Yellow + CivSettings.localize.localizedString("effectEvent_noTownHall"));
                continue;
            }

            AttrSource cultureSources = town.getCulture();

            // Get amount generated after culture rate/bonus.
            cultureGenerated = cultureSources.total;
            cultureGenerated = Math.round(cultureGenerated);
            town.addAccumulatedCulture(cultureGenerated);

            // Get from unused beakers.
            DecimalFormat df = new DecimalFormat();
            double unusedBeakers = town.getUnusedBeakers();

            try {
                double cultureToBeakerConversion = CivSettings.getDouble(CivSettings.cultureConfig, "beakers_per_culture");
                if (unusedBeakers > 0) {
                    double cultureFromBeakers = unusedBeakers * cultureToBeakerConversion;
                    cultureFromBeakers = Math.round(cultureFromBeakers);
                    unusedBeakers = Math.round(unusedBeakers);

                    if (cultureFromBeakers > 0) {
                        CivMessage.sendTown(town, CivColor.LightGreen + CivSettings.localize.localizedString("var_effectEvent_convertBeakers", (CivColor.LightPurple +
                                df.format(unusedBeakers) + CivColor.LightGreen), (CivColor.LightPurple +
                                df.format(cultureFromBeakers) + CivColor.LightGreen)));
                        cultureGenerated += cultureFromBeakers;
                        town.addAccumulatedCulture(cultureFromBeakers);
                        town.setUnusedBeakers(0);
                    }
                }
            } catch (InvalidConfiguration e) {
                e.printStackTrace();
                return;
            }

            cultureGenerated = Math.round(cultureGenerated);
            CivMessage.sendTown(town, CivColor.LightGreen + CivSettings.localize.localizedString("var_effectEvent_generatedCulture", (CivColor.LightPurple + cultureGenerated + CivColor.LightGreen)));
        }

        /* Checking for expired vassal states. */
        CivGlobal.checkForExpiredRelations();
    }

    @Override
    public void run() {

        if (runningLock.tryLock()) {
            try {
                processTick();
            } finally {
                runningLock.unlock();
            }
        } else {
            CivLog.error("COULDN'T GET LOCK FOR HOURLY TICK. LAST TICK STILL IN PROGRESS?");
        }


    }


}
