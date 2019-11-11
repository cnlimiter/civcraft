
package com.avrgaming.civcraft.loregui;

import java.text.SimpleDateFormat;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.loregui.GuiAction;
import com.avrgaming.civcraft.lorestorage.LoreGuiItem;
import com.avrgaming.civcraft.main.CivGlobal;

import com.avrgaming.civcraft.object.Civilization;
import com.avrgaming.civcraft.object.Relation;
import com.avrgaming.civcraft.util.ItemManager;

public class RelationPeaces
implements GuiAction {
    @Override
    public void performAction(InventoryClickEvent event, ItemStack stack) {
        Civilization civ = CivGlobal.getCiv(LoreGuiItem.getActionData(stack, "civilization"));
        SimpleDateFormat sdf = new SimpleDateFormat("M/dd h:mm:ss a z");
        ItemStack itemStack = null;
        Inventory inventory = Bukkit.getServer().createInventory((InventoryHolder)event.getWhoClicked(), 54, CivSettings.localize.localizedString("resident_relationsGui_peace"));
        for (Relation relation : civ.getDiplomacyManager().getRelations()) {
            if (relation.getStatus() == Relation.Status.PEACE) {
                itemStack = LoreGuiItem.build("", ItemManager.getId(Material.LAPIS_BLOCK), 0, (Object)ChatColor.RESET + CivSettings.localize.localizedString("resident_relationsGui_relationToString", relation.toString()), "§6" + CivSettings.localize.localizedString("relation_creationDate", sdf.format(relation.getCreatedDate())));
            }
            if (itemStack == null) continue;
            inventory.addItem(itemStack);
        }
        event.getWhoClicked().openInventory(inventory);
    }
}

