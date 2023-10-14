package cn.evole.plugins.civcraft.loregui.book;

import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.loregui.GuiAction;
import cn.evole.plugins.civcraft.loregui.OpenInventoryTask;
import cn.evole.plugins.civcraft.lorestorage.LoreGuiItem;
import cn.evole.plugins.civcraft.lorestorage.LoreGuiItemListener;
import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.object.Civilization;
import cn.evole.plugins.civcraft.object.Relation;
import cn.evole.plugins.civcraft.threading.TaskMaster;
import cn.evole.plugins.civcraft.util.ItemManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.text.SimpleDateFormat;

public class RelationGuiPeaces
        implements GuiAction {
    @Override
    public void performAction(InventoryClickEvent event, ItemStack stack) {
        Civilization civ = CivGlobal.getCiv(LoreGuiItem.getActionData(stack, "civilization"));
        SimpleDateFormat sdf = new SimpleDateFormat("M/dd h:mm:ss a z");
        ItemStack itemStack = null;
        Inventory inventory = Bukkit.getServer().createInventory((InventoryHolder) event.getWhoClicked(), 54, CivSettings.localize.localizedString("resident_relationsGui_peace"));
        for (Relation relation : civ.getDiplomacyManager().getRelations()) {
            if (relation.getStatus() == Relation.Status.PEACE) {
                itemStack = LoreGuiItem.build("", ItemManager.getId(Material.LAPIS_BLOCK), 0, (Object) ChatColor.RESET + CivSettings.localize.localizedString("resident_relationsGui_relationToString", relation.toString()), "§6" + CivSettings.localize.localizedString("relation_creationDate", sdf.format(relation.getCreatedDate())));
            }
            if (itemStack == null) continue;
            inventory.addItem(itemStack);
        }
        ItemStack backButton = LoreGuiItem.build(CivSettings.localize.localizedString("bookReborn_back"), ItemManager.getId(Material.MAP), 0, CivSettings.localize.localizedString("bookReborn_backTo", BookRelationsGui.inventory.getName()));
        backButton = LoreGuiItem.setAction(backButton, "OpenInventory");
        backButton = LoreGuiItem.setActionData(backButton, "invType", "showGuiInv");
        backButton = LoreGuiItem.setActionData(backButton, "invName", BookRelationsGui.inventory.getName());
        inventory.setItem(53, backButton);
        LoreGuiItemListener.guiInventories.put(inventory.getName(), inventory);
        TaskMaster.syncTask(new OpenInventoryTask((Player) event.getWhoClicked(), inventory));
    }
}

