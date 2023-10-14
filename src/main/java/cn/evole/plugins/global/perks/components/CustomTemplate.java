package cn.evole.plugins.global.perks.components;

import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.exception.CivException;
import cn.evole.plugins.civcraft.interactive.InteractiveCustomTemplateConfirm;
import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.main.CivLog;
import cn.evole.plugins.civcraft.main.CivMessage;
import cn.evole.plugins.civcraft.object.Resident;
import cn.evole.plugins.civcraft.object.Town;
import cn.evole.plugins.civcraft.sessiondb.SessionEntry;
import cn.evole.plugins.civcraft.structure.Buildable;
import cn.evole.plugins.civcraft.template.Template;
import cn.evole.plugins.civcraft.util.CivColor;
import cn.evole.plugins.global.perks.Perk;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.ArrayList;

public class CustomTemplate extends PerkComponent {

    private static String getTemplateSessionKey(Town town, String buildableBaseName) {
        return "customtemplate:" + town.getName() + ":" + buildableBaseName;
    }

    private static String getTemplateSessionValue(Perk perk, Resident resident) {
        return perk.getIdent() + ":" + resident.getName();
    }

    public static ArrayList<Perk> getTemplatePerksForBuildable(Town town, String buildableBaseName) {
        ArrayList<SessionEntry> entries = CivGlobal.getSessionDB().lookup(getTemplateSessionKey(town, buildableBaseName));
        ArrayList<Perk> perks = new ArrayList<Perk>();

        for (SessionEntry entry : entries) {
            String[] split = entry.value.split(":");

            Perk perk = Perk.staticPerks.get(split[0]);
            if (perk != null) {
                Perk tmpPerk = new Perk(perk.configPerk);
                tmpPerk.provider = split[1];
                perks.add(tmpPerk);
            } else {
                CivLog.warning("Unknown perk in session db:" + split[0]);
                continue;
            }
        }

        return perks;
    }

    @Override
    public void onActivate(Resident resident) {
        Player player;
        try {
            player = CivGlobal.getPlayer(resident);
        } catch (CivException e) {
            return;
        }

        Town town = resident.getTown();
        if (town == null) {
            CivMessage.sendError(player, CivSettings.localize.localizedString("customTemplate_noTown"));
            return;
        }

        if (hasTownTemplate(town)) {
            CivMessage.sendError(player, CivColor.Rose + CivSettings.localize.localizedString("customTemplatE_alreadyBound"));
            return;
        }

        /*
         * Send resident into interactive mode to confirm that they want
         * to bind the template to this town.
         */
        resident.setInteractiveMode(new InteractiveCustomTemplateConfirm(resident.getName(), this));

    }

    private String getTemplateSessionKey(Town town) {
        return "customtemplate:" + town.getName() + ":" + this.getString("template");
    }

    public void bindTemplateToTown(Town town, Resident resident) {
        CivGlobal.getSessionDB().add(getTemplateSessionKey(town), getTemplateSessionValue(this.getParent(), resident),
                town.getCiv().getId(), town.getId(), 0);
    }

    public boolean hasTownTemplate(Town town) {
        ArrayList<SessionEntry> entries = CivGlobal.getSessionDB().lookup(getTemplateSessionKey(town));

        for (SessionEntry entry : entries) {
            String[] split = entry.value.split(":");

            if (this.getParent().getIdent().equals(split[0])) {
                return true;
            }
        }

        return false;
    }

    public Template getTemplate(Player player, Buildable buildable) {
        Template tpl = new Template();
        try {
            tpl.initTemplate(player.getLocation(), buildable, this.getString("theme"));
        } catch (CivException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return tpl;
    }

}
