package cn.evole.plugins.civcraft.loregui;

import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.lorestorage.LoreGuiItem;
import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.main.CivLog;
import cn.evole.plugins.civcraft.object.Resident;
import cn.evole.plugins.global.perks.Perk;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class ShowTemplateType implements GuiAction {

    @Override
    public void performAction(InventoryClickEvent event, ItemStack stack) {
        // TODO Auto-generated method stub
        Resident resident = CivGlobal.getResident((Player) event.getWhoClicked());
        String perk_id = LoreGuiItem.getActionData(stack, "perk");
        Perk perk = resident.perks.get(perk_id);
        if (perk != null) {
            if (perk.getIdent().startsWith("template_arctic")) {
                resident.showTemplatePerks("arctic");
            } else if (perk.getIdent().startsWith("template_atlantean")) {
                resident.showTemplatePerks("atlantean");
            } else if (perk.getIdent().startsWith("template_aztec")) {
                resident.showTemplatePerks("aztec");
            } else if (perk.getIdent().startsWith("template_cultist")) {
                resident.showTemplatePerks("cultist");
            } else if (perk.getIdent().startsWith("template_egyptian")) {
                resident.showTemplatePerks("egyptian");
            } else if (perk.getIdent().startsWith("template_elven")) {
                resident.showTemplatePerks("elven");
            } else if (perk.getIdent().startsWith("template_roman")) {
                resident.showTemplatePerks("roman");
            } else if (perk.getIdent().startsWith("template_hell")) {
                resident.showTemplatePerks("hell");
            } else if (perk.getIdent().startsWith("template_medieval")) {
                resident.showTemplatePerks("medieval");
            }
        } else {
            CivLog.error(perk_id + " " + CivSettings.localize.localizedString("loreGui_perkActivationFailed"));
        }
    }

}
