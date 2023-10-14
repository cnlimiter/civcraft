package cn.evole.plugins.civcraft.loregui;


import cn.evole.plugins.civcraft.lorestorage.LoreGuiItem;
import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.object.Resident;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class ShowPerkPage implements GuiAction {

    @Override
    public void performAction(InventoryClickEvent event, ItemStack stack) {

        Resident resident = CivGlobal.getResident((Player) event.getWhoClicked());
        resident.showPerkPage(Integer.valueOf(LoreGuiItem.getActionData(stack, "page")));
    }

}
