package cn.evole.plugins.civcraft.loregui;

import cn.evole.plugins.civcraft.config.ConfigBuildableInfo;
import cn.evole.plugins.civcraft.exception.CivException;
import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.main.CivMessage;
import cn.evole.plugins.civcraft.object.Resident;
import cn.evole.plugins.civcraft.structure.Buildable;
import cn.evole.plugins.civcraft.structurevalidation.StructureValidator;
import cn.evole.plugins.civcraft.template.Template;
import cn.evole.plugins.civcraft.threading.TaskMaster;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;

public class BuildWithDefaultPersonalTemplate implements GuiAction {

    @Override
    public void performAction(InventoryClickEvent event, ItemStack stack) {
        Player player = (Player) event.getWhoClicked();
        Resident resident = CivGlobal.getResident(player);
        ConfigBuildableInfo info = resident.pendingBuildableInfo;

        try {
            String path = Template.getTemplateFilePath(info.template_base_name, Template.getDirection(player.getLocation()), Template.TemplateType.STRUCTURE, "default");
            Template tpl;
            try {
                //tpl.load_template(path);
                tpl = Template.getTemplate(path, player.getLocation());
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }

            Location centerLoc = Buildable.repositionCenterStatic(player.getLocation(), info, Template.getDirection(player.getLocation()), (double) tpl.size_x, (double) tpl.size_z);
            //Buildable.validate(player, null, tpl, centerLoc, resident.pendingCallback);
            TaskMaster.asyncTask(new StructureValidator(player, tpl.getFilepath(), centerLoc, resident.pendingCallback), 0);
            player.closeInventory();

        } catch (CivException e) {
            CivMessage.sendError(player, e.getMessage());
        }
    }

}
