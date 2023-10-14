package cn.evole.plugins.civcraft.loregui;

import cn.evole.plugins.civcraft.config.ConfigBuildableInfo;
import cn.evole.plugins.civcraft.exception.CivException;
import cn.evole.plugins.civcraft.lorestorage.LoreGuiItem;
import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.main.CivMessage;
import cn.evole.plugins.civcraft.object.Resident;
import cn.evole.plugins.civcraft.structure.Buildable;
import cn.evole.plugins.civcraft.structurevalidation.StructureValidator;
import cn.evole.plugins.civcraft.template.Template;
import cn.evole.plugins.civcraft.threading.TaskMaster;
import cn.evole.plugins.global.perks.Perk;
import cn.evole.plugins.global.perks.components.CustomPersonalTemplate;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class BuildWithPersonalTemplate implements GuiAction {

    @Override
    public void performAction(InventoryClickEvent event, ItemStack stack) {
        Player player = (Player) event.getWhoClicked();
        Resident resident = CivGlobal.getResident(player);

        ConfigBuildableInfo info = resident.pendingBuildableInfo;
        try {
            /* get the template name from the perk's CustomTemplate component. */
            String perk_id = LoreGuiItem.getActionData(stack, "perk");
            Perk perk = Perk.staticPerks.get(perk_id);
            CustomPersonalTemplate customTemplate = (CustomPersonalTemplate) perk.getComponent("CustomPersonalTemplate");
            Template tpl = customTemplate.getTemplate(player, resident.pendingBuildableInfo);
            Location centerLoc = Buildable.repositionCenterStatic(player.getLocation(), info, Template.getDirection(player.getLocation()), (double) tpl.size_x, (double) tpl.size_z);
            TaskMaster.asyncTask(new StructureValidator(player, tpl.getFilepath(), centerLoc, resident.pendingCallback), 0);
            resident.desiredTemplate = tpl;
            player.closeInventory();
        } catch (CivException e) {
            CivMessage.sendError(player, e.getMessage());
        }
    }

}
