package cn.evole.plugins.global.perks.components;

import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.config.ConfigBuildableInfo;
import cn.evole.plugins.civcraft.exception.CivException;
import cn.evole.plugins.civcraft.main.CivMessage;
import cn.evole.plugins.civcraft.object.Resident;
import cn.evole.plugins.civcraft.template.Template;
import cn.evole.plugins.civcraft.util.CivColor;
import org.bukkit.entity.Player;

import java.io.IOException;

public class CustomPersonalTemplate extends PerkComponent {

    @Override
    public void onActivate(Resident resident) {
        CivMessage.send(resident, CivColor.LightGreen + CivSettings.localize.localizedString("customTemplate_personal"));
    }


    public Template getTemplate(Player player, ConfigBuildableInfo info) {
        Template tpl = new Template();
        try {
            tpl.initTemplate(player.getLocation(), info, this.getString("theme"));
        } catch (CivException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return tpl;
    }
}
