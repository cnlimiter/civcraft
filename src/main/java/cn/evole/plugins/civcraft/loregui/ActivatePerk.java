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

public class ActivatePerk implements GuiAction {

    @Override
    public void performAction(InventoryClickEvent event, ItemStack stack) {
        Player player = (Player) event.getWhoClicked();
        Resident resident = CivGlobal.getResident((Player) event.getWhoClicked());
        String perk_id = LoreGuiItem.getActionData(stack, "perk");
        Perk perk = resident.perks.get(perk_id);
        if (perk != null) {

            perk.onActivate(resident);
        } else {
            CivLog.error(perk_id + " " + CivSettings.localize.localizedString("loreGui_perkActivationFailed"));
        }
        player.closeInventory();
    }

}
