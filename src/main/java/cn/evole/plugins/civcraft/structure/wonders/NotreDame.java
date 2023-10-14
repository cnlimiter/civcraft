/**
 * [2023] Cnlimiter LLC
 * All Rights Reserved.
 * Name: civcraft
 * Author: cnlimiter
 * UpdateTime: 2023/10/14 16:15
 * Description:
 * License: ARR
 */
package cn.evole.plugins.civcraft.structure.wonders;

import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.exception.CivException;
import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.main.CivMessage;
import cn.evole.plugins.civcraft.object.Civilization;
import cn.evole.plugins.civcraft.object.Town;
import org.bukkit.Location;

import java.sql.ResultSet;
import java.sql.SQLException;

public class NotreDame extends Wonder {

    public NotreDame(Location center, String id, Town town) throws CivException {
        super(center, id, town);
    }

    public NotreDame(ResultSet rs) throws SQLException, CivException {
        super(rs);
    }

    @Override
    public void onLoad() {
        if (this.isActive()) {
            addBuffs();
        }
    }

    @Override
    public void onComplete() {
        addBuffs();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        removeBuffs();
    }

    @Override
    protected void removeBuffs() {
        this.removeBuffFromCiv(this.getCiv(), "buff_notre_dame_no_anarchy");
        this.removeBuffFromTown(this.getTown(), "buff_notre_dame_coins_from_peace");
        this.removeBuffFromTown(this.getTown(), "buff_notre_dame_extra_war_penalty");
    }

    @Override
    protected void addBuffs() {
        this.addBuffToCiv(this.getCiv(), "buff_notre_dame_no_anarchy");
        this.addBuffToTown(this.getTown(), "buff_notre_dame_coins_from_peace");
        this.addBuffToTown(this.getTown(), "buff_notre_dame_extra_war_penalty");

    }

    public void processPeaceTownCoins() {
        double totalCoins = 0;
        int peacefulTowns = 0;
        double coinsPerTown = this.getTown().getBuffManager().getEffectiveInt("buff_notre_dame_coins_from_peace");

        for (Civilization civ : CivGlobal.getCivs()) {
            if (civ.isAdminCiv()) {
                continue;
            }

            if (civ.getDiplomacyManager().isAtWar()) {
                continue;
            }
            peacefulTowns++;
            totalCoins += (coinsPerTown * civ.getTowns().size());
        }

        this.getTown().depositTaxed(totalCoins);
        CivMessage.sendTown(this.getTown(), CivSettings.localize.localizedString("var_NotreDame_generatedCoins", totalCoins, CivSettings.CURRENCY_NAME, peacefulTowns));

    }

}
